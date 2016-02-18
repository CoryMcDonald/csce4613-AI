import java.awt.Point;

public class MyState implements Comparable<MyState>{
	public double cost;
	MyState parent;
	Point[] offset;
	
	MyState(double cost, MyState par, Point[] offset) {
		this.cost = cost;
		this.parent = par;
		this.offset = offset;
	}
	
	public int compareTo(MyState state) {
		if(this.cost < state.cost)
			return -1;
		if(this.cost > state.cost)
			return 0;
		return 1;
	}
	
}