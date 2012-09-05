package se.cbb.jprime.apps.vmcmc.libs;

/**
 * MCMCNewick: Functions for converting MCMCTreeNodes to newick strings
 */
abstract public class MCMCNewick {

	private static String newick = "";

	public static void TreeToNewick(MCMCTreeNode node) {
		if(node.getNumChildren() > 0) 
			newick += "(";
		else
			newick += node.getName();
		for(int i = 0; i < node.numChildren; i++) {
			TreeToNewick(node.getChild(i));
			if(i < node.getNumChildren()-1)
				newick += ", ";
			else
				newick += ")";
		}
	}
	
	public static String getNewick(MCMCTreeNode node) {
		newick = "";
		TreeToNewick(node);
		return newick+";";
	}
}