import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JFrame;
import java.util.Random;


public class Main extends JFrame {
	
	//double buffering
	Image dbImage;
	Graphics dbg;
	
	//Ant
	static Ant [] ants = new Ant[2];
	
	
	int GWIDTH = 700,GHEIGHT = 600;
	Dimension screenSize = new Dimension(GWIDTH,GHEIGHT);	
	public Main(){
		this.setTitle("VirtualAntColony");
		this.setSize(screenSize);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setBackground(Color.WHITE);
	}
	
	
	public static void main(String[] args) {
		Main m = new Main();
		//create and start thread
		initialiseAnts();
		Thread Ant1 = new Thread(ants[0]);
		Ant1.start();
		Thread Ant2 = new Thread(ants[1]);
		Ant2.start();
		
	}

	public void paint(Graphics g){
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		draw(dbg);
		g.drawImage(dbImage, 0, 0, this);
	}
	public void draw(Graphics g){
		ants[0].draw(g);
		ants[1].draw(g);
		//b.p1.draw(g);
		g.setColor(Color.BLACK);
		repaint();
	}
	public static void initialiseAnts(){
		for(int i = 0; i < ants.length; i++){
			Random rand1 = new Random();
			Random rand2 = new Random();
			int xPos = rand1.nextInt(690) + 1;
			int yPos = rand2.nextInt(590) + 1;
			ants[i] = new Ant(xPos,yPos,0);
		}
	}
	
	
	
}
