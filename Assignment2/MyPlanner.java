import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class MyPlanner {
	public static MyState uniform_cost_search(MyState startState, MyState goalState){
      PriorityQueue<MyState> frontier = new PriorityQueue<MyState>(); // lowest cost comes out first
      TreeSet<MyState> beenthere = new TreeSet<MyState>();
      startState.cost = 0.0;
      startState.parent = null;
      beenthere.add(startState);
      frontier.add(startState);
      while(frontier.size() > 0) {
         MyState s = frontier.poll();
         if(s.isEqual(goalState))
            return s;

         MyState north = new MyState(0.0, s, s.x, s.y+1);
         MyState south = new MyState(0.0, s, s.x, s.y-1);
         MyState east = new MyState(0.0, s, s.x+1, s.y);
         MyState west = new MyState(0.0, s, s.x-1, s.y);

         List<MyState> actions = new ArrayList<MyState>();
         
         for(MyState a : actions){
         	MyState child = transition(s, a); // compute the next state
            MyState oldchild = null;
         	double acost = action_cost(s, a); // compute the cost of the action
            if(beenthere.contains(child)) {
            	Iterator<MyState> iter = beenthere.iterator();
            	while(iter.hasNext()){
            		if(iter.next().isEqual(child)){
            			oldchild.cost = iter.next().cost;
            			oldchild.parent.equals(iter.next().parent);
            			oldchild.x = iter.next().x;
            			oldchild.y = iter.next().y;
            			break;
            		}
            	}
               if(s.cost + acost < oldchild.cost) {
                  oldchild.cost = s.cost + acost;
                  oldchild.parent = s;
               }
            }
            else {
               child.cost = s.cost + acost;
               child.parent = s;
               frontier.add(child);
               beenthere.add(child);
            }
         }
      }
      throw new Exception("There is no path to the goal");
   }

	private static MyState transition(MyState s, MyState a) {
		// TODO Auto-generated method stub
		return null;
	}

	private static double action_cost(MyState s, MyState a) {
		// TODO Auto-generated method stub
		Color c = new Color(image.getRGB(a.x, a.y));
		double green = c.getGreen();
		return green;
	}
	
	static BufferedImage image = null;
	public static void main(String[] args){
		// Load a image from a file
		String inputFilePath = "terrain.png";
		image = ImageIO.read(new File(inputFilePath));
		
		int x = 0, y = 0;
		
		// Read a pixel
		Color c = new Color(image.getRGB(x, y));
		int greenChannel = c.getGreen();
		System.out.println(greenChannel);

		// Set a pixel (0xAARRGGBB)
		image.setRGB(x, y, 0xff00ff00);

		// Write the image to a PNG file
		ImageIO.write(image, "png", new File(outputFilePath));
	}
}
