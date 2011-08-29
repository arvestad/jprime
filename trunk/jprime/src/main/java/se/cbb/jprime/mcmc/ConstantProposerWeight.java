package se.cbb.jprime.mcmc;

/**
 * Represents an invariant <code>ProposerWeight</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantProposerWeight extends ConstantTuningParameter implements ProposerWeight {

	/**
	 * Constructor.
	 * @param weight the invariant weight.
	 */
	public ConstantProposerWeight(double weight) {
		super(weight);
		if (weight < 0) {
			throw new IllegalArgumentException("Cannot set negative proposer weight.");
		}
	}

	/**
	 * Sets the weight.
	 * @param weight the new weight.
	 */
	public void setValue(double weight) {
		if (weight < 0) {
			throw new IllegalArgumentException("Cannot set negative proposer weight.");
		}
		this.value = weight;
	}

}
