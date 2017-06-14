package se.cbb.jprime.apps.dltrs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.Sequence;

import se.cbb.jprime.apps.dltrs.DLTRModel;
import se.cbb.jprime.apps.dltrs.DLTRMAPModel;
import se.cbb.jprime.apps.dltrs.DLTRSParameters;
import se.cbb.jprime.apps.dltrs.RealisationSampler;
import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.math.UniformDistribution;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.FineProposerStatistics;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.LinearTuningParameter;
import se.cbb.jprime.mcmc.NormalProposer;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.RealParameter;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Quadruple;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.MultiAlignment;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.LeafLeafMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.NeighbourJoiningTreeGenerator;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeBranchSwapper;
import se.cbb.jprime.topology.RBTreeBranchSwapperSampler;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.UniformRBTreeGenerator;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * A bit messy, code-wise.
 * 
 * @author Joel Sjöstrand.
 * @author Mehmood Alam Khan
 * @author Vincent Llorens.
 */
public class DLTRSParameterParser extends se.cbb.jprime.apps.ParameterParser {
	
	/**
	 * Reads the guest tree with names and possibly lengths. There are many options,
	 * e.g. reading a tree with or without lengths, or randomly creating one.
	 * @param <S> Multialignment sequence type.
	 * @param <C> Multialignment compound type.
	 * @param ps parameters.
	 * @param gsMap guest-to-host leaf map.
	 * @param prng PRNG.
	 * @param alignment alignment of sequences.
	 * @param samples guest tree set samples.
	 * @return guest tree, names and branch lengths.
	 */
	public static Triple<RBTree, NamesMap, DoubleMap>
		getGuestTreeAndLengths(DLTRSParameters ps, GuestHostMap gsMap, PRNG prng, LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs, BufferedWriter info, NewickRBTreeSamples samples) {
		Triple<RBTree, NamesMap, DoubleMap> guestTreeAndLengths = null;
		if (ps.msaFastPhyloTree && ps.guestTreeSet != null) {
			throw new IllegalArgumentException("Cannot use a fixed guest tree set and Fast Phylo at the same time.");
		} else if (ps.msaFastPhyloTree) {
			guestTreeAndLengths = getGuestTreeAndLengthsFastPhylo(ps, gsMap, info);
		} else if (ps.guestTreeSet != null) {
			guestTreeAndLengths = getGuestTreeAndLengthsFromSet(ps, prng, info, samples);
		} else {
			guestTreeAndLengths = getGuestTreeAndLengthsSimple(ps, gsMap, prng, seqs, info);
		}
		return guestTreeAndLengths;
	}
	
