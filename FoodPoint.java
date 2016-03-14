import java.awt.geom.Point2D;
//class to represent food points and store the points for ants to check
public class FoodPoint{
	private Point2D.Double Coords;
	private int strength;
	private boolean exists;
	FoodPoint(double x,double y){
		Coords = new Point2D.Double(x,y);
		strength = 1000;
		exists = true;
	}
	FoodPoint(){
		exists = false;
	}
	//decrement strength
	public void decrementStr(){strength -= 10;}
	
	//increment strength
	public void incrementStr(){strength += 500;}
	//accessors
	public double getX(){return Coords.x;}
	public double getY(){return Coords.y;}
	public int getStrength(){return strength;}
	public boolean checkExists(){return exists;}
	
}
