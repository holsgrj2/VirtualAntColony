import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

//Ship class - polygonal shape of the player's ship
public class Ant extends BaseVectorShape {
	// define the polygon
	private int[] antx = { -3, -1, 0, 1, 3, 0 };
	private int[] anty = { 3, 3, 3, 3, 3, -3 };
	// define arraylist of breadcrumbs for finding nest
	public ArrayList<FoodPoint> breadcrumbs = new ArrayList<FoodPoint>();
	Random r = new Random();
	// value for food
	private boolean hasFood;
	
	boolean decrement;
	
	private boolean beenChecked;
	// value for telling if ant is on a trail
	private boolean onTrail;
	// value for incrementing through breadcrumbs on way home
	private int distNest;
	// the distance an ant can search
	private double searchRadius = 3;
	// boolean for checking if no food around
	private boolean onDeadPath;
	//int for keeping which path the ant is on
	private int pathIndex;
	// bounding rectangle

	public Rectangle getBounds() {
		Rectangle r;
		r = new Rectangle((int) getX() - 6, (int) getY() - 6, 12, 12);
		return r;
	}

	public Rectangle getRadius() {
		Rectangle r;
		r = new Rectangle((int) getX() - 19, (int) getY() - 19, 40, 40);
		return r;
	}

	public Rectangle getDangerPheromone() {
		Rectangle r;
		r = new Rectangle((int) getX() - 30, (int) getY() - 30, 60, 60);
		return r;
	}

	Ant() {
		setShape(new Polygon(antx, anty, antx.length));
		setAlive(true);
		distNest = 0;
		hasFood = false;
		beenChecked = false;
		onTrail = false;
	}
	
	public boolean getDecrement(){
		return decrement;
	}
	
	public void setDecrement(boolean b){
		this.decrement = b;
	}

	public void checkFaceAngle() {
		double faceAngle = getFaceAngle();
		if (faceAngle > 180) {
			setFaceAngle(faceAngle - 180);
		} else {
			setFaceAngle(faceAngle + 180);
		}
	}
	public ArrayList<FoodPoint> getPath() {return breadcrumbs;}
	
	public boolean getBeenChecked() {return beenChecked;}
	public boolean getHasFood() {return hasFood;}
	public void setBeenChecked(boolean b) {this.beenChecked = b;}
	public int getDistnest() {return distNest;}
	public boolean getDeadPath(){return onDeadPath;}
	
	// add crumbs for every scouting step
	public void putBreadcrumb() {
		breadcrumbs.add(new FoodPoint(getX(), getY()));
		distNest++;
	}
	
	// get next step to home
	public FoodPoint getNestMove() {
		FoodPoint breadcrumb;
		if(decrement){
			distNest--;
		}
		if (distNest <= 0) {									// if distNest is less than 0 then the next step is home, set ant back to scout state
			hasFood = false;
			onTrail = false;
			onDeadPath = false;
			breadcrumb = breadcrumbs.get(0);
			breadcrumbs.clear(); 								// clear the breadcrumbs to start new random or start a new path
			return breadcrumb;
		}
		breadcrumb = breadcrumbs.get(distNest);
		return breadcrumb;
	}

	public FoodPoint getFoodMove() {
		FoodPoint move = breadcrumbs.get(distNest); 			// get the next step for moving through the path
		distNest++;
		return move;
	}

