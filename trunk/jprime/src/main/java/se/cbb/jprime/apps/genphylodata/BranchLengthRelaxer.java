package se.cbb.jprime.apps.genphylodata;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.StringMap;

/**
 * Relaxes branch lengths for a tree by imposing IID rates or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class BranchLengthRelaxer implements JPrIMEApp {

	/** Model used. */
	public RateModel model;
	
	@Override
	public String getAppName() {
		return "BranchLengthRelaxer";
	}

	@Override
	public void main(String[] args) throws Exception {
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			BranchLengthRelaxerParameters params = new BranchLengthRelaxerParameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"BranchLengthRelaxer is part of the JPrIME-GenPhyloData suite of tools for\n" +
						"creating realistic gene families. BranchLengthRelaxer will take a Newick tree" +
						"with branch lengths (often ultrametric times) and create a replica of the tree" +
						"with relaxed (non-clock like) branch lengths by applying rates drawn from a" +
						"probability distribution or similarly.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar BranchLengthRelaxer [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
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
