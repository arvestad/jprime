package se.cbb.jprime.mcmc;

import se.cbb.jprime.io.Sampleable;
import se.cbb.jprime.math.LogDouble;

/**
 * Interface for probabilistic models. Subject to change,
 * and very spartan at the moment.
 * <p/>
 * A model is a <code>ProperDependent</code>, and as such, typically a sink
 * in a dependency DAG where the state parameters are sources.
 * It should ideally compute its likelihood when <code>cacheAndUpdateAndSetChangeInfo()</code>
 * is invoked, and only return this value when <code>getDataProbability()</code> is
 * called. Also, it should be able to cache and restore its old values if the proposed
 * state is rejected like any other <code>ProperDependent</code>.
 * Priors are currently also encouraged to implement this interface.
 * <p/>
 * Furthermore, a model can act as a "sampleable". No particular requirements are made as to
 * what is sampled, although outputting the model's log-likelihood is recommended.
 * 
 * @author Joel Sjöstrand.
 */
public interface Model extends ProperDependent, Sampleable, InfoProvider {

	/**
	 * Returns the (often conditional) probability density of the observations (or intermediate parameters)
	 * given the current model instantiation, priors, and current parameters (or hyperparameters).
	 * @return the model probability.
	 */
	public LogDouble getDataProbability();
}
