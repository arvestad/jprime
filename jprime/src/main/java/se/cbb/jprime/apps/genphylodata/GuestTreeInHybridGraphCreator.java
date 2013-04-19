//package se.cbb.jprime.apps.genphylodata;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
//import se.cbb.jprime.io.GMLGraph;
//import se.cbb.jprime.io.NewickIOException;
//import se.cbb.jprime.io.NewickVertex;
//import se.cbb.jprime.io.PrIMENewickTree;
//import se.cbb.jprime.math.ExponentialDistribution;
//import se.cbb.jprime.math.NumberManipulation;
//import se.cbb.jprime.math.PRNG;
//import se.cbb.jprime.topology.DiscretisedArc;
//import se.cbb.jprime.topology.Epoch;
//import se.cbb.jprime.topology.HybridGraph;
//import se.cbb.jprime.topology.HybridGraph.VertexType;
//import se.cbb.jprime.topology.NamesMap;
//import se.cbb.jprime.topology.RBTree;
//import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
//import se.cbb.jprime.topology.TimesMap;
//import se.cbb.jprime.topology.TopologyException;
//
///**
// * Creates unpruned trees evolving over a hybrid graph.
// * 
// * @author Joel Sjöstrand.
// */
//public class GuestTreeInHybridGraphCreator implements UnprunedGuestTreeCreator {
//
//	/** Host graph. */
//	private HybridGraph hostGraph;
//	
//	/** Duplication rate. */
//	private double lambda;
//	
//	/** Loss rate. */
//	private double mu;
//	
//	/** Duplication rate change factor following hybridisation. */
//	private double lambdaChangeFact;
//	
//	/** Loss rate change factor following hybridisation. */
//	private double muChangeFact;
//	
//	/** Time span following hybridisation that lambda and mu are affected. */
//	private double rateChangeTimespan;
//	
//	/** Sampling probability. */
//	private double rho;
//	
//	/**
//	 * Constructor.
//	 * @param host host graph.
//	 * @param lambda duplication rate.
//	 * @param mu loss rate.
//	 * @param rho probability of sampling leaf.
//	 * @param lambdaChangeFact rate change factor.
//	 * @param muChangeFact mu rate change factor.
//	 * @param rateChangeTimespan rate change timespan.
//	 * @param rho probability of sampling leaf.
//	 * @throws TopologyException.
//	 * @throws NewickIOException.
//	 */
//	public GuestTreeInHybridGraphCreator(GMLGraph host, double lambda, double mu, double lambdaChangeFact,
//			double muChangeFact, double rateChangeTimespan, double rho, Double stem) {
//		
//		// Host graph.
//		this.hostGraph = new HybridGraph(host, 2, 2, 0.1, 2);
//		if (stem != null) {
//			this.hostGraph.overrideStemArc(stem.doubleValue(), 2);
//		}
//		// Hack: Set 0 stem to eps.
//		if (hostGraph.getStemArc().getArcTime() <= 0.0) {
//			this.hostGraph.overrideStemArc(1.0e-64, 2);
//		}
//		
//		// Rates.
//		this.lambda = lambda;
//		this.mu = mu;
//		this.lambdaChangeFact = lambdaChangeFact;
//		this.muChangeFact = muChangeFact;
//		this.rateChangeTimespan = rateChangeTimespan;
//		this.rho = rho;
//		if (lambda < 0 || mu < 0 || lambdaChangeFact < 0 || muChangeFact < 0 || rateChangeTimespan < 0) {
//			throw new IllegalArgumentException("Cannot have rate or rate factor less than 0.");
//		}
//		if (rho < 0 || rho > 1) {
//			throw new IllegalArgumentException("Cannot have leaf sampling probability outside [0,1].");
//		}
//	}
//	
//	
//	@Override
//	public GuestVertex createUnprunedTree(PRNG prng) {
//		
//		// Currently processed lineages.
//		LinkedList<GuestVertex> alive = new LinkedList<GuestVertex>();
//		
//		// Single lineage starts at tip.
//		int tip = this.hostGraph.getSource();
//		int x = this.hostGraph.getChild(0);
//		GuestVertex root = this.createGuestVertex(x, this.hostGraph.getStemArc().getSourceTime(), prng);
//		alive.add(root);
//		
//		// Recursively process lineages.
//		while (!alive.isEmpty()) {
//			GuestVertex lin = alive.pop();
//			if (lin.event == Event.LOSS || lin.event == Event.LEAF || lin.event == Event.UNSAMPLED_LEAF) {
//				// Lineage ends.
//				continue;	
//			}
//			
//			if (lin.event == Event.SPECIATION) {
//				// Five scenarios:
//				//   1) Ordinary speciation.
//				//   2) Speciation leading to alloploidic hybrid.
//				//   3) Speciation leading to alloploidic hybrid where the donor goes extinct.
//				//   4) Speciation leading to autoploidic hybrid.
//				//   5) Speciation leading to autoploidic hybrid where the donor goes extinct.
//				// In 4) and 5), the polyploidisation is modelled as a particular kind of duplication event in the
//				// hybridisation vertex.
//				int[] ch = hostGraph.getChildren(lin.sigma);
//				if (hostGraph.getVertexType(ch[0]) == VertexType.EXTINCT_HYBRID_DONOR) {
//					int hyb = hostGraph.getChild(ch[0]);
//					int hybch = hostGraph.getChild(hyb);
//					if (hostGraph.getVertexType(hyb) == VertexType.ALLOPOLYPLOIDIC_HYBRID) {
//						// Case 3).
//						GuestVertex c = this.createGuestVertex(hostTree.getLeftChild(lin.sigma), lin.abstime, prng);
//					}
//					
//					
//				}
//				
//				lc = 
//				rc = this.createGuestVertex(hostTree.getRightChild(lin.sigma), lin.abstime, prng);
//			} else if (lin.event == Event.DUPLICATION) {
//				lc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
//				rc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
//			} else if (lin.event == Event.TRANSFER) {
//				if (prng.nextDouble() < 0.5) {
//					lc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
//					rc = this.createGuestVertex(lin.epoch.sampleArc(prng, lin.sigma), lin.abstime, prng);
//				} else {
//					lc = this.createGuestVertex(lin.epoch.sampleArc(prng, lin.sigma), lin.abstime, prng);
//					rc = this.createGuestVertex(lin.sigma, lin.abstime, prng);
//				}
//			}
//			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(2);
//			children.add(lc);
//			children.add(rc);
//			lin.setChildren(children);
//			lc.setParent(lin);
//			rc.setParent(lin);
//			alive.add(lc);
//			alive.add(rc);
//		}
//		
//		// Restore 0 length stem.
//		if (root.getBranchLength() <= 1.0e-32) {
//			root.setBranchLength(0.0);
//		}
//		
//		return root;
//	}
//	
//	
//	/**
//	 * Samples a guest vertex, given a process starting in host arc X at a given time.
//	 * @param X host arc.
//	 * @param startTime start time of process.
//	 * @param prng PRNG.
//	 * @return guest vertex.
//	 */
//	private GuestVertex createGuestVertex(int X, double startTime, PRNG prng) {
//		boolean isRoot = this.hostTree.isRoot(X);
//		double sum = isRoot ? this.lambda + this.mu : this.lambda + this.mu + this.tau;
//		if (sum == 0.0) { sum = 1e-48; }
//		ExponentialDistribution pd = new ExponentialDistribution(sum);
//		double lowerTime = this.hostTree.getVertexTime(X);
//		double branchTime = pd.sampleValue(prng);
//		double eventTime = startTime - branchTime;
//		GuestVertex.Event event;
//		Epoch epoch;
//		if (eventTime <= lowerTime) {
//			// LEAF OR SPECIATION.
//			eventTime = lowerTime;
//			branchTime = NumberManipulation.roundToSignificantFigures(startTime - eventTime, 8);
//			if (this.hostTree.isLeaf(X)) {
//				event = (prng.nextDouble() < this.rho ? GuestVertex.Event.LEAF : GuestVertex.Event.UNSAMPLED_LEAF);
//			} else {
//				event = GuestVertex.Event.SPECIATION;
//			}
//			epoch = this.hostTree.getEpochAbove(X);
//		} else {
//			// DUPLICATION, LOSS OR TRANSFER.
//			double rnd = prng.nextDouble();
//			if (isRoot) {
//				if (rnd < this.lambda / sum) {
//					event = GuestVertex.Event.DUPLICATION;
//				} else {
//					event = GuestVertex.Event.LOSS;
//				}
//			} else {
//				if (rnd >= (this.mu + this.tau) / sum) {
//					event = GuestVertex.Event.DUPLICATION;
//				} else if (rnd < this.mu / sum) {
//					event = GuestVertex.Event.LOSS;
//				} else {
//					event = GuestVertex.Event.TRANSFER;
//				}
//			}
//			// Find correct epoch.
//			int epno = this.hostTree.getEpochNoAbove(X);
//			while (this.hostTree.getEpoch(epno).getUpperTime() < eventTime) {
//				epno++;
//			}
//			epoch = this.hostTree.getEpoch(epno);
//		}
//		return new GuestVertex(event, X, epoch, eventTime, branchTime);
//	}
//
//
//	@Override
//	public List<Integer> getHostLeaves() {
//		return this.hostTree.getLeaves();
//	}
//		
//	@Override
//	public String getInfo(GuestVertex guestRoot, boolean doML) {
//		StringBuilder sb = new StringBuilder(1024);
//		int noOfVertices = 0;
//		int noOfLeaves = 0;
//		int noOfSpecs = 0;
//		int noOfDups = 0;
//		int noOfLosses = 0;
//		int noOfTrans = 0;
//		double totalTime = 0.0;
//		double totalTimeBeneathStem = 0.0;
//		
//		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
//		if (guestRoot != null) {
//			vertices.add(guestRoot);
//		}
//		double hostRootTime = hostTree.getVertexTime(hostTree.getRoot());
//		while (!vertices.isEmpty()) {
//			GuestVertex v = vertices.pop();
//			if (!v.isLeaf()) {
//				vertices.add(v.getLeftChild());
//				vertices.add(v.getRightChild());
//			}
//			noOfVertices++;
//			totalTime += v.getBranchLength();
//			totalTimeBeneathStem += Math.max(Math.min(hostRootTime - v.abstime, v.getBranchLength()), 0.0);
//			switch (v.event) {
//			case DUPLICATION:
//				noOfDups++;
//				break;
//			case LOSS:
//				noOfLosses++;
//				break;
//			case TRANSFER:
//				noOfTrans++;
//				break;
//			case SPECIATION:
//				noOfSpecs++;
//				break;
//			case LEAF:
//			case UNSAMPLED_LEAF:
//				noOfLeaves++;
//				break;
//			}
//		}
//		totalTime = NumberManipulation.roundToSignificantFigures(totalTime, 8);
//		totalTimeBeneathStem = NumberManipulation.roundToSignificantFigures(totalTimeBeneathStem, 8);
//		
//		sb.append("No. of vertices:\t").append(noOfVertices).append('\n');
//		sb.append("No. of extant leaves:\t").append(noOfLeaves).append('\n');
//		sb.append("No. of speciations:\t").append(noOfSpecs).append('\n');
//		sb.append("No. of duplications:\t").append(noOfDups).append('\n');
//		sb.append("No. of losses:\t").append(noOfLosses).append('\n');
//		sb.append("No. of transfers:\t").append(noOfTrans).append('\n');
//		sb.append("Total branch time:\t").append(totalTime).append('\n');
//		sb.append("Total branch time beneath host stem:\t").append(totalTimeBeneathStem).append('\n');
//		if (doML) {
//			double dupMLEst = NumberManipulation.roundToSignificantFigures(noOfDups / totalTime, 8);
//			double lossMLEst = NumberManipulation.roundToSignificantFigures(noOfLosses / totalTime, 8);
//			double transMLEst = NumberManipulation.roundToSignificantFigures(noOfTrans / totalTimeBeneathStem, 8);  // Excl. stem-spanning arcs.
//			sb.append("Duplication ML estimate:\t").append(dupMLEst).append('\n');
//			sb.append("Loss ML estimate:\t").append(lossMLEst).append('\n');
//			sb.append("Transfer ML estimate:\t").append(transMLEst).append('\n');
//		}
//		return sb.toString();
//	}
//	
//	@Override
//	public String getLeafMap(GuestVertex guestRoot) {
//		StringBuilder sb = new StringBuilder(1024);
//		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
//		if (guestRoot != null) {
//			vertices.add(guestRoot);
//		}
//		while (!vertices.isEmpty()) {
//			GuestVertex v = vertices.pop();
//			if (!v.isLeaf()) {
//				vertices.add(v.getLeftChild());
//				vertices.add(v.getRightChild());
//			} else {
//				if (v.event == Event.LEAF || v.event == Event.UNSAMPLED_LEAF) {
//					sb.append(v.getName()).append('\t').append(hostNames.get(v.sigma)).append('\n');
//				}
//			}
//		}
//		return sb.toString();
//	}
//	
//	
//	@Override
//	public String getSigma(GuestVertex guestRoot) {
//		StringBuilder sb = new StringBuilder(4096);
//		sb.append("# GUEST-TO-HOST MAP\n");
//		sb.append("Host tree:\t").append(hostTree.toString()).append('\n');
//		sb.append("Guest vertex name:\tGuest vertex ID:\tGuest vertex type:\tGuest vertex time:\tHost vertex/arc ID:\tHost epoch ID:\n");
//		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
//		if (guestRoot != null) {
//			vertices.add(guestRoot);
//		}
//		while (!vertices.isEmpty()) {
//			GuestVertex v = vertices.pop();
//			if (!v.isLeaf()) {
//				vertices.add(v.getLeftChild());
//				vertices.add(v.getRightChild());
//			}
//			sb.append(v.getName()).append('\t');
//			sb.append(v.getNumber()).append('\t');
//			sb.append(v.event.toString()).append('\t');
//			sb.append(v.abstime).append('\t');
//			sb.append(v.sigma).append('\t');
//			sb.append(v.epoch.getNo()).append('\n');
//		}
//		return sb.toString();
//	}
//
//
//	@Override
//	public String getHost() {
//		return this.hostTree.toString();
//	}
//}
