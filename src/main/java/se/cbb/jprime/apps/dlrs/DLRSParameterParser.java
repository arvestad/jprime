package se.cbb.jprime.apps.dlrs;

import java.io.IOException;

import se.cbb.jprime.apps.dlrs.DLRModel;
import se.cbb.jprime.apps.dlrs.RealisationSampler;
import se.cbb.jprime.apps.dlrs.DLRSParameters;
import se.cbb.jprime.apps.dlrs.DupLossProbs;
import se.cbb.jprime.apps.dlrs.ReconciliationHelper;

import se.cbb.jprime.io.SampleDoubleArray;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;

import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.FineProposerStatistics;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.LinearTuningParameter;
import se.cbb.jprime.mcmc.NormalProposer;
import se.cbb.jprime.mcmc.RealParameter;
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
 */
public class DLRSParameterParser extends se.cbb.jprime.apps.ParameterParser {
	
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
			DLRSParameters ps, RBTree g, RBTree s, RBTreeArcDiscretiser dtimes,
			MPRMap mprMap) {
		return new ReconciliationHelper(g, s, dtimes, mprMap, ps.getMaxLosses());
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
	public static RealisationSampler getRealisationSampler(DLRSParameters ps, Iteration iter, PRNG prng, DLRModel model, NamesMap names) throws IOException {
		if (ps.getSampleRealisations() == null) { return null; }
		String fn = ps.getSampleRealisations().get(0);
		int n = Integer.parseInt(ps.getSampleRealisations().get(1));
		return new RealisationSampler(fn, n, iter, prng, model, names);
	}
	
	/**
	 * Returns a Normal proposer.
	 * @param ps parameters.
	 * @param p MCMC parameter.
	 * @param iter iterations.
	 * @param prng PRNG.
	 * @param tuningCV tuning CV parameter start-stop as an array in string format.
	 * @return proposer.
	 */
	public static NormalProposer getNormalProposer(DLRSParameters ps, RealParameter p, Iteration iter, PRNG prng, String tuning) {
		return getTruncatedNormalProposer(ps, new RealInterval(0, Double.POSITIVE_INFINITY, true, true), p, iter, prng, tuning);
	}
	
	/**
	 * Returns a Normal proposer, but restricted to an interval. I.e., a truncated normal distribution.
	 * @param ps parameters.
	 * @param interval The truncation 
	 * @param p MCMC parameter.
	 * @param iter iterations.
	 * @param prng PRNG.
	 * @param tuningCV tuning CV parameter start-stop as an array in string format.
	 * @return proposer.
	 */
	public static NormalProposer getTruncatedNormalProposer(DLRSParameters ps, RealInterval interval, RealParameter p, Iteration iter, PRNG prng, String tuning) {
		double[] tng = SampleDoubleArray.toDoubleArray(tuning);
		LinearTuningParameter tcv = new LinearTuningParameter(iter, tng[0], tng[1]);
		NormalProposer proposer = new NormalProposer(p, interval, tcv, prng);
		proposer.setStatistics(new FineProposerStatistics(iter, 8));
		return proposer;
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
	public static Triple<DoubleParameter, DoubleParameter, DupLossProbs> getDupLossProbs(DLRSParameters ps, MPRMap mpr, RBTree s, RBTree g, RBTreeArcDiscretiser times) {
						
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
}
