package se.cbb.jprime.apps.genphylodata;

import se.cbb.jprime.math.ExponentialDistribution;
import se.cbb.jprime.math.NumberManipulation;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.Epoch;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;

/**
 * Helps creation of events.
 * 
 * @author Joel Sjöstrand.
 */
public class EventCreator {

	/** Host tree. */
	private RBTreeEpochDiscretiser hostTree;
	
	/** Duplication rate. */
	private double lambda;
	
	/** Loss rate. */
	private double mu;
	
	/** Transfer rate. */
	private double tau;
	
	/** Sampling probability. */
	private double rho;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param hostTree host tree.
	 * @param lambda duplication rate.
	 * @param mu loss rate.
	 * @param tau transfer rate.
	 * @param rho probability of sampling leaf.
	 * @param prng PRNG.
	 */
	public EventCreator(RBTreeEpochDiscretiser hostTree, double lambda, double mu, double tau, double rho, PRNG prng) {
		this.hostTree = hostTree;
		this.lambda = lambda;
		this.mu = mu;
		this.tau = tau;
		this.rho = rho;
		this.prng = prng;
		if (lambda < 0 || mu < 0 || tau < 0) {
			throw new IllegalArgumentException("Cannot have rate less than 0.");
		}
		if (rho < 0 || rho > 1) {
			throw new IllegalArgumentException("Cannot have leaf sampling probability outside [0,1].");
		}
	}
	
	/**
	 * Samples a guest vertex, given a process starting in host arc X at a given time.
	 * @param X host arc.
	 * @param startTime start time of process.
	 * @return guest vertex.
	 */
	public GuestVertex createGuestVertex(int X, double startTime) {
		boolean isRoot = this.hostTree.isRoot(X);
		double sum = isRoot ? this.lambda + this.mu : this.lambda + this.mu + this.tau;
		if (sum == 0.0) { sum = 1e-48; }
		ExponentialDistribution pd = new ExponentialDistribution(sum);
		double lowerTime = this.hostTree.getVertexTime(X);
		double branchTime = pd.sampleValue(this.prng);
		double eventTime = startTime - branchTime;
		GuestVertex.Event event;
		Epoch epoch;
		if (eventTime <= lowerTime) {
			// LEAF OR SPECIATION.
			eventTime = lowerTime;
			branchTime = NumberManipulation.roundToSignificantFigures(startTime - eventTime, 8);
			if (this.hostTree.isLeaf(X)) {
				event = (this.prng.nextDouble() < this.rho ? GuestVertex.Event.LEAF : GuestVertex.Event.UNSAMPLED_LEAF);
			} else {
				event = GuestVertex.Event.SPECIATION;
			}
			epoch = this.hostTree.getEpochAbove(X);
		} else {
			// DUPLICATION, LOSS OR TRANSFER.
			double rnd = this.prng.nextDouble();
			if (isRoot) {
				if (rnd < this.lambda / sum) {
					event = GuestVertex.Event.DUPLICATION;
				} else {
					event = GuestVertex.Event.LOSS;
				}
			} else {
				if (rnd >= (this.mu + this.tau) / sum) {
					event = GuestVertex.Event.DUPLICATION;
				} else if (rnd < this.mu / sum) {
					event = GuestVertex.Event.LOSS;
				} else {
					event = GuestVertex.Event.TRANSFER;
				}
			}
			// Find correct epoch.
			int epno = this.hostTree.getEpochNoAbove(X);
			while (this.hostTree.getEpoch(epno).getUpperTime() < eventTime) {
				epno++;
			}
			epoch = this.hostTree.getEpoch(epno);
		}
		return new GuestVertex(event, X, epoch, eventTime, branchTime);
	}
}
