package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * MCMCWindow: JFrame with default layout implemented. Stores a JTabbedPane containing each
 * file openened.
 */
public class MCMCWindow extends JFrame{
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long serialVersionUID = 1L;
	private JTabbedPane fileTabs;
	private JPanel defaultpanel;
	
	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCWindow() 
	{
		defaultpanel = new JPanel();
		
		defaultpanel.setLayout(new BoxLayout(defaultpanel, BoxLayout.Y_AXIS));
		defaultpanel.setBackground(new Color(0xFFEEEEFF));
		
		fileTabs = new JTabbedPane();
		fileTabs.setBackground(new Color(0xFFEEFFEE));
		fileTabs.setFocusable(false);
		fileTabs.setVisible(false);
		
		defaultpanel.add(fileTabs);
		
		this.setContentPane(defaultpanel);
		this.setSize(new Dimension(700, 240));
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public MCMCWindow(int size) 
	{
		defaultpanel 		= new JPanel();
		
		defaultpanel.setLayout(new BoxLayout(defaultpanel, BoxLayout.Y_AXIS));
		defaultpanel.setBackground(new Color(0xFFEEEEFF));
		
		fileTabs 			= new JTabbedPane();
		fileTabs.setBackground(new Color(0xFFEEFFEE));
		fileTabs.setFocusable(false);
		fileTabs.setVisible(false);
		
		defaultpanel.add(fileTabs);
		
		this.setContentPane(defaultpanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void addTab(String title, Component panel) {
		fileTabs.add(title, panel);
		this.pack();
	}
	public void appear() 				{fileTabs.setVisible(true);}
	public void windowAppear() 			{this.setVisible(true);}
	public void selectTab(int index) 	{fileTabs.setSelectedIndex(index);}
	public void getTab(int index) 		{fileTabs.getComponentAt(index);}
	public JTabbedPane getTabs() 		{return fileTabs;}
	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}

