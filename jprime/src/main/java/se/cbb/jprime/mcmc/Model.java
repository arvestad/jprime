package se.cbb.jprime.mcmc;

import se.cbb.jprime.math.Probability;

/**
 * Interface for probabilistic models. Subject to change,
 * and very spartan at the moment.
 * <p/>
 * A model is a <code>Dependent</code>, and as such typically a sink
 * in a dependency DAG where the state parameters are sources.
 * It should ideally compute its likelihood when <code>update()</code>
 * is invoked, and only return this value when <code>getLikelihood()</code> is
 * called. Also, it should be able to cache and restore its old values if proposed
 * state rejected.
 * <p/>
 * Furthermore, a model can act as a "sampleable". No particular requirements are made as to
 * what is sampled, although outputting the model's log-likelihood is recommended.
 * 
 * @author Joel Sjöstrand.
 */
public interface Model extends Dependent, Sampleable {

	/**
	 * Returns the (often conditional) probability density of the observations
	 * given the current model instantiation and its current parameters.
	 * @return the probability of the data given the model and parameters.
	 */
	public Probability getLikelihood();
}
