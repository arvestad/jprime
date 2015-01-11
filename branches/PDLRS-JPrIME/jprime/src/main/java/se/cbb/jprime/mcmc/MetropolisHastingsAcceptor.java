package se.cbb.jprime.mcmc;

import java.util.List;

import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;

/**
 * Metropolis-Hastings proposal acceptor. As such, it probabilistically accepts or rejects a proposed
 * state x' originating from an old state x depending on the likelihood of x' and x, and the
 * probability of moving between them.
 * 
 * @author Joel Sjöstrand.
 */
public class MetropolisHastingsAcceptor implements ProposalAcceptor {

	/** Pseudo-random number generator. */
	private PRNG prng;

	/**
	 * Constructor.
	 * @param prng pseudo-random number generator.
	 */
	public MetropolisHastingsAcceptor(PRNG prng) {
		this.prng = prng;
	}
	
	/**
	 * Returns true if a proposed state x' should be accepted according to the Metropolis-Hastings sampling scheme.
	 * @param proposedStateLikelihood the likelihood P(x') of the proposed state x'.
	 * @param oldStateLikelihood the likelihood P(x) of the old state x.
	 * @param proposals details the proposals made for going from x to x'. Elements must be of type <code>MetropolisHastingsProposal</code>.
	 * @return true if suggested state accepted; false if rejected.
	 */
	@Override
	public boolean acceptProposedState(LogDouble proposedStateLikelihood,
		LogDouble oldStateLikelihood, List<Proposal> proposals) throws RunAbortedException {
		LogDouble a = proposedStateLikelihood.divToNew(oldStateLikelihood);
//		System.out.println("-----------------------------------------\nProposedStateLikelihood = " + proposedStateLikelihood.getValue() + " , log value : " + proposedStateLikelihood.getLogValue());
//		System.out.println("OldStateLikelihood = " + oldStateLikelihood.getValue()+ " , log value : " + oldStateLikelihood.getLogValue()+ "\n-----------------------------------------");
//		System.out.println( " ProposedStateLikelihood / oldStateLikelihood : " + a.getValue() + " , log value : " + a.getLogValue());
		int proposal_no=0;
		if (proposals != null) {
			for (Proposal prop : proposals) {
				proposal_no++;
				if (!prop.isValid()) {
					return false;
				}
//				System.out.println( " Density ratio for Proposal no "+ proposal_no + " : " + ((MetropolisHastingsProposal) prop).getDensityRatio().getValue());
				a.mult(((MetropolisHastingsProposal) prop).getDensityRatio());
			}
		}
//		System.out.println(" ( proposedStateLikelihood/oldStateLikelihood )*( Q(x;x')/Q(x';x) ) : " + a.getValue() +  " , log value : " + a.getLogValue());
		Double randomValue = prng.nextDouble();
//		System.out.println("Random Value = " + randomValue);
		return a.greaterThanOrEquals(new LogDouble(randomValue));   // Accounts also for case a >= 1.0.
	}
	
	@Override
	public String getPreInfo(String prefix) {
		return (prefix + "METROPOLIS-HASTINGS ACCEPTOR\n");
	}

	@Override
	public String getPostInfo(String prefix) {
		return (prefix + "METROPOLIS-HASTINGS ACCEPTOR\n");
	}

}
