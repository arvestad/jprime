package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.math.BigInteger;
import java.util.Arrays;
import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TimesMap;

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
			if (args.length == 0 || params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"GuestTreeGen is part of the JPrIME-GenPhyloData suite of tools for creating\n" +
						"realistic phylogenetic data. GuestTreeGen takes a Newick \"host\" tree with\n" +
						"ultrametric branch lengths and generates a \"guest\" tree evolving inside the\n" +
						"host tree. This is achieved through a canonical extension of a birth-death\n" +
						"process, in which guest tree lineages may be duplicated, lost, or be split with\n" +
						"one copy being laterally transferred to a contemporanous host edge. Guest\n" +
						"lineages branch deterministically at host tree vertices.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar GuestTreeGen [options] <host tree> <duplication rate> <loss rate> <transfer rate>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// Read host tree.
			PrIMENewickTree nw = params.getHostTree();
			RTree t = new RTree(nw, "HostTree");
			NamesMap names = nw.getVertexNamesMap(false, "HostNames");
			TimesMap hostTimes = nw.getTimesMap("HostTimes");
			
			// Get rates.
			double lambda = params.getDuplicationRate();
			double mu = params.getLossRate();
			double tau = params.getTransferRate();
			
			// PRNG.
			PRNG prng = (params.seed == null ? new PRNG() : new PRNG(new BigInteger(params.seed)));
			
			// TODO: Implement helpers to create the tree.
			
			String outtree = "";
			int noOfUnprunedSpecs;
			int noOfPrunedSpecs;
			int noOfUnprunedDups;
			int noOfPrunedDups;
			int noOfUnprunedLosses;
			int noOfPrunedLosses;
			int noOfUnprunedTrans;
			int noOfPrunedTrans;
			double unprunedTotalTime;
			double prunedTotalTime;
			
			
			
			// Output tree.
			if (params.outputfile == null) {
				System.out.println(outtree);
			} else {
				Pair<BufferedWriter, BufferedWriter> out = params.getOutputFiles();
				out.first.write(outtree + '\n');
				out.first.close();
				out.second.write("# GuestTreeGen\n");
				out.second.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.second.write("Host tree:\t" + params.args.get(0) + '\n');
				out.second.write("Duplication rate:\t" + lambda + '\n');
				out.second.write("Loss rate:\t" + mu + '\n');
				out.second.write("Transfer rate:\t" + tau + '\n');
				out.second.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}

	
}
