package se.cbb.jprime.prm;

import java.util.Random;

/**
 * Interface for discrete valued PRM attributes.
 * Any value should be able to represented as an integer in the range 0,...,k-1.
 * For latent attributes, there is a dual representation in that a
 * value (= hard assignment) may exist alongside a probability distribution
 * for all values (= soft assignment).
 * 
 * @author Joel Sjöstrand.
 */
public interface DiscreteAttribute extends ProbAttribute {

	/**
	 * Returns the number of valid values of this attribute, i.e. k for the range 0,...,k-1.
	 * @return the number of values.
	 */
	public int getNoOfValues();
	
	/**
	 * Returns the integer representation of an entity's value. For latent values, returns the
	 * current hard assignment.
	 * @param idx the index.
	 * @return the value in the range 0,...,k-1.
	 */
	public int getEntityAsInt(int idx);
	
	/**
	 * Setter mirroring <code>getEntityAsInt()</code>.
	 * If latent, does not affect the corresponding soft completion.
	 * No bounds checking.
	 * @param idx the index.
	 * @param value the value in the range 0,...,k-1.
	 */
	public void setEntityAsInt(int idx, int value);
	
	/**
	 * Adds an entity as an integer. If latent, this also
	 * adds a corresponding soft completion for the entity.
	 * See <code>getEntityAsInt()</code>
	 * for more info.
	 * @param value the value in the range 0,...,k-1.
	 */
	public void addEntityAsInt(int value);
	
	/**
	 * For latent attributes, returns the current estimation of
	 * an entity's probability distribution, i.e., its soft completion.
	 * Indexing complies
	 * with the <code>getEntityAsInt()</code> method.
	 * @param idx the index.
	 * @return the estimated probability distribution.
	 */
	public double[] getEntityProbDistribution(int idx);
	
	/**
	 * For latent attributes, sets the current estimation of
	 * an entity's probability distribution, i.e., its soft completion.
	 * Does not affect the corresponding current hard assignment.
	 * Indexing complies with the <code>getEntityAsInt()</code> method.
	 * @param idx the index.
	 * @param probDist the soft completion.
	 */
	public void setEntityProbDistribution(int idx, double[] probDist);
	
	/**
	 * For latent attributes, sets the current estimation of an
	 * entity's soft completion to NaN for all values.
	 * @param idx the index.
	 */
	public void clearEntityProbDistribution(int idx);
	
	/**
	 * For latent attributes, normalises the current estimation of an
	 * entity's soft completion so that they sum up to 1. A NaN value is
	 * considered 0.0.
	 * @param idx the index.
	 */
	public void normaliseEntityProbDistribution(int idx);
	
	/**
	 * For a latent attribute, sets the soft completion of a value to 1.0 for
	 * precisely that value. Mostly for debugging purposes. Only applicable on latent attributes.
	 */
	public void useSharpSoftCompletion();

	/**
	 * For a latent attribute entity, returns the highest ranked value in its
	 * current soft completion.
	 * Indexing complies with the <code>getEntityAsInt()</code> method.
	 * @param idx the entity.
	 * @return the value with the highest soft completion score.
	 */
	public int getMostProbEntityAsInt(int idx);
	
	/**
	 * For a latent attribute entity, alters it current soft completion.
	 * @param idx the entity.
	 */
	public void perturbEntityProbDistribution(int idx);
	
	/**
	 * For a latent attribute, assigns random values to all entities.
	 * @param rng the random number generator.
	 */
	public void assignRandomValues(Random rng);
}
