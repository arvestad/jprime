package se.cbb.jprime.mcmc;

/**
 * Represents a weight associated with a <code>Proposer</code> object,
 * ultimately deciding how often the latter will be invoked.
 * Implementing classes may e.g. listen for changes to an <code>Iteration</code>
 * object and adjust their weights accordingly.
 * At the moment, one may associate multiple parameters with the same weight instance.
 * <p/>
 * No constraints are made as to the range of valid weights other than that it
 * <b>must be non-negative</b>, although being able to specify an upper limit is recommended.
 * 
 * @author Joel Sjöstrand.
 */
public interface ProposerWeight extends MCMCSerializable {

	/**
	 * Returns the current weight of this object.
	 * It is assumed that this value is in the range [getMinWeight(),getMaxWeight()].
	 * @return the weight.
	 */
	public double getWeight();
	
	/**
	 * Returns the minimum weight that <code>getWeight()</code>
	 * can return. May return 0, but never less.
	 * @return the minimum weight.
	 */
	public double getMinWeight();
	
	/**
	 * Returns the maximum weight that <code>getWeight()</code>
	 * can return. May return 0, but never less.
	 * @return the maximum weight.
	 */
	public double getMaxWeight();
}
