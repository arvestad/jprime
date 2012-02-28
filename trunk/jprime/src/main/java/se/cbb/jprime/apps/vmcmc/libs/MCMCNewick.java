package se.cbb.jprime.apps.vmcmc.libs;

/**
 * MCMCNewick: Functions for converting MCMCTreeNodes to newick strings
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
abstract public class MCMCNewick {

	private static String newick = "";

	public static void TreeToNewick(MCMCTreeNode node) {

		if(node.getNumChildren() > 0) {
			newick += "(";
		}
		else{
			newick += node.getName();
		}
		for(int i = 0; i < node.numChildren; i++) {
			TreeToNewick(node.getChild(i));
			if(i < node.getNumChildren()-1){
				newick += ", ";
			}
			else{
				newick += ")";
			}
		}
	}
	
	public static String getNewick(MCMCTreeNode node) {
		newick = "";
		TreeToNewick(node);
		return newick+";";
	}

}