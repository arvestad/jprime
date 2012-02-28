package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;

/**
 * MCMCTree: Class containing information specific for MCMC trees. Contains one MCMCTreeNode
 * as root which specifies tree structure.
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
public class MCMCTree implements Comparable<MCMCTree> {
	private byte[] data;
	private int key;
	private int numDuplicates;
	
	private ArrayList<Integer> listIndices;
	
	private MCMCTreeNode root;
	
	public MCMCTree() {
		data = null;
		key = 0;
		numDuplicates = 0;
		
		listIndices = new ArrayList<Integer>();
	}
	
	public MCMCTree(MCMCTreeNode root) {
		this.root = root;
		this.root.setName("Root");
		
		data = null;
		key = 0;
		numDuplicates = 0;
		
		listIndices = new ArrayList<Integer>();
	}
	
	/*
	 * initNodes: Will set tree properties for each node stored in tree.
	 */
	public void initNodes() {
		if(root != null) {
			root.setTree(this);
			root.setNumDuplicates(numDuplicates);
			initNodes(root);
		}
	}
	
	/*
	 * initNodes: Will set tree properties for each node that has parameter node as
	 * parent.
	 */
	public void initNodes(MCMCTreeNode node) {
		for(int i=0; i<node.getNumChildren(); i++) {
			MCMCTreeNode child = node.getChild(i);
			
			child.setTree(this);
			child.setNumDuplicates(numDuplicates);
			
			initNodes(child);
		}
	}
	
	public byte[] getData() {return data;}
	public int getNumDuplicates() {return numDuplicates;}
	public int getKey() {return key;}
	public ArrayList<Integer> getIndexList() {return listIndices;}
	public MCMCTreeNode getRoot() {return root;}
	
	public void addIndex(int index) {listIndices.add(index);}
	public void addDuplicate() {numDuplicates++;}
	public void setData(byte[] data) {this.data = data;}
	public void setNumDuplicates(int numDuplicates) {this.numDuplicates = numDuplicates;}
	public void setKey(int key) {this.key = key;}
	public void setRoot(MCMCTreeNode root) {this.root = root;}

	public int compareTo(MCMCTree o) {
		MCMCTree arg0 = (MCMCTree) o;
		
		//Value of tree is determined by it's duplicates for easy sorting
		if(arg0.getNumDuplicates() < numDuplicates)
			return -1;
		else if(arg0.getNumDuplicates() > numDuplicates)
			return 1;
		else
			return 0;
	};
	
	/*
	 * print: Print tree information.
	 */
	public void print(MCMCTreeNode node) {
		for(int i=0; i<node.level; i++)
			System.out.print("  ");
		
		System.out.println("Tree: " + this + " node tree " + node.getTree());
		
		for(int i=0; i<node.getNumChildren(); i++)
			print(node.getChild(i));
	}
}
