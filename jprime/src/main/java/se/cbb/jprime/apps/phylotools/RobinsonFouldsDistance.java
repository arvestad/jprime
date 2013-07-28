package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.util.List;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.consensus.day.RobinsonFoulds;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;

/**
 * Computes the Robinson-Foulds distance between trees. Two modes are supported:
 * <ol>
 * <li>One input file: Computes a symmetric matrix for the all-vs.-all comparison of the trees in the file.</li>
 * <li>Two input files: Assumes that the files are ordered for per-line comparisons of trees.</li>
 * </ol>
 * 
 * @author Joel Sjöstrand.
 */
public class RobinsonFouldsDistance implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "RobinsonFouldsDistance";
	}
	
	/**
	 * Starter.
	 * @param args.
	 */
	public void main(String[] args) {
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
						" 1) One input file: Computes a matrix of all-vs.-all tree comparisons for the\n" +
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
				List<NewickTree> trees = NewickTreeReader.readTrees(f, false);
				double[][] dists = RobinsonFoulds.computeDistanceMatrix(trees, params.unrooted);
				for (int i = 0; i < trees.size(); ++i) {
					for (int j = 0; j < trees.size(); ++j) {
						System.out.print("" + ((int)(dists[i][j])) + '\t');
					}
					System.out.print("\n");
				}
			} else if (params.infiles.size() == 2) {
				// Create pairwise comparisons.
				File f1 = new File(params.infiles.get(0));
				File f2 = new File(params.infiles.get(1));
				List<NewickTree> trees1 = NewickTreeReader.readTrees(f1, false);
				List<NewickTree> trees2 = NewickTreeReader.readTrees(f2, false);
				double[] dists = RobinsonFoulds.computePairedDistances(trees1, trees2, params.unrooted);
				for (double dist : dists) {
					System.out.println((int)dist);
				}
			} else {
				throw new IllegalArgumentException("Must have one or two input files.");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}

}
