import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

public class MyPlanner {
	public static Point[] black;
   public static Point[][] pieces;
   
   public static MyState uniform_cost_search(MyState startState, MyState goalState) throws Exception{
      PriorityQueue<MyState> frontier = new PriorityQueue<MyState>(); // lowest cost comes out first
      HashMap<Point, MyState> beenthere = new HashMap<Point, MyState>();
      startState.cost = 0.0;
      startState.parent = null;
      beenthere.put(new Point (startState.x, startState.y), startState);
      frontier.add(startState);
      int iter = 0;
      while(frontier.size() > 0) {
         iter++;
         MyState s = frontier.poll();
         if(iter % 5000 < 1000)
            image.setRGB(s.x, s.y, 0xff00ff00);
         if(s.isEqual(goalState))
            return s;

         MyState north = new MyState(0.0, s, s.x, s.y+1);
         MyState south = new MyState(0.0, s, s.x, s.y-1);
         MyState east = new MyState(0.0, s, s.x+1, s.y);
         MyState west = new MyState(0.0, s, s.x-1, s.y);

         List<MyState> actions = new ArrayList<MyState>();
         
         actions.add(north);
         actions.add(south);
         actions.add(east);
         actions.add(west);
         
         MyState oldchild = null;
         for(MyState a : actions){
            if((a.x >= 0 && a.x < 500) && (a.y >= 0 && a.y < 500)){
               double acost = action_cost(a);
               Point childPoint = new Point(a.x, a.y);
               if(beenthere.containsKey(childPoint)){
                  oldchild = beenthere.get(childPoint);
                  if(s.cost + acost < oldchild.cost){
                     oldchild.cost = s.cost + acost;
                     oldchild.parent = s;
                  }
               }
               else {
                  a.cost = s.cost + acost;
                  a.parent = s;
                  frontier.add(a);
                  Point childPt = new Point(a.x, a.y);
                  beenthere.put(childPt, a);
               }
            }
         }
      }
      throw new Exception("There is no path to the goal");
   }

   private static double action_cost(MyState a) {
      // TODO Auto-generated method stub
      Color c = new Color(image.getRGB(a.x, a.y));
      double green = c.getGreen();
      return green;
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
   
   static BufferedImage image = null;
   public static final String INPUT_FILE_PATH = "terrain.png";
   public static final String OUTPUT_FILE_PATH = "path.png";
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
      
      image = ImageIO.read(new File(INPUT_FILE_PATH));
      
      MyState start = new MyState(0.0, null, 100, 100);
      MyState goal = new MyState(0.0, null, 400, 400);
      MyState answer = uniform_cost_search(start, goal);
      System.out.print(answer.cost);
      
      while(answer.parent != null){
         image.setRGB(answer.x, answer.y, 0xffff0000);
         answer = answer.parent;
      }
      
      ImageIO.write(image, "png", new File(OUTPUT_FILE_PATH));
   }
}