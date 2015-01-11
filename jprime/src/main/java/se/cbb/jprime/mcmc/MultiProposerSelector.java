package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import se.cbb.jprime.math.PRNG;

/**
 * Selects one or more proposers (acting on disjoint state parameters).
 * The user may specify a desired weight set for how often more than
 * one proposer should be attempted to be invoked, e.g. [0.60,0.30,0.10] for
 * 1 proposer 60% of the time, 2 proposers 30% of the time and 3 proposers
 * 10% of the time. However, there is no guarantee that exactly these
 * numbers will be achieved in practice (due to that they may
 * act on the same parameters).
 * 
 * @author Joel Sjöstrand.
 */
public class MultiProposerSelector implements ProposerSelector {

	/** The maximum number of attempts at trying to add another proposer when selecting. */
	public static final int MAX_NO_OF_ATTEMPTS = 100;
	
	/** All proposers to choose from. */
	private ArrayList<Proposer> proposers;
	
	/** Weight for each proposer. */
	private ArrayList<ProposerWeight> weights;
	
	/** PRNG. */
	private PRNG prng;
	
	/** Cumulative number-of-proposers weights, normalised as [0,...,1]. Null if not used. */
	private double[] cumNoWeights;
	
	/**
	 * Creates an instance where only one proposer at a time is invoked.
	 * @param prng the PRNG used for random selection.
	 */
	public MultiProposerSelector(PRNG prng) {
		this.proposers = new ArrayList<Proposer>(16);
		this.weights = new ArrayList<ProposerWeight>(16);
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
		this.proposers = new ArrayList<Proposer>(16);
		this.weights = new ArrayList<ProposerWeight>(16);
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
	
	/**
	 * Adds a proposer to the available set to draw from.
	 * @param proposer the proposer to add.
	 */
	public void add(Proposer proposer, ProposerWeight weight) {
		this.proposers.add(proposer);
		this.weights.add(weight);
	}
	
	@Override
	public Set<Proposer> getDisjointProposers() {
		if (this.proposers == null || this.proposers.isEmpty()) {
			throw new IllegalArgumentException("Cannot select proposer from empty list.");
		}
		
		// Special cases for speed.
		if (this.proposers.size() == 1) {
			return new HashSet<Proposer>(this.proposers);
		}
		if (this.cumNoWeights.length == 1) {
			HashSet<Proposer> ts = new HashSet<Proposer>(1);
			ts.add(this.proposers.get(this.prng.nextInt(this.proposers.size())));
			return ts;
		}
		
		// Determine desired number of proposers.
		double d = this.prng.nextDouble();
		int noOfProps = 1;
		while (d > this.cumNoWeights[noOfProps-1]) { ++noOfProps; }
		
		// Compute an accumulated weight array for the current proposer weights.
		double[] accWeights = new double[this.proposers.size()];
		double tot = 0.0;
		for (int i = 0; i < accWeights.length; ++i) {
			tot += this.weights.get(i).getValue();
			accWeights[i] = tot;
		}
		for (int i = 0; i < accWeights.length; ++i) {
			accWeights[i] /= tot;
		}
		accWeights[accWeights.length - 1] = 1.0;   // For numeric safety.
		
		// Try to add proposers.
		HashSet<Proposer> selProps = new HashSet<Proposer>(noOfProps);
		HashSet<StateParameter> selParams = new HashSet<StateParameter>(noOfProps * 2);
		int attempts = 0;
		while (attempts < MAX_NO_OF_ATTEMPTS && selProps.size() < noOfProps) {
			addProposer(selProps, selParams, this.proposers, accWeights);
			++attempts;
		}
		assert !selProps.isEmpty();
		
		return selProps;
	}
	
	
	public Set<Proposer> getGeneTreeProposers() {
		HashSet<Proposer> selProps = new HashSet<Proposer>(1);
		Proposer p = this.proposers.get(8);
		selProps.add(p);
//		p = this.proposers.get(9);
//		selProps.add(p);
//		for(int i=0; i<5; i++){
//			p = this.proposers.get(i);
//			selProps.add(p);
//		}
		selProps.add(p);
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
//		if(p.toString().contains("Point"))
//			System.out.println();
		// NormalProposer perturbing KappaRate, NormalProposer perturbing OmegaRate, NormalProposer perturbing DuplicationRate, NormalProposer perturbing LossRate, NormalProposer perturbing PseudogenizationRate, NormalProposer perturbing EdgeRateMean, NormalProposer perturbing EdgeRateCV, NormalProposer perturbing SiteRateShape, RBTreeBranchSwapper perturbing [GuestTree, BranchLengths], PerturbPseudoPoints perturbing [G-PGSwitches], NormalProposer perturbing BranchLengths
		//p.toString().contains("Omega") || p.toString().contains("Kappa") || p.toString().contains("PseudogenizationRate") || p.toString().contains("Point") || p.toString().contains("BranchLengths")
		// p.toString().contains("Omega") || p.toString().contains("Kappa") || p.toString().contains("DuplicationRate") || p.toString().contains("LossRate") || p.toString().contains("NormalProposer perturbing BranchLengths") || p.toString().contains("EdgeRateMean")
		// p.toString().contains("Point") || p.toString().contains("PseudogenizationRate")    || p.toString().contains("Omega") || p.toString().contains("Kappa") || p.toString().contains("DuplicationRate") || p.toString().contains("LossRate")  || p.toString().contains("EdgeRateCV") || p.toString().contains("SiteRateShape")  || p.toString().contains("EdgeRateMean") ||   p.toString().contains("Point")
		
//		if (!(   p.toString().contains("BranchLengths")    )){
		selProps.add(p);
		for (StateParameter sp : p.getParameters()) {
			selParams.add(sp);
		}
		return true;
//		}else
//		{
//			Proposer p1 = this.proposers.get(8);
//			selProps.add(p1);
//			for (StateParameter sp : p1.getParameters()) {
//				selParams.add(sp);
//			}
//			return true;
//		}
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("MULTI-PROPOSER SELECTOR\n");
		sb.append(prefix).append("Cumulative no.-of-proposer weights: ").append(Arrays.toString(this.cumNoWeights)).append('\n');
		for (int i = 0; i < this.proposers.size(); ++i) {
			sb.append(prefix).append("Proposer ").append(i+1).append(":\n");
			sb.append(this.proposers.get(i).getPreInfo(prefix + '\t'));
			sb.append(prefix).append("Proposer ").append(i+1).append("'s weight:\n");
			sb.append(this.weights.get(i).getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("MULTI-PROPOSER SELECTOR\n");
		for (int i = 0; i < this.proposers.size(); ++i) {
			sb.append(prefix).append("Proposer ").append(i+1).append(":\n");
			sb.append(this.proposers.get(i).getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}
	
}
