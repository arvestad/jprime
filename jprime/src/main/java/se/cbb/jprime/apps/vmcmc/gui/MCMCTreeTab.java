package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.forester.archaeopteryx.AptxUtil;
import org.forester.archaeopteryx.Configuration;
import org.forester.archaeopteryx.Options;
import org.forester.archaeopteryx.TreeColorSet;
import org.forester.archaeopteryx.AptxUtil.GraphicsExportType;

import se.cbb.jprime.apps.vmcmc.libs.MCMCConsensusTree;
import se.cbb.jprime.apps.vmcmc.libs.MCMCFileReader;
import se.cbb.jprime.apps.vmcmc.libs.MCMCNewick;
import se.cbb.jprime.apps.vmcmc.libs.MCMCNewickUtilities;
import se.cbb.jprime.apps.vmcmc.libs.MCMCTree;
import se.cbb.jprime.apps.vmcmc.libs.MCMCTreeNode;

/**
 * MCMCTreeTab: Tab panel for the tree tab.
 */
public class MCMCTreeTab extends MCMCStandardTab {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private static final long 						serialVersionUID = 1L;
	private JTable									table;
	private DefaultTableModel 						model;
	private JScrollPane								paneTree;
	private JPanel									panelTree;
	private JLabel 									treelabel;
	private JComboBox								droplist;
	private JComboBox								imageDisplayType;;
	private JTextArea								newickTextArea;
	private JTextArea								newickTreeArea;
	private ArrayList<TreeMap<Integer, MCMCTree>> 	treeMapList;
	private int 									treeIndex;
	private int										imageType;
	private Image									image;
	private ImagePanel 								imagePanel;
	private JScrollPane 							newickImageScrollPane;

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCTreeTab() {
		super();
		/* ******************** FUNCTION VARIABLES ******************************** */		
		JScrollPane 					newickScrollPane;
		JScrollPane 					newickTreeScrollPane;
		JButton 						createConsensus;
		JScrollPane 					scrollPane;
		JPanel 							displaypanel;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		treeIndex 						= 0;
		imageType 						= 0;
		treeMapList 					= new ArrayList<TreeMap<Integer, MCMCTree>>();
		droplist 						= new JComboBox();
		imageDisplayType				= new JComboBox();
		panelTree 						= new JPanel();
		createConsensus 				= new JButton("Create Consensus Tree");
		paneTree 						= new JScrollPane(panelTree);
		model 							= new DefaultTableModel();
		displaypanel 					= new JPanel();
		newickTextArea 					= new JTextArea();
		newickScrollPane 				= new JScrollPane(newickTextArea);
		newickTreeArea 					= new JTextArea();
		image 							= new BufferedImage(10,10,BufferedImage.TYPE_4BYTE_ABGR);
		newickTreeScrollPane 			= new JScrollPane(newickTreeArea);
		imagePanel 						= new ImagePanel(image);
		newickImageScrollPane			= new JScrollPane(imagePanel);

		/* ******************** FUNCTION BODY ************************************* */
		table 							= new JTable(model) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int colIndex) {
		        return false;   //Disallow the editing of any cell
		    }
		};
		scrollPane 						= new JScrollPane(table);
		
		westpanel	.setMinimumSize		(new Dimension(300, 0));
		westpanel	.setPreferredSize	(new Dimension(300, 0));
		eastPanel	.setPreferredSize	(new Dimension(900, 0));
		eastPanel	.setMinimumSize		(new Dimension(900, 0));
		southPanel	.setMinimumSize		(new Dimension(0  , 50));
		southPanel	.setPreferredSize	(new Dimension(0  , 50));
		northPanel	.setPreferredSize	(new Dimension(0  , 100));
		
		centerPanel	.setEnabled			(false);
		centerPanel	.setLayout			(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		westpanel	.setLayout			(new BoxLayout(westpanel  , BoxLayout.Y_AXIS));
		southPanel	.setLayout			(new BoxLayout(southPanel , BoxLayout.X_AXIS));
		eastPanel	.setLayout			(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

		droplist	.setBackground		(Color.WHITE);
		imageDisplayType.setBackground	(Color.WHITE);
		panelTree	.setBackground		(Color.WHITE);
		createConsensus.setBackground	(Color.WHITE);
		
		createConsensus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				calulateConsensusTree();
			}
		});

		//Droplist actionlistener used to change tree serie selected in tree tab
		droplist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				seriesID 				= droplist.getSelectedIndex();
				treeIndex 				= 0;
				
				updateTable();
