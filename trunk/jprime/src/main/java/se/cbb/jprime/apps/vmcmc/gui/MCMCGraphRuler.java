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
 *	Created by: M Bark & J Mir� Arredondo (2010)
 *   E-mail: mikbar at kth dot se & jorgma at kth dot se
 *
 *   This file is part of the bachelor thesis "Verktyg f�r visualisering av MCMC-data" - VMCMC
 *	Royal Institute of Technology, Sweden
 * 
 *	File version: 1.0
 *	VMCMC version: 1.0
 *
 *	Modification history for this file:
 *	v1.0  (2010-06-15) First released version.
 */
public class MCMCGraphRuler extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private double max, min;
	
	MCMCGraphRuler() {
		this.setMaximumSize(new Dimension(50, this.getMaximumSize().height));
		this.setPreferredSize(new Dimension(50, 0));
	}
	
	public void paint(Graphics g) {
		g.setColor(new Color(0xFFEEEEFF));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		double d = this.getHeight()/10;
		double dv = (max-min)/8;
		
		final NumberFormat formatter = new DecimalFormat("0.####");
		
		for(int i=1; i<10; i++) {
			g.setColor(Color.BLACK);
			g.fillRect(0,(int) (i*d)+1, 5, 1);
			
			g.setFont(new Font("arial", Font.BOLD, 9));
			g.drawString(String.valueOf(formatter.format(max - (dv*(i-1)))), 8, (int) (i*d+4));
		}
	}
	
	public void setMax(double max) {this.max = max;}
	public void setMin(double min) {this.min = min;}
	public double getMax() {return max;}
}
