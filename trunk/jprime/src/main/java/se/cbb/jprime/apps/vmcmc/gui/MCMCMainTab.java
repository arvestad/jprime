package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.cbb.jprime.apps.vmcmc.libs.MCMCMath;

import java.lang.Math;

/**
 * MCMCMainTab: Tab panel for the main tab. Central to all numerical analysis of a file.
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
public class MCMCMainTab extends MCMCStandardTab {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MCMCGraphToolPanel graphtoolPanel;

	private MCMCDisplayPanel filePanel;
	private MCMCDisplayPanel mathPanel;
	private MCMCDisplayPanel burninPanel;

	private JComboBox droplist;
	private JTextField burninField;

	private Thread[] workerThreads;

	private Lock displaypanelLock;
	
	private double confidencelevel;

	public MCMCMainTab() {
		super();

		displaypanelLock = new ReentrantLock();
		confidencelevel = 0.95;

		graphtoolPanel = new MCMCGraphToolPanel();
		
		workerThreads = new Thread[9];

		for(int i=0; i<workerThreads.length; i++)
			workerThreads[i] = new Thread();

		westpanel.setLayout(new FlowLayout());
		westpanel.setMinimumSize(new Dimension(300, 0));
		westpanel.setPreferredSize(new Dimension(300, 0));
		westpanel.setBackground(new Color(0xFFEEEEFF));

		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));

		southPanel.setMinimumSize(new Dimension(0, 28));
		southPanel.setPreferredSize(new Dimension(0, 28));

		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		southPanel.setBackground(new Color(0xFFEEEEFF));

		centerPanel.add(graphtoolPanel);
		westpanel.add(createDisplayPanels());
		
		initDefaultButtons();
	}
	
	/*
	 * initDefaultButtons: Add default buttons for the bottom panel.
	 */
	private void initDefaultButtons() {
		JButton clearTreeButton = new JButton("Clear Tree Markings");
		clearTreeButton.setBackground(Color.WHITE);
		
		//Button listener - removes tree markings inside the graph.
		clearTreeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				graphtoolPanel.clearGraphMarks();
			}
		});
		
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(clearTreeButton);
		southPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	}

	/*
	 * CreateDisplayPanels: Creates and adds panels responsible showing, file information,
	 * statistics and burn in.
	 */
	private JPanel createDisplayPanels() {
		JPanel panel = new JPanel();

		final JTextField confidencelevelField = new JTextField();	//Text field for bayesian confidence level
		burninField = new JTextField();	//Text field for burn in

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(new Color(0xFFEEEEFF));

		String[] paramnames = {"File Name:"};
		String[] burninNames = {"Burn in:", "Enter burnin", "Estimated burnin (ESS): ", "Estimated burnin (Geweke): ", "Estimated burnin (Our): "};

		JComponent[] burninComponents = {new JLabel(), burninField, new JLabel(), new JLabel(), new JLabel()};

		confidencelevelField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				try {
					confidencelevel = Double.parseDouble(confidencelevelField.getText())/100;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Not a valid number. Only numbers 0-100 allowed.");
				}

				if(confidencelevel > 1.0) {
					confidencelevel = 1.0;
				}
				else if(confidencelevel < 0) {
					confidencelevel = 0;
				}

				updateDisplayPanels();
			}
		});

		droplist = new JComboBox();
		droplist.setBackground(Color.WHITE);

		filePanel = new MCMCDisplayPanel(paramnames, "Parameter");
		mathPanel = new MCMCDisplayPanel("Statistics");

		mathPanel.addComponent("Arithmetic mean: ", new JLabel());
		mathPanel.addComponent("Arithmetic deviation: ", new JLabel());
		mathPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		mathPanel.addComponent("Geometric mean: ", new JLabel());
		mathPanel.addComponent("Geometric deviation: ", new JLabel());
		mathPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		mathPanel.addComponent("Harmonic mean: ", new JLabel());
		mathPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		mathPanel.addComponent("Maximum value: ", new JLabel());
		mathPanel.addComponent("Minimum value: ", new JLabel());
		mathPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		mathPanel.addComponent("Confidence level: ", new JLabel());
		mathPanel.addComponent("Bayesian confidence: ", new JLabel());

		burninPanel = new MCMCDisplayPanel();

		mathPanel.add(confidencelevelField);
		mathPanel.addComponent("Enter confidence level:", confidencelevelField);

		filePanel.add(Box.createRigidArea(new Dimension(0, 5)));
		filePanel.add(droplist);
		filePanel.add(Box.createRigidArea(new Dimension(0, 5)));

		burninPanel.addComponents(burninNames, burninComponents);

		panel.add(Box.createRigidArea(new Dimension(0, 2)));
		panel.add(filePanel);
		panel.add(Box.createRigidArea(new Dimension(0, 2)));
		panel.add(mathPanel);
		panel.add(Box.createRigidArea(new Dimension(0, 2)));
		panel.add(burninPanel);

		return panel;
	}
		
	
	/*
	 * updateDisplayPanels: Will calculate, format and display information for each display 
	 * panel in the left panel. 
	 */
	public void updateDisplayPanels(){
		final NumberFormat formatter = new DecimalFormat("0.#####E0");

		if(datacontainer != null) {
			final Object[] serie = datacontainer.getValueSerie(seriesID).toArray();
			String[] paramName = {datacontainer.getFileName()};

			filePanel.update(paramName);
			mathPanel.labels.get(7).setText(confidencelevel*100 + "%");

			int numPoints = serie.length;	
			final int numBurnInPoints = (int)(numPoints*burnin);		
			
			MCMCMath tests = new MCMCMath();
			int ess = tests.calculateESS(serie);
			int geweke = tests.calculateGeweke(serie);
			boolean gelmanRubin = tests.GelmanRubinTest(serie, ess);

			if(numPoints-numBurnInPoints != 0){
			
				burninPanel.getValueLabel(0).setText(String.valueOf(numBurnInPoints));
				burninPanel.getValueLabel(2).setText(String.valueOf(ess));
				if (geweke != -1)
					burninPanel.getValueLabel(3).setText(String.valueOf(geweke));
				else
					burninPanel.getValueLabel(3).setText(String.valueOf("Not Converged"));
				burninPanel.getValueLabel(4).setText(String.valueOf(gelmanRubin));
				burninField.setText(String.valueOf(numBurnInPoints));				

				for(int t=0; t<workerThreads.length; t++) {
					workerThreads[t].interrupt();
				}
				
				final Double[] data = new Double[serie.length-numBurnInPoints];
				System.arraycopy(serie, numBurnInPoints, data, 0, serie.length-numBurnInPoints);

				//Thread updating arithmetic mean.
				workerThreads[0] = new Thread() {
					public void run() {

						double values = 0;

						for(int i = 0; i < data.length; i++)
							values+= (Double)data[i];

						double result = values/data.length;

						displaypanelLock.lock();
						mathPanel.labels.get(0).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[0].start();

				//Thread updating arithmetic standard deviation.
				workerThreads[1] = new Thread() {
					public void run() {
						
						double sigmaSquare = 0;
						double mean, values = 0;
						int i;
						for(i = 0; i < data.length; i++)
							values+= (Double)data[i];
						
						i = 0;
						mean = values/data.length;
						for(; i < data.length; i++)
							sigmaSquare+= java.lang.Math.pow((Double)data[i] - mean,2);

						double result = (double)java.lang.Math.sqrt(sigmaSquare/(i-1));

						displaypanelLock.lock();
						mathPanel.labels.get(1).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[1].start();

				//Thread updating geometric mean
				workerThreads[2] = new Thread() {
					public void run() {
						double values = 1;
						double power = (double) 1/data.length;

						for(int i=0; i < data.length; i++)
							values *= java.lang.Math.pow((Double)data[i],power);

						double result = values;
			
						displaypanelLock.lock();
						if(Double.isNaN(result))
							mathPanel.labels.get(2).setText("NaN");
						else
							mathPanel.labels.get(2).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[2].start();

				//Thread updating geometric standard deviation
				workerThreads[3] = new Thread() {
					public void run() {
				        double sum = 0;
						double values = 1;
						double power = (double) 1/data.length;

						for(int i=0; i < data.length; i++)
							values *= java.lang.Math.pow((Double)data[i],power);
						
				        int numValues = data.length;
				        
				        for(int i = 0; i < numValues; i++ ) {
				            sum += Math.pow(Math.log((Double)data[i]), 2);
				        }
				        double result = Math.abs(Math.exp(Math.sqrt(sum/(numValues-1) - ((numValues/(numValues-1))*Math.pow(Math.log(values),2)))));

						displaypanelLock.lock();
						if(Double.isNaN(result))
							mathPanel.labels.get(3).setText("NaN");
						else
							mathPanel.labels.get(3).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[3].start();

				//Thread updating harmonic mean
				workerThreads[4] = new Thread() {
					public void run() {
						double sum = 0;
						int numValues = data.length;
						for(int i = 0; i < numValues; i++) {
							sum += (double)1/(Double)data[i];
						}
						
						double result = (double)numValues/sum;
						displaypanelLock.lock();
						mathPanel.labels.get(4).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[4].start();

				//Thread updating maximum value
				workerThreads[5] = new Thread() {
					public void run() {
						double result = data[0];
						for(int i=0; i<data.length; i++) {
							double value = data[i];

							if(value > result)
								result = value;
						}
						
						displaypanelLock.lock();
						mathPanel.labels.get(5).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[5].start();

				//Thread updating minimum value
				workerThreads[6] = new Thread() {
					public void run() {
						double result = data[0];
						for(int i=0; i<data.length; i++) {
							double value = data[i];

							if(value < result)
								result = value;
						}
						
						displaypanelLock.lock();
						mathPanel.labels.get(6).setText(String.valueOf(formatter.format(result)));
						displaypanelLock.unlock();
					}
				};
				workerThreads[6].start();

				//Thread updating bayesian confidence interval
				workerThreads[7] = new Thread() {
					public void run() {
						double values = 0;

						for(int i = 0; i < data.length; i++)
							values+= (Double)data[i];

						double aritMean = values/data.length;
						
						Arrays.sort(data);
						double comp, nearest=(Double)data[0];
						int equalStart = 0, equalEnd = 0, tempHolder;

						for(int i = 0; i < data.length-1; i++){

							comp = Math.abs(aritMean-nearest) - Math.abs(aritMean-(Double)data[i+1]);
							if(comp > 0) {nearest = (Double)data[i+1]; equalStart = i+1;}
							if(comp == 0){equalEnd = i+1;}
						}
						if(equalEnd == 0)
							tempHolder = equalStart;
						else
							tempHolder = equalStart + (equalEnd - equalStart)/2;
						double[] start = {nearest, tempHolder};
						
						int intervalLength = (int) ((double)(data.length)*(confidencelevel));

						int startPos = (int)start[1]; 
						double startNum = start[0];
						int leftIndex = startPos-1, rightIndex = startPos+1;
						double tempHolder1, tempHolder2;

						if(data.length == 0) 
						{
							tempHolder1 = Double.NaN;
							tempHolder2 = Double.NaN;
							double[] result = {tempHolder1, tempHolder2};

							displaypanelLock.lock();
							mathPanel.labels.get(8).setText(String.valueOf(result[0] + " ; " + result[1]));
							displaypanelLock.unlock();
						}
						else if(data.length == 1)
						{
							tempHolder1 = (Double) data[0];
							tempHolder2 = (Double) data[0];
							double[] result = {tempHolder1, tempHolder2};

							displaypanelLock.lock();
							mathPanel.labels.get(8).setText(String.valueOf(result[0] + " ; " + result[1]));
							displaypanelLock.unlock();
						}
						else if(data.length == 2) 
						{
							tempHolder1 = (Double) data[0];
							tempHolder2 = (Double) data[1];
							double[] result = {tempHolder1, tempHolder2};

							displaypanelLock.lock();
							mathPanel.labels.get(8).setText(String.valueOf(result[0] + " ; " + result[1]));
							displaypanelLock.unlock();
						}	
						else
						{
							for(int i = 0 ; i < intervalLength ; i++) 
							{

								if(leftIndex == 0)
									if(rightIndex < data.length-1)
										rightIndex++;

								if(rightIndex == data.length-1)
									if(leftIndex > 0)
										leftIndex--;

								if(leftIndex > 0 && Math.abs((Double)data[leftIndex] - startNum) <= Math.abs((Double)data[rightIndex] - startNum))
								{
									leftIndex--;
								}
								else if(rightIndex < data.length-1 && Math.abs((Double)data[leftIndex] - startNum) > Math.abs((Double)data[rightIndex] - startNum))
									rightIndex++;
							}
							double[] result = {(Double) data[leftIndex], (Double) data[rightIndex]};

							displaypanelLock.lock();
							mathPanel.labels.get(8).setText(String.valueOf(result[0] + " ; " + result[1]));
							displaypanelLock.unlock();
						}
					}
				};
				workerThreads[7].start();
			}
			else{
				//If burn in includes all data points set the following default values:
				String[] values = {"0","0","0","0", "0", "0", "0",  String.valueOf(confidencelevel*100 + "%"), "0 ; 0"};
				mathPanel.update(values);

				burninPanel.getValueLabel(0).setText(String.valueOf(numPoints));
				burninField.setText(String.valueOf(numPoints));
			}
		}
	}

	public MCMCDisplayPanel getFilePanel() {return filePanel;}
	public MCMCDisplayPanel getMathPanel() {return mathPanel;}
	public MCMCDisplayPanel getBurnInPanel() {return burninPanel;}
	public JComboBox getDropList() {return droplist;}
	public JTextField getBurnInField() {return burninField;}
	public MCMCGraphToolPanel getGraphTool() {return graphtoolPanel;}
}
