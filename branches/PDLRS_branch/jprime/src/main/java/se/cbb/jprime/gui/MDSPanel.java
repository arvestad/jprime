package se.cbb.jprime.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

import se.cbb.jprime.math.PRNG;

/**
 * Panel for showing 2-D multidimensional scaling (MDS) data.
 * MDS is also known as principal coordinate analysis (PCoA); not
 * to be confused with principal component analysis (PCA).
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
	
	private double xmin = Double.MAX_VALUE;
	private double xmax = -Double.MAX_VALUE;
	private double ymin = Double.MAX_VALUE;
	private double ymax = -Double.MAX_VALUE;
	
	private Font font;
	private Color backgroundColor;
	private Color[] pointColors;
	private Color textColor;
	private double pointMaxDiameter;
	private double[] pointDiameters;
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
		this.weights = weights;
		
		// Find min and max coords.
		int n = this.coords[0].length;
		xmin = Double.MAX_VALUE;
		xmax = -Double.MAX_VALUE;
		ymin = Double.MAX_VALUE;
		ymax = -Double.MAX_VALUE;
		for (int i = 0; i < n; ++i) {
			if (coords[0][i] < xmin) { xmin = coords[0][i]; }
			if (coords[0][i] > xmax) { xmax = coords[0][i]; }
			if (coords[1][i] < ymin) { ymin = coords[1][i]; }
			if (coords[1][i] > ymax) { ymax = coords[1][i]; }
		}
		
		// Scale point size.
		this.pointMaxDiameter = 30.0;
		double maxw = -Double.MAX_VALUE;
		for (int i = 0; i < n; ++i) {
			if (this.weights[i] > maxw) { maxw = this.weights[i]; }
		}
		this.pointDiameters = new double[n];
		for (int i = 0; i < n; ++i) {
			// Area prop to weight => diameter prop to sq.root of weight.
			this.pointDiameters[i] = pointMaxDiameter * Math.sqrt(weights[i] / maxw);
		}
		
		this.font = new Font("Verdana", Font.PLAIN, 12);
		this.backgroundColor = Color.WHITE;
		this.setBackground(this.backgroundColor);
		this.pointColors = new Color[n];
		PRNG prng = new PRNG();
		for (int i = 0; i < n; ++i) {
			float hue = prng.nextFloat();
			float saturation = 1.0f;  // 1.0 for brilliant, 0.0 for dull.
			float luminance = 0.7f;   // 1.0 for brighter, 0.0 for black.
			Color col = Color.getHSBColor(hue, saturation, luminance);
			pointColors[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(), 196);
		}
		this.textColor = Color.BLACK;
		this.textOffset = 0;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// Scale coords onto panel.
		int n = this.coords[0].length;
		double[][] scaledCoords = new double[2][];
		scaledCoords[0] = new double[n];
		scaledCoords[1] = new double[n];
		double xmargin = getWidth() / 8.0;
		double ymargin = getHeight() / 8.0;
		double xscale = (getWidth() - 2 * xmargin) / (xmax - xmin);
		double yscale = (getHeight() - 2 * ymargin) / (ymax - ymin);
		for (int i = 0; i < n; ++i) {
			scaledCoords[0][i] = xmargin + xscale * (coords[0][i] - xmin);
			scaledCoords[1][i] = ymargin + yscale * (coords[1][i] - ymin);
		}
		
		Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw points.
        for (int i = 0; i < scaledCoords[0].length; ++i) {
        	g2.setPaint(this.pointColors[i]);
        	g2.fill(new Ellipse2D.Double(scaledCoords[0][i], scaledCoords[1][i],
        			this.pointDiameters[i], this.pointDiameters[i]));
        }
        // Draw labels.
        g2.setFont(this.font);
        g2.setPaint(this.textColor);
        for (int i = 0; i < this.labels.length; ++i) {
            g2.drawString(this.labels[i], (float) (scaledCoords[0][i] + this.pointDiameters[i] + this.textOffset),
            		(float) (scaledCoords[1][i] + this.pointDiameters[i] + this.textOffset));
        }
	};
}
