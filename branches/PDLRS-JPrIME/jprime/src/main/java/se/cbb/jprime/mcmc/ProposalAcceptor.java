package se.cbb.jprime.mcmc;

import java.util.List;

import se.cbb.jprime.math.LogDouble;

/**
 * Interface for probabilistic techniques of determining whether a suggested
 * parameter state change should be accepted or not. At the moment this
 * has been written with Metropolis-Hastings-like methods in mind, so it is
 * subject to change to become more general.
 * <p/>
 * See also <code>MetropolisHastingsAcceptor</code> and <code>HillClimbingAcceptor</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface ProposalAcceptor extends InfoProvider {

	/**
	 * Returns true if a proposed state x' should be accepted, or false if the old state x should be retained.
	 * @param proposedStateLikelihood the likelihood P(x').
	 * @param oldStateLikelihood the likelihood P(x).
	 * @param proposals details the proposals made for going from x to x'. May be null for certain implementations (e.g. hill-climbing).
	 * @return true if suggested state accepted; false if rejected.
	 */
	public boolean acceptProposedState(LogDouble proposedStateLikelihood, LogDouble oldStateLikelihood,
			List<Proposal> proposals) throws RunAbortedException;
}
