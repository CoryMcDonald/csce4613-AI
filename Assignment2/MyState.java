
public class MyState {
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
		if(this.x == state.x)
			if(this.y == state.y)
				return true;
		return false;
	}
	
}
