package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.NumberManipulation;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.TimesMap;

/**
 * Generates an ultrametric guest tree ("gene tree") involving inside a host tree ("species tree"),
 * by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeGen implements JPrIMEApp {

	private RBTreeEpochDiscretiser hostTree;
	private NamesMap hostNames;
	private PRNG prng;
	private EventCreator mightyGodPlaysDice;
	private int attempts = 0;
	private PrIMENewickTree unprunedTree;
	private PrIMENewickTree prunedTree;


	@Override
	public String getAppName() {
		return "GuestTreeGen";
	}

	@Override
	public void main(String[] args) throws Exception {
		
		try {
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			GuestTreeGenParameters params = new GuestTreeGenParameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help || params.args.size() != 5) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"GuestTreeGen is part of the JPrIME-GenPhyloData suite of tools for creating\n" +
						"realistic phylogenetic data. GuestTreeGen takes a Newick \"host\" tree with\n" +
						"ultrametric branch lengths and generates a \"guest\" tree evolving inside the\n" +
						"host tree. This is achieved through a canonical extension of a birth-death\n" +
						"process, in which guest tree lineages may be duplicated, lost, or laterally\n" +
						"transferred (be split with one copy being transferred to a contemporaneous host\n" +
						"edge). Guest lineages branch deterministically at host tree vertices. Auxiliary\n" +
						"files detailing the process is also created by default.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar GuestTreeGen [options] <host tree> <dup rate> <loss rate> <trans rate> <out prefix>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// Read host tree.
			PrIMENewickTree nw = params.getHostTree();
			RBTree S = new RBTree(nw, "HostTree");
			this.hostNames = nw.getVertexNamesMap(false, "HostNames");
			TimesMap STimes = nw.getTimesMap("HostTimes");
			if (params.stem != null) {
				STimes.getArcTimes()[S.getRoot()] = Double.parseDouble(params.stem);
			}
			// Hack: Set 0 stem to eps.
			if (STimes.getArcTime(S.getRoot()) <= 0.0) {
				STimes.getArcTimes()[S.getRoot()] = 1.0e-64;
			}
			hostTree = new RBTreeEpochDiscretiser(S, this.hostNames, STimes);
			
			// PRNG.
			prng = (params.seed == null ? new PRNG() : new PRNG(new BigInteger(params.seed)));
			
			// Helper.
			mightyGodPlaysDice = new EventCreator(hostTree, params.getDuplicationRate(), params.getLossRate(),
					params.getTransferRate(), Double.parseDouble(params.leafSamplingProb), prng);
			
			// Leaf size samples.
			ArrayList<Integer> leafSizes = params.getLeafSizes();
			
			// Produce tree.
			GuestVertex unprunedRoot;
			int exact = -1;
			do {
				if (attempts > params.maxAttempts) {
					if (!params.doQuiet) {
						BufferedWriter outinfo = params.getOutputFile(".info");
						outinfo.write("Failed to produce valid pruned tree within max allowed attempts.\n");
						outinfo.close();
					}
					System.err.println("Failed to produce valid pruned tree within max allowed attempts.");
					System.exit(0);
				}
				unprunedRoot = createRawUnprunedTree();
				int no = PruningHelper.labelUnprunableVertices(unprunedRoot, 0);
				PruningHelper.labelPrunableVertices(unprunedRoot, no, params.vertexPrefix);
				attempts++;
				if (leafSizes != null) {
					exact = leafSizes.get(this.prng.nextInt(leafSizes.size()));
				}
			} while (!isOK(unprunedRoot, hostTree, params.min, params.max, params.minper, params.maxper, exact));
			
			// Restore 0 length stem.
			if (unprunedRoot.getBranchLength() <= 1.0e-32) {
				unprunedRoot.setBranchLength(0.0);
			}
			
			// Meta.
			if (!params.excludeMeta) {
				GuestVertex.setMeta(unprunedRoot);
			}
			
			// Finally, some trees.
			GuestVertex prunedRoot = PruningHelper.prune(unprunedRoot);
			String treeMeta = (params.excludeMeta ? null : "[&&PRIME NAME=UnprunedTree]");
			this.unprunedTree = new PrIMENewickTree(new NewickTree(unprunedRoot, treeMeta, false, false), false);
			treeMeta = (params.excludeMeta ? null : "[&&PRIME NAME=PrunedTree]");
			this.prunedTree = (prunedRoot == null ?
					null : new PrIMENewickTree(new NewickTree(prunedRoot, treeMeta, false, false), false));
			
			// Print output.
			if (params.doQuiet) {
				if (params.excludeMeta) {
					System.out.println(this.prunedTree == null ? ";" : NewickTreeWriter.write(this.prunedTree));
				} else {
					System.out.println(this.prunedTree == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(this.prunedTree));
				}
			} else {
				BufferedWriter out = params.getOutputFile(".unpruned.tree");
				out.write(NewickTreeWriter.write(this.unprunedTree));
				out.close();
				out = params.getOutputFile(".pruned.tree");
				if (params.excludeMeta) {
					out.write(this.prunedTree == null ? ";" : NewickTreeWriter.write(this.prunedTree));
				} else {
					out.write(this.prunedTree == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(this.prunedTree));
				}
				out.close();
				out = params.getOutputFile(".unpruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + this.attempts + '\n');
				out.write(this.getInfo(this.unprunedTree, true));
				out.close();
				out = params.getOutputFile(".pruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + this.attempts + '\n');
				out.write(this.getInfo(this.prunedTree, false));
				out.close();
				if (!params.excludeMeta) {
					out = params.getOutputFile(".unpruned.guest2host");
					out.write(this.getSigma(this.unprunedTree));
					out.close();
					out = params.getOutputFile(".pruned.guest2host");
					out.write(this.getSigma(this.prunedTree));
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}

	/**
	 * Creates guest-to-host mapping info.
	 * @param tree the tree.
	 * @return the info.
	 */
	private String getSigma(PrIMENewickTree tree) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("# GUEST-TO-HOST MAP\n");
		sb.append("Host tree:\t").append(this.hostTree.toString()).append('\n');
		sb.append("Guest vertex name:\tGuest vertex ID:\tGuest vertex type:\tGuest vertex time:\tHost vertex/arc ID:\tHost epoch ID:\n");
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (tree != null) {
			vertices.add((GuestVertex) tree.getRoot());
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
			sb.append(v.sigma).append('\t');
			sb.append(v.epoch.getNo()).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Creates info.
	 * @param tree tree.
	 * @param doML include ML estimates.
	 * @return the info.
	 */
	private String getInfo(PrIMENewickTree tree, boolean doML) {
		StringBuilder sb = new StringBuilder(1024);
		int noOfVertices = 0;
		int noOfLeaves = 0;
		int noOfSpecs = 0;
		int noOfDups = 0;
		int noOfLosses = 0;
		int noOfTrans = 0;
		double totalTime = 0.0;
		double totalTimeBeneathStem = 0.0;
		
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (tree != null) {
			vertices.add((GuestVertex) tree.getRoot());
		}
		double hostRootTime = this.hostTree.getVertexTime(this.hostTree.getRoot());
		while (!vertices.isEmpty()) {
			GuestVertex v = vertices.pop();
			if (!v.isLeaf()) {
				vertices.add(v.getLeftChild());
				vertices.add(v.getRightChild());
			}
			noOfVertices++;
			totalTime += v.getBranchLength();
			totalTimeBeneathStem += Math.max(Math.min(hostRootTime-v.abstime, v.getBranchLength()), 0.0);
			switch (v.event) {
			case DUPLICATION:
				noOfDups++;
				break;
			case LOSS:
				noOfLosses++;
				break;
			case TRANSFER:
				noOfTrans++;
				break;
			case SPECIATION:
				noOfSpecs++;
				break;
			case LEAF:
			case UNSAMPLED_LEAF:
				noOfLeaves++;
				break;
			}
		}
		totalTime = NumberManipulation.roundToSignificantFigures(totalTime, 8);
		totalTimeBeneathStem = NumberManipulation.roundToSignificantFigures(totalTimeBeneathStem, 8);
		
		sb.append("No. of vertices:\t").append(noOfVertices).append('\n');
		sb.append("No. of extant leaves:\t").append(noOfLeaves).append('\n');
		sb.append("No. of speciations:\t").append(noOfSpecs).append('\n');
		sb.append("No. of duplications:\t").append(noOfDups).append('\n');
		sb.append("No. of losses:\t").append(noOfLosses).append('\n');
		sb.append("No. of transfers:\t").append(noOfTrans).append('\n');
		sb.append("Total branch time:\t").append(totalTime).append('\n');
		sb.append("Total branch time beneath host stem:\t").append(totalTimeBeneathStem).append('\n');
		if (doML) {
			double dupMLEst = NumberManipulation.roundToSignificantFigures(noOfDups / totalTime, 8);
			double lossMLEst = NumberManipulation.roundToSignificantFigures(noOfLosses / totalTime, 8);
			double transMLEst = NumberManipulation.roundToSignificantFigures(noOfTrans / totalTimeBeneathStem, 8);  // Excl. stem arcs.
			sb.append("Duplication ML estimate:\t").append(dupMLEst).append('\n');
			sb.append("Loss ML estimate:\t").append(lossMLEst).append('\n');
			sb.append("Transfer ML estimate:\t").append(transMLEst).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Creates an unpruned tree.
	 * @return the root.
	 */
	private GuestVertex createRawUnprunedTree() {
		LinkedList<GuestVertex> alive = new LinkedList<GuestVertex>();
				
		GuestVertex root = mightyGodPlaysDice.createGuestVertex(this.hostTree.getRoot(), this.hostTree.getTipToLeafTime());
		alive.add(root);
		while (!alive.isEmpty()) {
			GuestVertex lin = alive.pop();
			if (lin.event == Event.LOSS || lin.event == Event.LEAF || lin.event == Event.UNSAMPLED_LEAF) {
				continue;	
			}
			
			GuestVertex lc = null;
			GuestVertex rc = null;
			if (lin.event == Event.SPECIATION) {
				lc = mightyGodPlaysDice.createGuestVertex(this.hostTree.getLeftChild(lin.sigma), lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(this.hostTree.getRightChild(lin.sigma), lin.abstime);
			} else if (lin.event == Event.DUPLICATION) {
				lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
			} else if (lin.event == Event.TRANSFER) {
				if (this.prng.nextDouble() < 0.5) {
					lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
				} else {
					lc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				}
			}
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(2);
			children.add(lc);
			children.add(rc);
			lin.setChildren(children);
			lc.setParent(lin);
			rc.setParent(lin);
			alive.add(lc);
			alive.add(rc);
		}
		return root;
	}
	
	
	
	/**
	 * Validates.
	 * @param root guest tree root.
	 * @param hostTree host tree.
	 * @param min min sampled leaves.
	 * @param max max sampled leaves.
	 * @param minPer min sampled leaves per host leaf.
	 * @param maxPer max sampled leaves per host leaf.
	 * @param exact -1 if not applicable, otherwise exact number of leaves required.
	 * @return true if OK; otherwise false.
	 */
	private boolean isOK(GuestVertex root, RBTreeEpochDiscretiser hostTree, int min, int max, int minPer, int maxPer, int exact) {
		int sampledLeaves = 0;
		LinkedList<NewickVertex> vertices = new LinkedList<NewickVertex>();
		HashMap<Integer, Integer> sigmaCnt = new HashMap<Integer, Integer>(512); 
		if (root != null) {
			vertices.add(root);
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = (GuestVertex) vertices.pop();
			if (v.event == Event.LEAF) {
				sampledLeaves++;
				Integer cnt = sigmaCnt.get(v.sigma);
				if (cnt != null) {
					sigmaCnt.put(v.sigma, cnt + 1);
				} else {
					sigmaCnt.put(v.sigma, 1);
				}
			} else if (!v.isLeaf()) {
				vertices.addAll(v.getChildren());
			}
		}
		if (exact != -1 && sampledLeaves != exact) {
			return false;
		}
		if (sampledLeaves < min || sampledLeaves > max) {
			return false;
		}
		for (int l : hostTree.getLeaves()) {
			Integer cnt = sigmaCnt.get(l) ;
			if (cnt == null) { cnt = 0; }
			if (cnt < minPer || cnt > maxPer) {
				return false;
			}
		}
		return true;
	}
	
}
