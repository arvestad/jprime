package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/**
 * MCMCGraphPanel: Graph class able to recieve an array of data and display it graphically.
 *	Created by: M Bark & J Mir� Arredondo (2010)
 *   E-mail: mikbar at kth dot se & jorgma at kth dot se
 */
public class MCMCGraphPanel extends JPanel implements Scrollable {
	public class Selection {
		private int leftPos;
		private int rightPos;
		private int width;
		
		Selection(int leftPos, int rightPos) {
			select(leftPos, rightPos);
		}

		public void select(int leftPos, int rightPos) {
			if(leftPos > rightPos) {
				this.leftPos = rightPos;
				this.rightPos = leftPos;
			} else {
				this.leftPos = leftPos;
				this.rightPos = rightPos;
			}
			this.width = this.rightPos - this.leftPos;
		}

		public int getLeftPos() 	{return leftPos;}
		public int getRightPos() 	{return rightPos;}
		public int getWidth() 		{return width;}
	}

	private class Vector2D {
		public double x;
		public double y;

		Vector2D() {
			x=0;
			y=0;
		}

		@SuppressWarnings("unused")
		Vector2D(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long 				serialVersionUID = 1L;
	private Selection 						selection;
	private ArrayList<ArrayList<Double>> 	marks;
	private int 							width;
	private int 							height;
	private int 							numGridsX;
	private int 							numGridsY;
	private int 							numPoints;
	private int 							burninMarkerPos;
	private Double[] 						data;
	private Vector2D[] 						plotpoints;
	private Thread 							workerThread;
	private boolean 						bThreadActive;

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCGraphPanel() {
		numGridsX 		= 10;
		numGridsY 		= 10;
		data 			= null;
		plotpoints 		= null;
		bThreadActive	= false;
		marks 			= new ArrayList<ArrayList<Double>>();
		selection 		= new Selection(0, 0);
	}
	
	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	private void drawGraph(Graphics g) {
		Graphics2D graphics2d = (Graphics2D) g;
		BasicStroke stroke = new BasicStroke(1);

		if(plotpoints != null) {
			graphics2d.setStroke(stroke);

			for(int i=0; i<numPoints-1; i++) {
				if(width == 0)
					width = 1;
				if(i >= (int) (numPoints*selection.leftPos/width) && i <= (int) (numPoints*selection.rightPos/width)) 
					graphics2d.setColor(new Color(0xFF9999FF));
				else
					graphics2d.setColor(new Color(0xFFFF0000));
				if(i > numPoints*((double) burninMarkerPos/width)) {
					double x1 = plotpoints[i].x;
					double x2 = plotpoints[i+1].x;

					double y1 = plotpoints[i].y*0.8 + 0.1*height;
					double y2 = plotpoints[i+1].y*0.8 + 0.1*height;

					Line2D line = new Line2D.Double(x1, y1, x2, y2);
					graphics2d.draw(line);
				} else {
					double x1 = plotpoints[i].x;
					double y1 = plotpoints[i].y*0.8 + 0.1*height;

					g.fillRect((int) x1, (int) y1, 2, 2);
				}
			}
		} else {
			graphics2d.setColor(new Color(0xFF000000));
			g.drawString("No graph available", width/2-25, height/2);
		}
	}
	
	/**
	 * createColors: Method for creating an array of different matching colors. 
	 */
	private Color[] createColors(int num) {
		Color[] colors = new Color[num];

		double PI = java.lang.Math.PI;
		double d = PI/num;
		for(int i=0; i<num; i++) {
			float r = java.lang.Math.abs((float) java.lang.Math.cos(i*d + 2*PI/3));
			float g = java.lang.Math.abs((float) java.lang.Math.cos(i*d));
			float b = java.lang.Math.abs((float) java.lang.Math.cos(i*d + PI/3));
			colors[i] = new Color(r, g, b);
		}

		return colors;
	}
	
	private void drawLine(Graphics g) {
		if(burninMarkerPos > selection.leftPos && burninMarkerPos < selection.rightPos)
			g.setColor(new Color(0xFF7777FF));
		else
			g.setColor(new Color(0xFF00FF00));
		g.fillRect(burninMarkerPos, 0, 2, height);
	}
	
	private void drawMarks(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.04));

		int markWidth = (int) Math.ceil((double) width/numPoints);
		
		Color[] colors = createColors(marks.size());
		for(int i=0; i<marks.size(); i++) {
			ArrayList<Double> list = marks.get(i);

			g2.setColor(colors[i]);

			for(int j=0; j<list.size(); j++)
				g2.fillRect((int) (list.get(j)*width), 0, markWidth, height);
		}
	}

