package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * MCMCDisplayPanel: Panels with specific layout. Provides functionality for easy creation
 * and manipulation of multiple labels. Used for showing numerical calculations and information
 * about the files and parameters.
 */
public class MCMCDisplayPanel extends JPanel{
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long serialVersionUID = 1L;
	private JLabel[] labelNames;
	private JLabel[] labelValues;
	public ArrayList<JLabel> labels = new ArrayList<JLabel>();

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCDisplayPanel(){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(new Color(0xFFEEEEFF));
		TitledBorder border = BorderFactory.createTitledBorder("");
		border.setTitleFont(new Font("arial", Font.BOLD, 13));
		this.setBorder(border);
	}
	
	public MCMCDisplayPanel(String [] arr, String name){
		setupLabels(arr);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(new Color(0xFFEEEEFF));
		TitledBorder border = BorderFactory.createTitledBorder(name);
		border.setTitleFont(new Font("arial", Font.BOLD, 13));
		this.setBorder(border);
	}

	public MCMCDisplayPanel(String name){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(new Color(0xFFEEEEFF));
		TitledBorder border = BorderFactory.createTitledBorder(name);
		border.setTitleFont(new Font("arial", Font.BOLD, 13));
		this.setBorder(border);
	}

	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	/** setupLabels: Creates description labels */
	private void setupLabels(String[] nameArray) {
		labelNames = new JLabel[nameArray.length];
		labelValues = new JLabel[nameArray.length];

		for(int i = 0; i <nameArray.length; i++){
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setBackground(new Color(0xFFEEEEFF));
			labelValues[i] = label;  
		}

		for(int i = 0, k = 0; i < nameArray.length; i++, k++){
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(275, 18));
			panel.setLayout(new GridLayout());
			if(k == 2){
				this.add(Box.createRigidArea(new Dimension(200,10)));
				k = 0;
			}

			labelNames[i] = new JLabel(nameArray[i]);
			labelNames[i].setFont(new Font("arial", Font.BOLD, 11));
			labelNames[i].setOpaque(true);
			labelNames[i].setBackground(new Color(0xFFEEEEFF));

			panel.add(labelNames[i]);
			panel.add(labelValues[i]);

			this.add(panel);
		}
	}

	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	/** addComponents: Creates components from a given array of names */
	public void addComponents(String[] nameArray, JComponent[] components) {
		JPanel panel;
		labelNames = new JLabel[nameArray.length];
		labelValues = new JLabel[nameArray.length];

		for(int i = 0; i < nameArray.length; i++) {
			labelNames[i] = new JLabel(nameArray[i]);

			if(components[i] instanceof JLabel) 
				labelValues[i] = (JLabel) components[i];

			panel = new JPanel();

			panel.setPreferredSize(new Dimension(275, 18));
			panel.setLayout(new GridLayout());
			panel.setBackground(new Color(0xFFEEEEFF));
			labelNames[i].setFont(new Font("arial", Font.BOLD, 11));

			panel.add(labelNames[i]);
			panel.add(components[i]);
			this.add(panel);
		}
	}
	
	/** addComponent: Creates single component */
	public void addComponent(String name, Component component) {
		JPanel panel = new JPanel();
		
		if(component instanceof JLabel) 
			labels.add((JLabel) component);

		panel.setPreferredSize(new Dimension(275, 18));
		panel.setLayout(new GridLayout());
		panel.setBackground(new Color(0xFFEEEEFF));
		
		if(name != null) {
			JLabel label = new JLabel(name);
			label.setFont(new Font("arial", Font.BOLD, 11));
			panel.add(label);
		}
		panel.add(component);
		this.add(panel);
	}
	
	/** addJTextField: Used for burn-in text field. */
	public void addJTextField(String caption, JTextField field) {
		JPanel panel = new JPanel();

		panel.setPreferredSize(new Dimension(275, 18));
		panel.setLayout(new GridLayout());

		JLabel label = new JLabel(caption);
		label.setFont(new Font("arial", Font.BOLD, 11));

		panel.add(label);
		this.add(panel);
	}

	public JLabel getNameLabel	(int index) 	{return this.labelNames[index];}
	public JLabel getValueLabel	(int index)		{return labelValues[index];}

	/** Update: Used for updating the labels that show the numerical calculation results.*/
	public void update(String value, int index) {labelValues[index].setText(value);}

	/** Update: Used for updating the burn-in text field. */
	public void update(String[] values) {
		for(int i = 0 ; i < labels.size(); i++)
			labels.get(i).setText(values[i]);
	}

	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
