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
		strength-=2;
		if(strength <= 0)
			exists = false;
	}
	
	//increment strength
	public void incrementStr(){
		strength += 60;
		if(strength > 3000)
			strength = 3000;
	}
	//accessors
	public double getX(){return Coords.x;}
	public double getY(){return Coords.y;}
	public int getStrength(){return strength;}
	public boolean checkExists(){return exists;}
	
}