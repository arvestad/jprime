package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

/**
 * MCMCGraphRuler: Draw ruler between values min and max.
 */
public class MCMCGraphRuler extends JPanel {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long 	serialVersionUID = 1L;
	private double 				max;
	private double 				min;
	
	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCGraphRuler() {
		this.setMaximumSize(new Dimension(50, this.getMaximumSize().height));
		this.setPreferredSize(new Dimension(50, 0));
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void paint(Graphics g) {
		double d = this.getHeight()/10;
		double dv = (max-min)/8;
		final NumberFormat formatter = new DecimalFormat("0.####");
		
		g.setColor(new Color(0xFFEEEEFF));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
				
		for(int i=1; i<10; i++) {
			g.setColor(Color.BLACK);
			g.fillRect(0,(int) (i*d)+1, 5, 1);
			
			g.setFont(new Font("arial", Font.BOLD, 9));
			g.drawString(String.valueOf(formatter.format(max - (dv*(i-1)))), 8, (int) (i*d+4));
		}
	}
	
	public void setMax(double max) 	{this.max = max;}
	public void setMin(double min) 	{this.min = min;}
	public double getMax() 			{return max;}
	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