	private void drawCanvas(Graphics g) {	
		g.setColor(new Color(0xFFFFFFFF));
		g.fillRect(0, 0, width, height);
		
		double dx = (double) 1/numGridsX;
		double dy = (double) 1/numGridsY;

		if(selection != null) {
			g.setColor(new Color(0xFFDDDDFF));
			g.fillRect((int) selection.leftPos, 0, (int) (selection.rightPos-selection.leftPos), height);
		}

		for(int y=0; y<numGridsY; y++) {
			g.setColor(new Color(0xFFEEEEEE));
			g.fillRect(2, (int) (height*y*dy), width, 1);
		}
		for(int x=0; x<numGridsX; x++) {
			g.setColor(new Color(0xFFEEEEEE));
			g.fillRect((int) (width*x*dx), 2, 1, height);

			g.setColor(new Color(0xFF000000));
			g.fillRect((int) (width*x*dx), height-10, 1, 10);

			if(x > 0 && x < numPoints) {
				g.setFont(new Font("arial", Font.BOLD, 9));
				g.drawString(Integer.toString((int) (numPoints*x*dx)), (int) (width*x*dx), height-15);
			}
		}

		g.setColor(new Color(0xFFBBBBBB));
		g.fillRect(0, (int) (height-height*0.1), width, 2);
		g.fillRect(0, (int) (height*0.1), width, 2);

		g.setColor(new Color(0xFF000000));
		g.drawRect(0, 0, width, height);
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	/**
	 * startThread: Create and start thread responsible for redrawing the graph.
	 */
	public void startThread() {
		bThreadActive = true;

		workerThread = new Thread() {
			public void run() {
				while(bThreadActive) {
					try {
						sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					repaint();
					yield();
				}
			}
		};
		workerThread.start();
	}

	public void paint(Graphics g) {
		drawCanvas(g);
		drawGraph(g);
		drawLine(g);
		drawMarks(g);
	}
	
	/**
	 * updatePlotPoints: Calculates the position each value stored in the data array should
	 * have in the graph. This is based on min and max values as well as the size of the
	 * graph.
	 */
	public void updatePlotPoints() {
		setPreferredSize(new Dimension(width, height));

		if(data != null) {
			numPoints = data.length;
			double maxY = data[0];
			for(int i=0; i<data.length; i++) {
				double value = data[i];

				if(value > maxY)
					maxY = value;
			}

			double minY = data[0];
			for(int i=0; i<data.length; i++) {
				double value = data[i];

				if(value < minY)
					minY = value;
			}

			double diffY = maxY - minY;
			double dx, dy;

			dx = (double) width/(numPoints);
			dy = (double) height/(diffY);

			plotpoints = new Vector2D[numPoints];

			for(int i=0; i<numPoints; i++) {
				plotpoints[i] = new Vector2D();

				plotpoints[i].x = (double) i*dx;
				plotpoints[i].y = (double) height - (data[i]-minY)*dy;
			}
		}
	}
	
	/**
	 * stopThread: Let workerThread run to completion to stop redrawing graph.
	 */
	public void stopThread() 																		{bThreadActive = false;}
	public void select(int left, int right) 														{selection.select(left, right);}
	public void setBurnInMarkerPos(int burninMarkerPos) 											{this.burninMarkerPos = burninMarkerPos;}
	public void setData(Double[] data) 																{this.data = data;};
	public void setHeight(int height) 																{this.height = height;}
	public void setWidth(int width) 																{this.width = width;}
	public void setSelection(Selection selection) 													{this.selection = selection;}
	public void setPanelSize(int width, int height) 												{this.width = width; this.height = height;}
	public Selection getSelection() 																{return this.selection;}
	public int getBurnInMarkerPos() 																{return this.burninMarkerPos;}
	public Thread getThread() 																		{return this.workerThread;}
	public int getWidth() 																			{return this.width;}
	public ArrayList<ArrayList<Double>> getMarksList() 												{return marks;}

	//Scrollable interface functions:
	public Dimension getPreferredScrollableViewportSize() 											{return null;}
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) 	{return 0;}
	public boolean getScrollableTracksViewportHeight() 												{return true;}
	public boolean getScrollableTracksViewportWidth() 												{return getParent() instanceof JViewport && getParent().getWidth() > getPreferredSize().width;}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) 	{return 0;}

	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
