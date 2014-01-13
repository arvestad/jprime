package se.cbb.jprime.mcmc;

import java.util.Arrays;
import java.util.List;
import se.cbb.jprime.math.LogDouble;

/**
 * Details a state parameter change w.r.t. a Metropolis-Hastings proposal by a <code>Proposer</code>.
 * Thus, enables storing the "backward" and "forward" proposal densities. 
 * 
 * @author Joel Sjöstrand.
 */
public class MetropolisHastingsProposal implements Proposal {

	/** Proposer which generated this object. */
	private Proposer proposer;
	
	/** Forward probability density. */
	private LogDouble forwardDensity;
	
	/** Backward probability density. */
	private LogDouble backwardDensity;

	/** Perturbed parameters. */
	private List<StateParameter> params;
	
	/** Number of perturbed sub-parameters. */
	private int noOfSubParams;
	
	/**
	 * Constructor.
	 * @param proposer the proposer which generated this object.
	 * @param forwardDensity the probability density of obtaining the new state given the old one.
	 * @param backwardDensity the probability density of obtaining the old state given the new one.
	 * @param perturbedParams the actually changed parameters from old state to new state.
	 * @param noOfPerturbedSubParams the total number of actually changed sub-parameters of the above.
	 */
	public MetropolisHastingsProposal(Proposer proposer, LogDouble forwardDensity, LogDouble backwardDensity, List<StateParameter> perturbedParams,
			int noOfPerturbedSubParams) {
		this.proposer = proposer;
		this.forwardDensity = forwardDensity;
		this.backwardDensity = backwardDensity;
		this.params = perturbedParams;
		this.noOfSubParams = noOfPerturbedSubParams;
	}
	
	/**
	 * Constructor for when a single parameter has been perturbed.
	 * @param proposer the proposer which generated this object.
	 * @param forwardDensity the probability density of obtaining the new state given the old one.
	 * @param backwardDensity the probability density of obtaining the old state given the new one.
	 * @param perturbedParam the perturbed parameter from old state to new state.
	 * @param noOfPerturbedSubParams the total number of actually changed sub-parameters of the above.
	 */
	public MetropolisHastingsProposal(Proposer proposer, LogDouble forwardDensity, LogDouble backwardDensity, StateParameter perturbedParam,
			int noOfPerturbedSubParams) {
		this(proposer, forwardDensity, backwardDensity, Arrays.asList(new StateParameter[] {perturbedParam}), noOfPerturbedSubParams);
	}

	/**
	 * Constructor for a failed proposal, i.e., when parameters could not be perturbed.
	 * Sets the backward probability density to 0 and the forward probability density
	 * to 1.
	 * @param proposer the proposer which generated this object.
	 * @param attemptedParams the parameters which failed to be perturbed.
	 */
	public MetropolisHastingsProposal(Proposer proposer, List<StateParameter> attemptedParams) {
		this.proposer = proposer;
		this.params = attemptedParams;
		this.forwardDensity = new LogDouble(1.0);
		this.backwardDensity = new LogDouble(0.0);
		this.noOfSubParams = 0;
	}
	
	/**
	 * Constructor for a failed proposal, i.e., when a parameter could not be perturbed.
	 * Sets the backward probability density to 0 and the forward probability density
	 * to 1.
	 * @param proposer the proposer which generated this object.
	 * @param attemptedParam the parameter which failed to be perturbed.
	 */
	public MetropolisHastingsProposal(Proposer proposer, StateParameter attemptedParam) {
		this(proposer, Arrays.asList(new StateParameter[] {attemptedParam}));
	}
	
	/**
	 * Returns the probability density Q(x';x) for
	 * obtaining the new value x' given the old value x.
	 * @return the "forward" probability density.
	 */
	public LogDouble getForwardDensity() {
		return this.forwardDensity;
	}

	/**
	 * Returns the probability density Q(x;x') for
	 * obtaining the old value x given the new value x'.
	 * @return the "backward" probability density.
	 */
	public LogDouble getBackwardDensity() {
		return this.backwardDensity;
	}

	/**
	 * Returns the ratio Q(x;x')/Q(x';x) for the old state x and the new state
	 * x', i.e. the ratio between the "backward" and "forward" proposal densities
	 * respectively.
	 * @return the ratio between the "backward" and "forward" proposal densities.
	 */
	public LogDouble getDensityRatio() {
		return this.backwardDensity.divToNew(this.forwardDensity);
	}

	@Override
	public Proposer getProposer() {
		return this.proposer;
	}

	@Override
	public List<StateParameter> getPerturbedParameters() {
		return this.params;
	}

	@Override
	public int getNoOfPerturbedParameters() {
		return this.params.size();
	}

	@Override
	public int getNoOfPerturbedSubParameters() {
		return this.noOfSubParams;
	}

	@Override
	public boolean isValid() {
		return (!this.backwardDensity.isZero() && !this.forwardDensity.isZero());
	}


}
