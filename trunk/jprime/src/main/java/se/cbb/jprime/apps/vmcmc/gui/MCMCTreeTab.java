package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

import se.cbb.jprime.apps.vmcmc.libs.MCMCConsensusTree;
import se.cbb.jprime.apps.vmcmc.libs.MCMCFileReader;
import se.cbb.jprime.apps.vmcmc.libs.MCMCNewick;
import se.cbb.jprime.apps.vmcmc.libs.MCMCTree;
import se.cbb.jprime.apps.vmcmc.libs.MCMCTreeNode;

/**
 * MCMCTreeTab: Tab panel for the tree tab.
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
public class MCMCTreeTab extends MCMCStandardTab {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTable table;
	JTree tree;
	private DefaultTableModel model;
	JScrollPane paneTree;
	JPanel panelTree;
	JLabel treelabel;
	JComboBox droplist;
	JTextArea newickTextArea;

	ArrayList<TreeMap<Integer, MCMCTree>> treeMapList;

	int treeIndex=0;

	public MCMCTreeTab() {
		super();

		treeMapList = new ArrayList<TreeMap<Integer, MCMCTree>>();

		westpanel.setMinimumSize(new Dimension(300, 0));
		westpanel.setPreferredSize(new Dimension(300, 0));
		southPanel.setMinimumSize(new Dimension(0, 50));
		southPanel.setPreferredSize(new Dimension(0, 50));

		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		westpanel.setLayout(new BoxLayout(westpanel, BoxLayout.Y_AXIS));

		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));

		northPanel.setPreferredSize(new Dimension(0, 100));

		droplist = new JComboBox();
		droplist.setBackground(Color.WHITE);

		panelTree = new JPanel();
		panelTree.setBackground(Color.WHITE);
		paneTree = new JScrollPane(panelTree);
		
		JButton createConsensus = new JButton("Create Consensus Tree");
		createConsensus.setBackground(Color.WHITE);

		createConsensus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				calulateConsensusTree();
			}
		});

		//Droplist actionlistener used to change tree serie selected in tree tab
		droplist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				seriesID = droplist.getSelectedIndex();
				treeIndex = 0;
				updateTable();
				updateTreePanel();
			}
		});

		model = new DefaultTableModel();
		table = new JTable(model);
		table.getTableHeader().setBackground(new Color(0xFFDDDDFF));

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int[] rowindices = table.getSelectedRows();
				String newick = "";

				ArrayList<MCMCTree> uniqueTreeList = new java.util.ArrayList<MCMCTree>(treeMapList.get(seriesID).values());
				Collections.sort(uniqueTreeList);
				
				for(int i=0; i<rowindices.length; i++) {
					newick += new String(uniqueTreeList.get(rowindices[i]).getData()) + ";\n";
				}
				
				if(rowindices.length > 0)
					treeIndex = rowindices[0];
				else
					treeIndex = 0;
				
				newickTextArea.setText(newick);
				
				updateTreePanel();
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane(table);

		JPanel displaypanel = new JPanel();
		displaypanel.setBackground(new Color(0xFFDDDDFF));
		displaypanel.setBorder(BorderFactory.createLineBorder(new Color(0xFFAAAAFF)));

		newickTextArea = new JTextArea();
		JScrollPane newickScrollPane = new JScrollPane(newickTextArea);
		newickScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newickScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		treelabel = new JLabel("Tree ?");
		treelabel.setFont(new Font("arial", Font.BOLD, 15));

		displaypanel.add(treelabel);
		
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(createConsensus);
		southPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		centerPanel.add(displaypanel);
		westpanel.add(scrollPane);
		centerPanel.add(paneTree);
		northPanel.add(newickScrollPane);
	}
	
	public void replaceJTree(JTree tree) {
		paneTree.repaint();
		panelTree.removeAll();
		panelTree.add(tree);
		panelTree.revalidate();
	}

	/*
	 * calculateConsensusTree: Uses library MCMCConsensusTree to find consensus tree for
	 * currently selected tree series with current burn in. Result is then displayed in
	 * the tabs JTree structure.
	 */
	public void calulateConsensusTree() {		
		ArrayList<MCMCTree> uniqueTreeList = new java.util.ArrayList<MCMCTree>(treeMapList.get(seriesID).values());
		
		int numBurninTrees = (int) (datacontainer.getNumTrees(seriesID)*burnin);
		int numTrees = datacontainer.getNumTrees(seriesID)-numBurninTrees;
		MCMCTreeNode root = MCMCConsensusTree.initializeConsensusTree(uniqueTreeList, numTrees);

		if(root != null) {
			JTree temp = new JTree(MCMCTreeToJTree(root));

			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

			for(int i = 0; i < temp.getRowCount(); i++)
				temp.expandRow(i);

			renderer.setLeafIcon(null);
			renderer.setClosedIcon(null);
			renderer.setOpenIcon(null);
			temp.setCellRenderer(renderer);
			replaceJTree(temp);
			treelabel.setText("Consensus tree for serie " + datacontainer.getTreeNames().get(seriesID));
		
			newickTextArea.setText(MCMCNewick.getNewick(root) + "\n");
		}
		else{ 
			JLabel noConsensusLabel = new JLabel("No consensus tree available");
			noConsensusLabel.setFont(new Font("arial", Font.BOLD, 24));

			paneTree.repaint();
			panelTree.removeAll();

			panelTree.add(noConsensusLabel);
			treelabel.setText("Consensus tree for serie " + datacontainer.getTreeNames().get(seriesID));
		
			panelTree.revalidate();
		}
	}
	
	public void updateTreePanel() {
		MCMCTree tree = datacontainer.getTreeSerie(seriesID).get(treeIndex);
		
		tree.setRoot(MCMCFileReader.treeArrayToTreeNode(tree.getData()));
		JTree temp = new JTree(MCMCTreeToJTree(tree.getRoot()));

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		for(int i = 0; i < temp.getRowCount(); i++)
			temp.expandRow(i);

		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		temp.setCellRenderer(renderer);

		replaceJTree(temp);

		treelabel.setText("Tree " + treeIndex);
	}

	public void updateTreeMaps() {
		treeMapList.clear();
		
		for(int i=0; i<datacontainer.getNumTreeSeries(); i++) {
			TreeMap<Integer, MCMCTree> treeMap = new TreeMap<Integer, MCMCTree>();
			treeMapList.add(treeMap);
			
			ArrayList<MCMCTree> serie = datacontainer.getTreeSerie(i);

			int numPoints = serie.size();	
			final int numBurnInPoints = (int)(numPoints*burnin);

			MCMCTree[] data = new MCMCTree[numPoints-numBurnInPoints];
			System.arraycopy(serie.toArray(), numBurnInPoints, data, 0, numPoints-numBurnInPoints);

			for(int j=0; j<data.length; j++) {
				MCMCTree tree = data[j];
				int key = tree.getKey();

				MCMCTree uniqueTree = treeMap.get(key);

				if(uniqueTree == null) {
					uniqueTree = new MCMCTree();

					uniqueTree.addIndex(tree.getIndexList().get(0));
					uniqueTree.addDuplicate();
					uniqueTree.setData(tree.getData());
					treeMap.put(key, uniqueTree);
				}
				else {
					uniqueTree.addIndex(tree.getIndexList().get(0));
					uniqueTree.addDuplicate();
				}
			}
		}
	}

	public void updateTable() {
		final NumberFormat formatter = new DecimalFormat("0.00");
		TreeMap<Integer, MCMCTree> treeMap = treeMapList.get(seriesID);
		
		ArrayList<MCMCTree> uniqueserie = new java.util.ArrayList<MCMCTree>(treeMap.values());
		Collections.sort(uniqueserie);

		model = new DefaultTableModel();

		model.addColumn(datacontainer.getTreeNames().get(seriesID));
		model.addColumn("Duplicates n");
		model.addColumn("");
		model.addColumn("Newick");
		
		for(int i=0; i<uniqueserie.size(); i++) {
			MCMCTree uniqueTree = uniqueserie.get(i);
			int numDuplicates = uniqueTree.getNumDuplicates();

			Object[] rowdata = {"Tree " + i, numDuplicates, (formatter.format((double) numDuplicates/datacontainer.getNumLines()*100) + " %"), uniqueTree.getData()};
			
			model.addRow(rowdata);
		}
		
		table.setModel(model);
	}

	private DefaultMutableTreeNode MCMCTreeToJTree(MCMCTreeNode treeNode) {
		DefaultMutableTreeNode node;
		final NumberFormat formatter = new DecimalFormat("0.00");

		int numBurninTrees = (int) (datacontainer.getNumTrees(seriesID)*burnin);
		int numTrees = datacontainer.getNumTrees(seriesID)-numBurninTrees;

		String branchString = String.valueOf(formatter.format(((double) treeNode.getNumDuplicates()/numTrees)*100) + " %");

		if(treeNode.getNumChildren() > 0)
			node = new DefaultMutableTreeNode(branchString);
		else
			node = new DefaultMutableTreeNode(treeNode.getName());

		for(int i = 0; i <treeNode.getNumChildren(); i++) {
			node.add(MCMCTreeToJTree(treeNode.getChildren().get(i)));
		}

		return node;
	}
	
	public void addTableColumn(String name, ArrayList<?> data) {
		model.addColumn(name , data.toArray());
	}

	public JComboBox getDropList() {return droplist;}
	public JTable getTable() {return table;}
	public TreeMap<Integer, MCMCTree> getTreeMap(int index) {return treeMapList.get(index);}
}
