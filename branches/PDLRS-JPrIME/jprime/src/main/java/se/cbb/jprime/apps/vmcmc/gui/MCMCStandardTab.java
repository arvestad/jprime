package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import se.cbb.jprime.apps.vmcmc.libs.MCMCDataContainer;
import se.cbb.jprime.apps.vmcmc.libs.MCMCInterface;

/**
 * MCMCStandardTab: Default class for tab panels used in MCMCApplication. Sets default layout and
 * dimensions. Implements MCMCInterface to make sure that all tabs conform to the same basic
 * functionality.
 */
public class MCMCStandardTab extends JPanel implements MCMCInterface {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long 	serialVersionUID = 1L;
	protected JPanel 			centerPanel;
	protected JPanel 			westpanel;
	protected JPanel 			eastPanel;
	protected JPanel 			northPanel;
	protected JPanel 			southPanel;
	protected MCMCDataContainer datacontainer;
	protected int 				seriesID;
	protected double 			burnin;
	
	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCStandardTab() {
		seriesID 		= 0;
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(1200, 680));
		
		centerPanel 	= new JPanel();
		westpanel 		= new JPanel();
		eastPanel 		= new JPanel();
		northPanel 		= new JPanel();
		southPanel 		= new JPanel();
		
		centerPanel	.setBackground(new Color(0xFFEEEEFF));
		westpanel	.setBackground(new Color(0xFFEEEEFF));
		eastPanel	.setBackground(new Color(0xFFEEEEFF));
		northPanel	.setBackground(new Color(0xFFEEEEFF));
		southPanel	.setBackground(new Color(0xFFEEEEFF));
		
		centerPanel	.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));		
		westpanel	.setLayout(new BoxLayout(westpanel, BoxLayout.Y_AXIS));
		eastPanel	.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
		northPanel	.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		southPanel	.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		
		add(centerPanel, 	BorderLayout.CENTER);
		add(westpanel, 		BorderLayout.WEST);
		add(eastPanel, 		BorderLayout.EAST);
		add(northPanel, 	BorderLayout.NORTH);
		add(southPanel, 	BorderLayout.SOUTH);
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void addToCenter(Component component)					{centerPanel.add(component);}
	public void addToWest(Component component) 						{westpanel.add(component);}
	public void addToNorth(Component component) 					{northPanel.add(component);}
	public void addToSouth(Component component) 					{southPanel.add(component);}
	
	public JPanel getcenterPanel() 									{return centerPanel;}
	public JPanel getWestPanel() 									{return westpanel;}
	public JPanel getNorthPanel() 									{return eastPanel;}
	public JPanel getSouthPanel() 									{return southPanel;}
	
	public void setBurnIn(double burnin) 							{this.burnin = burnin;}
	public void setDataContainer(MCMCDataContainer datacontainer) 	{this.datacontainer = datacontainer;}
	public void setSeriesID(int id) 								{seriesID = id;}
	
	public int getSeriesID() 										{return seriesID;}
	public MCMCDataContainer getDataContainer() 					{return datacontainer;}
	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
