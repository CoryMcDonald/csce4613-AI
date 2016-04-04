import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Random;

// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class DaiTKhy implements IAgent
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

	public void update(Model m) {
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			uniformCostSearch(m, i, new Point(Model.XFLAG_OPPONENT - (Model.MAX_THROW_RADIUS / 2), Model.YFLAG_OPPONENT));

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

	static class Shadow1 implements IAgent{
		int param;
		double[] params;

		Shadow1 () {
			this.param = 0;
		}

		Shadow1(int param) {
			this.param = param;
		}

		Shadow1(double[] params) {
			this.param = 0;
			this.params = params;
		}

		public void reset(){

		}

		public void update(Model m){

		}
	}
	static class Shadow2 implements IAgent{
		int param;
		double[] params;

		Shadow2 () {
			this.param = 0;
		}


		Shadow2(int param) {
			this.param = param;
		}

		Shadow2(double[] params) {
			this.param = 0;
			this.params = params;
		}

		public void reset(){
			
		}

		public void update(Model m){
			
		}
	}

	public void geneticAlgorithm() throws Exception{
		final int POPULATION_SIZE = 20;

		ArrayList<double []> root = new ArrayList<double []>();
		Random r = new Random(1234);
		for(int i = 0; i < POPULATION_SIZE; i++){
			double[] param = new double[100];
				for(int j = 0; j<100; j++)
					param[j] = 0.01 * r.nextGaussian();
			root.add(param);
		}

		while(true){
			ArrayList<IAgent> agents = new ArrayList<IAgent>();
			for(int i = 0; i < root.size(); i++){
				Shadow1 shadow = new Shadow1(root.get(i));
				if (Controller.doBattleNoGui(shadow, new Shadow2()) == 1){
					for(int j = 0; j < root.get(i).length; j++)
						System.out.print(j + ", ");
					System.out.println();
				}
				else{
					agents.add(shadow);
				}
			}

			int[] temp = new int[agents.size()];
			int[] ranked = Controller.rankAgents(agents, temp, false);

			System.out.println("Ranked: ");
			for(int i = 0; i < ranked.length; i++){
				System.out.print(ranked[i] + ", ");
			}
			System.out.println();

			// conduct crossover with the top agents
			double [] chr = new double[291];
			for(int i = 0; i < 145; i++){
				chr[i] = root.get(ranked[0])[i];
			}

			for(int i = 145; i < 291; i++){
				chr[i] = root.get(ranked[1])[i];
			}

			root.add(ranked[root.size()-1], chr);
		}
	}

	void uniformCostSearch(Model m, int i, Point goal) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		int goalThreshold = 10;
		int distanceToMove = 10;

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

				Point north = new Point(s.x, s.y + distanceToMove, s.cost);
				Point south = new Point(s.x, s.y - distanceToMove, s.cost);
				Point east = new Point(s.x + distanceToMove, s.y, s.cost);
				Point west = new Point(s.x - distanceToMove, s.y, s.cost);

				ArrayList<Point> actions = new ArrayList<Point>();

				actions.add(north);
				actions.add(south);
				actions.add(east);
				actions.add(west);

				for(Point a : actions){
					if(a.x >= 0 && a.y >= 0 && a.x < Model.XMAX && a.y < Model.YMAX){

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