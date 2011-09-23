package se.cbb.jprime.mcmc;

/**
 * Interface for tuning parameters of <code>Proposer</code> objects,
 * i.e. parameters that typically define how large state changes these
 * suggest. A tuning parameter may of course implement <code>IterationListener</code>
 * to change its value over time.
 * <p/>
 * A <code>ProposerWeight</code> constitutes a special kind of tuning parameter,
 * since it defines how often a <code>Proposer</code> itself will be utilised.
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
