package se.cbb.jprime.mcmc;

/**
 * Interface for tuning parameters of <code>Proposer</code> objects,
 * i.e. parameters that typically define how large state changes <code>Proposer</code>s
 * suggest.
 * <p/>
 * A <code>ProposerWeight</code> constitutes a special kind of tuning parameter,
 * since it governs how often a <code>Proposer</code> itself will be utilised.
 * <p/>
 * See also <code>ConstantTuningParameter</code> and <code>LinearTuningParameter</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface TuningParameter extends InfoProvider {

	/**
	 * Returns the current value.
	 * @return the value.
	 */
	public double getValue();
	
	/**
	 * Returns the minimum value this tuning
	 * parameter may return.
	 * @return the minimum value.
	 */
	public double getMinValue();
	
	/**
	 * Returns the maximum value this tuning
	 * parameter may return.
	 * @return the maximum value.
	 */
	public double getMaxValue();
}
