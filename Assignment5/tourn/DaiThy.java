import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.awt.Point;

// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class DaiThy implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around

	DaiThy() {
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

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw boms if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}

	void beFlagAttacker(Model m, int i) {
		// Head for the opponent's flag
		uniformCostSearch(m, i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));
		// m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

		// Avoid opponents
		float myX = m.getX(i);
		float myY = m.getY(i);
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))
				m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY));
		}

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {

				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {

				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
				}
				else {
					m.setDestination(i, myX, myY); // Rest
				}
			}
		}
		else {

			// Head for the opponent's flag
			if(m.getSpriteCountOpponent() < 1)
				uniformCostSearch(m, i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));
			// m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

			// Shoot at the flag if I can hit it
			if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}

	public void update(Model m) {
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			uniformCostSearch(m, i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));

			// Head for the opponent's flag
			// m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

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
			parent = null;
			this.x = x;
			this.y = y;
			this.cost = (float)0;
		}

		public Point(float x, float y, float cost){
			parent = null;
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
			Point origin = new Point(myX, myY);
			Point s = null;
			frontier.add(origin);

			while(frontier.size() > 0) {
				s = frontier.poll();
				if((s.x > goal.x - goalThreshold && s.x < goal.x + goalThreshold) && (s.y > goal.y - goalThreshold && s.y < goal.y + goalThreshold) ){
					Point childPoint = s;
					while(childPoint.parent != null && childPoint.parent.parent != null)
						childPoint = childPoint.parent;
					m.setDestination(i, (float)childPoint.x, (float)childPoint.y);
					break;
				}

				Point left = new Point(s.x-distanceToMove, s.y, s.cost);
				Point right = new Point(s.x+distanceToMove, s.y, s.cost);
				Point up = new Point(s.x, s.y-distanceToMove, s.cost);
				Point down = new Point(s.x, s.y+distanceToMove, s.cost );

				ArrayList<Point> actions = new ArrayList<Point>();

				actions.add(left);
				actions.add(right);
				actions.add(up);
				actions.add(down);

				for(Point a : actions){
					if(a.x > 0 && a.y >0 && a.x < Model.XMAX && a.y < Model.YMAX){

						if(!beenthere.contains(a)){
							float travelSpeed = (float)3.5-m.getTravelSpeed(a.x, a.y);
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
}
