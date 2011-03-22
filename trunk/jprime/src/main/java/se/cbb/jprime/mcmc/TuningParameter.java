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
public interface TuningParameter {

	/**
	 * Returns this tuning parameter's name.
	 * @return the name.
	 */
	public String getName();
	
}
