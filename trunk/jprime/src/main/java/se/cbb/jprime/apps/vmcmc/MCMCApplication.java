package se.cbb.jprime.apps.vmcmc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.vmcmc.libs.*;
import se.cbb.jprime.apps.vmcmc.gui.*;
import se.cbb.jprime.misc.Triple;

/**																							
 * 	The main class for VMCMC. It is responsible for efficiently co-ordinating calls between various GUI classes, Data handling classes, User requirements and MCMC statistics computation and convergence test classes. 
 *	The main method is to get the filename and the type of results a user wants from this program and to use the function calls from various other implemented classes to generate the required graphics and/or results.
 * 
 *   <p>This file is part of the bachelor thesis "Verktyg för visualisering av MCMC-data" - VMCMC
 *   Royal Institute of Technology, Sweden. (M.Bark, J. Miró)
 *  <p>This file is part of PhD project work for Royal Institute of Technology. (R. H. Ali)
 *
 *	@Modification_history
 *	v1.0  (2010-06-15) First released version by M Bark (mikbar at kth dot se) and J Miró (jorgma at kth dot se)
 *  <p>v1.1  (2011-2014) Second version work in progress
 *  <p>Updated by: Raja Hashim Ali (rhali at kth dot se)
 *  <p>This code has used a few functions and classes from JPrime code developed by Joel Sjöstrand. We are extremely grateful to him for helping us graciously in this project with his ideas, sincere help, developed codes and classes. 
 *  
 *  <code>
 *  @Class_Variables MCMCWindow window;
 *  @Private_Class_Functions
 *  MCMCApplication(), MCMCApplication(String), MCMCApplication(int, String, int, double)
 *  JMenuBar createMenuBar(), JMenuBar createDirectMenuBar(final File), void linkTabsToTrees(final JTabbedPane, final MCMCTreeTab),
 *  MCMCMainTab createMainPanel(final MCMCDataContainer), void linkMainToTabs(final MCMCMainTab, final JTabbedPane),
 *  void linkMainToTable(final MCMCMainTab, final MCMCTableTab), void linkMainToTrees(final MCMCMainTab, final MCMCTreeTab),
 *  MCMCTableTab createTablePanel(final MCMCDataContainer), void linkeTreesToMain(final MCMCTreeTab, final MCMCMainTab),
 *  MCMCTreeTab createTreePanel(MCMCDataContainer).
 *  @Public_Class_Functions
 *  static void main(String[]), MCMCWindow getWindow().
 *  @Classes
 *  MCMCApplication, MCMCDisplayPanel, MCMCGraphPanel, MCMCGraphRuler, MCMCGraphToolPanel, MCMCMainTab, MCMCStandardTab, MCMCTableTab,
 *  MCMCTreeTab, MCMCWindow, JcommanderUserWrapper, MCMCConsensusTree, MCMCDataContainer, MCMCFileReader, MCMCInterface, MCMCMath,
 *  MCMCNewick, MCMCTree, MCMCTreeNode, ParameterParser, Parameters, misc.Triple.
 *  </code>
 *
 *	@author Mikael Bark, J. Miró and Raja Hashim Ali
 *	@param filename, burnin, confidence_level.
 *	@return Statistical and convergence analysis of MCMC output from CODA, JPRiME and PRiME.
 *	@Usage java MCMCApplication [-h] [-f FILENAME] [[-b burnin] [-c confidencelevel] [-n] [-s] [-t] [-e] [-r] [-g]]
 */
public class MCMCApplication {
	private MCMCWindow window;

	/** Definition: 			Default constructor for VMCMC.										
		<p>Usage: 				When no filename is provided as input parameter.						
	 	<p>Function:			Opens up the first window that is used to supply input file. 				
	 	<p>Classes: 			MCMCWindow.
	 	<p>Internal Functions:	createMenuBar(). 		
	 	@return: 				A new graphical basic window by invoking MCMCWindow.					
	 */
	MCMCApplication() {
		window = new MCMCWindow();				
		
		window.setTitle("VMCMC Application");	
		window.setJMenuBar(createMenuBar());	
		window.validate();

		UIManager.put("TabbedPane.selected", new Color(0xFFEEEEFF));	
		UIManager.put("TabbedPane.contentAreaColor", new Color(0xFFEEEEFF));
		UIManager.put("TabbedPane.shadow", new Color(0xFF000000));
	}


