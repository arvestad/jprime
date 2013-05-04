package se.cbb.jprime.apps.genphylodata;

import java.util.LinkedList;
import java.util.List;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.io.GMLGraph;
import se.cbb.jprime.math.ExponentialDistribution;
import se.cbb.jprime.math.NumberManipulation;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.HybridGraph;
import se.cbb.jprime.topology.HybridGraph.VertexType;

/**
 * Creates unpruned trees evolving over a hybrid graph.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeInHybridGraphCreator implements UnprunedGuestTreeCreator {

	/** Host graph. */
	private HybridGraph hostGraph;
	
	/** Duplication rate. */
	private double lambda;
	
	/** Loss rate. */
	private double mu;
	
	/** Temporary duplication rate after hybridisation. */
	private double lambdaPostHyb;
	
	/** Temporary loss rate after hybridisation. */
	private double muPostHyb;
	
	/** Time span following hybridisation that lambda and mu are affected. */
	private double postHybTimespan;
	
	/** Sampling probability. */
	private double rho;
	
	/**
	 * Constructor.
	 * @param host host graph.
	 * @param lambda duplication rate.
	 * @param mu loss rate.
	 * @param rho probability of sampling leaf.
	 * @param lambdaChangeFact rate change factor.
	 * @param muChangeFact mu rate change factor.
	 * @param postHybTimespan rate change timespan.
	 * @param rho probability of sampling leaf.
	 * @throws TopologyException.
	 * @throws NewickIOException.
	 */
	public GuestTreeInHybridGraphCreator(GMLGraph host, double lambda, double mu, double lambdaChangeFact,
			double muChangeFact, double postHybTimespan, double rho, Double stem) {
		
		// Host graph.
		this.hostGraph = new HybridGraph(host, 2, 2, 0.1, 2);
		if (stem != null) {
			this.hostGraph.overrideStemArc(stem.doubleValue(), 2);
		}
		// Hack: Set 0 stem to eps.
		if (hostGraph.getStemArc().getArcTime() <= 0.0) {
			this.hostGraph.overrideStemArc(1.0e-64, 2);
		}
		
		// Rates.
		this.lambda = lambda;
		this.mu = mu;
		this.lambdaPostHyb = lambdaChangeFact * lambda;
		this.muPostHyb = muPostHyb * mu;
		this.postHybTimespan = postHybTimespan;
		this.rho = rho;
		if (lambda < 0 || mu < 0 || lambdaChangeFact < 0 || muChangeFact < 0 || postHybTimespan < 0) {
			throw new IllegalArgumentException("Cannot have rate or rate factor less than 0.");
		}
		if (rho < 0 || rho > 1) {
			throw new IllegalArgumentException("Cannot have leaf sampling probability outside [0,1].");
		}
	}
	
	
	@Override
	public GuestVertex createUnprunedTree(PRNG prng) {
		
		// Currently processed lineages.
		LinkedList<GuestVertex> alive = new LinkedList<GuestVertex>();
		
		// Single lineage starts at tip.
		int tip = this.hostGraph.getSource();
		int x = this.hostGraph.getChild(tip);
		GuestVertex root = this.createGuestVertex(x, this.hostGraph.getStemArc().getSourceTime(), prng);
		alive.add(root);
		
		// Recursively process lineages.
		while (!alive.isEmpty()) {
			GuestVertex lin = alive.pop();
			if (lin.event == Event.LOSS || lin.event == Event.LEAF || lin.event == Event.UNSAMPLED_LEAF) {
				// Lineage ends.
				continue;	
			}
			if (lin.event == Event.SPECIATION) {
				int[] sigmacs = hostGraph.getChildren(lin.sigma);
				GuestVertex lc = this.createGuestVertex(sigmacs[0], lin.abstime, prng);
				GuestVertex rc = this.createGuestVertex(sigmacs[1], lin.abstime, prng);
				lc.setParent(lin);
				rc.setParent(lin);
				lin.setChildren(lc, rc);
				alive.add(lc);
				alive.add(rc);
			} else if (lin.event == Event.DUPLICATION) {
				GuestVertex lc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
				GuestVertex rc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
				lc.setParent(lin);
				rc.setParent(lin);
				lin.setChildren(lc, rc);
				alive.add(lc);
				alive.add(rc);
			} else if (lin.event == Event.TRANSFER) {
				throw new UnsupportedOperationException("Transfers in hybrid host graphs are currently not supported.");
			} else if (lin.event == Event.HYBRID_DONATION) {
				int[] sigmacs = hostGraph.getChildren(lin.sigma);
				int hyb = sigmacs[0];
				int ch = sigmacs[1];
				if (hostGraph.getVertexType(hyb) != VertexType.ALLOPOLYPLOIDIC_HYBRID && 
						hostGraph.getVertexType(hyb) != VertexType.AUTOPOLYPLOIDIC_HYBRID) {
					// Other way around.
					hyb = sigmacs[1];
					ch = sigmacs[0];
				}
				double tdon = hostGraph.getVertexTime(lin.sigma);
				double thyb = hostGraph.getVertexTime(hyb);
				GuestVertex p;
				// We now either have alloploidic or autoploidic hybridisation.
				if (hostGraph.getVertexType(hyb) == VertexType.ALLOPOLYPLOIDIC_HYBRID) {
					p = new GuestVertex(Event.ALLOPLOIDIC_HYBRID_RECEPTION, hyb, null, tdon, tdon - thyb);
				} else if (hostGraph.getVertexType(hyb) == VertexType.AUTOPOLYPLOIDIC_HYBRID) {
					p = new GuestVertex(Event.AUTOPLOIDIC_HYBRID_RECEPTION, hyb, null, tdon, tdon - thyb);
				} else {
					throw new UnsupportedOperationException("Unexpected guest vertex type.");
				}
				GuestVertex c = this.createGuestVertex(ch, lin.abstime, prng);
				p.setParent(lin);
				c.setParent(lin);
				lin.setChildren(p, c);
				alive.add(p);
				alive.add(c);
			} else if (lin.event == Event.HYBRID_DONATION_FROM_EXTINCT_DONOR) {
				int hyb = hostGraph.getChildren(lin.sigma)[0];
				double tdon = hostGraph.getVertexTime(lin.sigma);
				double thyb = hostGraph.getVertexTime(hyb);
				GuestVertex p;
				// We now either have alloploidic or autoploidic hybridisation.
				if (hostGraph.getVertexType(hyb) == VertexType.ALLOPOLYPLOIDIC_HYBRID) {
					p = new GuestVertex(Event.ALLOPLOIDIC_HYBRID_RECEPTION, hyb, null, tdon, tdon - thyb);
				} else if (hostGraph.getVertexType(hyb) == VertexType.AUTOPOLYPLOIDIC_HYBRID) {
					p = new GuestVertex(Event.AUTOPLOIDIC_HYBRID_RECEPTION, hyb, null, tdon, tdon - thyb);
				} else {
					throw new UnsupportedOperationException("Unexpected guest vertex type.");
				}
				p.setParent(lin);
				lin.setChild(p);
				alive.add(p);
			} else if (lin.event == Event.ALLOPLOIDIC_HYBRID_RECEPTION) {
				GuestVertex c = this.createGuestVertex(hostGraph.getChild(lin.sigma), lin.abstime, prng);
				c.setParent(lin);
				lin.setChild(c);
				alive.add(c);
			} else if (lin.event == Event.AUTOPLOIDIC_HYBRID_RECEPTION) {
				int ch = hostGraph.getChild(lin.sigma);
				GuestVertex lc = this.createGuestVertex(ch, lin.abstime, prng);
				GuestVertex rc = this.createGuestVertex(ch, lin.abstime, prng);
				lc.setParent(lin);
				rc.setParent(lin);
				lin.setChildren(lc, rc);
				alive.add(lc);
				alive.add(rc);
			}
		}
		
		// Restore 0 length stem.
		if (root.getBranchLength() <= 1.0e-32) {
			root.setBranchLength(0.0);
		}
		
		return root;
	}
	
	/**
	 * Samples a guest vertex, given a process starting in host arc X at a given time.
	 * @param X host arc.
	 * @param startTime start time of process.
	 * @param prng PRNG.
	 * @return guest vertex.
	 */
	private GuestVertex createGuestVertex(int X, double startTime, PRNG prng) {
		double lowerTime = this.hostGraph.getVertexTime(X);
		int Xp = this.hostGraph.getParents(X)[0];
		VertexType Xptype = this.hostGraph.getVertexType(Xp);
		double upperTime = this.hostGraph.getVertexTime(Xp);
		ExponentialDistribution pd;
		boolean withinPostHyb;
		double branchTime;
		
		// If within the upper region after a hybridisation,
		// we need to sample according to different duplication-loss models.
		if ((Xptype == VertexType.ALLOPOLYPLOIDIC_HYBRID || Xptype == VertexType.AUTOPOLYPLOIDIC_HYBRID) && startTime >= upperTime - this.postHybTimespan) {
			// OK, we're within region with changed rate.
			pd = new ExponentialDistribution(Math.max(this.lambdaPostHyb + this.muPostHyb, 1e-48));
			branchTime = pd.sampleValue(prng);
			if (startTime - branchTime < upperTime - this.postHybTimespan) {
				// Sample again, according to usual rates.
				pd = new ExponentialDistribution(Math.max(this.lambda + this.mu, 1e-48));
				branchTime += pd.sampleValue(prng);
				withinPostHyb = false;
			} else {
				withinPostHyb = true;
			}
		} else {
			pd = new ExponentialDistribution(Math.max(this.lambda + this.mu, 1e-48));
			branchTime = pd.sampleValue(prng);
			withinPostHyb = false;
		}
		
		// Figure out event.
		double eventTime = startTime - branchTime;
		GuestVertex.Event event;
		if (eventTime <= lowerTime) {
			// LEAF, SPECIATION, HYBRID DONATION.
			eventTime = lowerTime;
			branchTime = NumberManipulation.roundToSignificantFigures(startTime - eventTime, 8);
			switch (this.hostGraph.getVertexType(X)) {
			case LEAF:
				event = (prng.nextDouble() < this.rho ? GuestVertex.Event.LEAF : GuestVertex.Event.UNSAMPLED_LEAF);
				break;
			case SPECIATION:
				event = GuestVertex.Event.SPECIATION;
				break;
			case HYBRID_DONOR:
				event = GuestVertex.Event.HYBRID_DONATION;
				break;
			case EXTINCT_HYBRID_DONOR:
				event = GuestVertex.Event.HYBRID_DONATION_FROM_EXTINCT_DONOR;
				break;
			default:
				throw new UnsupportedOperationException("Lineage evolved to unexpected event type.");
			}
		} else {
			// DUPLICATION OR LOSS.
			double rnd = prng.nextDouble();
			if (withinPostHyb) {
				event = rnd < this.lambdaPostHyb / (this.lambdaPostHyb + this.muPostHyb) ?
						GuestVertex.Event.DUPLICATION : GuestVertex.Event.LOSS;
			} else {
				event = rnd < this.lambda / (this.lambda + this.mu) ?
						GuestVertex.Event.DUPLICATION : GuestVertex.Event.LOSS;
			}
		}
		return new GuestVertex(event, X, null, eventTime, branchTime);
	}


	@Override
	public List<Integer> getHostLeaves() {
		return this.hostGraph.getLeaves();
	}
		
	@Override
	public String getInfo(GuestVertex guestRoot, boolean doML) {
		StringBuilder sb = new StringBuilder(1024);
		int noOfVertices = 0;
		int noOfLeaves = 0;
		int noOfSpecs = 0;
		int noOfHybDon = 0;
		int noOfHybDonExt = 0;
		int noOfDups = 0;
		int noOfLosses = 0;
		int noOfAllHyb = 0;
		int noOfAutoHyb = 0;
		double totalTime = 0.0;
		
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (guestRoot != null) {
			vertices.add(guestRoot);
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = vertices.pop();
			if (!v.isLeaf()) {
				vertices.add(v.getLeftChild());
				vertices.add(v.getRightChild());
			}
			noOfVertices++;
			totalTime += v.getBranchLength();
			switch (v.event) {
			case DUPLICATION:
				noOfDups++;
				break;
			case LOSS:
				noOfLosses++;
				break;
			case TRANSFER:
				throw new UnsupportedOperationException("Unexpected operation type.");
			case SPECIATION:
				noOfSpecs++;
				break;
			case HYBRID_DONATION:
				noOfHybDon++;
				break;
			case HYBRID_DONATION_FROM_EXTINCT_DONOR:
				noOfHybDonExt++;
				break;
			case ALLOPLOIDIC_HYBRID_RECEPTION:
				noOfAllHyb++;
				break;
			case AUTOPLOIDIC_HYBRID_RECEPTION:
				noOfAutoHyb++;
				break;
			case LEAF:
			case UNSAMPLED_LEAF:
				noOfLeaves++;
				break;
			}
		}
		totalTime = NumberManipulation.roundToSignificantFigures(totalTime, 8);
		
		sb.append("No. of vertices:\t").append(noOfVertices).append('\n');
		sb.append("No. of extant leaves:\t").append(noOfLeaves).append('\n');
		sb.append("No. of speciations:\t").append(noOfSpecs).append('\n');
		sb.append("No. of hybrid donations:\t").append(noOfHybDon).append('\n');
		sb.append("No. of hybrid donations from extinct donors:\t").append(noOfHybDonExt).append('\n');
		sb.append("No. of duplications:\t").append(noOfDups).append('\n');
		sb.append("No. of losses:\t").append(noOfLosses).append('\n');
		sb.append("No. of alloploidic hybrid receptions:\t").append(noOfAllHyb).append('\n');
		sb.append("No. of autoploidic hybrid receptions:\t").append(noOfAutoHyb).append('\n');
		sb.append("Total branch time:\t").append(totalTime).append('\n');
		if (doML) {
			double dupMLEst = NumberManipulation.roundToSignificantFigures(noOfDups / totalTime, 8);
			double lossMLEst = NumberManipulation.roundToSignificantFigures(noOfLosses / totalTime, 8);
			sb.append("Duplication ML estimate:\t").append(dupMLEst).append('\n');
			sb.append("Loss ML estimate:\t").append(lossMLEst).append('\n');
		}
		return sb.toString();
	}
	
	@Override
	public String getLeafMap(GuestVertex guestRoot) {
		StringBuilder sb = new StringBuilder(1024);
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (guestRoot != null) {
			vertices.add(guestRoot);
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = vertices.pop();
			if (!v.isLeaf()) {
				vertices.add(v.getLeftChild());
				vertices.add(v.getRightChild());
			} else {
				if (v.event == Event.LEAF || v.event == Event.UNSAMPLED_LEAF) {
					sb.append(v.getName()).append('\t').append(hostGraph.getVertexName(v.sigma)).append('\n');
				}
			}
		}
		return sb.toString();
	}
	
	
	@Override
	public String getSigma(GuestVertex guestRoot) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("# GUEST-TO-HOST MAP\n");
		sb.append("Host tree:\t").append(hostGraph.toString()).append('\n');
		sb.append("Guest vertex name:\tGuest vertex ID:\tGuest vertex type:\tGuest vertex time:\tHost vertex/arc ID:\n");
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (guestRoot != null) {
			vertices.add(guestRoot);
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = vertices.pop();
			if (!v.isLeaf()) {
				vertices.add(v.getLeftChild());
				vertices.add(v.getRightChild());
			}
			sb.append(v.getName()).append('\t');
			sb.append(v.getNumber()).append('\t');
			sb.append(v.event.toString()).append('\t');
			sb.append(v.abstime).append('\t');
			sb.append(v.sigma).append('\n');
		}
		return sb.toString();
	}


	@Override
	public String getHost() {
		return this.hostGraph.toString();
	}
}
