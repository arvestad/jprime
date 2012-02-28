package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;

/**
 * MCMCTreeNode: Tree node containing node specifik information together with list of
 * children.
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
public class MCMCTreeNode {
	MCMCTree tree;
	
	ArrayList<MCMCTreeNode> listChildren;
	ArrayList<String> path;
	String name;
	int level;
	int numDuplicates;
	
	int genericKey;
	
	int numChildren;
	
	public MCMCTreeNode() {
		name = "";
		listChildren = new ArrayList<MCMCTreeNode>();
		numChildren = 0;
		genericKey = 0;
		numDuplicates = 0;
	}
	
	public void addChild(MCMCTreeNode child) {
		listChildren.add(child);
		numChildren++;
	}
	
	
	public ArrayList<MCMCTreeNode> getChildren() {return listChildren;}
	public String getName() {return name;}
	public int getNumChildren() {return numChildren;}
	public int getLevel() { return this.level;}
	public ArrayList<String> getPath() { return this.path;}
	public int getGenericKey() {return this.genericKey;}
	public MCMCTreeNode getChild(int index) {return listChildren.get(index);}
	public MCMCTree getTree() {return this.tree;}
	public int getNumDuplicates() {return numDuplicates;}
	
	public void setName(String name) {this.name = name;}
	public void setLevel(int level) {this.level = level;}
	public void setPath(String orientation) {path.add(orientation);}
	public void setGenericKey(int genericKey) {this.genericKey = genericKey;}
	public void setTree(MCMCTree tree) {this.tree = tree;}
	public void setNumDuplicates(int numDuplicates) {this.numDuplicates = numDuplicates;}
	
	/*
	 * print: Recursive print of nodes tree structure.
	 */
	public void print() {
		for(int i=0; i<level; i++)
			System.out.print("  ");
		
		System.out.println("Node " + name + " number of children " + numChildren + " key " + genericKey);
		
		for(int i=0; i<numChildren; i++)
			listChildren.get(i).print();
	}
	
	
	public String toString() {
		return String.valueOf(genericKey);
	}
}
