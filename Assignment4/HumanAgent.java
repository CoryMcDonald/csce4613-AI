// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

class HumanAgent implements IAgent
{
	double[] action;

	/// General-purpose constructor
	HumanAgent() {
		action = new double[2];
	}

	/// Decides what to do
	public double[] update(Controller c, Model m) {
		while(true) {
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			action[0] = Math.atan2(e.getY() - m.getY(), e.getX() - m.getX()) / (2.0 * Math.PI);
			action[1] = 0.3;
		}
		return action;
	}
}
