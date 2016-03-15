import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

//primary class for the game

public class AntColony extends Applet implements Runnable, KeyListener {
	//the main Thread becomes the game loop
	Thread gameloop;
	
	//use this as a backbuffer
	BufferedImage backbuffer;
	
	//the main drawing object for the back buffer
	Graphics2D g2d;
	
	//toggle for drawing bounding boxes
	boolean showBounds = false;
	
	//makes the ant rest
	int resting = 0;
	
	//the player's ship
	final int ANTS = 100;
	Ant [] ant = new Ant[ANTS];
	
	//the food
	final int FOODS = 5;
	Food [] food = new Food[FOODS];
	
	//the food points
	//ArrayList<FoodPoint> foodpoints = new ArrayList<FoodPoint>();
	ArrayList<ArrayList<FoodPoint>> paths = new ArrayList<ArrayList<FoodPoint>>();
	
	Enemy enemy = new Enemy();
	
	//the ant's Nest
	Nest nest = new Nest();
	
	//create the identity transform(0,0)
	AffineTransform identity = new AffineTransform();
	
	//create a random number generator
	Random rand = new Random();
	
	//applet init event
	public void init(){
		//create the back buffer for smooth graphics
		backbuffer = new BufferedImage(640,480, BufferedImage.TYPE_INT_RGB);
		g2d = backbuffer.createGraphics();
		
		//set up the ship
		for(int i = 0; i < ANTS; i++){
			ant[i] = new Ant();
			ant[i].setX(320);
			ant[i].setY(240);
			changeDirection(ant[i]);
		}
		
		enemy.setX(60);
		enemy.setY(60);
		//set up food
		for(int i = 0; i < FOODS; i++){
			food[i] = new Food();
		}
		//start the user input listener
		addKeyListener(this);
	}
	
	//applet update event to redraw the screen
	public void update(Graphics g){
		//start off transforms at identity
		g2d.setTransform(identity);
		
		//erase the background
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0,0,getSize().width,getSize().height);
		
		//print some status information
		g2d.setColor(Color.WHITE);
		g2d.drawString("Food Stock: " + nest.getStock(), 5, 10);
		//g2d.drawString("Move angle: " + Math.round(ant.getMoveAngle()) + 90,5,25);
		//g2d.drawString("Face angle: " + Math.round(ant.getFaceAngle()), 5, 40);
		
		//draw the game graphics
		drawBounds();
		drawBreadcrumbs();
		drawShip();
		drawEnemy();
		drawFood();
		drawNest();
		
