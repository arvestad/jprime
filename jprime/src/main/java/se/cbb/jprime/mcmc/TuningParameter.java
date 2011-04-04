package se.cbb.jprime.mcmc;

/**
 * Interface for tuning parameters of <code>Proposer</code> objects,
 * i.e. parameters that typically define how large state changes these
 * suggest. A tuning parameter may of course implement <code>IterationListener</code>
 * to change its value over time.
 * 
 * @author Joel Sjöstrand
 *
 */
public interface TuningParameter extends MCMCSerializable {

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
