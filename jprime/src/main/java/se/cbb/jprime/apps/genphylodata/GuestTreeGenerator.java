package se.cbb.jprime.apps.genphylodata;

import java.util.ArrayList;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;

/**
 * Generates an ultrametric guest tree ("gene tree") involving inside a host tree ("species tree"),
 * by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeGenerator implements JPrIMEApp {

	@Override
	public String getAppName() {
		return "GuestTreeGenerator";
	}

	@Override
	public void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Advances the gene evolution process during 'time' units of time
	 * assuming that the process takes place during a slice of the species
	 * tree, i.e., the only edges present during this time is the edges in
	 * 'slice'. 
	 * @param time Time till which current BD process has to proceed
	 * @param slice species tree stem where BD process has to occur
	 * @param cur_genes current genes (leaves) in the slice to which 
	 * BD process has to be applied 
	 */
	public void runBDProcess(double time, ArrayList<NewickVertex> slice, ArrayList<NewickVertex> cur_genes) {
	    
	}
	
	/**
	 * Creates children for any gene in 'cur_genes' that is labeled by
	 * 'slice[sliceID]'.
	 * @param stree species tree
	 * @param slice species tree stem
	 * @param cur_genes current set of genes (leaves) in gene tree
	 * @param sliceID ID of the slice where speciation is occurring 
	 */
	public void speciate(PrIMENewickTree stree, ArrayList<NewickVertex> slice, ArrayList<NewickVertex> cur_genes, int sliceID) {
	    
	}

	
}
