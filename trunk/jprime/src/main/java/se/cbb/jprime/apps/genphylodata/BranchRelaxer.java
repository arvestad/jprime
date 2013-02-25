package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.StringMap;

/**
 * Relaxes branch lengths for a tree by imposing IID rates or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class BranchRelaxer implements JPrIMEApp {
	
	/** Attempts. */
	private int attempts = 0;
	
	@Override
	public String getAppName() {
		return "BranchRelaxer";
	}

//	@Test
//	public void test() {
//		String[] args = new String[] { "(A:0.5,B:0.5):0.3;", "IIDNormal", "100.0", "2.0" };
//		try {
//			main(args);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public void main(String[] args) throws Exception {
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			BranchRelaxerParameters params = new BranchRelaxerParameters();
			JCommander jc = new JCommander(params, args);
			if (params.help || args.length < 1) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"BranchRelaxer is part of the JPrIME-GenPhyloData suite of tools for creating\n" +
						"realistic phylogenetic data. BranchRelaxer takes a Newick tree with\n" +
						"branch lengths (often ultrametric times) and creates a replica of the tree\n" +
						"with relaxed (non-clock like) branch lengths by applying rates drawn from a\n" +
						"probability distribution or similarly. The tree must have at least two leaves.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar BranchRelaxer [options] <model> <arg1> <arg2> <...>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				sb.append(params.getModelsHelpMsg());
				System.out.println(sb.toString());
				return;
			}
			
			RateModel model = params.getRateModel();
			PrIMENewickTree nw = params.getTree(model.lengthsMustBeUltrametric());
			RTree t = new RTree(nw, "RelaxationTree");
			NamesMap names = nw.getVertexNamesMap(false, "Names");
			DoubleMap origLengths = nw.getBranchLengthsMap("OrigLengths");
			if (origLengths == null) {
				origLengths = nw.getTimesMap("OrigLengths").getArcTimesMap();
			}
			int ignoreVertex = -1;
			if (Double.isNaN(origLengths.get(t.getRoot()))) {
				// Makes non-stem trees easy to handle in most cases.
				origLengths.set(t.getRoot(), 0.0);
				ignoreVertex = t.getRoot();
			}
			DoubleMap rates;
			do {
				if (attempts > params.maxAttempts) {
					System.err.println("Failed to create valid rates within max allowed attempts.");
					System.exit(0);
				}
				rates = model.getRates(t, names, origLengths);
				attempts++;
			} while (!isOK(rates, ignoreVertex, Double.parseDouble(params.min), Double.parseDouble(params.max)));
			
			DoubleMap relLengths = getRelaxedLengths(origLengths, rates);
			boolean doMeta = params.doMeta;
			
			String outtree = toNewickTree(t, names, relLengths, origLengths, rates, doMeta);
			if (params.outputfile == null) {
				System.out.println(outtree);
			} else {
				Pair<BufferedWriter, BufferedWriter> out = params.getOutputFiles();
				out.first.write(outtree + '\n');
				out.first.close();
				out.second.write("# BranchLengthRelaxer\n");
				out.second.write("Arguments:\t" +  Arrays.toString(args) + '\n');
				out.second.write("Attempts:\t" + this.attempts + '\n');
				out.second.write("Original lengths:\t" + origLengths.toString() + '\n');
				out.second.write("Rates:\t" + rates.toString() + '\n');
				out.second.write("Relaxed lengths:\t" + relLengths.toString() + '\n');
				out.second.write("Model:\t" + model.getModelName() + '\n');
				out.second.write("Model parameters:\n");
				Map<String, String> mp = model.getModelParameters();
				for (Entry<String, String> p : mp.entrySet()) {
					out.second.write("\t" + p.getKey() + "\t" + p.getValue() + '\n');
				}
				out.second.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}
	
	/**
	 * Validates rates.
	 * @param rates rates.
	 * @return true if OK; otherwise false.
	 */
	private boolean isOK(DoubleMap rates, int ignoreVertex, double min, double max) {
		for (int x = 0; x < rates.getSize(); ++x) {
			if (x == ignoreVertex) {
				continue;
			}
			if (rates.get(x) < min || rates.get(x) > max) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates relaxed lengths.
	 * @param origLengths original lengths. Could of course be ultrametric times.
	 * @param rates the rates.
	 * @return the relaxed lengths.
	 */
	private static DoubleMap getRelaxedLengths(DoubleMap origLengths, DoubleMap rates) {
		int n = origLengths.getSize();
		DoubleMap relaxedLenghts = new DoubleMap("RelaxedLengths", n);
		for (int x = 0; x < n; ++x) {
			relaxedLenghts.set(x, origLengths.get(x) * rates.get(x));
		}
		return relaxedLenghts;
	}
	
	/**
	 * Converts tree to a Newick string.
	 * @param T the tree.
	 * @param names the leaf (or vertex) names.
	 * @param lengths the new relaxed lengths.
	 * @param origLengths the original lengths.
	 * @param rates the rates.
	 * @param withMeta true to include PrIME-meta-style info in tree.
	 * @return the Newick tree.
	 * @throws NewickIOException.
	 */
	private String toNewickTree(RTree T, NamesMap names, DoubleMap lengths, DoubleMap origLengths, DoubleMap rates, boolean withMeta) throws NewickIOException {
		if (withMeta) {
			int n = names.getSize();
			StringMap metas = new StringMap("Meta", n);
			for (int x = 0; x < n; ++x) {
				metas.set(x, "[&&PRIME ID=" + x + " ORIGBL=" + origLengths.get(x) + " RATE=" + rates.get(x) + "]");
			}
			return NewickTreeWriter.write(T, names, lengths, metas, false);
		} else {
			return NewickTreeWriter.write(T, names, lengths, false);
		}
	}
}
