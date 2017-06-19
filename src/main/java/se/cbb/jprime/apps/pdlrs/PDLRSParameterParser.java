package se.cbb.jprime.apps.pdlrs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import se.cbb.jprime.io.GenePseudogeneMapReader;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * A bit messy, code-wise.
 * 
 * @author Joel Sjöstrand.
 * @author Vincent Llorens.
 * @author Owais Mahmudi.
 */
public class PDLRSParameterParser extends se.cbb.jprime.apps.ParameterParser {
	
	/**
	 * Reads a gene-pseudogene map.
	 * @param ps parameters.
	 * @return map.
	 */
	public static LinkedHashMap<String, Integer> getGenePseudogeneMap(PDLRSParameters ps) {
		try {
			return GenePseudogeneMapReader.readGenePseudogeneMap(new File(ps.files.get(3)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Gene-Pseudogene leaf map.", e);
		}
	}	
	
	/**
	 * Returns a reconciliation helper.
	 * @param params parameters.
	 * @param g guest tree.
	 * @param s host tree.
	 * @param dtimes disc. times.
	 * @param mprMap MPR map.
	 * @return the helper.
	 */
	public static ReconciliationHelper getReconciliationHelper(
			PDLRSParameters ps, RBTree g, RBTree s, RBTreeArcDiscretiser dtimes,
			MPRMap mprMap) {
		return new ReconciliationHelper(g, s, dtimes, mprMap, ps.getMaxLosses());
	}
	
	/**
	 * Creates duplication-loss probabilities over discretised host tree.
	 * @param ps parameters.
	 * @param mpr guest-to-host tree reconciliation info.
	 * @param s host tree.
	 * @param g guest tree.
	 * @param times discretisation times.
	 * @return duplication rate, loss rate, duplication-loss probabilities.
	 */
	public static Triple<DoubleParameter, DoubleParameter, DupLossProbs> getDupLossProbs(PDLRSParameters ps, MPRMap mpr, RBTree s, RBTree g, RBTreeArcDiscretiser times) {
						
		// Set initial duplication rate as number of inferred MPR duplications divided by total time tree span.
		// Then set loss rate to the same amount.
		int dups = 0;
		for (int u = 0; u < g.getNoOfVertices(); ++u) {
			if (mpr.isDuplication(u)) {
				++dups;
			}
		}
		double totTime = 0.0;
		for (int x = 0; x < s.getNoOfVertices(); ++x) {
			totTime += times.getArcTime(x);
		}
		
		double lambda = (ps.getDupRate() == null ? dups / totTime + 1e-3 : Double.parseDouble(ps.getDupRate().replaceFirst("FIXED|Fixed|fixed", "")));
		double mu = (ps.getLossRate() == null ? dups / totTime + 1e-3 : Double.parseDouble(ps.getLossRate().replaceFirst("FIXED|Fixed|fixed", "")));
		DoubleParameter dr = new DoubleParameter("DuplicationRate", lambda);
		DoubleParameter lr = new DoubleParameter("LossRate", mu);
		DupLossProbs dlProbs = new DupLossProbs(s, times, dr, lr);
		return new Triple<DoubleParameter, DoubleParameter, DupLossProbs>(dr, lr, dlProbs);
	}
	
	/**
	 * Returns a realisation sampler.
	 * @param ps parameters.
	 * @param iter iteration.
	 * @param prng PRNG.
	 * @param model DLR model.
	 * @param names names of guest tree leaves.
	 * @return the sampler.
	 * @throws IOException.
	 */
	public static RealisationSampler getRealisationSampler(PDLRSParameters ps, Iteration iter, PRNG prng, DLRModel model, NamesMap names) throws IOException {
		if (ps.getSampleRealisations() == null) { return null; }
		String fn = ps.getSampleRealisations().get(0);
		int n = Integer.parseInt(ps.getSampleRealisations().get(1));
		return new RealisationSampler(fn, n, iter, prng, model, names);
	}
}
