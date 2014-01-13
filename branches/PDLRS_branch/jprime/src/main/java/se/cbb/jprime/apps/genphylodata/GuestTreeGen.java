package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.util.Arrays;
import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.misc.Pair;

/**
 * Generates an ultrametric guest tree ("gene tree") involving inside a host tree ("species tree"),
 * by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeGen implements JPrIMEApp {
	
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
						"transferred (i.e., be split with one copy being transferred to a contemporaneous\n" +
						"host edge). Guest lineages branch deterministically at host tree vertices.\n" +
						"Auxiliary files detailing the process are also created by default.\n\n" +
						"It is now also possible to generate gene trees over hybrid graphs. This is\n" +
						"covered in more detail in the online tutorial.\n\n" +
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
			
			// Machine.
			GuestTreeMachina machina = new GuestTreeMachina(params.seed, params.min, params.max, params.minper, params.maxper, params.getLeafSizes(), params.maxAttempts,
					params.vertexPrefix, params.excludeMeta, params.appendSigma);
			
			// Machine motor.
			UnprunedGuestTreeCreator motor = (params.hybrid == null || params.hybrid.isEmpty()) ? params.getHostTreeCreator() : params.getHostHybridGraphCreator();
			
			// Create guest tree.
			Pair<PrIMENewickTree, PrIMENewickTree> guestTree = null;
			try {
				guestTree  = machina.sampleGuestTree(motor);
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
					System.out.println(guestTree.first == null ? ";" : NewickTreeWriter.write(guestTree.first));
				} else {
					System.out.println(guestTree.first == null ? "[&&PRIME NAME=PrunedTree];" : NewickTreeWriter.write(guestTree.first));
				}
			} else {
				BufferedWriter out = params.getOutputFile(".unpruned.tree");
				out.write(NewickTreeWriter.write(guestTree.second) + '\n');
				out.close();
				out = params.getOutputFile(".pruned.tree");
				if (params.excludeMeta) {
					out.write(guestTree.first == null ? ";\n" : NewickTreeWriter.write(guestTree.first) + '\n');
				} else {
					out.write(guestTree.first == null ? "[&&PRIME NAME=PrunedTree];\n" : NewickTreeWriter.write(guestTree.first) + '\n');
				}
				out.close();
				out = params.getOutputFile(".unpruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + machina.getAttempts() + '\n');
				out.write(motor.getInfo((GuestVertex) guestTree.second.getRoot(), true));
				out.close();
				out = params.getOutputFile(".pruned.info");
				out.write("# GUESTTREEGEN\n");
				out.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.write("Attempts:\t" + machina.getAttempts() + '\n');
				out.write(motor.getInfo((GuestVertex) guestTree.first.getRoot(), true));
				out.close();
				if (!params.excludeMeta) {
					out = params.getOutputFile(".unpruned.guest2host");
					out.write(motor.getSigma((GuestVertex) guestTree.second.getRoot()));
					out.close();
					out = params.getOutputFile(".pruned.guest2host");
					out.write(motor.getSigma((GuestVertex) guestTree.first.getRoot()));
					out.close();
					out = params.getOutputFile(".unpruned.leafmap");
					out.write(motor.getLeafMap((GuestVertex) guestTree.second.getRoot()));
					out.close();
					out = params.getOutputFile(".pruned.leafmap");
					out.write(motor.getLeafMap((GuestVertex) guestTree.first.getRoot()));
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}

}