		//repaint the applet window
		paint(g);
	}
	
	public void drawBounds(){
		if(showBounds){
			g2d.setColor(Color.WHITE);
			g2d.draw(enemy.getBounds());
			for(int i = 0; i < ANTS; i++){
				g2d.draw(ant[i].getBounds());
				//g2d.draw(ant[i].getFoodRadius());
				g2d.draw(ant[i].getRadius());
				//g2d.draw(ant[i].getDangerPheromone());
			}
			for(int j = 0; j < FOODS; j++){
				g2d.setColor(Color.WHITE);
				g2d.draw(food[j].getBounds());
			}
		}
	}
	
	//drawShip called by applet update event
	public void drawShip(){
		for(int i = 0; i < ANTS; i++){
			g2d.setTransform(identity);
			g2d.translate(ant[i].getX(),ant[i].getY());
			g2d.rotate(Math.toRadians(ant[i].getFaceAngle()));
			if(ant[i].getHasFood())
				g2d.setColor(Color.GREEN);
			else if(ant[i].getDeadPath())
				g2d.setColor(Color.CYAN);
			else
				g2d.setColor(Color.RED);
			g2d.fill(ant[i].getShape());
			}
		}
	
	public void drawEnemy(){
		g2d.setTransform(identity);
		g2d.translate(enemy.getX(),enemy.getY());
		g2d.rotate(Math.toRadians(enemy.getFaceAngle()));
		g2d.setColor(Color.PINK);
		g2d.fill(enemy.getShape());
	}
	
	public void drawFood(){
		for(int i = 0; i < FOODS; i++){
			g2d.setTransform(identity);
			g2d.translate(food[i].getX(),food[i].getY());
			g2d.rotate(Math.toRadians(food[i].getFaceAngle()));
			if(food[i].getFoodLevel() == 0){
				g2d.setColor(Color.BLACK);
				food[i].setNotExist();
			}
			else
				g2d.setColor(Color.BLUE);
			g2d.fill(food[i].getShape());
		}
	}
	
	public void drawBreadcrumbs(){
		for(int i = 0; i < paths.size(); i++){
			ArrayList<FoodPoint> path = paths.get(i);
			for(int j = 0; j < path.size(); j++){
				double x = path.get(j).getX();
				double y = path.get(j).getY();
				path.get(j).decrementStr();
				if(path.get(j).checkExists()){
					g2d.setColor(Color.WHITE);
					g2d.fillRect((int)x,(int)y,2,2);
				}
			}
		}
	}
	
	public void drawNest(){
		g2d.setTransform(identity);
		g2d.translate(nest.getX(),nest.getY());
		g2d.setColor(Color.GREEN);
	    g2d.setStroke(new BasicStroke(5));
		g2d.draw(nest.getShape());
		g2d.setStroke(new BasicStroke(1));
	}

	//applet window repaint event -- draw the back buffer
	public void paint(Graphics g){
		//draw the back buffer onto the applet window
		this.setSize(640, 480);
		g.drawImage(backbuffer, 0, 0, this);
	}
	//thread start event - start the game loop running
	public void start(){
		//create the gameloop thread for real-time updates
		gameloop = new Thread(this);
		gameloop.start();
	}
	
	//thread run event(game loop)
	public void run(){
		//acquire the current thread
		Thread t = Thread.currentThread();
		
		//keep going as long as the thread is alive
		while(t == gameloop){
			try {
					//update the game loop
					gameUpdate();
					//target framerate is 50 fps
					Thread.sleep(100);
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
			repaint();
		}
	}
	//thread stop event
	public void stop(){
		//kill the gameloop thread
		gameloop = null;
	}
	//move and animate the objects in the game
	private void gameUpdate(){
		checkCollisions();
		updateAnt();
		updateEnemy();
		//updateFoodPoints();
	}
	
	public void changeDirection(Ant ant){
			int randomFaceAngle = rand.nextInt((360 - 0) + 1) + 0;
			ant.setFaceAngle(randomFaceAngle);
	}
	
	public static double getAngle(double x, double y, Ant centerPt) //getAngle(nearest.x,nearest.y,ant[i]
	{
	    double theta = Math.atan2(y - centerPt.getY(), x - centerPt.getX());
	    theta += Math.PI/2.0;
	    double angle = Math.toDegrees(theta);
	    if (angle < 0) {
	        angle += 360;
	    }
	    return angle;
	}
	
	public double getDistanceToEnemy(Enemy e, Ant a){
		return (Math.sqrt(Math.pow((e.getX() - a.getX()), 2) + 
				Math.pow((e.getY() - a.getY()), 2)));
	}
	
	public double getDistanceToAnt(Ant b, Ant a){
		return (Math.sqrt(Math.pow((b.getX() - a.getX()), 2) + 
				Math.pow((b.getY() - a.getY()), 2)));
	}
	
public void getAway(Enemy e, Ant a){
		double faceAngle = getAngle(e.getX(), e.getY(), a);
		System.out.println("faceAngle 1 " + faceAngle);
		a.checkFaceAngle();
		System.out.println("faceAngle 2 " +faceAngle);
	}
	
	//Update the ship position based on velocity
	public void updateAnt(){
		resting++;
		for(int i = 0; i < ANTS; i++){
		
			//in place of pressing up to move the ship
			ant[i].setMoveAngle(ant[i].getFaceAngle() - 90);
			ant[i].setVelX(calcAngleMoveX(ant[i].getMoveAngle()));
			ant[i].setVelY(calcAngleMoveY(ant[i].getMoveAngle()));
		
			//used in reversing the direction the ship is facing
			double faceAngle = ant[i].getFaceAngle();
		
			if(resting == 1){//how many steps before ant changes the way it is facing.
				//System.out.println(resting);
				if(i == ANTS - 1){
					resting = 0;
				}
				
				if(getDistanceToEnemy(enemy, ant[i]) > 15 && ant[i].getBeenChecked() == true){
					ant[i].setBeenChecked(false);
				//	System.out.println(getDistance(enemy, ant[i]));
				//	System.out.println("unchecked");
				}
				
				ant[i].setFaceAngle(ant[i].searchAngle(paths, food, nest));
				//System.out.println(ant[i].getDistnest());
			}
			
			//System.out.println(ant[i].getOnWayHome());
			
			
			
			//update ship X's position, distance the ant moves in one step
			ant[i].incX(ant[i].getVelX() * 5);
			//System.out.println(ant.getVelX());
		
			//collide with left/right edge
			if(ant[i].getX() < - 5){
				ant[i].setX(5);
				checkFaceAngle(faceAngle);	
				bounceOffEdge(faceAngle, ant[i]);
			}
			else if(ant[i].getX() > 640 + 5){
				ant[i].setX(640 - 5);
				checkFaceAngle(faceAngle);	
				bounceOffEdge(faceAngle, ant[i]);
			}		
			//update ship Y's position
			ant[i].incY(ant[i].getVelY() * 5);
			//System.out.println(ant.getVelY());
		
		
			//wrap around top/bottom
			if(ant[i].getY() < -5){
				ant[i].setY(5);
				checkFaceAngle(faceAngle);	
				bounceOffEdge(faceAngle,ant[i]);
			}	
			else if(ant[i].getY() > 480 + 5){
				ant[i].setY(480 - 5);
				checkFaceAngle(faceAngle);	
				bounceOffEdge(faceAngle,ant[i]);
			}
		}
	}
	
	//Update the ship position based on velocity
		public void updateEnemy(){
			//update ship X's position
			enemy.incX(enemy.getVelX() * 5);
			//wrap around left/right
			if(enemy.getX() < - 5)
				enemy.setX(5);
			else if(enemy.getX() > 640 + 5)
				enemy.setX(640 - 5);
			//update ship Y's position
			enemy.incY(enemy.getVelY() * 3);
			
			//wrap around top/bottom
			if(enemy.getY() < -5)
				enemy.setY(5);
			else if(enemy.getY() > 480 + 5)
				enemy.setY(480 - 5);
		}
		
		//update foodpoints
		/*public void updateFoodPoints(){
			for(int i = 0; i < paths.size(); i++){
				ArrayList<FoodPoint> path = paths.get(i);
				for(int j = 0; j < path.size(); j++){
					FoodPoint f = path.get(j);
					f.decrementStr();
				}
			}
		}*/
		
	//keeps face angle in range 0-360
	public void checkFaceAngle(double faceAngle){
		if(faceAngle > 180){
			faceAngle -= 180;
		}
		else{
			faceAngle += 180;
		}
	}
	
	public void bounceOffEdge(double faceAngle, Ant ant){
		ant.setFaceAngle(faceAngle);
		ant.setMoveAngle(ant.getFaceAngle() - 90);
		ant.setVelX(calcAngleMoveX(ant.getMoveAngle()) * 0.2);
		ant.setVelY(calcAngleMoveY(ant.getMoveAngle()) * 0.2);
	}
	
	public void checkCollisions(){
		for(int i = 0; i < ANTS; i++){
			Food f = new Food();
			//f = ant[i].checkFood(food);
			//if(f.exists){
				//ant[i].setFaceAngle(getAngle(f.getX(),f.getY(),ant[i]));
			//}
			
			if(ant[i].getRadius().intersects(enemy.getBounds()) && ant[i].getBeenChecked() == false){
				System.out.println("checked");
				ant[i].checkFaceAngle();
				//ant[m].setMoveAngle(ant[m].getFaceAngle() - 90);
				//ant[m].setVelX(calcAngleMoveX(ant[m].getMoveAngle()));
				//ant[m].setVelY(calcAngleMoveY(ant[m].getMoveAngle()));
				ant[i].setBeenChecked(true);
		}	
	/*	for(int j = i + 1; j < ANTS - 1; j++){
			if(ant[i].getRadius().intersects(ant[j].getRadius())){
					if(ant[i].getOnTrail() == true){
						ant[j].setOnTrail(true);
						}
					if(ant[j].getOnTrail() == true){
						ant[i].setOnTrail(true);
						}
					}
				}*/
			}
		}
	
	public void keyReleased(KeyEvent e){
		int keyCode = e.getKeyCode();
		if(keyCode == e.VK_LEFT){
			enemy.setVelX(0);
			enemy.setVelY(0);
		}
		if(keyCode == e.VK_RIGHT){
			enemy.setVelX(0);
			enemy.setVelY(0);
		}
		if(keyCode == e.VK_UP){
			enemy.setVelX(0);
			enemy.setVelY(0);
		}
		if(keyCode == e.VK_DOWN){
			enemy.setVelX(0);
			enemy.setVelY(0);
		}
	
}
	//key listener events
	public void keyTyped(KeyEvent k){}
	public void keyPressed(KeyEvent k){
		int keyCode = k.getKeyCode();
		switch(keyCode){
		case KeyEvent.VK_LEFT:
			//left arrow rotates ship left 5 degrees
			enemy.setFaceAngle(270);
			enemy.setMoveAngle(enemy.getFaceAngle() - 90);
			enemy.setVelX(calcAngleMoveX(enemy.getMoveAngle()));
			enemy.setVelY(calcAngleMoveY(enemy.getMoveAngle()));
			break;
		case KeyEvent.VK_RIGHT:
			//right arrow rotates ship 5 degrees
			enemy.setFaceAngle(90);
			enemy.setMoveAngle(enemy.getFaceAngle() - 90);
			enemy.setVelX(calcAngleMoveX(enemy.getMoveAngle()));
			enemy.setVelY(calcAngleMoveY(enemy.getMoveAngle()));
			break;
		case KeyEvent.VK_UP:
			//up arrow adds thrust to ship(1/10 normal speed)
			enemy.setFaceAngle(0);
			enemy.setMoveAngle(enemy.getFaceAngle() - 90);
			enemy.setVelX(calcAngleMoveX(enemy.getMoveAngle()));
			enemy.setVelY(calcAngleMoveY(enemy.getMoveAngle()));
			break;
		case KeyEvent.VK_DOWN:
			//up arrow adds thrust to ship(1/10 normal speed)
			enemy.setFaceAngle(180);
			enemy.setMoveAngle(enemy.getFaceAngle() - 90);
			enemy.setVelX(calcAngleMoveX(enemy.getMoveAngle()));
			enemy.setVelY(calcAngleMoveY(enemy.getMoveAngle()));
			break;
	}
}
	
	
	//calculate X movement value based on direction angle
	public double calcAngleMoveX(double angle){
		return(double)(Math.cos(angle * Math.PI / 180));
	}
	
	//calculate Y movement value based on direction angle
	public double calcAngleMoveY(double angle) {
		return(double)(Math.sin(angle * Math.PI / 180));
	}
}