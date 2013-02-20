package se.cbb.jprime.apps.genphylodata;

import se.cbb.jprime.mcmc.GenerativeModel;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Interface for branch length relaxation models.
 * 
 * @author Joel Sjöstrand.
 */
public interface RateModel extends GenerativeModel {

	/**
	 * Returns rates.
	 * @param t the tree.
	 * @param names leaf/vertex names.
	 * @param origLengths the original lengths
	 * @return rates.
	 */
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths);
	
	/**
	 * Ultrametricity requirement on original lengths.
	 * @return true if required; false if arbitrary.
	 */
	public boolean lengthsMustBeUltrametric();
}
