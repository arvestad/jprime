package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.LinkedList;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.NumberManipulation;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;

/**
 * Generates an ultrametric guest tree ("gene tree") involving inside a host tree ("species tree"),
 * by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeGen implements JPrIMEApp {

	
	private GuestTree guestTree;
	
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
			int noargs = params.doQuiet ? 4 : 5;
			if (args.length == 0 || params.help || params.args.size() != noargs) {
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
						"files detailing the process are also created by default.\n\n" +
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
			PrIMENewickTree host = params.getHostTree();
			
			// Create guest tree.
			try {
				guestTree = new GuestTree(host, params.getStem(), params.seed, params.getDuplicationRate(),
						params.getLossRate(), params.getTransferRate(), params.getLeafSamplingProb(),
						params.min, params.max, params.minper, params.maxper,params.getLeafSizes(), params.maxAttempts,
						params.vertexPrefix, params.excludeMeta);
			} catch (MaxAttemptsException ex) {
				if (!params.doQuiet) {
					BufferedWriter outinfo = params.getOutputFile(".pruned.info");
					outinfo.write("Failed to produce valid pruned tree within max allowed attempts.\n");
					outinfo.close();
				}
				System.err.println("Failed to produce valid pruned tree within max allowed attempts.");
				System.exit(0);
			}
			
			// Print output.
			if (params.doQuiet) {
				if (params.excludeMeta) {
					System.out.println(guestTree.prunedTree == null ? ";" : NewickTreeWriter.write(guestTree.prunedTree));
				} else {
					System.out.println(guestTree.prunedTree == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(guestTree.prunedTree));
				}
			} else {
				BufferedWriter out = params.getOutputFile(".unpruned.tree");
				out.write(NewickTreeWriter.write(guestTree.unprunedTree) + '\n');
				out.close();
				out = params.getOutputFile(".pruned.tree");
				if (params.excludeMeta) {
					out.write(guestTree.prunedTree == null ? ";\n" : NewickTreeWriter.write(guestTree.prunedTree) + '\n');
				} else {
					out.write(guestTree.prunedTree == null ? "[&&PRIME NAME=PrunedTree];\n" : NewickTreeWriter.write(guestTree.prunedTree) + '\n');
				}
				out.close();
				out = params.getOutputFile(".unpruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + guestTree.attempts + '\n');
				out.write(this.getInfo(guestTree.unprunedTree, guestTree.hostTree, true));
				out.close();
				out = params.getOutputFile(".pruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + guestTree.attempts + '\n');
				out.write(this.getInfo(guestTree.prunedTree, guestTree.hostTree, false));
				out.close();
				if (!params.excludeMeta) {
					out = params.getOutputFile(".unpruned.guest2host");
					out.write(this.getSigma(guestTree.unprunedTree, guestTree.hostTree));
					out.close();
					out = params.getOutputFile(".pruned.guest2host");
					out.write(this.getSigma(guestTree.prunedTree, guestTree.hostTree));
					out.close();
					out = params.getOutputFile(".unpruned.leafmap");
					out.write(this.getLeafMap(guestTree.unprunedTree, guestTree.hostNames));
					out.close();
					out = params.getOutputFile(".pruned.leafmap");
					out.write(this.getLeafMap(guestTree.prunedTree, guestTree.hostNames));
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
	 * @param tree the guest tree.
	 * @param hostTree host tree.
	 * @return the info.
	 */
	private String getSigma(PrIMENewickTree tree, RBTreeEpochDiscretiser hostTree) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("# GUEST-TO-HOST MAP\n");
		sb.append("Host tree:\t").append(hostTree.toString()).append('\n');
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
	 * @param tree guest tree.
	 * @param hostTree host tree.
	 * @param doML include ML estimates.
	 * @return the info.
	 */
	private String getInfo(PrIMENewickTree tree, RBTreeEpochDiscretiser hostTree, boolean doML) {
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
		double hostRootTime = hostTree.getVertexTime(hostTree.getRoot());
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
	 * Creates leaf map.
	 * @param tree guest tree.
	 * @param hostNames host names.
	 * @return the leaf map.
	 */
	private String getLeafMap(PrIMENewickTree tree, NamesMap hostNames) {
		StringBuilder sb = new StringBuilder(1024);
		LinkedList<GuestVertex> vertices = new LinkedList<GuestVertex>();
		if (tree != null) {
			vertices.add((GuestVertex) tree.getRoot());
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = vertices.pop();
			if (!v.isLeaf()) {
				vertices.add(v.getLeftChild());
				vertices.add(v.getRightChild());
			} else {
				if (v.event == Event.LEAF || v.event == Event.UNSAMPLED_LEAF) {
					sb.append(v.getName()).append('\t').append(hostNames.get(v.sigma)).append('\n');
				}
			}
		}
		return sb.toString();
	}
}
