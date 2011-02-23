package se.cbb.jprime.mcmc;

/**
 * Represents an invariant perturber weight.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantPerturberWeight implements PerturberWeight {

	/** The weight. */
	private double weight;

	/**
	 * Constructor.
	 * @param weight the invariant weight.
	 */
	public ConstantPerturberWeight(double weight) {
		if (weight < 0) {
			throw new IllegalArgumentException("Cannot set negative perturber weight.");
		}
		this.weight = weight;
	}
	
	@Override
	public double getWeight() {
		return this.weight;
	}

	@Override
	public double getMinWeight() {
		return this.weight;
	}

	@Override
	public double getMaxWeight() {
		return this.weight;
	}

}
