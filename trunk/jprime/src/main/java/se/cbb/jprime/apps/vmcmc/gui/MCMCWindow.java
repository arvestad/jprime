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
public class MCMCWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane fileTabs;
	private JPanel defaultpanel;
	
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
		defaultpanel = new JPanel();
		
		defaultpanel.setLayout(new BoxLayout(defaultpanel, BoxLayout.Y_AXIS));
		defaultpanel.setBackground(new Color(0xFFEEEEFF));
		
		fileTabs = new JTabbedPane();
		fileTabs.setBackground(new Color(0xFFEEFFEE));
		fileTabs.setFocusable(false);
		fileTabs.setVisible(false);
		
		defaultpanel.add(fileTabs);
		
		this.setContentPane(defaultpanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void appear() {fileTabs.setVisible(true);}
	public void windowAppear() {this.setVisible(true);}
	
	public void addTab(String title, Component panel) {
		fileTabs.add(title, panel);
		this.pack();
	}
	
	public void selectTab(int index) {fileTabs.setSelectedIndex(index);}
	public void getTab(int index) {fileTabs.getComponentAt(index);}
	public JTabbedPane getTabs() {return fileTabs;}
}

