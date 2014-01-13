package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;

/**
 * MCMCTreeNode: Tree node containing node specifik information together with list of children.
 */
public class MCMCTreeNode {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	MCMCTree 				tree;
	ArrayList<MCMCTreeNode> listChildren;
	ArrayList<String> 		path;
	String 					name;
	int 					level;
	int 					numDuplicates;
	int 					genericKey;
	int 					numChildren;

	/* **************************************************************************** *
	 * 							CLASS CONSTRUCTORS									*
	 * **************************************************************************** */
	public MCMCTreeNode() {
		name 				= "";
		listChildren 		= new ArrayList<MCMCTreeNode>();
		numChildren 		= 0;
		genericKey 			= 0;
		numDuplicates 		= 0;
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public void addChild(MCMCTreeNode child) {
		listChildren.add(child);
		numChildren++;
	}
	
	/** print: Recursive print of nodes tree structure. */
	public void print() {
		for(int i=0; i<level; i++)
			System.out.print("  ");
		System.out.println("Node " + name + " number of children " + numChildren + " key " + genericKey);
		for(int i=0; i<numChildren; i++)
			listChildren.get(i).print();
	}
	
	@Override
	public String toString() 						{return String.valueOf(genericKey);}
	
	public ArrayList<MCMCTreeNode> getChildren() 	{return listChildren;}
	public String getName() 						{return name;}
	public int getNumChildren() 					{return numChildren;}
	public int getLevel() 							{return this.level;}
	public ArrayList<String> getPath() 				{return this.path;}
	public int getGenericKey() 						{return this.genericKey;}
	public MCMCTreeNode getChild(int index) 		{return listChildren.get(index);}
	public MCMCTree getTree() 						{return this.tree;}
	public int getNumDuplicates() 					{return numDuplicates;}
	
	public void setName(String name) 				{this.name = name;}
	public void setLevel(int level) 				{this.level = level;}
	public void setPath(String orientation) 		{path.add(orientation);}
	public void setGenericKey(int genericKey) 		{this.genericKey = genericKey;}
	public void setTree(MCMCTree tree) 				{this.tree = tree;}
	public void setNumDuplicates(int numDuplicates) {this.numDuplicates = numDuplicates;}
}
