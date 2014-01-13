package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;

/**
 * MCMCTree: Class containing information specific for MCMC trees. Contains one MCMCTreeNode
 * as root which specifies tree structure.
 */
public class MCMCTree implements Comparable<MCMCTree> {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	private byte[] 				data;
	private int 				key;
	private int 				numDuplicates;
	private ArrayList<Integer> 	listIndices;
	private MCMCTreeNode 		root;
	
	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCTree() {
		data 			= null;
		key 			= 0;
		numDuplicates 	= 0;
		listIndices 	= new ArrayList<Integer>();
	}
	
	public MCMCTree(MCMCTreeNode root) {
		this.root 		= root;
		this.root.setName("Root");
		data 			= null;
		key 			= 0;
		numDuplicates 	= 0;
		listIndices 	= new ArrayList<Integer>();
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	/** initNodes: Will set tree properties for each node stored in tree. */
	public void initNodes() {
		if(root != null) {
			root.setTree(this);
			root.setNumDuplicates(numDuplicates);
			initNodes(root);
		}
	}
	
	/** initNodes: Will set tree properties for each node that has parameter node as parent. */
	public void initNodes(MCMCTreeNode node) {
		for(int i=0; i<node.getNumChildren(); i++) {
			MCMCTreeNode child = node.getChild(i);
			
			child.setTree(this);
			child.setNumDuplicates(numDuplicates);
			
			initNodes(child);
		}
	}
	
	/** print: Print tree information. */
	public void print(MCMCTreeNode node) {
		for(int i=0; i<node.level; i++)
			System.out.print("  ");
		
		System.out.println("Tree: " + this + " node tree " + node.getTree());
		
		for(int i=0; i<node.getNumChildren(); i++)
			print(node.getChild(i));
	}
	
	@Override
	public int compareTo(MCMCTree tree) {
		MCMCTree mcmcTree = (MCMCTree) tree;
		if(mcmcTree.getNumDuplicates() < numDuplicates) //Value of tree is determined by it's duplicates for easy sorting
			return -1;
		else if(mcmcTree.getNumDuplicates() > numDuplicates)
			return 1;
		else
			return 0;
	}
	
	public byte[] getData() 						{return data;}
	public int getNumDuplicates() 					{return numDuplicates;}
	public int getKey() 							{return key;}
	public ArrayList<Integer> getIndexList() 		{return listIndices;}
	public MCMCTreeNode getRoot() 					{return root;}
	
	public void addIndex(int index) 				{listIndices.add(index);}
	public void addDuplicate() 						{numDuplicates++;}
	public void setData(byte[] data) 				{this.data = data;}
	public void setNumDuplicates(int numDuplicates) {this.numDuplicates = numDuplicates;}
	public void setKey(int key) 					{this.key = key;}
	public void setRoot(MCMCTreeNode root) 			{this.root = root;}
}