	/**
	 * Helper for getGuestTreeAndLengths when using simple methods to compute the tree,
	 * like NJ, Uniform, or from a file.
	 * @param ps parameters.
	 * @param gsMap guest-to-host leaf map.
	 * @param prng PRNG.
	 * @param seqs alignment of sequences.
	 * @param info information output.
	 * @return guest tree, names and branch lengths.
	 */
	private static Triple<RBTree, NamesMap, DoubleMap> getGuestTreeAndLengthsSimple(DLTRSParameters ps, GuestHostMap gsMap, PRNG prng, LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs, BufferedWriter info) {
		RBTree g = null;
		NamesMap gNames = null;
		DoubleMap gLengths = null;
		try {
			if (ps.guestTree == null || ps.guestTree.equalsIgnoreCase("NJ")) {
				// "Randomly rooted" NJ tree. Produced lengths seem suspicious, so we won't use'em.
				info.append("# Initial guest tree: Produced with NJ on sequence identity (arbitrarily rooted).\n");
				@SuppressWarnings({ "unchecked", "rawtypes" })
				NewickTree gRaw = NeighbourJoiningTreeGenerator.createNewickTree(new MultiAlignment(seqs, false));
				g = new RBTree(gRaw, "GuestTree");
				gNames = gRaw.getVertexNamesMap(true, "GuestTreeNames");
				gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
			} else if (ps.guestTree.equalsIgnoreCase("UNIFORM")) {
				// Uniformly drawn tree.
				info.append("# Initial guest tree: Uniformly selected random unlabelled tree.\n");
				Pair<RBTree, NamesMap> gn = UniformRBTreeGenerator.createUniformTree("GuestTree", new ArrayList<String>(gsMap.getAllGuestLeafNames()), prng);
				g = gn.first;
				gNames = gn.second;
				gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
			} else {
				// Read tree from file.
				info.append("# Initial guest tree: User-specified from file ").append(ps.guestTree).append(".\n");
				PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree(new File(ps.guestTree), true, false);
				g = new RBTree(GRaw, "GuestTree");
				gNames = GRaw.getVertexNamesMap(true, "GuestTreeNames");
				if (GRaw.hasProperty(MetaProperty.BRANCH_LENGTHS)) {
					gLengths = GRaw.getBranchLengthsMap("BranchLengths");
				} else {
					gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest tree parameter or file.", e);
		}
		return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
	}
	
	
	/**
	 * Reads the probability distribution used for iid rates over guest tree edges.
	 * @param ps parameters.
	 * @return probability distribution.
	 */
	public static Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> getEdgeRatePD(DLTRSParameters ps, double d1, double d2) {
		// TODO: Implement more PDs.
		boolean isUni = ps.edgeRatePD.equalsIgnoreCase("UNIFORM");
		DoubleParameter p1 = new DoubleParameter(isUni ? "EdgeRateLowerBound" : "EdgeRateMean", d1);
		DoubleParameter p2 = new DoubleParameter(isUni ? "EdgeRateUpperBound" : "EdgeRateCV", d2);
		
		Continuous1DPDDependent pd = null;
		if (ps.edgeRatePD.equalsIgnoreCase("GAMMA")) {
			pd = new GammaDistribution(p1, p2);
		} else if (ps.edgeRatePD.equalsIgnoreCase("UNIFORM")) {
			pd = new UniformDistribution(p1, p2, true, true);
		} else {
			throw new IllegalArgumentException("Invalid edge rate distribution.");
		}
		return new Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent>(p1, p2, pd);
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
	public static RBTreeEpochDiscretiser getDiscretizer(DLTRSParameters ps, RBTree S, NamesMap names, TimesMap times, RBTree G) {
		if (ps.discStem == null) {
			// Try to find a small but sufficient number of stem points to accommodate all
			// duplications in the stem during G perturbation. Not really necessary since LGT supported...
			int k = G.getNoOfLeaves();
			int h = (int) Math.round(Math.log((double) k) / Math.log(2.0)); // Height of balanced tree...
			ps.discStem = Math.min(Math.min(h, k), 10);
		}
		return new RBTreeEpochDiscretiser(S, names, times, ps.discMin, ps.discMax, Double.parseDouble(ps.discTimestep), ps.discStem);
	}
		
	/**
	 * Creates duplication-loss-transfer probabilities over discretised host tree.
	 * @param ps parameters.
	 * @param s host tree.
	 * @param g guest tree.
	 * @param times discretisation times.
	 * @param gsMap 
	 * @param gNames 
	 * @param sNames 
	 * @return duplication rate, loss rate, transfer rate, duplication-loss-transfer probabilities.
	 */
	public static Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs> getDLTProbs(DLTRSParameters ps, RBTree s, NamesMap sNames, RBTree g, NamesMap gNames, GuestHostMap gsMap, RBTreeEpochDiscretiser times) {
						
		// Set initial rates based on number of inferred MPR duplications divided by total time tree span.
		MPRMap mpr = new MPRMap(gsMap, g, gNames, s, sNames);
		int dups = mpr.getTotalNoOfDuplications();
		double totTime = times.getTotalArcTime();
		
		double lambda = (ps.dupRate == null ? 0.5 * dups / totTime + 1e-3 : Double.parseDouble(ps.dupRate.replaceFirst("FIXED|Fixed|fixed", "")));
		double mu = (ps.lossRate == null ? dups / totTime + 1e-3 : Double.parseDouble(ps.lossRate.replaceFirst("FIXED|Fixed|fixed", "")));
		double tau = (ps.transRate == null ? 0.5 * dups / totTime + 1e-3 : Double.parseDouble(ps.transRate.replaceFirst("FIXED|Fixed|fixed", "")));
		boolean adjust = true;  //ps.adjust;  //TODO: Investigate behaviour!
		
		DoubleParameter dr = new DoubleParameter("DuplicationRate", lambda);
		DoubleParameter lr = new DoubleParameter("LossRate", mu);
		DoubleParameter tr = new DoubleParameter("TransferRate", tau);
		EpochDLTProbs dltProbs = new EpochDLTProbs(times, dr, lr, tr, adjust);
		return new Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs>(dr, lr, tr, dltProbs);
	}
	
	
	/**
	 * Creates duplication-loss-transfer probabilities over discretised host tree.
	 * @param ps parameters.
	 * @param s host tree.
	 * @param g guest tree.
	 * @param times discretisation times.
	 * @param gsMap 
	 * @param gNames 
	 * @param sNames 
	 * @return duplication rate, loss rate, transfer rate, duplication-loss-transfer probabilities.
	 */
	public static Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs> getDLTProbs( RBTreeEpochDiscretiser times, double d, double l, double t) {
		
		DoubleParameter dr = new DoubleParameter("DuplicationRate", d);
		DoubleParameter lr = new DoubleParameter("LossRate", l);
		DoubleParameter tr = new DoubleParameter("TransferRate", t);
		EpochDLTProbs dltProbs = new EpochDLTProbs(times, dr, lr, tr, true);
		return new Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs>(dr, lr, tr, dltProbs);
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
	public static NormalProposer getNormalProposer(DLTRSParameters ps, RealParameter p, Iteration iter, PRNG prng, String tuning) {
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
	public static NormalProposer getTruncatedNormalProposer(DLTRSParameters ps, RealInterval interval, RealParameter p, Iteration iter, PRNG prng, String tuning) {
		double[] tng = SampleDoubleArray.toDoubleArray(tuning);
		LinearTuningParameter tcv = new LinearTuningParameter(iter, tng[0], tng[1]);
		NormalProposer proposer = new NormalProposer(p, interval, tcv, prng);
		proposer.setStatistics(new FineProposerStatistics(iter, 8));
		return proposer;
	}
	
	/**
	 * Returns a branch swapper proposer.
	 * @param tree tree.
	 * @param lengths branch lengths.
	 * @param prng PRNG.
	 * @param samples guest tree samples.
	 * @return branch swapper.
	 */
	public static Proposer getBranchSwapper(DLTRSParameters ps, RBTree tree, DoubleMap lengths, Iteration iter, PRNG prng, NewickRBTreeSamples samples) {
		Proposer mrGardener;
		if (ps.guestTreeSet == null) {
			mrGardener = new RBTreeBranchSwapper(tree, lengths, prng);
			double[] moves = SampleDoubleArray.toDoubleArray(ps.tuningGuestTreeMoveWeights);
			((RBTreeBranchSwapper) mrGardener).setOperationWeights(moves[0], moves[1], moves[2]);
		} else {
			if (ps.guestTreeSetWithLengths) {
				mrGardener = new RBTreeBranchSwapperSampler(tree, lengths, prng, samples, ps.guestTreeSetEqualTopoChance);
			} else {
				mrGardener = new RBTreeBranchSwapperSampler(tree, prng, samples, ps.guestTreeSetEqualTopoChance);
			}
		}
		mrGardener.setStatistics(new FineProposerStatistics(iter, 8));
		return mrGardener;
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
			DLTRSParameters params, RBTree g, RBTree s, RBTreeEpochDiscretiser dtimes, LeafLeafMap llMap) {
		return new ReconciliationHelper(g, s, dtimes, llMap);
	}
	
	/**
	 * Returns a realisation sampler.
	 * @param ps parameters.
	 * @param iter iteration.
	 * @param prng PRNG.
	 * @param model DLTR model.
	 * @param names names of guest tree leaves.
	 * @return the sampler.
	 * @throws IOException.
	 */
	public static RealisationSampler getRealisationSampler(DLTRSParameters ps, Iteration iter, PRNG prng, DLTRModel model, DLTRMAPModel msModel, NamesMap names, Boolean maxRealizationFlag) throws IOException {
		if (ps.sampleRealisations == false && ps.maxRealizationFlag == false ) { return null; }
		if (ps.sampleRealisations == true && ps.maxRealizationFlag == true ) { return null; }
		String fn = ps.outfile.trim() + ".disct.host.tree";
		int NO_OF_REALIZATION_PER_SATATE= 1;
		return new RealisationSampler(fn,NO_OF_REALIZATION_PER_SATATE, iter, prng, model, msModel, names, maxRealizationFlag);
	}
}
