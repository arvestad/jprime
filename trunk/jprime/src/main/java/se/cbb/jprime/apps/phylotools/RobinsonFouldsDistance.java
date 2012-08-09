package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.beust.jcommander.JCommander;
import se.cbb.jprime.consensus.day.RobinsonFoulds;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * Computes the Robinson-Foulds distance between trees. Two modes are supported:
 * <ol>
 * <li>One input file: Computes a symmetric matrix for the all-vs.-all comparison of the trees in the file.</li>
 * <li>Two input files: Assumes that the files are ordered for per-line comparisons of trees.</li>
 * </ol>
 * 
 * @author Joel Sjöstrand.
 */
public class RobinsonFouldsDistance {
	
	/**
	 * Starter.
	 * @param args.
	 */
	public static void main(String[] args) {
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			
			RobinsonFouldsDistanceParameters params = new RobinsonFouldsDistanceParameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						" Computes the symmetric Robinson-Foulds distance between trees. Two modes are\n" +
						" supported:\n" +
						" 1) One input tree: Computes a matrix of all-vs.-all tree comparisons for the\n" +
						"    trees in the file. Output is a tab-delimited symmetric matrix.\n" +
						" 2) Two input files: Assumes that the files are ordered for paired comparisons\n" +
						"    of trees. Output is a list with the same number of lines.\n" +
						" Trees must be provided on the Newick format.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -cp jprime-X.Y.Z.jar se/cbb/jprime/apps/phylotools/RobinsonFouldsDistance [options] <infile> [infile2]\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}	
			
			
			// ================ COMPUTE DISTANCES ================
			
			if (params.infiles.size() == 1) {
				// Create matrix of comparisons.
				File f = new File(params.infiles.get(0));
				List<PrIMENewickTree> trees = PrIMENewickTreeReader.readTrees(f, false, false);
				computeDistanceMatrix(trees, params.unrooted);
			} else if (params.infiles.size() == 2) {
				// Create pairwise comparisons.
				File f1 = new File(params.infiles.get(0));
				File f2 = new File(params.infiles.get(1));
				List<PrIMENewickTree> trees1 = PrIMENewickTreeReader.readTrees(f1, false, false);
				List<PrIMENewickTree> trees2 = PrIMENewickTreeReader.readTrees(f2, false, false);
				computePairedDistances(trees1, trees2, params.unrooted);
			} else {
				throw new IllegalArgumentException("Must have one or two input files.");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}
	
	
	private static void computeDistanceMatrix(List<PrIMENewickTree> trees, boolean treatAsUnrooted) throws IOException, TopologyException {
		// Not optimised in the slightest way right now...
		for (PrIMENewickTree t1 : trees) {
			RTree r1 = new RTree(t1, "T1");
			NamesMap n1 = t1.getVertexNamesMap(true, "N1");
			StringBuilder sb = new StringBuilder(trees.size() * 4);
			for (PrIMENewickTree t2 : trees) {
				RTree r2 = new RTree(t1, "T2");
				NamesMap n2 = t2.getVertexNamesMap(true, "N2");
				int dist = RobinsonFoulds.computeDistance(r1, n1, r2, n2, treatAsUnrooted);
				sb.append(dist).append('\t');
			}
			System.out.println(sb.substring(0, sb.length() - 1));
		}
	}
	
	private static void computePairedDistances(List<PrIMENewickTree> trees1, List<PrIMENewickTree> trees2, boolean treatAsUnrooted) throws TopologyException, IOException {
		if (trees1.size() != trees2.size()) {
			throw new IllegalArgumentException("Input tree lists do not have equal length.");
		}
		for (int i = 0; i < trees1.size(); ++i) {
			RTree r1 = new RTree(trees1.get(i), "T1");
			NamesMap n1 = trees1.get(i).getVertexNamesMap(true, "N1");
			RTree r2 = new RTree(trees2.get(i), "T2");
			NamesMap n2 = trees2.get(i).getVertexNamesMap(true, "N2");
			int dist = RobinsonFoulds.computeDistance(r1, n1, r2, n2, treatAsUnrooted);
			System.out.println(dist);
		}
	}
	

}
