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
		if(this.x == state.x && this.y == state.y)
			return true;
		return false;
	}

	@Override
	public int compareTo(MyState a) {
		// TODO Auto-generated method stub
		if(this.cost < a.cost)
			return -1;
		if(this.cost > a.cost)
			return 1;
		return 0;
	}
	
}