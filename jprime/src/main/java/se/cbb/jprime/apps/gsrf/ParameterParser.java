package se.cbb.jprime.apps.gsrf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class ParameterParser {

	public static String getMultialignment(Parameters ps) {
		// TODO: Implement.
		return null;
	}

	public static Pair<RBTree, TimesMap> getHostTreeAndTimes(Parameters ps) {
		try {
			PrIMENewickTree SRaw = PrIMENewickTreeReader.readTree(new File(ps.files.get(1)), true, true);
			RBTree S = new RBTree(SRaw, "S");
			TimesMap times = SRaw.getTimesMap();
			return new Pair<RBTree, TimesMap>(S, times);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid host tree.", e);
		}
	}
	
	public static GuestHostMap getGSMap(Parameters ps) {
		try {
			return GuestHostMapReader.readGuestHostMap(new File(ps.files.get(2)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest-to-host leaf map.", e);
		}
	}
	
	public static SampleWriter getOut(Parameters ps) {
		try {
			return (ps.outfile == null ? new SampleWriter() : new SampleWriter(new File(ps.outfile)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid output file.", e);
		}
	}
	
	public static BufferedWriter getInfo(Parameters ps) {
		try {
			if (ps.infofile == null) {
				if (ps.outfile == null) {
					// stdout.
					return new BufferedWriter(new OutputStreamWriter(System.out));
				} else {
					// <outfile>.info.
					return new BufferedWriter(new FileWriter(ps.outfile.trim() + ".info"));
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
	
	public static PRNG getPRNG(Parameters ps) {
		return (ps.seed == null ? new PRNG() : new PRNG(ps.seed));
	}
	
	public static Iteration getIteration(Parameters ps) {
		return new Iteration(ps.iterations.intValue());
	}
	
}
