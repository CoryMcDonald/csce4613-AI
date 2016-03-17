import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.awt.Point;

// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class Human implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around

	Human() {
		reset();
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	public void update(Model m) {
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			uniformCostSearch(m, i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));

			// Head for the opponent's flag
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

			// Shoot at any opponents within range
			if(nearestOpponent(m, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, m.getXOpponent(index), m.getYOpponent(index));
			}

			// Shoot at the flag if I can hit it
			if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}

			// Flee from any bombs targetting me
			if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
				float dx = m.getX(i) - m.getBombTargetX(index);
				float dy = m.getY(i) - m.getBombTargetY(index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
			}
		}

		iter++;
	}

	static class Point implements Comparable<Point>{
		Point parent;
		float x;
		float y;
		float cost;
		int heuristicAndCost;

		public Point(float x, float y){
			this.x = x;
			this.y = y;
			this.cost = (float)0;
		}

		public Point(float x, float y, float cost){
			this.x = x;
			this.y = y;
			this.cost = cost;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Point)) return false;
			Point that = (Point) o;
			return ((int)x == (int)that.x && (int)y == (int)that.y);
		}
		@Override
		public int hashCode() {
			int result = (int)x;
			result = 31 * result + (int)y;
			return result;
		}
		@Override
		public int compareTo(Point state) {
			if(heuristicAndCost > state.heuristicAndCost) {
				return 1;
			}
			else if(heuristicAndCost == state.heuristicAndCost)
				return 0;

			return -1;
		}

		@Override
		public String toString() {
			return "(" + this.x + "," + this.y + ")";
		}
	}

	void uniformCostSearch(Model m, int i, Point goal) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		int goalThreshold = 10;
		int distanceToMove = 10;

		if(goal.x < 0 && goal.x > Model.XMAX) {
			goal.x = myX;
		}
		if(goal.y < 0 && goal.y > Model.YMAX){
			goal.y = myY;
		}
		if(m.getTravelSpeed(goal.x, goal.y) <= .5){
			for(int a = (int)goal.x-60; a< goal.x+60; a++)
				if(a > 1 && goal.y > 1 && a < Model.XMAX  && goal.y < Model.YMAX )
					if(m.getTravelSpeed(a, goal.y) > .5)
						goal.x = a;
					
				
			for(int b = (int)goal.y-60; b< goal.y+60; b++)
				if(goal.x > 1 && b > 1 && goal.x < Model.XMAX  && b < Model.YMAX )
					if(m.getTravelSpeed(goal.x, b) > .5)
						goal.y = b;
		}
		
		//Threshold for just going directly to that destination
		if(sq_dist(myX, myY, goal.x, goal.y) <= 250){
			m.setDestination(i, goal.x, goal.y);
		}else{
			PriorityQueue<Point> frontier = new PriorityQueue<Point>();
			HashSet<Point> beenthere = new HashSet<Point>();
			boolean success = false;
			Point origin = new Point(myX, myY, 0);
			Point parent = null;
			Point s = null;
			int num = 0;
			frontier.add(origin);

			while(frontier.size() > 0) {
				s = frontier.poll();
				num++;
				if((s.x > goal.x - goalThreshold && s.x < goal.x + goalThreshold) && (s.y > goal.y - goalThreshold && s.y < goal.y + goalThreshold) ){
					Point childPoint = s;
					while(childPoint.parent != null && childPoint.parent.parent != null)
						childPoint = childPoint.parent;
					m.setDestination(i, (float)childPoint.x, (float)childPoint.y);
					success = true;
					break;
				}

				Point left = new Point(s.x-distanceToMove, s.y, s.cost);
				Point right = new Point(s.x+distanceToMove, s.y, s.cost);
				Point up = new Point(s.x, s.y-distanceToMove, s.cost);
				Point down = new Point(s.x, s.y+distanceToMove, s.cost );
				boolean added =false;
				ArrayList<Point> actions = new ArrayList<Point>();
				actions.add(left);
				actions.add(right);
				actions.add(up);
				actions.add(down);

				for(Point a : actions)
				{
					if(!beenthere.contains(a) && a.x > 0 && a.y >0 && a.x < Model.XMAX && a.y < Model.YMAX){
						//Doing magic numbers trying to get it where the fasatest tiles are the ones getting used
						float travelSpeed = (float)3.5-m.getTravelSpeed(a.x, a.y);
						int distance = (int)Math.sqrt(sq_dist(a.x, a.y, goal.x, goal.y));
						if ( travelSpeed < 0)
							travelSpeed = 0;
						a.cost += travelSpeed*200;
						a.parent = s;
						a.heuristicAndCost = (int)a.cost;
						if(travelSpeed < 3){
							frontier.add(a);
							beenthere.add(a);
						}
					}
				}

			}

		}
	}
}
