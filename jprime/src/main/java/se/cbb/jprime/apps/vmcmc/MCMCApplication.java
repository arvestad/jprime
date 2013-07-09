package se.cbb.jprime.apps.vmcmc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

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

import se.cbb.jprime.apps.vmcmc.libs.*;
import se.cbb.jprime.apps.vmcmc.gui.*;

/**																							
 * 	The main class for VMCMC. It is responsible for efficiently co-ordinating calls between various GUI classes, Data handling classes, User requirements and MCMC statistics computation and convergence test classes. 
 *	The main method is to get the filename and the type of results a user wants from this program and to use the function calls from various other implemented classes to generate the required graphics and/or results.
 * 
 *   <p>This file is part of the bachelor thesis "Verktyg för visualisering av MCMC-data" - VMCMC
 *   Royal Institute of Technology, Sweden. (M.Bark, J. Miró)
 *  <p>This file is part of PhD project work for Royal Institute of Technology. (R. H. Ali)
 *
 *	@author Mikael Bark, J. Miró and Raja Hashim Ali
 *	@param filename, burnin, confidence_level.
 *	@return Statistical and convergence analysis of MCMC output from CODA, JPRiME and PRiME.
 *	@Usage java MCMCApplication [-h] [-f FILENAME] [[-b burnin] [-c confidencelevel] [-n] [-s] [-t] [-e] [-r] [-g]]
 */
public class MCMCApplication {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private MCMCWindow window;
	
	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	/** Definition: 			Default constructor for VMCMC.										
	 *	<p>Usage: 				When no filename is provided as input parameter.						
 	 *	<p>Function:			Opens up the first window that is used to supply input file. 				
 	 *	<p>Classes: 			MCMCWindow.
 	 *	<p>Internal Functions:	createMenuBar(). 		
 	 *	@return: 				A new graphical basic window by invoking MCMCWindow.					
	 */
	public MCMCApplication() {
		/* ******************** FUNCTION VARIABLES ******************************** */
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		window 				= new MCMCWindow();
		
		/* ******************** FUNCTION BODY ************************************* */
		window.setTitle("VMCMC Application");	
		window.setJMenuBar(createMenuBar());	
		window.validate();

		UIManager.put("TabbedPane.selected"			, new Color(0xFFEEEEFF));	
		UIManager.put("TabbedPane.contentAreaColor"	, new Color(0xFFEEEEFF));
		UIManager.put("TabbedPane.shadow"			, new Color(0xFF000000));
		
		/* ******************** END OF FUNCTION *********************************** */				
	}


	/** Definition: 			Constructor with filename for VMCMC.										
	<p>Usage: 				When a filename is provided as input parameter.						
 	<p>Function:			Opens up the second window that displays all graphs and statistical analysis. 
 	<p>Classes: 		MCMCWindow.		
	<p>Internal Functions:	createDirectMenuBar(File). 		
 	@return: 				A new graphical basic window by invoking MCMCWindow that has all statistics and convergence tests for the MCMC file performed.					
	 */
	public MCMCApplication(String file1) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		File 	file;
		int 	size;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		file 			= new File(file1);
		size 			= 540;
		window 			= new MCMCWindow(size);
		
		/* ******************** FUNCTION BODY ************************************* */
		window.setTitle("VMCMC Application");
		window.setJMenuBar(createDirectMenuBar(file));	//Window will contain all the information provided in the file.
		window.validate();
		window.windowAppear();

