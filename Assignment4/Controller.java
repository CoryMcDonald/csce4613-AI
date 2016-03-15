// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Timer;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

class Controller implements MouseListener
{
	public static final long MAX_ITERS = 2000; // The maximum length of a game in frames (At 20 frames/sec, 18000 frames takes 15 minutes)

	private Model model; // holds all the game data
	private View view; // the GUI
	private Object secret_symbol; // used to prevent agents from accessing methods that they could use to cheat
	private IAgent blueAgent;
	private IAgent redAgent;
	LinkedList<MouseEvent> mouseEvents; // a queue of mouse events (used by the human agent)
	int selectedSprite; // the blue agent to draw a box around (used by the human agent)
	private long iter;
	private boolean amIblue;

	Controller(Object secret_symbol, IAgent blueAgent, IAgent redAgent) {
		this.secret_symbol = secret_symbol;
		this.selectedSprite = -1;
		this.mouseEvents = new LinkedList<MouseEvent>();
		this.blueAgent = blueAgent;
		this.redAgent = redAgent;
	}

	private void init() throws Exception {
		this.model = new Model(this, secret_symbol);
		this.model.initGame();
		this.iter = 0;
	}

	Controller fork(IAgent myShadowAgent, IAgent opponentShadowAgent) {
		amIblue = model.amIblue(secret_symbol);
		Controller c = new Controller(secret_symbol, amIblue ? myShadowAgent : opponentShadowAgent, amIblue ? opponentShadowAgent : myShadowAgent);
		c.iter = iter;
		c.amIblue = amIblue;
		c.model = model.clone(c, secret_symbol);
		return c;
	}

	Controller makeReplayPoint(Object symbol) {
		if(symbol != this.secret_symbol)
			throw new NullPointerException("Counterfeit symbol!");
		Controller c = fork(blueAgent, redAgent);
		c.view = this.view;
		return c;
	}

	long getIter() { return iter; }

	boolean update() {
		long timeA = System.nanoTime();
		model.setPerspectiveBlue(secret_symbol); // Blue on left, Red on right
		double[] blueActions = blueAgent.update(this, model);
		long timeB = System.nanoTime();
		model.setPerspectiveRed(secret_symbol); // Red on left, Blue on right
		double[] redActions = redAgent.update(this, model);
		long timeC = System.nanoTime();
		if(iter++ >= MAX_ITERS)
			return false; // out of time
		model.update(blueActions, redActions);
		if(amIblue)
			model.setPerspectiveBlue(secret_symbol); // Blue on left, Red on right
		else
			model.setPerspectiveRed(secret_symbol); // Red on left, Blue on right
		double distSelf = (model.getX() - 300.0) * (model.getX() - 300.0) + (model.getY() - 300.0) * (model.getY() - 300.0);
		double distOpp = (model.getXOpponent() - 300.0) * (model.getXOpponent() - 300.0) + (model.getYOpponent() - 300.0) * (model.getYOpponent() - 300.0);
		return (distSelf <= Model.SQDEATH && distOpp < Model.SQDEATH);
	}

	Model getModel() { return model; }

	int getSelectedSprite() { return selectedSprite; }

	void setSelectedSprite(int i) {
		selectedSprite = i;
	}

	MouseEvent nextMouseEvent() {
		if(mouseEvents.size() == 0)
			return null;
		return mouseEvents.remove();
	}

	public void mousePressed(MouseEvent e) {
		if(e.getY() < 600) {
			mouseEvents.add(e);
			if(mouseEvents.size() > 20) // discard events if the queue gets big
				mouseEvents.remove();
		}
		else {
			if(this.view != null) {
				this.view.doInstantReplay(e.getX());
			}
		}
	}

	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }

	static void doBattle(IAgent blue, IAgent red) throws Exception {
		Object ss = new Object();
		Controller c = new Controller(ss, blue, red);
		c.init();
		c.view = new View(c, c.model, ss); // instantiates a JFrame, which spawns another thread to pump events and keeps the whole program running until the JFrame is closed
		new Timer(20, c.view).start(); // creates an ActionEvent at regular intervals, which is handled by View.actionPerformed
	}

	static int doBattleNoGui(IAgent blue, IAgent red) throws Exception {
		Object ss = new Object();
		Controller c = new Controller(ss, blue, red);
		c.init();
		while(c.update()) { }
		c.model.setPerspectiveBlue(c.secret_symbol);
		double distSelf = (c.model.getX() - 300.0) * (c.model.getX() - 300.0) + (c.model.getY() - 300.0) * (c.model.getY() - 300.0);
		double distOpp = (c.model.getXOpponent() - 300.0) * (c.model.getXOpponent() - 300.0) + (c.model.getYOpponent() - 300.0) * (c.model.getYOpponent() - 300.0);
		if(distSelf > Model.SQDEATH)
			return -1;
		else if(distOpp > Model.SQDEATH)
			return 1;
		else
			return 0;
	}


	/// agents is an ArrayList of agents.
	/// wins is an int[] of size agents.size().
	/// verbose specifies whether to print progress to stdout.
	/// returns an int[] that contains the index of the agent that ranks first, second, third, ...
	static int[] rankAgents(ArrayList<IAgent> agents, int[] wins, boolean verbose) throws Exception {

		// Make every agent battle against every other agent
		if(verbose)
			System.out.println("\nBattles:");
		int n = agents.size() * (agents.size() - 1);
		for(int i = 0; i < agents.size(); i++) {
			for(int j = 0; j < agents.size(); j++) {
				if(j == i)
					continue;
				if(verbose)
					System.out.print("	" + agents.get(i).getClass().getName() + " vs " + agents.get(j).getClass().getName() + ". Winner: ");
				int outcome = Controller.doBattleNoGui(agents.get(i), agents.get(j));
				if(outcome > 0) {
					if(verbose)
						System.out.println(agents.get(i).getClass().getName());
					wins[i]++;
				}
				else if(outcome < 0) {
					if(verbose)
						System.out.println(agents.get(j).getClass().getName());
					wins[j]++;
				}
				else {
					if(verbose)
						System.out.println("Tie");
				}
			}
		}

		// Sort the agents by wins (using insertion sort)
		int[] agentIndexes = new int[agents.size()];
		for(int i = 0; i < agents.size(); i++)
			agentIndexes[i] = i;
		for(int i = 1; i < agents.size(); i++) {
			for(int j = i; j > 0; j--) {
				if(wins[agentIndexes[j]] > wins[agentIndexes[j - 1]]) {
					int t = agentIndexes[j];
					agentIndexes[j] = agentIndexes[j - 1];
					agentIndexes[j - 1] = t;
				}
				else
					break;
			}
		}
		return agentIndexes;
	}

	static void doTournament(ArrayList<IAgent> agents) throws Exception {
		int[] wins = new int[agents.size()];
		int[] agentIndexes = rankAgents(agents, wins, true);
		System.out.println("\nRankings:");
		for(int i = 0; i < agents.size(); i++) {
			int a = agentIndexes[i];
			System.out.println("	#" + Integer.toString(i + 1) + ". " + agents.get(a).getClass().getName() + " (" + Integer.toString(wins[a]) + " wins)");
		}
	}
}