//				updateTreePanel();
			}
		});
		
		//Droplist actionlistener used to change tree display method selected in tree tab
		imageDisplayType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imageType 				= imageDisplayType.getSelectedIndex();
				
				int[] rowindices 		= table.getSelectedRows();
				String newick;
				ArrayList<MCMCTree> uniqueTreeList;
				
				newick = "";
				uniqueTreeList = new java.util.ArrayList<MCMCTree>(treeMapList.get(seriesID).values());
				Collections.sort(uniqueTreeList);
				
				for(int i=0; i<rowindices.length; i++) 
					newick += new String(uniqueTreeList.get(rowindices[i]).getData()) + ";\n";
				
				if(rowindices.length > 0)
					treeIndex = rowindices[0];
				else
					treeIndex = 0;
				
				if(imageType == 0) {
					centerPanel.setVisible(false);
					centerPanel.setEnabled(false);
					eastPanel.setEnabled(true);
					eastPanel.setVisible(true);
					image = null;
					updateGraphPanel(newick);
				} else {
					centerPanel.setEnabled(true);
					centerPanel.setVisible(true);
					eastPanel.setVisible(false);
					eastPanel.setEnabled(false);
					updateImage(newick);
					imagePanel = new ImagePanel(image);
					newickImageScrollPane.add(imagePanel);
					
				}
			}
		});

		table.getTableHeader().setBackground(new Color(0xFFDDDDFF));
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int[] rowindices 		= table.getSelectedRows();
				String newick;
				ArrayList<MCMCTree> uniqueTreeList;
				
				newick = "";
				uniqueTreeList = new java.util.ArrayList<MCMCTree>(treeMapList.get(seriesID).values());
				Collections.sort(uniqueTreeList);
				
				for(int i=0; i<rowindices.length; i++) 
					newick += new String(uniqueTreeList.get(rowindices[i]).getData()) + ";\n";
				
				if(rowindices.length > 0)
					treeIndex = rowindices[0];
				else
					treeIndex = 0;
				
				newickTextArea.setText(newick);		
				
				if(imageType == 0) {
					centerPanel.setVisible(false);
					centerPanel.setEnabled(false);
					eastPanel.setEnabled(true);
					eastPanel.setVisible(true);
					image = null;
					updateGraphPanel(newick);
				} else {
					updateImage(newick);
					newickImageScrollPane.add(imagePanel);
					centerPanel.setEnabled(true);
					centerPanel.setVisible(true);
					eastPanel.setVisible(false);
					eastPanel.setEnabled(false);
				}
			}
		});
		
		displaypanel.setBackground(new Color(0xFFDDDDFF));
		displaypanel.setBorder(BorderFactory.createLineBorder(new Color(0xFFAAAAFF)));
		displaypanel.setMaximumSize(new Dimension(950, 50));
		displaypanel.setPreferredSize(new Dimension(950, 50));

		newickTextArea	.setEditable					(false);
		newickScrollPane.setHorizontalScrollBarPolicy	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newickScrollPane.setVerticalScrollBarPolicy		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		newickTreeArea		.setEditable					(false);
		newickTreeArea		.setFont						(new Font("Menlo", Font.PLAIN, 12));
		newickTreeScrollPane.setHorizontalScrollBarPolicy	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newickTreeScrollPane.setVerticalScrollBarPolicy		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		newickImageScrollPane.setHorizontalScrollBarPolicy	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newickImageScrollPane.setVerticalScrollBarPolicy		(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		treelabel 						= new JLabel("Tree ?");
		treelabel.setFont(new Font("arial", Font.BOLD, 15));

		displaypanel.add(treelabel);
		
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(createConsensus);
		southPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		centerPanel.add(displaypanel);
		eastPanel.add(displaypanel);
		westpanel.add(scrollPane);
		centerPanel.add(newickImageScrollPane);
		northPanel.add(newickScrollPane);
		eastPanel.add(newickTreeScrollPane);
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	private DefaultMutableTreeNode MCMCTreeToJTree(MCMCTreeNode treeNode) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		DefaultMutableTreeNode 	node;
		final NumberFormat 		formatter 	= new DecimalFormat("0.00");
		int 					numBurninTrees;
		int 					numTrees;
		String 					branchString;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		numBurninTrees		= (int) (datacontainer.getNumTrees(seriesID)*burnin);
		numTrees 			= datacontainer.getNumTrees(seriesID)-numBurninTrees;
		branchString 		= String.valueOf(formatter.format(((double) treeNode.getNumDuplicates()/numTrees)*100) + " %");
		
		/* ******************** FUNCTION BODY ************************************* */
		if(treeNode.getNumChildren() > 0)
			node = new DefaultMutableTreeNode(branchString);
		else
			node = new DefaultMutableTreeNode(treeNode.getName());

		for(int i = 0; i <treeNode.getNumChildren(); i++) 
			node.add(MCMCTreeToJTree(treeNode.getChildren().get(i)));

		return node;
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void replaceJTree(JTree tree) {
		paneTree	.repaint	();
		panelTree	.removeAll	();
		panelTree	.add		(tree);
		panelTree	.revalidate	();
	}
	
	public void replaceImage() {
		newickImageScrollPane	.repaint	();
		newickImageScrollPane.removeAll();
		imagePanel.repaint();
		imagePanel = new ImagePanel(image);
		newickImageScrollPane.add(imagePanel);
		newickImageScrollPane	.revalidate	();
	}

	/**
	 * calculateConsensusTree: Uses library MCMCConsensusTree to find consensus tree for
	 * currently selected tree series with current burn in. Result is then displayed in
	 * the tabs JTree structure.
	 */
	public void calulateConsensusTree() {		
		/* ******************** FUNCTION VARIABLES ******************************** */
		ArrayList<MCMCTree> 	uniqueTreeList;
		int 					numBurninTrees;
		int 					numTrees;
		MCMCTreeNode 			root;
		DefaultTreeCellRenderer renderer;
		JLabel 					noConsensusLabel;
		JTree 					temp;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		uniqueTreeList 		= new java.util.ArrayList<MCMCTree>(treeMapList.get(seriesID).values());
		numBurninTrees 		= (int) (datacontainer.getNumTrees(seriesID)*burnin);
		numTrees 			= datacontainer.getNumTrees(seriesID)-numBurninTrees;
		root 				= MCMCConsensusTree.initializeConsensusTree(uniqueTreeList, numTrees);
	
		/* ******************** FUNCTION BODY ************************************* */
		if(root != null) {
			temp 			= new JTree(MCMCTreeToJTree(root));
			renderer 		= new DefaultTreeCellRenderer();

			for(int i = 0; i < temp.getRowCount(); i++)
				temp		.expandRow		(i);

			renderer		.setLeafIcon	(null);
			renderer		.setClosedIcon	(null);
			renderer		.setOpenIcon	(null);
			temp			.setCellRenderer(renderer);
			replaceJTree(temp);
			treelabel		.setText		("Consensus tree for serie " + datacontainer.getTreeNames().get(seriesID));
			newickTextArea	.setText		(MCMCNewick.getNewick(root) + "\n");
			if(imageType == 0)
				updateGraphPanel(MCMCNewick.getNewick(root));
			else
				updateImage(MCMCNewick.getNewick(root));
		} else { 
			noConsensusLabel 		= new JLabel("No consensus tree available");
			noConsensusLabel	.setFont	(new Font("arial", Font.BOLD, 24));

			paneTree	.repaint	();
			panelTree	.removeAll	();
			panelTree	.add		(noConsensusLabel);
			treelabel	.setText	("Consensus tree for serie " + datacontainer.getTreeNames().get(seriesID));
			panelTree	.revalidate	();
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	public void updateImage(String newick) {
		MCMCNewickUtilities 	newickTreeFigure;
		if(newick == null || newick == "")
			return;
		final Configuration config = new Configuration();
        config.putDisplayColors( TreeColorSet.BACKGROUND, new Color( 0, 0, 0 ) );
        config.putDisplayColors( TreeColorSet.BRANCH, new Color( 255, 255, 255 ) );
        config.putDisplayColors( TreeColorSet.TAXONOMY, new Color( 255, 255, 255 ) );
        config.putDisplayColors( TreeColorSet.BRANCH_LENGTH, new Color( 255, 255, 255 ) );
        config.setPhylogenyGraphicsType( Options.PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE );
        config.setBaseFontSize(20);
        config.setColorizeBranches(true);
        config.setShowBranchLengthValues(true);
        try {
        	newickTreeFigure 	= new MCMCNewickUtilities();
			newickTreeFigure.createAndWriteString("temp.txt", newick);
        	AptxUtil.writePhylogenyToGraphicsFile( new File( "./src/main/resources/vmcmc/Results/temp.txt" ), new File( "./src/main/resources/vmcmc/Results/my_tree_graphics.jpg" ), 1000, 400, GraphicsExportType.JPG, config );
			
        	image = new ImageIcon("./src/main/resources/vmcmc/Results/my_tree_graphics.jpg").getImage();
        	
//        	replaceImage();
        	treelabel	.setText		("Tree " + treeIndex);
	        newickTreeFigure.FileDeleter("temp.txt");
//        	newickTreeFigure.FileDeleter("my_tree_graphics.jpg");
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        	System.exit(-1);
        }
	}
	
	public void updateTreePanel() {
		/* ******************** FUNCTION VARIABLES ******************************** */
		MCMCTree 				tree;
		JTree 					temp;
		DefaultTreeCellRenderer renderer;
		MCMCTreeNode 			root;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		tree 			= datacontainer.getTreeSerie(seriesID).get(treeIndex);
		renderer 		= new DefaultTreeCellRenderer();
		root			= MCMCFileReader.treeArrayToTreeNode(tree.getData());
		tree		.setRoot		(root);
		temp 			= new JTree(MCMCTreeToJTree(tree.getRoot()));

		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < temp.getRowCount(); i++)
			temp	.expandRow		(i);

		renderer	.setLeafIcon	(null);
		renderer	.setClosedIcon	(null);
		renderer	.setOpenIcon	(null);
		temp		.setCellRenderer(renderer);
		replaceJTree(temp);
		treelabel	.setText		("Tree " + treeIndex);
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	public void updateGraphPanel(String newickDisplay) {
		MCMCNewickUtilities 	newickTreeFigure;
		String 					command;
		String 					newickFigure;
		if(newickDisplay == null || newickDisplay == "") {
			newickTreeArea	.setText	("");
			treelabel	.setText		("No Selected Tree");
			return;
		}
		try{
			newickTreeFigure 	= new MCMCNewickUtilities();
			newickTreeFigure.createAndWriteString("temp.txt", newickDisplay);
			newickTreeFigure.setParameter("temp.txt", "newickTree.txt");
			command 			= newickTreeFigure.makeCommand();
			newickTreeFigure.runCommand	(command);

			newickFigure 		= newickTreeFigure.stringReader("newickTree.txt");

			newickTreeFigure.FileDeleter("temp.txt");
			newickTreeFigure.FileDeleter("newickTree.txt");
			newickTreeFigure.FileDeleter("NewickUtilitiesScript.sh");

			newickTreeArea	.setText	(newickFigure);
			treelabel	.setText		("Tree " + treeIndex);
		} catch(Exception exc) {
			System.out.println("Newick File Writing failed. Error:" + exc.getMessage());
			System.exit(-1);
		}
	}

	public void updateTreeMaps() {
		/* ******************** FUNCTION VARIABLES ******************************** */
		TreeMap<Integer, MCMCTree> 	treeMap;
		ArrayList<MCMCTree> 		serie;
		int 						numPoints;	
		int 						numBurnInPoints;
		MCMCTree[] 					data;
		MCMCTree 					tree;
		int 						key;
		MCMCTree 					uniqueTree;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		
		/* ******************** FUNCTION BODY ************************************* */
		treeMapList.clear();
		
		for(int i=0; i<datacontainer.getNumTreeSeries(); i++) {
			treeMap = new TreeMap<Integer, MCMCTree>();
			treeMapList.add(treeMap);
			
			serie 					= datacontainer.getTreeSerie(i);
			numPoints 				= serie.size();	
			numBurnInPoints 		= (int)(numPoints*burnin);
			data 					= new MCMCTree[numPoints-numBurnInPoints];

			System.arraycopy(serie.toArray(), numBurnInPoints, data, 0, numPoints-numBurnInPoints);

			for(int j=0; j<data.length; j++) {
				tree 				= data[j];
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
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	public void updateTable() {
		/* ******************** FUNCTION VARIABLES ******************************** */
		final NumberFormat 			formatter = new DecimalFormat("0.00");
		TreeMap<Integer, MCMCTree> 	treeMap;
		ArrayList<MCMCTree> 		uniqueserie;
		MCMCTree 					uniqueTree;
		int 						numDuplicates;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		treeMap 				= treeMapList.get(seriesID);
		uniqueserie 			= new java.util.ArrayList<MCMCTree>(treeMap.values());
		model 					= new DefaultTableModel();
		
		/* ******************** FUNCTION BODY ************************************* */
		Collections.sort(uniqueserie);

		model.addColumn(datacontainer.getTreeNames().get(seriesID));
		model.addColumn("Duplicates n");
		model.addColumn("Tree Freq %");
		
		for(int i = 0; i < uniqueserie.size(); i++) {
			uniqueTree 	= uniqueserie.get(i);
			numDuplicates 		= uniqueTree.getNumDuplicates();
			Object[] rowdata 	= {"Tree " + i, numDuplicates, (formatter.format((double) numDuplicates/datacontainer.getNumLines()*100) + " %")};
			
			model.addRow(rowdata);
		}
		table.setModel(model);
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	public JComboBox getDropList() 								{return droplist;}
	public JComboBox getImageDisplayType() 						{return imageDisplayType;}
	public JTable getTable() 									{return table;}
	public TreeMap<Integer, MCMCTree> getTreeMap(int index) 	{return treeMapList.get(index);}

	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
