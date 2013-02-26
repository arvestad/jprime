package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.LinkedList;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.math.NumberManipulation;

/**
 * Generates a synthetic bifurcating tree ("species tree") by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class HostTreeGen implements JPrIMEApp {

	private GuestTree tree;
	
	@Override
	public String getAppName() {
		return "HostTreeGen";
	}

	@Override
	public void main(String[] args) throws Exception {
		
		try {
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			HostTreeGenParameters params = new HostTreeGenParameters();
			JCommander jc = new JCommander(params, args);
			int noargs = params.doQuiet ? 3 : 4;
			if (args.length == 0 || params.help || params.args.size() != noargs) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"HostTreeGen is part of the JPrIME-GenPhyloData suite of tools for creating\n" +
						"realistic phylogenetic data. HostTreeGen takes a time interval, a birth rate\n" +
						"and a death rate as input, and generates a bifurcating tree over the interval\n" +
						"by means of a birth-death process. Auxiliary files detailing the process are\n" +
						"also created by default.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar HostTreeGen [options] <time interval> <birth rate> <death rate> <out prefix>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// Create hostTree.
			double T = params.getT();
			String S;
			if (params.bifurcationStart) {
				// Bifurcating host tree.
				S = "(H0:" + T + ",H1:" + T + "):0.0;";
			} else {
				S = "H0:" + T + ";";
			}
			PrIMENewickTree host = PrIMENewickTreeReader.readTree(S, false, true);
			
			// Create guest tree.
			double lambda = params.getBirthRate();
			double mu = params.getDeathRate();
			double rho = params.getLeafSamplingProb();
			int minper = (params.bifurcationStart ? 1 : 0);
			try {
				
				tree = new GuestTree(host, null, params.seed, lambda,
						mu, 0.0, rho,
						params.min, params.max, minper, Integer.MAX_VALUE, params.getLeafSizes(), params.maxAttempts,
						params.vertexPrefix, params.excludeMeta);
			} catch (MaxAttemptsException ex) {
				if (!params.doQuiet) {
					BufferedWriter outinfo = params.getOutputFile(".info");
					outinfo.write("Failed to produce valid pruned tree within max allowed attempts.\n");
					outinfo.close();
				}
				System.err.println("Failed to produce valid pruned tree within max allowed attempts.");
				System.exit(0);
			}
			
			// Fix stem override.
			if (params.stem != null) {
				tree.unprunedTree.getRoot().setBranchLength(Double.parseDouble(params.stem));
				if (tree.prunedTree != null) {
					tree.prunedTree.getRoot().setBranchLength(Double.parseDouble(params.stem));
				}
			}
			
			// Print output.
			if (params.doQuiet) {
				if (params.excludeMeta) {
					System.out.println(tree.prunedTree == null ? ";" : NewickTreeWriter.write(tree.prunedTree));
				} else {
					System.out.println(tree.prunedTree == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(tree.prunedTree));
				}
			} else {
				BufferedWriter out = params.getOutputFile(".unpruned.tree");
				out.write(NewickTreeWriter.write(tree.unprunedTree) + '\n');
				out.close();
				out = params.getOutputFile(".pruned.tree");
				if (params.excludeMeta) {
					out.write(tree.prunedTree == null ? ";\n" : NewickTreeWriter.write(tree.prunedTree) + '\n');
				} else {
					out.write(tree.prunedTree == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(tree.prunedTree) + '\n');
				}
				out.close();
				out = params.getOutputFile(".unpruned.info");
				out.write("# HOSTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + tree.attempts + '\n');
				out.write(this.getInfo(tree.unprunedTree, true));
				out.close();
				out = params.getOutputFile(".pruned.info");
				out.write("# HOSTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + tree.attempts + '\n');
				out.write(this.getInfo(tree.prunedTree, false));
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
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
		int noOfBirths = 0;
		int noOfDeaths = 0;
		int noOfLeaves = 0;
		double totalTime = 0.0;
		
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
			noOfVertices++;
			totalTime += v.getBranchLength();
			switch (v.event) {
			case DUPLICATION:
			case SPECIATION:   // For the forced bifurcation case.
				noOfBirths++;
				break;
			case LOSS:
				noOfDeaths++;
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
		sb.append("No. of births:\t").append(noOfBirths).append('\n');
		sb.append("No. of deaths:\t").append(noOfDeaths).append('\n');
		sb.append("Total branch time:\t").append(totalTime).append('\n');
		if (doML) {
			double birthMLEst = NumberManipulation.roundToSignificantFigures(noOfBirths / totalTime, 8);
			double deathMLEst = NumberManipulation.roundToSignificantFigures(noOfDeaths / totalTime, 8);
			sb.append("Birth ML estimate:\t").append(birthMLEst).append('\n');
			sb.append("Death ML estimate:\t").append(deathMLEst).append('\n');
		}
		return sb.toString();
	}
}