		UIManager.put("TabbedPane.selected"			, new Color(0xFFEEEEFF));	//Setting color for selected tabs
		UIManager.put("TabbedPane.contentAreaColor"	, new Color(0xFFEEEEFF));
		UIManager.put("TabbedPane.shadow"			, new Color(0xFF000000));
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Constructor with filename, custom burnin and/or confidence level for VMCMC.										
	<p>Usage: 				When filename and custom burnin/confidence level/both are provided as input parameters.						
 	<p>Function:			Opens up the second window that displays all graphs and statistical analysis.  				
 	<p>Classes: 			MCMCWindow, MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None. 				
 	@return: 				A new graphical basic window by invoking MCMCWindow.					
	 */
	public MCMCApplication(int choice, String file1, int burnin, double confidencelevel)  throws Exception {
		/* ******************** FUNCTION VARIABLES ******************************** */
		File file;
		int numSeries;
		int serieLength;
		int numValues;
		int ess;
		int geweke;
		int equalStart; 
		int equalEnd;
		int tempHolder;
		int intervalLength;
		int startPos; 
		int leftIndex;
		int rightIndex;
		int numTreeSeries;
		double values;
		double values1;
		double mean;
		double sigmaSquare;
		double arithmetic_standard_dev;
		double power;
		double geometric_mean;
		double sum;
		double sum1;
		double geometric_standard_dev;
		double harmonic_mean;
		double max_value;
		double min_value;
		double comp;
		double nearest;
		double startNum;
		double tempHolder1;
		double tempHolder2;
		MCMCDataContainer datacontainer;
		ArrayList<String> numSeriesArray;
		boolean gelmanRubin;
		Double[] data;
		Object[] serie;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		file 						= new File(file1);
		datacontainer 				= null;
		
		/* ******************** FUNCTION BODY ************************************* */
		if (confidencelevel<0)
			confidencelevel = 0;
		else if(confidencelevel>100)
			confidencelevel = 100;

		if(file != null) {
			datacontainer = MCMCFileReader.readMCMCFile(file);	

			if(datacontainer != null) {
				if(choice < 8) {
					numSeries = datacontainer.getNumValueSeries();

					if(numSeries != 0) {
						numSeriesArray = datacontainer.getValueNames();

						for (int i = 0; i < numSeries; i++) {
							serie = datacontainer.getValueSerie(i).toArray();
							serieLength	= serie.length;

							if(serieLength < 100){
								System.out.println("\t\tSmall dataset. Stistics and tests can not be computed.\n\t}\n}");
								System.exit(0);
							}

							System.out.println("\t\t\"Name\": "+ numSeriesArray.get(i) + "{");

							if(choice == 5) {
								ess 				= MCMCMath.calculateESS(serie);
								if (ess > serieLength/800) {
									System.out.println("\t\t\t\"ESS\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + ess + "\n\t\t\t]");
								} else
									System.out.println("\t\t\t\"ESS\": \"Insignificant. Not converged\"");
							} else if(choice == 4) {
								geweke 				= MCMCMath.calculateGeweke(serie);
								if (geweke != -1) {
									System.out.println("\t\t\t\"Geweke\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + geweke + "\n\t\t\t]");							
								} else 
									System.out.println("\t\t\t\"Geweke\": \"Not converged\"");
							} else if(choice == 6) {
								gelmanRubin 		= MCMCMath.GelmanRubinTest(serie, burnin);
								if(gelmanRubin == true)	{
									System.out.println("\t\t\t\"Gelman-Rubin\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + burnin + "\n\t\t\t]");
								} else {
									System.out.println("\t\t\t\"Gelman-Rubin\": [\n\t\t\t\t\"Status\": \"Not Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + burnin + "\n\t\t\t]");
								}
							} 

							if(choice == 2 || choice == 7) {
								geweke = MCMCMath.calculateGeweke(serie);
								ess = MCMCMath.calculateESS(serie);

								if (ess > serieLength/800) {
									System.out.println("\t\t\t\"ESS\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + ess + "\n\t\t\t]");
								} else
									System.out.println("\t\t\t\"ESS\": \"Insignificant. Not converged\"");

								if (geweke != -1) {
									System.out.println("\t\t\t\"Geweke\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + geweke + "\n\t\t\t]");							
								} else 
									System.out.println("\t\t\t\"Geweke\": \"Not converged\"");

								int originalBurnin = 0;
								gelmanRubin	= MCMCMath.GelmanRubinTest(serie, originalBurnin);
								while(gelmanRubin != true && originalBurnin < (0.5 * serie.length)) {
									originalBurnin = originalBurnin + 1;
									gelmanRubin	= MCMCMath.GelmanRubinTest(serie, originalBurnin);
								}
								if(gelmanRubin == true)	{
									System.out.println("\t\t\t\"Gelman-Rubin\": [\n\t\t\t\t\"Status\": \"Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + originalBurnin + "\n\t\t\t]");
								} else {
									System.out.println("\t\t\t\"Gelman-Rubin\": [\n\t\t\t\t\"Status\": \" Not Converged\"");
									System.out.println("\t\t\t\t\"Burn-in\": " + originalBurnin + "\n\t\t\t]");
								}
							}

							if(serie.length - burnin > 0 && (choice == 3 || choice == 7)) {
								data = new Double[serie.length-burnin];
								System.arraycopy(serie, burnin, data, 0, serie.length-burnin);

								numValues = data.length;
								values = 0;
								values1	= 1;
								sigmaSquare = 0;
								power = (double) 1/numValues; 
								sum = 0;
								sum1 = 0;
								max_value = data[0];
								min_value = data[0];

								for(int k = 0; k < numValues; k++) {
									values += (Double)data[k];
									values1	*= java.lang.Math.pow((Double)data[k],power);
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
									sigmaSquare += java.lang.Math.pow((Double)data[k] - mean,2);

								arithmetic_standard_dev = (double)java.lang.Math.sqrt(sigmaSquare/(numValues-1));
								geometric_standard_dev = Math.abs(Math.exp(Math.sqrt(sum/(numValues-1) - ((numValues/(numValues-1))*Math.pow(Math.log(values1),2)))));

								System.out.println("\t\t\t\"Arithmetic Mean\": " + mean);
								System.out.println("\t\t\t\"Arithmetic Standard Deviation\": " + arithmetic_standard_dev);
								System.out.println("\t\t\t\"Geometric Mean\": " + geometric_mean);
								System.out.println("\t\t\t\"Geometric Standard Deviation\": " + geometric_standard_dev);
								System.out.println("\t\t\t\"Harmonic Mean\": " + harmonic_mean);
								System.out.println("\t\t\t\"Minimum Value\": " + min_value);
								System.out.println("\t\t\t\"Maximum Value\": " + max_value);

								Arrays.sort(data);
								nearest	=(Double)data[0];
								equalStart = 0;
								equalEnd = 0;

								for(int k = 0; k < numValues-1; k++) {
									comp = Math.abs(mean-nearest) - Math.abs(mean-(Double)data[k+1]);
									if (comp > 0) {
										nearest = (Double)data[k+1]; 
										equalStart = k+1;
									} else if (comp == 0) 
										equalEnd = k+1;
								}

								if(equalEnd == 0)
									tempHolder = equalStart;
								else
									tempHolder = equalStart + (equalEnd - equalStart)/2;

								double[] start = {nearest, tempHolder};
								intervalLength = (int) ((double)(numValues)*(confidencelevel/100));
								startPos = (int)start[1]; 
								startNum = start[0];
								leftIndex = startPos-1;
								rightIndex = startPos+1;

								System.out.println("\t\t\t\"Confidence Level\": " + confidencelevel + "%");

								if(numValues == 0) {
									tempHolder1 = Double.NaN;
									tempHolder2 = Double.NaN;
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("\t\t\t\"Bayesian Confidence\": " + result[0] + " ; " + result[1]);
								} else if(numValues == 1) {
									tempHolder1 = (Double) data[0];
									tempHolder2 = (Double) data[0];
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("\t\t\t\"Bayesian Confidence\": " + result[0] + " ; " + result[1]);
								} else if(numValues == 2) {
									tempHolder1 = (Double) data[0];
									tempHolder2 = (Double) data[1];
									double[] result = {tempHolder1, tempHolder2};
									System.out.println("\t\t\t\"Bayesian Confidence\":  " + result[0] + " ; " + result[1]);
								} else {
									for(int k = 0 ; k < intervalLength ; k++) {
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
									System.out.println("\t\t\t\"Bayesian Confidence\": " + result[0] + " ; " + result[1]);
								}
							}
							System.out.print("\t\t}");
							if(i<numSeries-1)
								System.out.println(",");
						}
					} 
				} else {
					numTreeSeries = datacontainer.getNumTreeSeries();
					if(numTreeSeries < 1)
						System.out.println("No tree data found");
					else {
						final NumberFormat 			formatter = new DecimalFormat("0.00");
						TreeMap<Integer, MCMCTree> 	treeMap;
						ArrayList<MCMCTree> 		uniqueserie;
						MCMCTree 					uniqueTree;
						int 						numDuplicates;
						ArrayList<TreeMap<Integer, MCMCTree>> 	treeMapList;
						ArrayList<MCMCTree> 		treeSerie;
						int 						numPoints;	
						int 						numBurnInPoints;
						MCMCTree[] 					treeData;
						MCMCTree 					tree;
						int 						key;
						
						treeMapList 			= new ArrayList<TreeMap<Integer, MCMCTree>>();
													
						for(int i=0; i<datacontainer.getNumTreeSeries(); i++) {
							treeMap = new TreeMap<Integer, MCMCTree>();
							treeMapList.add(treeMap);
							
							treeSerie 				= datacontainer.getTreeSerie(i);
							numPoints 				= treeSerie.size();	
							numBurnInPoints 		= burnin;
							treeData 				= new MCMCTree[numPoints-numBurnInPoints];

							System.arraycopy(treeSerie.toArray(), numBurnInPoints, treeData, 0, numPoints-numBurnInPoints);

							for(int j = 0; j < treeData.length; j++) {
								tree 				= treeData[j];
								key 				= tree.getKey();
								uniqueTree 			= treeMap.get(key);

								if(uniqueTree == null) {
									uniqueTree 		= new MCMCTree();

									uniqueTree	.addIndex		(tree.getIndexList().get(0));
									uniqueTree	.addDuplicate	();
									uniqueTree	.setData		(tree.getData());
									treeMap		.put			(key, uniqueTree);
								} else {
									uniqueTree	.addIndex		(tree.getIndexList().get(0));
									uniqueTree	.addDuplicate	();
								}
							}
							treeMap 				= treeMapList.get(i);
							uniqueserie 			= new java.util.ArrayList<MCMCTree>(treeMap.values());
							
							Collections.sort(uniqueserie);
							
							System.out.println("\t\t\"Series\": " + i + " [");
							for(int j = 0; j < uniqueserie.size(); j++) {
								System.out.println("\t\t\t{");
								uniqueTree 	= uniqueserie.get(j);
								String newick = new String(uniqueTree.getData()) + ";\n";
								numDuplicates 		= uniqueTree.getNumDuplicates();
								System.out.print("\t\t\t\t\"Index\": " + j + "\n\t\t\t\t\"Duplicates\": " + numDuplicates + "\n\t\t\t\t\"Posterior Distribution\": " + (formatter.format((double) numDuplicates/datacontainer.getNumLines()*100) + " %") + "\n\t\t\t\t\"Newick\": " + newick + "\t\t\t}");
								if(j < uniqueserie.size()-1)
									System.out.println(",");
							}
							System.out.println("\n\t\t]");
						}
					}
				}
			}
		}		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	/** Definition: 			Private method for opening the first graphical window for VMCMC.										
	<p>Usage: 				When no filename is provided as input parameter, the default constructor with no file input refers to this function for coming up with the first interface.		
 	<p>Function:			Populates the first window and all its other options like about and exit etc. that are displayed in the first window. 	
 	<p>Classes: 			MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None.		
 	@return: 				A populated graphical basic window and handles all options used inside it.					
	 */
	private JMenuBar createMenuBar() {
		/* ******************** FUNCTION VARIABLES ******************************** */
		JMenuBar 		menubar;
		JMenu 			menuFile;
		JMenuItem 		itemOpen;
		JMenuItem 		itemClose;
		JMenu 			menuAbout;  
		JMenuItem 		itemAbout;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		menubar 		=  new JMenuBar();
		menuFile 		= new JMenu("File");
		itemOpen 		= new JMenuItem("Open file");
		itemClose 		= new JMenuItem("Exit");

		menuAbout 		= new JMenu("About");  
		itemAbout 		= new JMenuItem("About Visual MCMC");
		
		/* ******************** FUNCTION BODY ************************************* */
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
						if(file.isDirectory())	//Show folders inside chooser
							return true;

						String extension = null;
						String name = file.getName();
						int i = name.lastIndexOf('.');

						if(i > 0 &&  i < name.length() - 1) 
							extension 		= name.substring(i+1).toLowerCase();	//Extract file extension

						if(extension != null) {
							if (extension.equals("mcmc")||extension.equals("txt") ) 	//Show files with extension mcmc
								return true;
							else 
								return false;
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
				aboutPanel.add(new JLabel("VISUAL MCMC Ver 1.1\n"));
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
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Private method for opening the second graphical window directly for VMCMC.										
	<p>Usage: 				When a filename is provided as input parameter, the second constructor with filename refers to this function for coming up with the second interface.		
 	<p>Function:			Populates the second window with all its other options and all the statitical computations and convergence tests, that are displayed in the second window. 	
 	<p>Classes: 			MCMCDataContainer, MCMCFileReader, MCMCMath.		
	<p>Internal Functions:	None.		
 	@return: 				A populated graphical extended window that displays parameter values, sattistics, tests and trees found in the input file.					
	 */
	private JMenuBar createDirectMenuBar(final File file) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		JMenuBar 			menubar;
		JMenu 				menuFile;
		JMenuItem 			itemClose;
		JMenu 				menuAbout;  
		JMenuItem 			itemAbout;
		MCMCDataContainer 	datacontainer;
		JTabbedPane 		tabs;
		MCMCMainTab 		mainPanel;
		MCMCTableTab 		tablePanel;
		MCMCTreeTab 		treePanel;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		menubar 			= new JMenuBar();
		menuFile 			= new JMenu("File");
		itemClose 			= new JMenuItem("Exit");
		menuAbout 			= new JMenu("About");  
		itemAbout 			= new JMenuItem("About Visual MCMC");
		mainPanel			= null;
		tablePanel			= null;
		treePanel			= null;
		datacontainer 		= null;
		
		/* ******************** FUNCTION BODY ************************************* */
		//Menu actionlistener responsible for closing application.
		itemClose.addActionListener(new ActionListener() {   
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		if(file != null) {
			try {
				if(!file.isFile()) {
					if (file.isDirectory()) {
						System.out.println("This is a directory. Please give full file path with file name.");
						System.exit(-1);
					} else {
						System.out.println("Filename or file path incorrect. Also check help for suggestions.");
						System.exit(-2);
					}
				}
				datacontainer = MCMCFileReader.readMCMCFile(file);	
			} catch (IOException e) {
				e.printStackTrace();
			}

			tabs = new JTabbedPane();
			tabs.setBackground(new Color(0xFFDDDDFF));
			tabs.setFocusable(false);
			tabs.setBorder(BorderFactory.createLineBorder(new Color(0xFFEEEEFF), 2));

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
			window.addTab(datacontainer.getFileName(), tabs);	//Name the tab after file
			window.selectTab(window.getTabs().getTabCount()-1);	//Switch to most recent tab
		}

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
		menuFile.add(itemClose);
		menubar.add(menuFile);
		menuAbout.add(itemAbout);
		menubar.add(menuAbout);
		return menubar;
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** Definition: 			Adds the applications default listeners for the tabs associated with current file.										
 	<p>Function:			 Update Tree Maps and tabel in the tree panel based on the Event Change.								
	 */
	private void linkTabsToTrees(final JTabbedPane tabs, final MCMCTreeTab treePanel) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		
		/* ******************** FUNCTION BODY ************************************* */
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(tabs.getSelectedIndex() == 2) {
					treePanel.updateTreeMaps();
					treePanel.updateTable();
				}
			}
		});
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** Definition: 			Creates and updates new instance of MCMCMainTab and adds default components. Sets application specific default values (including burnin - 10%).						
 	<p>Function:			Handle MCMCMainTab and set default values for it. Displays all the statistics and convergence test results for a parameter.				
 	<p>Classes:				MCMCMainTab, MCMCDataContainer, MCMCGraphToolPanel, 												
	 */
	private MCMCMainTab createMainPanel(final MCMCDataContainer datacontainer) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		final MCMCMainTab 		mainPanel 	= new MCMCMainTab();
		MCMCGraphToolPanel 		graphtoolPanel;
		JComboBox 				droplist;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		graphtoolPanel = mainPanel.getGraphTool();
		droplist = mainPanel.getDropList();
		
		/* ******************** FUNCTION BODY ************************************* */
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
				JSlider source 	= (JSlider) arg0.getSource();
				double burnin 	= (double) source.getValue()/source.getMaximum();

				mainPanel.setBurnIn(burnin);
				mainPanel.updateDisplayPanels();
			}
		});

		//Burn in field listener - updates burnin for main panel based on text field value. 
		mainPanel.getBurnInField().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int serieSize 	= datacontainer.getValueSerie(mainPanel.getSeriesID()).size();
				int fieldValue 	= 0;

				try {
					fieldValue 	= Integer.valueOf(mainPanel.getBurnInField().getText());
				} catch (NumberFormatException e) {}

				double burnin 	= (double) fieldValue/serieSize;

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

		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Adds functionalty between Main tab and	the JTabbedPane.									
	<p>Usage: 				When data in an interval is to be extracted and examined.						
 	<p>Function:			Extract the interval from the graph and display in a new pane. 				
 	<p>Classes:				MCMCMainTab.  		
	 */
	private void linkMainToTabs(final MCMCMainTab mainPanel, final JTabbedPane tabs) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		JButton 				extractSelectionButton;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		extractSelectionButton 	= new JButton("Extract interval");
		
		/* ******************** FUNCTION BODY ************************************* */
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
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Adds functionalty between Main tab and the Table tab.								
	<p>Usage: 				Select a parameter to see its data.						
 	<p>Function:			For a selected parameter in the main tab, display its corresponding data in table panel. 				
 	<p>Classes:				MCMCMainTab, MCMCTableTab.  													
	 */
	private void linkMainToTable(final MCMCMainTab mainPanel, final MCMCTableTab tablePanel) {
		/* ******************** FUNCTION BODY ************************************* */
		mainPanel.getDropList().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int seriesID = mainPanel.getDropList().getSelectedIndex();

				if(tablePanel != null) {
					tablePanel.setSelectedButton(seriesID);
				}
			}
		});
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 		Adds functionality between Main tab and the Trees tab.										
	<p>Usage: 				Make the main panel and tree panel uniform.						
 	<p>Function:			Handle and mantain the uniformity of the burnin selection between tree panel and main panel. 				
 	<p>Classes:				MCMCMainTab, MCMCTreeTab		
	 */
	private void linkMainToTrees(final MCMCMainTab mainPanel, final MCMCTreeTab treePanel) {
		/* ******************** FUNCTION BODY ************************************* */
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
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Creates and updates new instance of MCMCTableTab and adds default components.										
	<p>Usage: 				Display the data in a tabular format. 						
 	<p>Function:			Adds the parameter names and parameter values to the Table Tab (Second Tab) 				
 	<p>Classes:				MCMCTabletab, MCMCDataContainer.
	 */
	private MCMCTableTab createTablePanel(final MCMCDataContainer datacontainer) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		final MCMCTableTab 		tablePanel 	= new MCMCTableTab();
		ArrayList<String> 		names;
		int 					numValues;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		names 								= datacontainer.getValueNames();
		numValues 							= datacontainer.getNumValueSeries();
		
		/* ******************** FUNCTION BODY ************************************* */
		tablePanel.setDataContainer(datacontainer);
		for(int i=0; i<numValues; i++) 
			tablePanel.addColumn(names.get(i), datacontainer.getValueSerie(i));
		return tablePanel;
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Adds functionality between Table tab and the Main tab.						
 	<p>Function:			Create the left table tab on the main window for selecting the parameter.				
 	<p>Classes:				MCMCTableTab, MCMCMainTab, MCMCDataContainer.  													
	 */
	private void linkTableToMain(final MCMCTableTab tablePanel, final MCMCMainTab mainPanel) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		MCMCDataContainer 		datacontainer;
		int 					numValues;
		JRadioButton[] 			buttons;
		JPanel 					buttonPanel;
		ButtonGroup 			buttonGroup;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		datacontainer 			= tablePanel.getDataContainer();
		numValues 				= datacontainer.getNumValueSeries();
		buttons 				= new JRadioButton[numValues];
		buttonPanel 			= new JPanel();
		buttonGroup 			= new ButtonGroup();
		
		/* ******************** FUNCTION BODY ************************************* */
		tablePanel.setButtons(buttons);
		buttonPanel.setLayout(new GridLayout(1, 0));
		buttonPanel.setBackground(new Color(0xFFEEEEFF));

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
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Creates and updates new instance of MCMCMTreeTab and adds default components.
	<p>Usage: 				Used when tree data is found in the input file.						
 	<p>Function:			Build up teh Tree Panel using all the data from the file provided. 				
 	<p>Classes:				MCMCTreeTab, MCMCDataContainer. 												
	 */
	private MCMCTreeTab createTreePanel(MCMCDataContainer datacontainer) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		final MCMCTreeTab treePanel = new MCMCTreeTab();
		JPanel 						droplistPanel;
		JPanel						imageTypePanel;
		ArrayList<String> 			imageTypes;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		droplistPanel 				= new JPanel();
		imageTypePanel				= new JPanel();
		imageTypes 					= new ArrayList<String>();
		
		/* ******************** FUNCTION BODY ************************************* */
		treePanel.setDataContainer(datacontainer);
		treePanel.setSeriesID(0);
		treePanel.setBurnIn(0.1);

		treePanel.updateTreeMaps();
		treePanel.updateTable();
		treePanel.updateTreePanel();
		
		imageTypes.add("Simple Text");
		imageTypes.add("Graphical");

		treePanel.getDropList().setModel(new DefaultComboBoxModel(datacontainer.getTreeNames().toArray()));
		treePanel.getImageDisplayType().setModel(new DefaultComboBoxModel(imageTypes.toArray()));

		
		droplistPanel.setOpaque(false);
		droplistPanel.add(treePanel.getDropList());

		treePanel.addToSouth(droplistPanel);
		treePanel.addToSouth(Box.createRigidArea(new Dimension(10, 0)));
		
		imageTypePanel.setOpaque(false);
		imageTypePanel.add(treePanel.getImageDisplayType());

		treePanel.addToSouth(imageTypePanel);
		treePanel.addToSouth(Box.createRigidArea(new Dimension(10, 0)));

		return treePanel;
		
		/* ******************** END OF FUNCTION *********************************** */
	}


	/** Definition: 			Adds functionalty between Tree tab and the Main Tab.										
	<p>Usage: 										
 	<p>Function:			 				
 	<p>Classes:				MCMCTreeTab, MCMCMainTab.
	 */
	private void linkeTreesToMain(final MCMCTreeTab treePanel, final MCMCMainTab mainPanel) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		JButton 		markTreeButton;
		final JTable 	table 	= treePanel.getTable();
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		markTreeButton 			= new JButton("Mark selection in graph");
		
		/* ******************** FUNCTION BODY ************************************* */
		markTreeButton.setBackground(Color.WHITE);

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

		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */	
	/** Definition: 			Returns the private variable .											  		
 	@return 				Graphical window.				
	 */
	public MCMCWindow getWindow() {return window;}

	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
