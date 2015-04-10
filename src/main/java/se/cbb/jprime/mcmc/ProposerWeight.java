package se.cbb.jprime.mcmc;

/**
 * Represents a weight associated with a <code>Proposer</code> object,
 * ultimately deciding how often the latter will be invoked.
 * At the moment, one may associate multiple <code>Proposer</code> objects with the same weight instance.
 * <p/>
 * No constraints are made as to the range of valid weights other than that it
 * <b>must be non-negative</b>, although being able to specify an upper limit may be required.
 * <p/>
 * See also <code>ConstantProposerWeight</code> and <code>LinearProposerWeight</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface ProposerWeight extends TuningParameter {

	/**
	 * Returns the current weight of this object.
	 * It is assumed that this value is in the range [getMinWeight(),getMaxWeight()].
	 * @return the weight.
	 */
	@Override
	public double getValue();
	
	/**
	 * Returns the minimum weight that <code>getWeight()</code>
	 * can return. May return 0, but never less.
	 * @return the minimum weight.
	 */
	@Override
	public double getMinValue();
	
	/**
	 * Returns the maximum weight that <code>getWeight()</code>
	 * can return. May return 0, but never less.
	 * @return the maximum weight.
	 */
	@Override
	public double getMaxValue();
}