	/** Definition: 			Constructor with filename for VMCMC.										
	<p>Usage: 				When a filename is provided as input parameter.						
 	<p>Function:			Opens up the second window that displays all graphs and statistical analysis. 
 	<p>Classes: 		MCMCWindow.		
	<p>Internal Functions:	createDirectMenuBar(File). 		
 	@return: 				A new graphical basic window by invoking MCMCWindow that has all statistics and convergence tests for the MCMC file performed.					
	 */
	MCMCApplication(String file1) {
		File file = new File(file1);
		int size = 540;
		window = new MCMCWindow(size);
		window.setTitle("VMCMC Application");
		window.setJMenuBar(createDirectMenuBar(file));	//Window will contain all the information provided in the file.
		window.validate();
		window.windowAppear();

		UIManager.put("TabbedPane.selected", new Color(0xFFEEEEFF));	//Setting color for selected tabs
		UIManager.put("TabbedPane.contentAreaColor", new Color(0xFFEEEEFF));
		UIManager.put("TabbedPane.shadow", new Color(0xFF000000));
	}


	/** Definition: 			Constructor with filename, custom burnin and/or confidence level for VMCMC.										
	<p>Usage: 				When filename and custom burnin/confidence level/both are provided as input parameters.						
 	<p>Function:			Opens up the second window that displays all graphs and statistical analysis.  				
 	<p>Classes: 			MCMCWindow, MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None. 				
 	@return: 				A new graphical basic window by invoking MCMCWindow.					
	 */
	MCMCApplication(int choice, String file1, int burnin, double confidencelevel) {
		File file = new File(file1);

		if (confidencelevel<0)
			confidencelevel = 0;
		else if(confidencelevel>100)
			confidencelevel = 100;

		if(file != null) 
		{
			MCMCDataContainer datacontainer = null;

			try 
			{
				//Call library method to store contents of file to MCMCDataContainer
				if(!file.isFile())
				{
					if (file.isDirectory())
					{
						System.out.println("This is a directory. Please give full file path with file name.");
						System.exit(-1);
					}
					else
					{
						System.out.println("Filename incorrect or file path not found.");
						System.exit(-2);
					}
				}
				datacontainer = MCMCFileReader.readMCMCFile(file);	
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(datacontainer != null) {
				int numSeries = datacontainer.getNumValueSeries();
				if(numSeries != 0)
				{
					ArrayList<String> array = datacontainer.getValueNames();
					for (int i = 0; i < numSeries; i++)
					{
						final Object[] serie = datacontainer.getValueSerie(i).toArray();
						int j = i+1;
						int length = serie.length;

						MCMCMath tests = new MCMCMath();
						if(choice == 5)
						{
							int ess = tests.calculateESS(serie);
							if (ess > length/800)
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" estimated burn in point is " + ess);
							else
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" insignificant. Not converged");
						}
						else if(choice == 4)
						{
							int geweke = tests.calculateGeweke(serie);
							if (geweke != -1)
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" estimated burn in point is " + geweke);
							else
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" not converged");
						}
						else if(choice == 6)
						{
							boolean gelmanRubin = tests.GelmanRubinTest(serie, burnin);
							if(gelmanRubin == true)	
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" converged at data point " + burnin);
							else
								System.out.println("    Parameter " + j + ": \"" + array.get(i) + "\" not converged at data point " + burnin);
						}
						else if(choice == 2)
						{
							System.out.println("\n    ------ Parameter " + j + ": "+ array.get(i) + " ------");
							
							int geweke = tests.calculateGeweke(serie);
							boolean gelmanRubin = tests.GelmanRubinTest(serie, burnin);
							int ess = tests.calculateESS(serie);

							if (ess > length/800)
								System.out.println("    'ESS': Estimated burn in point is " + ess);
							else
								System.out.println("    'ESS': Insignificant. Not converged");

							if (geweke != -1)
								System.out.println("    'Geweke': Estimated burn in point is " + geweke);
							else
								System.out.println("    'Geweke': Not converged");

							if(gelmanRubin == true)	
								System.out.println("    'Gelman Rubin': Converged at data point " + burnin);
							else
								System.out.println("    'Gelman Rubin': Not converged at data point " + burnin);
						}
						else if(choice == 3)
						{
							if(serie.length - burnin > 0)
							{
								final Double[] data = new Double[serie.length-burnin];
								System.arraycopy(serie, burnin, data, 0, serie.length-burnin);

								int numValues = data.length;
								double values = 0, values1 = 1, mean, sigmaSquare = 0, arithmetic_standard_dev, power = (double) 1/numValues, geometric_mean, sum = 0, sum1 = 0, geometric_standard_dev, harmonic_mean, max_value = data[0], min_value = data[0];

								System.out.println("\n    ------ Parameter " + j + ": "+ array.get(i) + " ------");

								for(int k = 0; k < numValues; k++)
								{
									values+= (Double)data[k];
									values1 *= java.lang.Math.pow((Double)data[k],power);
									sum += Math.pow(Math.log((Double)data[k]), 2);
									sum1 += (double)1/(Double)data[k];

									if(data[k] > max_value)
										max_value = data[k];

									if(data[k] < min_value)
										min_value = data[k];
								}

								mean = values/numValues;
								geometric_mean = values1;
								harmonic_mean = (double)numValues/sum1;

								for(int k = 0; k < numValues; k++)
									sigmaSquare+= java.lang.Math.pow((Double)data[k] - mean,2);

								arithmetic_standard_dev = (double)java.lang.Math.sqrt(sigmaSquare/(numValues-1));
								geometric_standard_dev = Math.abs(Math.exp(Math.sqrt(sum/(numValues-1) - ((numValues/(numValues-1))*Math.pow(Math.log(values1),2)))));

								System.out.println("    Arithmetic Mean:                         " + mean);
								System.out.println("    Arithmetic Standard Deviation:           " + arithmetic_standard_dev);
								System.out.println("    Geometric Mean:                          " + geometric_mean);
								System.out.println("    Geometric Standard Deviation:            " + geometric_standard_dev);
								System.out.println("    Harmonic Mean:                           " + harmonic_mean);
								System.out.println("    Minimum Value:                           " + min_value);
								System.out.println("    Maximum Value:                           " + max_value);

								Arrays.sort(data);
								double comp, nearest=(Double)data[0];
								int equalStart = 0, equalEnd = 0, tempHolder;

								for(int k = 0; k < numValues-1; k++)
								{
									comp = Math.abs(mean-nearest) - Math.abs(mean-(Double)data[k+1]);
									if(comp > 0) {nearest = (Double)data[k+1]; equalStart = k+1;}
									if(comp == 0){equalEnd = k+1;}
								}

								if(equalEnd == 0)
									tempHolder = equalStart;
								else
									tempHolder = equalStart + (equalEnd - equalStart)/2;

								double[] start = {nearest, tempHolder};
								int intervalLength = (int) ((double)(numValues)*(confidencelevel/100));
								int startPos = (int)start[1]; 
								double startNum = start[0];
								int leftIndex = startPos-1, rightIndex = startPos+1;
								double tempHolder1, tempHolder2;
								System.out.println("    Confidence Level:                        " + confidencelevel + "%");

								if(numValues == 0) 
								{
									tempHolder1 = Double.NaN;
									tempHolder2 = Double.NaN;
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("    Bayesian Confidence:                     " + result[0] + " ; " + result[1]);
								}
								else if(numValues == 1)
								{
									tempHolder1 = (Double) data[0];
									tempHolder2 = (Double) data[0];
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("    Bayesian Confidence:                     " + result[0] + " ; " + result[1]);
								}
								else if(numValues == 2) 
								{
									tempHolder1 = (Double) data[0];
									tempHolder2 = (Double) data[1];
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("    Bayesian Confidence:                     " + result[0] + " ; " + result[1]);
								}	
								else
								{
									for(int k = 0 ; k < intervalLength ; k++) 
									{
										if(leftIndex == 0)
											if(rightIndex < numValues-1)
												rightIndex++;

										if(rightIndex == numValues-1)
											if(leftIndex > 0)
												leftIndex--;

										if(leftIndex > 0 && Math.abs((Double)data[leftIndex] - startNum) <= Math.abs((Double)data[rightIndex] - startNum))
											leftIndex--;
										else if(rightIndex < numValues-1 && Math.abs((Double)data[leftIndex] - startNum) > Math.abs((Double)data[rightIndex] - startNum))
											rightIndex++;
									}
									double[] result = {(Double) data[leftIndex], (Double) data[rightIndex]};
									System.out.println("    Bayesian Confidence:                     " + result[0] + " ; " + result[1]);
								}
							}
						}
					}
				}
			}
		}
	}


	/** Definition: 			Private method for opening the first graphical window for VMCMC.										
	<p>Usage: 				When no filename is provided as input parameter, the default constructor with no file input refers to this function for coming up with the first interface.		
 	<p>Function:			Populates the first window and all its other options like about and exit etc. that are displayed in the first window. 	
 	<p>Classes: 			MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None.		
 	@return: 				A populated graphical basic window and handles all options used inside it.					
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menubar =  new JMenuBar();

		JMenu menuFile = new JMenu("File");
		JMenuItem itemOpen = new JMenuItem("Open file");
		JMenuItem itemClose = new JMenuItem("Exit");

		JMenu menuAbout = new JMenu("About");  
		JMenuItem itemAbout = new JMenuItem("About Visual MCMC");

		//Menu actionlistener responsible for closing application.
		itemClose.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		//Menu actionlistener responsible for creating filechooser, datacontainer and tabs.
		itemOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {   
				File file = null;

				//Filter class responsible for filtering out all but mcmc files and folders
				class MCMCFilter extends FileFilter {
					public boolean accept(File file) {
						if(file.isDirectory()) {	//Show folders inside chooser
							return true;
						}

						String extension = null;
						String name = file.getName();
						int i = name.lastIndexOf('.');

						if(i > 0 &&  i < name.length() - 1) {
							extension = name.substring(i+1).toLowerCase();	//Extract file extension
						}

						if(extension != null) {
							if (extension.equals("mcmc")||extension.equals("txt") ) {	//Show files with extension mcmc
								return true;
							} else {
								return false;
							}
						}

						return false;
					}

					public String getDescription() {  //Description of filter
						return "MCMC Files";
					}
				}

				JFileChooser chooser = new JFileChooser();
				chooser.addChoosableFileFilter(new MCMCFilter());	//Add custom filter to file chooser

				int returnValue = chooser.showOpenDialog(chooser);
				if(returnValue == JFileChooser.APPROVE_OPTION)
					file = chooser.getSelectedFile();

				if(file != null) {
					MCMCDataContainer datacontainer = null;

					try {
						//Call library method to store contents of file to MCMCDataContainer
						datacontainer = MCMCFileReader.readMCMCFile(file);	
					} catch (IOException e) {
						e.printStackTrace();
					}

					JTabbedPane tabs = new JTabbedPane();
					tabs.setBackground(new Color(0xFFDDDDFF));
					tabs.setFocusable(false);
					tabs.setBorder(BorderFactory.createLineBorder(new Color(0xFFEEEEFF), 2));

					MCMCMainTab mainPanel=null;
					MCMCTableTab tablePanel=null;
					MCMCTreeTab treePanel=null;

					//If there are numerical values in file. Create main panel and table panel
					if(datacontainer.getNumValueSeries() != 0) {
						mainPanel = createMainPanel(datacontainer);
						tablePanel = createTablePanel(datacontainer);

						tabs.add("Graph", mainPanel);
						tabs.add("Table", tablePanel);
					}

					//If there are tree parameters in file. Create tree tab.
					if(datacontainer.getNumTreeSeries() != 0) {
						treePanel = createTreePanel(datacontainer);

						tabs.add("Trees", treePanel);
					}

					//Link all tabs that should be linked when opened through menu
					if(mainPanel != null)
						linkMainToTabs(mainPanel, tabs);
					if(treePanel != null)
						linkTabsToTrees(tabs, treePanel);
					if(mainPanel != null && tablePanel != null) {
						linkMainToTable(mainPanel, tablePanel);
						linkTableToMain(tablePanel, mainPanel);
					}
					if(mainPanel != null && treePanel != null) {
						linkMainToTrees(mainPanel, treePanel);
						linkeTreesToMain(treePanel, mainPanel);
					}

					window.appear();	//Make tabs appear
					window.windowAppear();
					window.addTab(datacontainer.getFileName(), tabs);	//Name the tab after file
					window.selectTab(window.getTabs().getTabCount()-1);	//Switch to most recent tab
				}
			}
		});

		//Menu actionlistener responsible for displaying "about" window
		itemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JPanel aboutPanel = new JPanel();

				aboutPanel.setLayout(new GridLayout(5, 1));

				aboutPanel.add(new JLabel("VISUAL MCMC Ver 1.0\n"));
				aboutPanel.add(new JSeparator());
				aboutPanel.add(new JLabel("Created by: Jorge Miró & Mikael Bark"));
				aboutPanel.add(new JLabel("Updated by: Raja Hashim Ali"));

				JOptionPane.showMessageDialog(window, aboutPanel);
			}

		});

		menuFile.add(itemOpen);
		menuFile.add(itemClose);
		menubar.add(menuFile);
		menuAbout.add(itemAbout);
		menubar.add(menuAbout);

		return menubar;
	}


	/** Definition: 			Private method for opening the second graphical window directly for VMCMC.										
	<p>Usage: 				When a filename is provided as input parameter, the second constructor with filename refers to this function for coming up with the second interface.		
 	<p>Function:			Populates the second window with all its other options and all the statitical computations and convergence tests, that are displayed in the second window. 	
 	<p>Classes: 			MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None.		
 	@return: 				A populated graphical extended window that displays parameter values, sattistics, tests and trees found in the input file.					
	 */
	private JMenuBar createDirectMenuBar(final File file) 
	{
		JMenuBar menubar =  new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenuItem itemClose = new JMenuItem("Exit");
		JMenu menuAbout = new JMenu("About");  
		JMenuItem itemAbout = new JMenuItem("About Visual MCMC");

		//Menu actionlistener responsible for closing application.
		itemClose.addActionListener(new ActionListener() 
		{   
			public void actionPerformed(ActionEvent arg0) 
			{
				System.exit(0);
			}
		});

		if(file != null) 
		{
			MCMCDataContainer datacontainer = null;

			try 
			{
				//Call library method to store contents of file to MCMCDataContainer
				if(!file.isFile())
				{
					if (file.isDirectory())
					{
						System.out.println("This is a directory. Please give full file path with file name.");
						System.exit(-1);
					}
					else
					{
						System.out.println("Filename or file path incorrect. Also check help for suggestions.");
						System.exit(-2);
					}
				}

				datacontainer = MCMCFileReader.readMCMCFile(file);	
			} catch (IOException e) {
				e.printStackTrace();
			}

			JTabbedPane tabs = new JTabbedPane();
			tabs.setBackground(new Color(0xFFDDDDFF));
			tabs.setFocusable(false);
			tabs.setBorder(BorderFactory.createLineBorder(new Color(0xFFEEEEFF), 2));

			MCMCMainTab mainPanel=null;
			MCMCTableTab tablePanel=null;
			MCMCTreeTab treePanel=null;

			//If there are numerical values in file. Create main panel and table panel
			if(datacontainer.getNumValueSeries() != 0) {
				mainPanel = createMainPanel(datacontainer);
				tablePanel = createTablePanel(datacontainer);			
				tabs.add("Graph", mainPanel);
				tabs.add("Table", tablePanel);
			}

			//If there are tree parameters in file. Create tree tab.
			if(datacontainer.getNumTreeSeries() != 0) 
			{
				treePanel = createTreePanel(datacontainer);	
				tabs.add("Trees", treePanel);
			}

			//Link all tabs that should be linked when opened through menu
			if(mainPanel != null)
				linkMainToTabs(mainPanel, tabs);
			if(treePanel != null)
				linkTabsToTrees(tabs, treePanel);
			if(mainPanel != null && tablePanel != null) {
				linkMainToTable(mainPanel, tablePanel);
				linkTableToMain(tablePanel, mainPanel);
			}
			if(mainPanel != null && treePanel != null) {
				linkMainToTrees(mainPanel, treePanel);
				linkeTreesToMain(treePanel, mainPanel);
			}

			window.appear();	//Make tabs appear
			window.addTab(datacontainer.getFileName(), tabs);	//Name the tab after file
			window.selectTab(window.getTabs().getTabCount()-1);	//Switch to most recent tab
		}

		//Menu actionlistener responsible for displaying "about" window
		itemAbout.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				JPanel aboutPanel = new JPanel();

				aboutPanel.setLayout(new GridLayout(5, 1));

				aboutPanel.add(new JLabel("VISUAL MCMC Ver 1.0\n"));
				aboutPanel.add(new JSeparator());
				aboutPanel.add(new JLabel("Created by: Jorge Miró & Mikael Bark"));
				aboutPanel.add(new JLabel("Updated by: Raja Hashim Ali"));

				JOptionPane.showMessageDialog(window, aboutPanel);
			}

		});

		menuFile.add(itemClose);
		menubar.add(menuFile);
		menuAbout.add(itemAbout);
		menubar.add(menuAbout);

		return menubar;
	}

	/** Definition: 			Adds the applications default listeners for the tabs associated with current file.										
 	<p>Function:			 Update Tree Maps and tabel in the tree panel based on the Event Change.								
	 */
	private void linkTabsToTrees(final JTabbedPane tabs, final MCMCTreeTab treePanel) {
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(tabs.getSelectedIndex() == 2) {
					treePanel.updateTreeMaps();
					treePanel.updateTable();
				}
			}
		});
	}


	/** Definition: 			Creates and updates new instance of MCMCMainTab and adds default components. Sets application specific default values (including burnin - 10%).						
 	<p>Function:			Handle MCMCMainTab and set default values for it. Displays all the statistics and convergence test results for a parameter.				
 	<p>Classes:				MCMCMainTab, MCMCDataContainer, MCMCGraphToolPanel, 												
	 */
	private MCMCMainTab createMainPanel(final MCMCDataContainer datacontainer) {
		final MCMCMainTab mainPanel = new MCMCMainTab();

		MCMCGraphToolPanel graphtoolPanel = mainPanel.getGraphTool();
		JComboBox droplist = mainPanel.getDropList();

		mainPanel.setDataContainer(datacontainer);
		mainPanel.setBurnIn(0.1);
		mainPanel.updateDisplayPanels();

		graphtoolPanel.setDataContainer(datacontainer);
		graphtoolPanel.setSeriesID(0);
		graphtoolPanel.setBurnIn(0.1);

		graphtoolPanel.updateGraph();
		graphtoolPanel.updateRuler();

		droplist.setModel(new DefaultComboBoxModel(datacontainer.getValueNames().toArray())); 
		droplist.setSelectedIndex(0);

		//Slider listener - only updates components available in main.
		mainPanel.getGraphTool().getSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JSlider source = (JSlider) arg0.getSource();
				double burnin = (double) source.getValue()/source.getMaximum();

				mainPanel.setBurnIn(burnin);
				mainPanel.updateDisplayPanels();
			}
		});

		//Burn in field listener - updates burnin for main panel based on text field value. 
		mainPanel.getBurnInField().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int serieSize = datacontainer.getValueSerie(mainPanel.getSeriesID()).size();
				int fieldValue = 0;

				try {
					fieldValue = Integer.valueOf(mainPanel.getBurnInField().getText());
				} catch (NumberFormatException e) {}

				double burnin = (double) fieldValue/serieSize;

				mainPanel.setBurnIn(burnin);

				mainPanel.getGraphTool().setBurnIn(burnin);
				mainPanel.getGraphTool().updateGraph();

				mainPanel.updateDisplayPanels();
			}
		});

		//Droplist listener - will update seriesID for mainpanel and it's components.
		mainPanel.getDropList().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int seriesID = mainPanel.getDropList().getSelectedIndex();
				MCMCGraphToolPanel graphTool = mainPanel.getGraphTool();

				mainPanel.setSeriesID(seriesID);

				graphTool.setSeriesID(seriesID);
				graphTool.updateGraph();
				graphTool.updateRuler();

				mainPanel.updateDisplayPanels();
			}
		});

		return mainPanel;
	}


	/** Definition: 			Adds functionalty between Main tab and	the JTabbedPane.									
	<p>Usage: 				When data in an interval is to be extracted and examined.						
 	<p>Function:			Extract the interval from the graph and display in a new pane. 				
 	<p>Classes:				MCMCMainTab.  		
	 */
	private void linkMainToTabs(final MCMCMainTab mainPanel, final JTabbedPane tabs) {
		JButton extractSelectionButton = new JButton("Extract interval");
		extractSelectionButton.setBackground(Color.WHITE);

		extractSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MCMCDataContainer datacontainer = mainPanel.getDataContainer();
				MCMCGraphToolPanel graphTool = mainPanel.getGraphTool();
				MCMCGraphPanel graph = graphTool.getGraph();

				int first = (int) (((double) graph.getSelection().getLeftPos()/graphTool.getScrollPane().getWidth())*datacontainer.getNumLines());
				int last = (int) (((double) graph.getSelection().getRightPos()/graphTool.getScrollPane().getWidth())*datacontainer.getNumLines());

				MCMCMainTab submainPanel = createMainPanel(datacontainer.getSubDataContainer(first, last));

				tabs.add("Graph " + first + " - " + last, submainPanel);

				mainPanel.updateDisplayPanels();
				mainPanel.getGraphTool().updateGraph();
			}
		});

		mainPanel.addToSouth(extractSelectionButton);
		mainPanel.addToSouth(Box.createRigidArea(new Dimension(10, 0)));
	}


	/** Definition: 			Adds functionalty between Main tab and the Table tab.								
	<p>Usage: 				Select a parameter to see its data.						
 	<p>Function:			For a selected parameter in the main tab, display its corresponding data in table panel. 				
 	<p>Classes:				MCMCMainTab, MCMCTableTab.  													
	 */
	private void linkMainToTable(final MCMCMainTab mainPanel, final MCMCTableTab tablePanel) {
		mainPanel.getDropList().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int seriesID = mainPanel.getDropList().getSelectedIndex();

				if(tablePanel != null) {
					tablePanel.setSelectedButton(seriesID);
				}
			}
		});
	}


	/** Definition: 			Adds functionality between Main tab and the Trees tab.										
	<p>Usage: 				Make the main panel and tree panel uniform.						
 	<p>Function:			Handle and mantain the uniformity of the burnin selection between tree panel and main panel. 				
 	<p>Classes:				MCMCMainTab, MCMCTreeTab		
	 */
	private void linkMainToTrees(final MCMCMainTab mainPanel, final MCMCTreeTab treePanel) {
		mainPanel.getGraphTool().getSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JSlider source = (JSlider) arg0.getSource();
				double burnin = (double) source.getValue()/source.getMaximum();

				treePanel.setBurnIn(burnin);
			}
		});

		mainPanel.getBurnInField().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int serieSize = mainPanel.getDataContainer().getValueSerie(mainPanel.getSeriesID()).size();
				int fieldValue = 0;

				try {
					fieldValue = Integer.valueOf(mainPanel.getBurnInField().getText());
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Not a valid number. Only numbers allowed.");
				}

				double burnin = (double) fieldValue/serieSize;

				treePanel.setBurnIn(burnin);
			}
		});
	}


	/** Definition: 			Creates and updates new instance of MCMCTableTab and adds default components.										
	<p>Usage: 				Display the data in a tabular format. 						
 	<p>Function:			Adds the parameter names and parameter values to the Table Tab (Second Tab) 				
 	<p>Classes:				MCMCTabletab, MCMCDataContainer.
	 */
	private MCMCTableTab createTablePanel(final MCMCDataContainer datacontainer) {
		final MCMCTableTab tablePanel = new MCMCTableTab();
		ArrayList<String> names = datacontainer.getValueNames();
		int numValues = datacontainer.getNumValueSeries();

		tablePanel.setDataContainer(datacontainer);

		for(int i=0; i<numValues; i++) 
			tablePanel.addColumn(names.get(i), datacontainer.getValueSerie(i));

		return tablePanel;
	}


	/** Definition: 			Adds functionality between Table tab and the Main tab.						
 	<p>Function:			Create the left table tab on the main window for selecting the parameter.				
 	<p>Classes:				MCMCTableTab, MCMCMainTab, MCMCDataContainer.  													
	 */
	private void linkTableToMain(final MCMCTableTab tablePanel, final MCMCMainTab mainPanel) {
		MCMCDataContainer datacontainer = tablePanel.getDataContainer();

		int numValues = datacontainer.getNumValueSeries();

		JRadioButton[] buttons = new JRadioButton[numValues];
		tablePanel.setButtons(buttons);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 0));
		buttonPanel.setBackground(new Color(0xFFEEEEFF));

		ButtonGroup buttonGroup = new ButtonGroup();

		for(int i=0; i<numValues; i++) {

			buttons[i] = new JRadioButton();
			buttons[i].setBackground(new Color(0xFFEEEEFF));

			final int id = i;

			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					mainPanel.getDropList().setSelectedIndex(id);

					mainPanel.updateDisplayPanels();
				}
			});

			JPanel panel = new JPanel();
			panel.setBackground(new Color(0xFFEEEEFF));
			panel.add(buttons[i]);

			buttonPanel.add(panel);
			buttonGroup.add(buttons[i]);
		}

		tablePanel.addToNorth(buttonPanel);
	}


	/** Definition: 			Creates and updates new instance of MCMCMTreeTab and adds default components.
	<p>Usage: 				Used when tree data is found in the input file.						
 	<p>Function:			Build up teh Tree Panel using all the data from the file provided. 				
 	<p>Classes:				MCMCTreeTab, MCMCDataContainer. 												
	 */
	private MCMCTreeTab createTreePanel(MCMCDataContainer datacontainer) {
		final MCMCTreeTab treePanel = new MCMCTreeTab();

		treePanel.setDataContainer(datacontainer);
		treePanel.setSeriesID(0);
		treePanel.setBurnIn(0.1);

		treePanel.updateTreeMaps();
		treePanel.updateTable();
		treePanel.updateTreePanel();

		treePanel.getDropList().setModel(new DefaultComboBoxModel(datacontainer.getTreeNames().toArray()));

		JPanel droplistPanel = new JPanel();
		droplistPanel.setOpaque(false);
		droplistPanel.add(treePanel.getDropList());

		treePanel.addToSouth(droplistPanel);
		treePanel.addToSouth(Box.createRigidArea(new Dimension(10, 0)));

		return treePanel;
	}


	/** Definition: 			Adds functionalty between Tree tab and the Main Tab.										
	<p>Usage: 										
 	<p>Function:			 				
 	<p>Classes:				MCMCTreeTab, MCMCMainTab.
	 */
	private void linkeTreesToMain(final MCMCTreeTab treePanel, final MCMCMainTab mainPanel) {
		JButton markTreeButton = new JButton("Mark selection in graph");
		markTreeButton.setBackground(Color.WHITE);

		final JTable table = treePanel.getTable();
		markTreeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] rowindices = table.getSelectedRows();

				mainPanel.getGraphTool().clearGraphMarks();
				int treeSerieID = treePanel.getSeriesID();

				ArrayList<MCMCTree> uniqueTreeList = new java.util.ArrayList<MCMCTree>(treePanel.getTreeMap(treeSerieID).values());
				Collections.sort(uniqueTreeList);

				for(int i=0; i<rowindices.length; i++) {
					ArrayList<Integer> list = uniqueTreeList.get(rowindices[i]).getIndexList();

					mainPanel.getGraphTool().addMarksToGraph(list);
				}
			}
		});

		treePanel.addToSouth(markTreeButton);
		treePanel.addToSouth(Box.createRigidArea(new Dimension(10, 0)));
	}
	

	/** Definition: 			Returns the private variable .											  		
 	@return 				Graphical window.				
	 */
	public MCMCWindow getWindow() {return window;}

	/** Definition: 			Main function for VMCMC.										
	<p>Usage: 				Initialize the application from command line.						
 	<p>Function:			Gets the inputs from command line, parses them and calls the appropriate constructor of MCMCWindow. 				
 	<p>Classes:				Parameters, JCommander, JCommanderUserWrapper, Triple.
 	<p>Internal Functions: 	MCMCApplication(),  		
 	@return 				(A new graphical window)/(command line) statistical and/or convergence test analysis.					
	 */
	public static void main(String[] args) {
		if (args.length == 0)
			new MCMCApplication();
		else
		{
			Parameters params = new Parameters();
			JCommander vmcmc = new JCommander(params, args);

			if (params.help) 
			{
				StringBuilder sb = new StringBuilder(65536);
				sb.append("Usage: java vmcmc [options] ").append('\n');
				JCommanderUserWrapper.getUnsortedUsage(vmcmc, params, sb);
				System.out.println(sb.toString());
			}
			else if (args.length == 1)
				new MCMCApplication(args[0]);
			else if (params.filename == null)
				System.out.println("File Name not provided. Use -f for inputting filename or see -h for valid options.");
			else if ((params.nogui == false) && (params.test == false) && (params.stats == false) && (params.ess == false) && (params.geweke == false) && (params.gr == false))
				new MCMCApplication(params.filename);
			else 
			{
				Triple<String, Integer, Double> paramData = ParameterParser.Getoptions(params);

				if (params.nogui == true)
				{
					System.out.println("\n\n            ****** TEST STATISTICS OF THE PARAMETERS ******");
					new MCMCApplication(2, paramData.first, paramData.second, paramData.third);
					System.out.println("\n");

					System.out.println("\n\n            ****** SIMPLE STATISTICS OF THE PARAMETERS ******");
					new MCMCApplication(3, paramData.first, paramData.second, paramData.third);
					System.out.println("\n");
				}
				else if (params.test == true)
				{
					System.out.println("\n\n            ****** TEST STATISTICS OF THE PARAMETERS ******");
					new MCMCApplication(2, paramData.first, paramData.second, paramData.third);
					System.out.println("\n");
				}
				else if (params.stats == true)
				{
					System.out.println("\n\n            ****** SIMPLE STATISTICS OF THE PARAMETERS ******");
					new MCMCApplication(3, paramData.first, paramData.second, paramData.third);
					System.out.println("\n");
				}
				else if (params.geweke == true)
				{
					System.out.println("            ****** GEWEKE TEST BURN-IN INDICATOR ******");
					new MCMCApplication(4, paramData.first, paramData.second, paramData.third);
				}
				else if (params.ess == true)
				{
					System.out.println("            ****** ESTIMATED SAMPLE SIZE BURN-IN INDICATOR ******");
					new MCMCApplication(5, paramData.first, paramData.second, paramData.third);
				}
				else if (params.gr == true)
				{
					System.out.println("            ******* GELMAN RUBIN CONVERGENCE TEST *******");
					new MCMCApplication(6, paramData.first, paramData.second, paramData.third);
				}
			}
		}
	}
}
