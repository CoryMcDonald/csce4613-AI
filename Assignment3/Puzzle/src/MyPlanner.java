import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class MyPlanner {
	public static Point[] black;
   public static Point[][] pieces;
   
   public static MyState uniform_cost_search(MyState startState, Point goal) throws Exception{
      PriorityQueue<MyState> frontier = new PriorityQueue<MyState>(); // lowest cost comes out first
      HashMap<String, MyState> beenthere = new HashMap<String, MyState>();
      startState.cost = 0.0;
      startState.parent = null;
      
      frontier.add(startState);
      while(frontier.size() > 0) {
         MyState s = frontier.poll();
         if(s.offset[0].equals(goal))
         	return s;
         
         List<Point[]> validMoves = new ArrayList<Point[]>();
         
         for(int i = 0; i < s.offset.length; i++){
         	for(int x = -1; x <=1; x++){
         		for(int y = -1; y<=1; y++){
         			if(Math.abs(x) != Math.abs(y)){
         				Point[] copy = new Point[s.offset.length];
         				System.arraycopy(s.offset, 0, copy, 0, copy.length);
         				copy[i] = new Point(copy[i].x+x, copy[i].y+y);
         				if(isValid(copy))
         					validMoves.add(copy);
         			}
         		}
         	}
         }
        
         for(Point[] moves : validMoves){
         	String str = "";
         	for(Point m : moves)
         		str += m.x + " " + m.y + " ";
         	
         	double cost = s.cost + 1;
         	MyState oldchild = null;
         	if(beenthere.containsKey(str)){
         		if(cost < (oldchild=beenthere.get(str)).cost){
         			oldchild.cost = cost;
         			//oldchild.parent = s;
         			frontier.add(oldchild);
         			beenthere.put(str, oldchild);
         		}
         	}
         	else{
         		oldchild = new MyState(s.cost + 1, s, moves);
         		oldchild.parent = s;
         		frontier.add(oldchild);
         		beenthere.put(str, oldchild);
         	}
         }
      }
      return new MyState(-1, null, new Point[]{new Point(0,0)});
   }

   public static boolean isValid(Point[] questionedPiece){
   	boolean used[][] = new boolean[10][10];
   	for(Point p : black){
   		used[p.x][p.y] = true;
   	}
   	for(int i = 0; i < pieces.length; i++){
   		for(int j = 0; j < pieces[i].length; j++){
   			int x = pieces[i][j].x + questionedPiece[i].x;
   			int y = pieces[i][j].y + questionedPiece[i].y;
   			if(x >= 0 && y >= 0 && x <= 10 && y <=10 && !used[x][y])
   				used[x][y] = true;
   			else
   				return false;
   		}
   	}
   	return true;
   }
   
   public static void main(String[] args) throws Exception{
   	black = new Point[]{ new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0), new Point(4,0),
   								new Point(5,0), new Point(6,0), new Point(7,0), new Point(8,0), new Point(9,0),
   								new Point(0,1), new Point(1,1), new Point(2,1), new Point(7,1), new Point(8,1), new Point(9,1),
   								new Point(0,2), new Point(1,2), new Point(8,2), new Point(9,2),
   								new Point(0,3), new Point(4,3), new Point(9,3),
   								new Point(0,4), new Point(3,4), new Point(4,4), new Point(9,4),
   								new Point(0,5), new Point(9,5),
   								new Point(0,6), new Point(9,6),
   								new Point(0,7), new Point(1,7), new Point(8,7), new Point(9,7),
   								new Point(0,8), new Point(1,8), new Point(2,8), new Point(7,8), new Point(8,8), new Point(9,8),
   								new Point(0,9), new Point(1,9), new Point(2,9), new Point(3,9), new Point(4,9),
   								new Point(5,9), new Point(6,9), new Point(7,9), new Point(8,9), new Point(9,9)};
   	
   	pieces = new Point[11][4];
   	pieces[0] = new Point[]{new Point(1,3), new Point(2,3), new Point(1,4), new Point(2,4)};
   	pieces[1] = new Point[]{new Point(1,5), new Point(1,6), new Point(2,6)};
   	pieces[2] = new Point[]{new Point(2,5), new Point(3,5), new Point(3,6)};
   	pieces[3] = new Point[]{new Point(3,7), new Point(3,8), new Point(4,8)};
   	pieces[4] = new Point[]{new Point(4,7), new Point(5,7), new Point(5,8)};
   	pieces[5] = new Point[]{new Point(6,7), new Point(6,8), new Point(7,7)};
   	pieces[6] = new Point[]{new Point(4,5), new Point(5,4), new Point(5,5), new Point(5,6)};
   	pieces[7] = new Point[]{new Point(6,4), new Point(6,5), new Point(6,6), new Point(7,5)};
   	pieces[8] = new Point[]{new Point(7,6), new Point(8,5), new Point(8,6)};
   	pieces[9] = new Point[]{new Point(5,3), new Point(6,3), new Point(6,2)};
   	pieces[10] = new Point[]{new Point(5,1), new Point(5,2), new Point(6,1)};
   	
   	Point[] offset = new Point[]{
   			new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0),
   			new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0)};
   	
   	Point goal = new Point(4,-2);
          
      MyState start = new MyState(0.0, null, offset);
      MyState answer;
      answer = uniform_cost_search(start, goal);
      
      while(answer.parent != null){
			for(int i = 0; i < pieces.length; i++){
	      	int x = pieces[i][0].x + answer.offset[i].x;
				int y = pieces[i][0].y + answer.offset[i].y;
				System.out.print("(" + x + "," + y + ") ");  
			}
			System.out.println();
			answer = answer.parent;

      }
      
      
   }
}