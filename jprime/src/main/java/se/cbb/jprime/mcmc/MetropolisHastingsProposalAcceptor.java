package se.cbb.jprime.mcmc;

import java.util.List;

import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;

/**
 * Metropolis-Hastings proposal acceptor.
 * 
 * @author Joel Sjöstrand.
 */
public class MetropolisHastingsProposalAcceptor implements ProposalAcceptor {

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
	 * Returns true if a proposed state x' should be accepted according to the Metropolis-Hastings sampling scheme.
	 * @param proposedStateLikelihood the likelihood P(x') of the proposed state x'.
	 * @param oldStateLikelihood the likelihood P(x) of the old state x.
	 * @param proposals details the proposals made for going from x to x'. Elements must be of type <code>MetropolisHastingsProposal</code>.
	 * @param prng pseudo-random number generator.
	 * @return true if suggested state accepted; false if rejected.
	 */
	@Override
	public boolean acceptProposedState(LogDouble proposedStateLikelihood,
			LogDouble oldStateLikelihood, List<Proposal> proposals, PRNG prng) {
		LogDouble a = proposedStateLikelihood.divToNew(oldStateLikelihood);
		if (proposals != null) {
			for (Proposal prop : proposals) {
				if (!prop.isValid()) {
					return false;
				}
				a.mult(((MetropolisHastingsProposal) prop).getDensityRatio());
			}
		}
		return a.greaterThanOrEquals(new LogDouble(prng.nextDouble()));   // Accounts also for case a >= 1.0.
	}

}
