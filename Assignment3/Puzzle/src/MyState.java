public class MyState implements Comparable<MyState>{
	public double cost;
	MyState parent;
	public int x;
	public int y;
	
	MyState(double cost, MyState par, int x, int y) {
		this.cost = cost;
		this.parent = par;
		this.x = x;
		this.y = y;
	}

	public boolean isEqual(MyState state) {
		// TODO Auto-generated method stub
		if(this.x == state.x&& this.y == state.y)
			return true;
		return false;
	}
	
	public int compareTo(MyState state) {
		if(this.cost < state.cost)
			return -1;
		if(this.cost > state.cost)
			return 1;
		return 0;
	}
	
}