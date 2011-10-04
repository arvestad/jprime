package se.cbb.jprime.mcmc;

import java.util.List;

import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;

/**
 * Hill-climbing proposal acceptor. Only accepts proposed states which are better than the current.
 * 
 * @author Joel Sjöstrand.
 */
public class HillClimbingProposalAcceptor implements ProposalAcceptor {

	@Override
	public String getPreInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPostInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns true if a proposed state x' has a higher likelihood than the old state x. Does not require any proposal
	 * information nor a pseudo-random number generator.
	 * @param proposedStateLikelihood the likelihood P(x').
	 * @param oldStateLikelihood the likelihood P(x).
	 * @param proposals details the proposals made for going from x to x'. May be null.
	 * @param prng pseudo-random number generator. Not used; may be null.
	 * @return true if suggested state accepted; false if rejected.
	 */
	@Override
	public boolean acceptProposedState(LogDouble proposedStateLikelihood,
			LogDouble oldStateLikelihood, List<Proposal> proposals, PRNG prng) {
		// If we do have proposal information, just verify it's OK.
		if (proposals != null) {
			for (Proposal prop : proposals) {
				if (!prop.isValid()) { return false; }
			}
		}
		return proposedStateLikelihood.greaterThan(oldStateLikelihood);
	}

}
