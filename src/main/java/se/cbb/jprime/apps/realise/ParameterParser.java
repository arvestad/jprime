package se.cbb.jprime.apps.realise;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.TimesMap;


/**
 * For more complex parameters, performs appropriate type casts, etc.
 * A bit messy, code-wise.
 * 
 * @author Joel Sjöstrand.
 * @author Owais Mahmudi
 */
public class ParameterParser {

	/**
	 * Reads host tree with leaf names and times.
	 * <p/>
	 * <b>The host tree is rescaled so that the
	 * root-to-leaf time (excluding stem arc) is 1.0.</b>
	 * @param ps parameters.
	 * @return host tree, names, times.
	 */
	public static Triple<RBTree, NamesMap, TimesMap> getHostTree(Parameters ps, BufferedWriter info, boolean trim) {
		try {
			int arg=1;
			File f=new File(ps.files.get(arg));
			Scanner sct = new Scanner(f);
			String hostree = sct.nextLine();
                        if(trim)
			hostree = hostree.substring(13);
			
			PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(hostree, false, true);
			RBTree s = new RBTree(sRaw, "HostTree");
			NamesMap sNames = sRaw.getVertexNamesMap(true, "HostTreeNames");
			TimesMap sTimes = sRaw.getTimesMap("HostTreeRawTimes");
			double stemTime = sTimes.getArcTime(s.getRoot());
			if (Double.isNaN(stemTime) || stemTime <= 0.0) {
				sct.close();
				throw new IllegalArgumentException("Missing time for stem in host tree (i.e., \"arc\" predating root).");
			}
			double leafTime = sTimes.get(s.getLeaves().get(0));
			if (Math.abs(leafTime) > 1e-8) {
				sct.close();
				throw new IllegalArgumentException("Absolute leaf times for host tree must be 0.");
			}
			// Rescale tree so that root has time 1.0.
			double rootTime = sTimes.getVertexTime(s.getRoot());
			if (Math.abs(rootTime - 1.0) > 1e-6) {
				info.append("# Host tree rescaling factor: ").append("" + (1.0/rootTime)).append('\n');
				double[] vts = sTimes.getVertexTimes();
				double[] ats = sTimes.getArcTimes();
				for (int x = 0; x < vts.length; ++x) {
					vts[x] /= rootTime;
					ats[x] /= rootTime;
				}
			}
			sct.close();
			return new Triple<RBTree, NamesMap, TimesMap>(s, sNames, sTimes);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid host tree.", e);
		}
	}

	/**
	 * Creates an output stream for the MCMC chain. If no parameter is found, stdout is used.
	 * @param ps parameters.
	 * @return output stream.
	 */
	public static SampleWriter getOut(Parameters ps) {
		try {
			return (ps.outmcmcfile == null ? new SampleWriter() : new SampleWriter(new File(ps.outmcmcfile), 10));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid output file.", e);
		}
	}
	
	/**
	 * Creates an output stream for auxiliary run info. If no parameter is found then,
	 * <ol>
	 * <li>if the ordinary output is directed to stdout, so will the info.</li>
	 * <li>if the ordinary output is directed to a file, e.g., "myout", info will be written to "myout.info".</li>
	 * </ol>
	 * @param ps parameters.
	 * @return output stream.
	 */
	public static BufferedWriter getInfo(Parameters ps) {
		try {
			if (ps.infofile == null) {
				if (ps.outmcmcfile == null) {
					// stdout.
					return new BufferedWriter(new OutputStreamWriter(System.out));
				} else {
					// <outfile>.info.
					return new BufferedWriter(new FileWriter(ps.outmcmcfile.trim() + ".info"));
				}
				
			} else {
				if (ps.infofile.equalsIgnoreCase("NONE")) {
					// No info file.
					return null;
				}
				// User-defined info file.
				return new BufferedWriter(new FileWriter(ps.infofile));
			}			
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid output file.", e);
		}
	}

	/**
	 * Creates a discretisation of the host tree.
	 * @param ps parameters.
	 * @param S host tree.
	 * @param names names of the host tree.
	 * @param times times of the host tree.
	 * @param G guest tree.
	 * @return the discretisation.
	 */
	public static RBTreeEpochDiscretiser getDiscretizer(Parameters ps, RBTree S, NamesMap names, TimesMap times, RBTree G) {
		if (ps.discStem == null) {
			// Try to find a small but sufficient number of stem points to accommodate all
			// duplications in the stem during G perturbation. Not really necessary since LGT supported...
			int k = G.getNoOfLeaves();
			int h = (int) Math.round(Math.log((double) k) / Math.log(2.0)); // Height of balanced tree...
			ps.discStem = Math.min(Math.min(h, k), 10);
		}
		return new RBTreeEpochDiscretiser(S, names, times, ps.discMin, ps.discMax, Double.parseDouble(ps.discTimestep), ps.discStem);
	}
}
