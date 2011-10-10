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

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("CONSTANT PROPOSER WEIGHT\n");
		sb.append(prefix).append("Value: ").append(this.value).append('\n');
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return null;
	}
}
