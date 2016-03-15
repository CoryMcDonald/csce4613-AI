// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.Graphics;
import java.io.File;
import java.util.Random;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;

class Model {
	public static final double EPSILON = 0.000001; // A small number
	public static final double XMAX = 600.0 - EPSILON; // The maximum horizontal screen position. (The minimum is 0.)
	public static final double YMAX = 600.0 - EPSILON; // The maximum vertical screen position. (The minimum is 0.)
	public static final double RADIUS = 35.0; // The radius of each player
	public static final double SQCOLL = (RADIUS + RADIUS) * (RADIUS + RADIUS); // The squared distance at which a collision occurs
	public static final double SQDEATH = (300 - RADIUS) * (300 - RADIUS); // The squared distance from the center of the border

	private Controller controller;
	private Object secret_symbol; // used to limit access to methods that agents could potentially use to cheat
	private Sprite blue;
	private Sprite red;
	private Sprite self;
	private Sprite opponent;
	public boolean doing;

	Model(Controller c, Object symbol) {
		this.controller = c;
		this.secret_symbol = symbol;
		this.doing = false;
	}

	void initGame() throws Exception {
		blue = new Sprite();
		red = new Sprite();
		setPerspectiveBlue(secret_symbol);
	}

	Model clone(Controller c, Object symbol) {
		Model m = new Model(c, symbol);
		m.blue = blue.clone(m);
		m.red = red.clone(m);
		m.self = (self == blue) ? m.blue : m.red;
		m.opponent = (opponent == blue) ? m.blue : m.red;
		return m;
	}

	// These methods are used internally. They are not useful to the agents.
	private void checkSymbol(Object symbol) { if(symbol != this.secret_symbol) throw new NullPointerException("Counterfeit symbol!"); }
	boolean amIblue(Object symbol) { checkSymbol(symbol); return self == blue; }
	void setPerspectiveBlue(Object symbol) { checkSymbol(symbol); self = blue; opponent = red; }
	void setPerspectiveRed(Object symbol) { checkSymbol(symbol); self = red; opponent = blue; }
	Sprite getBlue(Object symbol) { checkSymbol(symbol); return this.blue; }
	Sprite getRed(Object symbol) { checkSymbol(symbol); return this.red; }

	void update(double[] blueActions, double[] redActions) {
		this.doing = false;

		// Accelerate blue
		double theta = blueActions[0] * 2.0 * Math.PI;
		blue.xVel += blueActions[1] * Math.cos(theta);
		blue.yVel += blueActions[1] * Math.sin(theta);

		// Accelerate red
		theta = redActions[0] * 2.0 * Math.PI;
		red.xVel += redActions[1] * Math.cos(theta);
		red.yVel += redActions[1] * Math.sin(theta);

		// Move the balls
		blue.x += blue.xVel; blue.y += blue.yVel; red.x += red.xVel; red.y += red.yVel;

		// Check for collisions
		double sqDist = (blue.x - (XMAX - red.x)) * (blue.x - (XMAX - red.x)) + (blue.y - (YMAX - red.y)) * (blue.y - (YMAX - red.y));
		if(sqDist < SQCOLL) {

			this.doing = true;

			// Approximate the point of collision with binary search
			double rem = 0.0;
			double t = 0.5;
			for(int i = 0; i < 20; i++)
			{
				sqDist = (blue.x - (XMAX - red.x)) * (blue.x - (XMAX - red.x)) + (blue.y - (YMAX - red.y)) * (blue.y - (YMAX - red.y));
				if(sqDist < SQCOLL) {
					rem += t;
					blue.x -= t * blue.xVel; blue.y -= t * blue.yVel; red.x -= t * red.xVel; red.y -= t * red.yVel;
				} else {
					rem -= t;
					blue.x += t * blue.xVel; blue.y += t * blue.yVel; red.x += t * red.xVel; red.y += t * red.yVel;
				}
				t *= 0.5;
			}

			// Bounce (Exchange the component of velocity in the normal direction. Keep the component of velocity in the tangent direction.)
			double a1 = blue.x - (XMAX - red.x);
			double a2 = blue.y - (YMAX - red.y);
			double s = 1.0 / Math.sqrt(a1 * a1 + a2 * a2);
			a1 *= s;
			a2 *= s;
			double bNorm = blue.xVel * a1 + blue.yVel * a2;
			double rNorm = -red.xVel * a1 - red.yVel * a2;
			double bTang = blue.xVel * a2 - blue.yVel * a1;
			double rTang = -red.xVel * a2 + red.yVel * a1;
			blue.xVel = rNorm * a1 + bTang * a2;
			blue.yVel = rNorm * a2 - bTang * a1;
			red.xVel = -(bNorm * a1 + rTang * a2);
			red.yVel = -(bNorm * a2 - rTang * a1);

			// Advance the amount of time that remains after the collision
			blue.x += rem * blue.xVel; blue.y += rem * blue.yVel; red.x += rem * red.xVel; red.y += rem * red.yVel;
		}
	}

	Controller getController() { return controller; }
	double getX() { return self.x; }
	double getY() { return self.y; }
	double getXvel() { return self.xVel; }
	double getYvel() { return self.yVel; }
	double getXOpponent() { return XMAX - opponent.x; }
	double getYOpponent() { return YMAX - opponent.y; }
	double getXvelOpponent() { return -opponent.xVel; }
	double getYvelOpponent() { return -opponent.yVel; }

	class Sprite {
		double x;
		double y;
		double xVel;
		double yVel;

		Sprite() {
			this.x = 200;
			this.y = 200;
			this.xVel = 1;
			this.yVel = 1.5;
		}

		Sprite clone(Model m) {
			Sprite s = m.new Sprite();
			s.x = x;
			s.y = y;
			s.xVel = xVel;
			s.yVel = yVel;
			return s;
		}
	}
}
