package se.cbb.jprime.mcmc;

/**
 * Represents an invariant <code>ProposerWeight</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantProposerWeight implements ProposerWeight {

	/** The fixed weight. */
	private double weight;

	/**
	 * Constructor.
	 * @param weight the invariant weight.
	 */
	public ConstantProposerWeight(double weight) {
		this.setWeight(weight);
	}
	
	@Override
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Sets the weight.
	 * @param weight the new weight.
	 */
	public void setWeight(double weight) {
		if (weight < 0) {
			throw new IllegalArgumentException("Cannot set negative proposer weight.");
		}
		this.weight = weight;
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