	// return the strongest foodpoint in range, a non existing point will be returned if there isnt any.
	public FoodPoint searchPath(ArrayList<ArrayList<FoodPoint>> paths, Food[] food, Nest n) {
		Food nearFood = checkFood(food);
		if (onTrail && !hasFood && !onDeadPath) {				// ant is on trail & moving towards the food
			if (distNest < breadcrumbs.size()-1)				// if not at end get the next move on trail
				return getFoodMove();
			else if (nearFood.getFoodLevel() > 0) {				// when at end check for any food
				nearFood.removeFood();								
				hasFood = true;									// set true if there is
				return getNestMove();
			} else {
																// return home and set deadPath to true as the path has no food
				onDeadPath = true;
				return getNestMove();
			}
		} else if (onTrail && hasFood) {						// going back after finding food
			if(distNest ==1)
				n.addStock();
			FoodPoint goodTrail = getNestMove();				// get next step to home
			paths.get(pathIndex).get(distNest).incrementStr();	// increment strength along the way
			return goodTrail;
		} else if (onTrail && onDeadPath) {						//returning home from finding no food - dont increment
			return getNestMove();
		}
		else if (!onTrail && !hasFood && nearFood.getFoodLevel() > 0) {
			putBreadcrumb();									//found food while scouting
			paths.add(new ArrayList<FoodPoint>(breadcrumbs)); 	// path for other ants to find 
			pathIndex = paths.size() - 1;
			FoodPoint nestMove = getNestMove();						
			onTrail = true; 									// set for returning home
			hasFood = true;
			return nestMove;
		}

		else { 													// otherwise check for any trails within range of the ant
			int strongest = 0;
			FoodPoint trail = new FoodPoint();
			for (int i = 0; i < paths.size(); i++) { 			// iterate through the paths arraylist of paths
				ArrayList<FoodPoint> path = paths.get(i);
				for (int j = 0; j < path.size(); j++) { 		// iterate through a path
					double x = path.get(j).getX();
					double y = path.get(j).getY();
					int strength = path.get(j).getStrength();
					if (isInRadius(x, y) ) { 					// if in radius the pheromone is within radius of the ant
						if (strength > strongest) { 			// if multiple trails choose the strongest one
							distNest = j--; 					// set the dist to 1 away from the current index
							breadcrumbs = cloneList(path); 		// make breadcrumbs a copy of the path
							pathIndex = i;
							onTrail = true; 					// ant will now be on a trail
							strongest = strength;
							trail = path.get(i); 				// move onto the trail
						}
					}
				}
			}
			return trail;
		}
	}
	// get any angles found, otherwise random rangle
	public double searchAngle(ArrayList<ArrayList<FoodPoint>> paths, Food[] food, Nest n) { 
		FoodPoint search = searchPath(paths, food, n);
		if (search.checkExists())								//something nearby exists
			return getAngle(search);
		else {
			putBreadcrumb();									//add to the scouting path
			return (1 + (360 - 1) * r.nextDouble());			//move randomly because there is nothing
		}
	}

	// check for any food in radius, if none found return a foodpoint which does not exist
	public Food checkFood(Food[] food) {
		Food bestFood = new Food(0);							//non-existing food point
		for (int i = 0; i < 5; i++) {							//iterate through the food array
			if (isInRadius(food[i].getX(), food[i].getY())) {
				bestFood = food[i];								//food is in radius, return it
			}
		}
		return bestFood;
	}
	// uses pythagoras theorem to find any points within or touching a circle around an ant, uses larger radius if on a trail to prevent possible errors
	public boolean isInRadius(double x, double y) {
		if(!onTrail)
			return (((x - getX()) * (x - getX())) + ((y - getY()) * (y - getY())) <= searchRadius * searchRadius);
		else
			return (((x - getX()) * (x - getX())) + ((y - getY()) * (y - getY())) <= 15 * 15);
	}
	//gets a deep copy of an arraylist
	public ArrayList<FoodPoint> cloneList(ArrayList<FoodPoint> list){
		ArrayList<FoodPoint> clone = new ArrayList<FoodPoint>();
		for(int i = 0; i < list.size(); i++){
			double x = list.get(i).getX();
			double y = list.get(i).getY();
			int z = list.get(i).getStrength();
			clone.add(new FoodPoint(x,y,z));
		}
		return clone;
	}
	// compute the angle towards the foodpoint passed
	public double getAngle(FoodPoint f) { 
		double theta = Math.atan2(f.getY() - getY(), f.getX() - getX());
		theta += Math.PI / 2.0;
		double angle = Math.toDegrees(theta);
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}
}