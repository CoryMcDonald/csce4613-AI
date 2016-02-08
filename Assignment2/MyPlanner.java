import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

public class MyPlanner {
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
   
   static BufferedImage image = null;
   public static final String INPUT_FILE_PATH = "terrain.png";
   public static final String OUTPUT_FILE_PATH = "path.png";
   public static void main(String[] args) throws Exception{
      image = ImageIO.read(new File(INPUT_FILE_PATH));
      
      MyState start = new MyState(0.0, null, 100, 100);
      MyState goal = new MyState(0.0, null, 400, 400);
      double cost = uniform_cost_search(start, goal).cost;
      System.out.print(cost);
      
      ImageIO.write(image, "png", new File(OUTPUT_FILE_PATH));
   }
}
