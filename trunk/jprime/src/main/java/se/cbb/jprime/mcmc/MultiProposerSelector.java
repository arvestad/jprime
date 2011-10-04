package se.cbb.jprime.mcmc;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.math.PRNG;

/**
 * Selects one or more proposers (acting on disjoint state parameters).
 * The user may specify a desired weight set for how often more than
 * one proposer should be invoked, e.g. [0.60,0.30,0.10] for
 * 1 proposer 60% of the time, 2 proposers 30% of the time and 3 proposers
 * 10% of the time. However, there is no guarantee that exactly these
 * numbers will be achieved in practice (due to that they
 * act on the same parameters).
 * 
 * @author Joel Sjöstrand.
 */
public class MultiProposerSelector implements ProposerSelector {

	/** The maximum number of attempts at trying to add another proposer when selecting. */
	public static final int MAX_NO_OF_ATTEMPTS = 100;
	
	/** PRNG. */
	private PRNG prng;
	
	/** Cumulative number-of-proposers weights, normalised as [0,...,1]. Null if not used. */
	private double[] cumNoWeights;
	
	/**
	 * Creates an instance where only one proposer at a time is invoked.
	 * @param prng the PRNG used for random selection.
	 */
	public MultiProposerSelector(PRNG prng) {
		this.prng = prng;
		this.cumNoWeights = new double[] { 1.0 };
	}
	
	/**
	 * Creates an instance where more than one proposer may be selected.
	 * The number of proposers attempted is specified in a weight array
	 * where element 0 corresponds to 1 proposer and so forth.
	 * @param prng the PRNG used for random selection.
	 * @param noWeights the desired weights for the number of selected proposers, e.g. [0.5,0.5] for
	 * 1 proposer 50% of the time and 2 proposers 50% of the time.
	 */
	public MultiProposerSelector(PRNG prng, double[] noWeights) {
		if (noWeights == null || noWeights.length == 0) {
			throw new IllegalArgumentException("Invalid weights in multi proposer selector.");
		}
		this.prng = prng;
		this.cumNoWeights = new double[noWeights.length];
		double tot = 0.0;
		for (int i = 0; i < noWeights.length; ++i) {
			if (noWeights[i] < 0.0) {
				throw new IllegalArgumentException("Cannot assign negative weight in multi proposer selector.");
			}
			tot += noWeights[i];
			this.cumNoWeights[i] = tot;
		}
		for (int i = 0; i < this.cumNoWeights.length; ++i) {
			this.cumNoWeights[i] /= tot;
		}
		this.cumNoWeights[this.cumNoWeights.length - 1] = 1.0;   // For numeric safety.
	}
	
	@Override
	public Set<Proposer> getDisjointProposers(List<Proposer> proposers) {
		if (proposers == null || proposers.isEmpty()) {
			throw new IllegalArgumentException("Cannot select proposer from empty list.");
		}
		
		// Special cases for speed.
		if (proposers.size() == 1) {
			return new TreeSet<Proposer>(proposers);
		}
		if (this.cumNoWeights.length == 1) {
			TreeSet<Proposer> ts = new TreeSet<Proposer>();
			ts.add(proposers.get(this.prng.nextInt(proposers.size())));
			return ts;
		}
		
		// Determine desired number of proposers.
		double d = this.prng.nextDouble();
		int noOfProps = 1;
		while (d > this.cumNoWeights[noOfProps-1]) { ++noOfProps; }
		
		// Compute an accumulated weight array for the current proposer weights.
		double[] accWeights = new double[proposers.size()];
		double tot = 0.0;
		for (int i = 0; i< accWeights.length; ++i) {
			tot += proposers.get(i).getWeight();
			accWeights[i] = tot;
		}
		for (int i = 0; i < accWeights.length; ++i) {
			accWeights[i] /= tot;
		}
		accWeights[accWeights.length - 1] = 1.0;   // For numeric safety.
		
		// Try to add proposers.
		TreeSet<Proposer> selProps = new TreeSet<Proposer>();
		TreeSet<StateParameter> selParams = new TreeSet<StateParameter>();
		int attempts = 0;
		while (attempts < MAX_NO_OF_ATTEMPTS && selProps.size() < noOfProps) {
			addProposer(selProps, selParams, proposers, accWeights);
			++attempts;
		}
		assert !selProps.isEmpty();
		
		return selProps;
	}

	/**
	 * Tries to add a proposer to the current selection.
	 * @param selProps the current selection of proposers.
	 * @param selParams the current selection of state parameters perturbed by selProps.
	 * @param props the list of all available proposers.
	 * @param accWeights the accumulated normalised weights of all proposers.
	 * @return true if a proposer was added; false if not added.
	 */
	private boolean addProposer(Set<Proposer> selProps, Set<StateParameter> selParams, List<Proposer> props, double[] accWeights) {
		// Select a pending proposer.
		double d = this.prng.nextDouble();
		int i = 0;
		while (d > accWeights[i]) { ++i; }
		Proposer p = props.get(i);
		
		// If not active, abort.
		if (!p.isEnabled()) {
			return false;
		}
		
		// If corresponding state parameters not already selected, add the proposer.
		for (StateParameter sp : p.getParameters()) {
			if (selParams.contains(sp)) {
				return false;
			}
		}
		selProps.add(p);
		for (StateParameter sp : p.getParameters()) {
			selParams.add(sp);
		}
		return true;
	}

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
	
}
