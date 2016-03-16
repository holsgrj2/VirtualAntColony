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
	FoodPoint(double x,double y,int z){
		Coords = new Point2D.Double(x,y);
		strength = 1000;
		exists = true;
		strength = z;
	}
	FoodPoint(){
		exists = false;
	}
	//decrement strength
	public void decrementStr(){
		strength-=4;
		if(strength <= 0){
			exists = false;
			strength = 0;
		}
	}
	
	//increment strength
	public void incrementStr(){
		if(strength == 0)
			exists = true;
		strength += 1000;
		if(strength >= 7000)
			strength = 7000;
	}
	//accessors
	public double getX(){return Coords.x;}
	public double getY(){return Coords.y;}
	public int getStrength(){return strength;}
	public boolean checkExists(){return exists;}
	
}