import java.awt.Polygon;
import java.awt.Rectangle;
public class Nest extends BaseVectorShape{
	//define the polygon
	private int foodStock;
	private int [] shipx = {0, -20, -14, 6, 14, 20};
	private int [] shipy = {-20, -4, 20, 14, 20, -4};
	
	//bounding rectangle
	public Rectangle getBounds(){
		Rectangle r;
		r = new Rectangle((int)getX() - 6, (int)getY() - 6, 12,12);
		return r;
	}
	Nest(){
		setShape(new Polygon(shipx,shipy,shipx.length));
		foodStock = 0;
		setX(320);
		setY(240);
	}
	public void addStock(){foodStock++;}
	public void feed(){if(foodStock>0) foodStock--;}
}