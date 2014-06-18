package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

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
	public ArrayList<Proposer> getDisjointProposers() {
		
		
		if (this.proposers == null || this.proposers.isEmpty()) {
			throw new IllegalArgumentException("Cannot select proposer from empty list.");
		}
		
//		// Special cases for speed.
//		if (this.proposers.size() == 1) {
//			System.out.println("I am here 1: ");
//			return new HashSet<Proposer>(this.proposers);
//		}
//		
//		if (this.cumNoWeights.length == 1) {
//			System.out.println("I am here 2: ");
//			HashSet<Proposer> ts = new HashSet<Proposer>(1);
//			ts.add(this.proposers.get(prng.nextInt(this.proposers.size())));
//			return ts;
//		}
		// Special cases for speed.
		if (this.proposers.size() == 1) {
			return new ArrayList<Proposer>(this.proposers);
		}
		
		if (this.cumNoWeights.length == 1) {
			ArrayList<Proposer> ts = new ArrayList<Proposer>(1);
			ts.add(this.proposers.get(prng.nextInt(this.proposers.size())));
			return ts;
		}
		
		
		// HERE IS THE PROBLEM OF RANDOM PERTURBATION
		//System.out.println("aaaaaaaaaaaaaaa:"+this.prng.nextDouble());
		// Determine desired number of proposers.
		double d;
		d= prng.nextDouble();
		
		
		int noOfProps = 1;
		while (d > this.cumNoWeights[noOfProps-1]) { ++noOfProps; }
		//System.out.println("noOfProps: "+ noOfProps);
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
//		HashSet<Proposer> selProps = new HashSet<Proposer>(noOfProps);
//		HashSet<StateParameter> selParams = new HashSet<StateParameter>(noOfProps * 2);
		ArrayList<Proposer> selProps = new ArrayList<Proposer>(noOfProps);
		ArrayList<StateParameter> selParams = new ArrayList<StateParameter>(noOfProps * 2);
		int attempts = 0;

		while (attempts < MAX_NO_OF_ATTEMPTS && selProps.size() < noOfProps) {
			// HERE IS THE PROBLEM
			addProposer(selProps, selParams, this.proposers, accWeights, prng);
			++attempts;
		}
//		while (selProps.size() < noOfProps) {
//			// HERE IS THE PROBLEM
//			System.out.println("selProps.size():\t"+selProps.size()+ "\tnoOfProps:"+ noOfProps);
//			if (addProposer(selProps, selParams, this.proposers, accWeights, prng)){
//				System.out.println(selProps.toString()+ " is added");
//			}else{ System.out.println("No proposer added is added");}
//			++attempts;
//		}

		//System.out.println("attempts:"+ attempts);
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
	private boolean addProposer(ArrayList<Proposer> selProps, ArrayList<StateParameter> selParams, List<Proposer> props, double[] accWeights, PRNG prng) {
		// Select a pending proposer.
		double d = prng.nextDouble();
		
		int i = 0;
		while (d > accWeights[i]) { ++i; /*System.out.println(i);*/ }
		//System.out.println();
		Proposer p = props.get(i);
		//System.out.println(p.toString());
		
		// If not active, abort.
		if (!p.isEnabled()) {
			return false;
		}

		//System.out.println("p.getParameters"+ p.getParameters().toString());
		
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
