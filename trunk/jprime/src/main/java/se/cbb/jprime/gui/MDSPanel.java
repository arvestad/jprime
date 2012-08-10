package se.cbb.jprime.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import javax.swing.JPanel;

import se.cbb.jprime.math.PRNG;

/**
 * Panel for showing 2-D multidimensional scaling (MDS) data.
 * MDS is also known as principal coordinate analysis (PCoA).
 * 
 * @author Joel Sjöstrand.
 */
public class MDSPanel extends JPanel {
	
	/** Eclipse-generated ID. */
	private static final long serialVersionUID = 3972285834995269026L;
	
	/** IDs for the points. */
	private String[] labels;
	
	/** Original coords. */
	private double[][] coords;
	
	/** Weights. */
	private double[] weights;
	
	/** Scaled coords for the display. */
	private double[][] scaledCoords;
	
	private Font font;
	private Color backgroundColor;
	private Color[] pointColors;
	private Color textColor;
	private double pointMinRadius;
	private double[] pointSizes;
	private int textOffset;
	
	/**
	 * Constructor.
	 * @param labels labels for the various elements.
	 * @param coords 2-D coordinates of the elements, 2*k in size.
	 * @param weights weights for the elements.
	 * @param width panel width.
	 * @param height panel height.
	 */
	public MDSPanel(String[] labels, double[][] coords, double[] weights, int width, int height) {
		super();
		this.setSize(width, height);
		this.labels = labels;
		this.coords = coords;
		int n = this.coords[0].length;
		this.weights = weights;
		this.scaledCoords = new double[2][];
		this.scaledCoords[0] = new double[n];
		this.scaledCoords[1] = new double[n];
		
		// Scale according to panel size.
		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		for (int i = 0; i < n; ++i) {
			if (coords[0][i] < xmin) { xmin = coords[0][i]; }
			if (coords[0][i] > xmax) { xmax = coords[0][i]; }
			if (coords[1][i] < ymin) { ymin = coords[1][i]; }
			if (coords[1][i] > ymax) { ymax = coords[1][i]; }
		}
		double xmargin = width / 8.0;
		double ymargin = height / 8.0;
		double xscale = (width - 2 * xmargin) / (xmax - xmin);
		double yscale = (height - 2 * ymargin) / (ymax - ymin);
		for (int i = 0; i < n; ++i) {
			this.scaledCoords[0][i] = xmargin + xscale * (coords[0][i] - xmin);
			this.scaledCoords[1][i] = ymargin + yscale * (coords[1][i] - ymin);
		}
		
		// Scale point size.
		this.pointMinRadius = 1.0;
		double minw = Double.MAX_VALUE;
		for (int i = 0; i < n; ++i) {
			if (weights[i] < minw) { minw = weights[i]; }
		}
		this.pointSizes = new double[n];
		for (int i = 0; i < n; ++i) {
			// Area prop to weight => radius prop to sq.root of weight.
			this.pointSizes[i] = pointMinRadius * Math.sqrt(weights[i] / minw);
		}
		
		this.font = new Font("Verdana", Font.PLAIN, 12);
		this.backgroundColor = Color.WHITE;
		this.setBackground(this.backgroundColor);
		this.pointColors = new Color[n];
		PRNG prng = new PRNG();
		for (int i = 0; i < n; ++i) {
			//to get rainbow, pastel colors
			
			float hue = prng.nextFloat();
			float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
			float luminance = 1.0f; //1.0 for brighter, 0.0 for black
			Color col = Color.getHSBColor(hue, saturation, luminance);
			pointColors[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(), 196);
		}
		this.textColor = Color.BLACK;
		this.textOffset = 5;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(this.font);
        for (int i = 0; i < this.scaledCoords[0].length; ++i) {
        	g2.setPaint(this.pointColors[i]);
        	g2.fill(new Ellipse2D.Double(this.scaledCoords[0][i], this.scaledCoords[1][i],
        			this.pointSizes[i], this.pointSizes[i]));
        }
        g2.setPaint(this.textColor);
        for (int i = 0; i < this.labels.length; ++i) {
            g2.drawString(this.labels[i], (float) (this.scaledCoords[0][i] + this.textOffset),
            		(float) (this.scaledCoords[1][i] + this.textOffset));
        }
	};
}
