// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import javax.swing.JFrame;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.lang.InterruptedException;


public class View extends JFrame implements ActionListener {
	public static final int REPLAY_GRANULARITY = 30;

	Controller controller;
	Model model;
	private Object secret_symbol; // used to limit access to methods that agents could potentially use to cheat
	private MyPanel panel;
	private ArrayList<Controller> replayPoints;
	private int slomo;
	private int skipframes;

	public View(Controller c, Model m, Object symbol) throws Exception {
		this.controller = c;
		this.model = m;
		secret_symbol = symbol;

		// Make the game window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Sumo Billiards");
		this.setSize(603, 636);
		this.panel = new MyPanel();
		this.panel.addMouseListener(controller);
		this.getContentPane().add(this.panel);
		this.setVisible(true);

		this.replayPoints = new ArrayList<Controller>();
	}

	public void actionPerformed(ActionEvent evt) {
		repaint(); // indirectly calls MyPanel.paintComponent
	}

	void doInstantReplay(int x) {
		if(x >= 1190) { // If the user clicked in the slomo box
			if(slomo > 0)
				slomo = 0;
			else
				slomo = 5;
			return;
		}
		int i = x * (int)Controller.MAX_ITERS / (1200 * REPLAY_GRANULARITY);
		if(i < replayPoints.size()) {
			System.out.println("Replaying " + Integer.toString(i));
			Controller c = replayPoints.get(i);
			if(this.panel.getMouseListeners()[0] != controller)
				System.out.println("other listener?");
			this.model = c.getModel();
			this.controller = c;
			MouseListener[] oldListeners = panel.getMouseListeners();
			if(oldListeners.length > 0)
				this.panel.removeMouseListener(oldListeners[0]);
			this.panel.addMouseListener(controller);
			replayPoints.set(i, controller.makeReplayPoint(secret_symbol));
		} else
			System.out.println("Cannot replay the future");
	}

	class MyPanel extends JPanel {
		public static final int FLAG_IMAGE_HEIGHT = 25;

		Image image_robot_blue;
		Image image_robot_red;
		Image image_broken;
		Image image_flag_blue;
		Image image_flag_red;
		MySoundClip sound_doing;

		MyPanel() throws Exception {
			this.sound_doing = new MySoundClip("metal_doing.wav", 3);
		}

		private void drawTerrain(Graphics g) {
			g.setColor(new Color(0, 128, 0));
			g.drawOval(0, 0, 599, 599);
		}

		private void drawSprites(Graphics g) {
			Model.Sprite blue = model.getBlue(secret_symbol);
			g.setColor(new Color(0, 0, 128));
			g.fillOval((int)blue.x - (int)Model.RADIUS, (int)blue.y - (int)Model.RADIUS, (int)(Model.RADIUS + Model.RADIUS), (int)(Model.RADIUS + Model.RADIUS));
			Model.Sprite red = model.getRed(secret_symbol);
			g.setColor(new Color(128, 0, 0));
			g.fillOval((int)(Model.XMAX - red.x) - (int)Model.RADIUS, (int)(Model.YMAX - red.y) - (int)Model.RADIUS, (int)(Model.RADIUS + Model.RADIUS), (int)(Model.RADIUS + Model.RADIUS));
		}

		private void drawTime(Graphics g) {
			int iter = (int)controller.getIter();
			if(replayPoints.size() < iter / REPLAY_GRANULARITY) {
				replayPoints.add(controller.makeReplayPoint(secret_symbol));
				//System.out.println("Recording " + Integer.toString(replayPoints.size()));
			}
			int i = 600 * iter / (int)Controller.MAX_ITERS;
			int j = replayPoints.size() * REPLAY_GRANULARITY * 600 / (int)Controller.MAX_ITERS;
			g.setColor(new Color(128, 128, 128));
			g.fillRect(i, 600, j - i, 10);
			g.setColor(new Color(0, 128, 128));
			g.fillRect(0, 600, i, 10);

			// Draw slomo box
			if(slomo > 0)
				g.fillRect(1190, 600, 10, 10);
			else
				g.drawRect(1190, 600, 10, 10);
		}

		public void paintComponent(Graphics g) {
			if(skipframes > 0)
				skipframes--;
			else {
				// Give the agents a chance to make decisions
				if(!controller.update()) {
					model.setPerspectiveBlue(secret_symbol);
					double distSelf = (model.getX() - 300.0) * (model.getX() - 300.0) + (model.getY() - 300.0) * (model.getY() - 300.0);
					double distOpp = (model.getXOpponent() - 300.0) * (model.getXOpponent() - 300.0) + (model.getYOpponent() - 300.0) * (model.getYOpponent() - 300.0);
					if(distSelf > Model.SQDEATH)
						System.out.println("\nRed wins!");
					else if(distOpp > Model.SQDEATH)
						System.out.println("\nBlue wins!");
					else
						System.out.println("\nTie.");
					try { Thread.sleep(1000); } catch(Exception e) {}
					View.this.dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING)); // The game is over, so close this window
				}
				skipframes = slomo;
			}

			// Play a sound
			if(model.doing)
				sound_doing.play();

			// Draw the view
			model.setPerspectiveBlue(secret_symbol);
			drawTerrain(g);
			drawSprites(g);
			drawTime(g);
		}
	}

	class MySoundClip {
		Clip[] clips;
		int pos;

		MySoundClip(String filename, int copies) throws Exception {
			clips = new Clip[copies];
			for(int i = 0; i < copies; i++) {
				try{
					clips[i] = AudioSystem.getClip();
					AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
					clips[i].open(ais);
				} catch(Exception e)
				{
					System.out.println("Unable to create audio stream. Oh well, ignoring the problem.");
					clips[i] = null;
				}
			}
			pos = 0;
		}

		void play() {
			if(clips[pos] != null)
			{
				clips[pos].setFramePosition(0);
				clips[pos].loop(0);
			}
			if(++pos >= clips.length)
				pos = 0;
		}
	}
}
