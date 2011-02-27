package se.cbb.jprime.mcmc;

/**
 * Interface for classes governing e.g. MCMC thinning (i.e. how often the current state
 * is in fact sampled).
 * Ideally, implementing classes listen for <code>Iteration</code> changes.
 * 
 * @author Joel Sjöstrand.
 */
public interface Thinner {

	/**
	 * Returns true if sampling should be performed.
	 * @return true if sample suggested, otherwise false.
	 */
	public boolean doSample();
}
