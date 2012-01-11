package se.cbb.jprime.apps.gsrf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;

import org.biojava3.core.sequence.template.AbstractSequence;
import org.biojava3.core.sequence.template.Compound;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.math.UniformDistribution;
import se.cbb.jprime.mcmc.ConstantThinner;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.FineProposerStatistics;
import se.cbb.jprime.mcmc.HillClimbingAcceptor;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.LinearProposerWeight;
import se.cbb.jprime.mcmc.LinearTuningParameter;
import se.cbb.jprime.mcmc.MetropolisHastingsAcceptor;
import se.cbb.jprime.mcmc.MultiProposerSelector;
import se.cbb.jprime.mcmc.NormalProposer;
import se.cbb.jprime.mcmc.ProposalAcceptor;
import se.cbb.jprime.mcmc.RealParameter;
import se.cbb.jprime.mcmc.Thinner;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.MultiAlignment;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.NeighbourJoiningTreeGenerator;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RBTreeBranchSwapper;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.UniformRBTreeGenerator;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * A bit messy, code-wise.
 * 
 * @author Joel Sjöstrand.
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
	public static Triple<RBTree, NamesMap, TimesMap> getHostTree(Parameters ps) {
		try {
			PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(new File(ps.files.get(0)), false, true);
			RBTree s = new RBTree(sRaw, "HostTree");
			NamesMap sNames = sRaw.getVertexNamesMap(true, "HostTreeNames");
			TimesMap sTimes = sRaw.getTimesMap("HostTreeRawTimes");
			double stemTime = sTimes.getArcTime(s.getRoot());
			if (Double.isNaN(stemTime) || stemTime <= 0.0) {
				throw new IllegalArgumentException("Missing time for stem in host tree (i.e., \"arc\" predating root).");
			}
			double leafTime = sTimes.get(s.getLeaves().get(0));
			if (Math.abs(leafTime) > 1e-8) {
				throw new IllegalArgumentException("Absolute leaf times for host tree must be 0.");
			}
			// Rescale tree so that root has time 1.0.
			double rootTime = sTimes.getVertexTime(s.getRoot());
			if (Math.abs(rootTime - 1.0) > 1e-6) {
				double[] vts = sTimes.getVertexTimes();
				double[] ats = sTimes.getArcTimes();
				for (int x = 0; x < vts.length; ++x) {
					vts[x] /= rootTime;
					ats[x] /= rootTime;
				}
			}
			return new Triple<RBTree, NamesMap, TimesMap>(s, sNames, sTimes);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid host tree.", e);
		}
	}
	
	public static String getMultialignment(Parameters ps) {
		// TODO: Implement.
		return null;
	}
	
	/**
	 * Reads a guest-to-host leaf map.
	 * @param ps parameters.
	 * @return map.
	 */
	public static GuestHostMap getGSMap(Parameters ps) {
		try {
			return GuestHostMapReader.readGuestHostMap(new File(ps.files.get(2)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest-to-host leaf map.", e);
		}
	}
	
	/**
	 * Creates an output stream for the MCMC chain. If no parameter is found, stdout is used.
	 * @param ps parameters.
	 * @return output stream.
	 */
	public static SampleWriter getOut(Parameters ps) {
		try {
			return (ps.outfile == null ? new SampleWriter() : new SampleWriter(new File(ps.outfile)));
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
	
	/**
	 * Returns a PRNG. If no seed is found, a random seed is used.
	 * @param ps parameters.
	 * @return PRNG.
	 */
	public static PRNG getPRNG(Parameters ps) {
		return (ps.seed == null ? new PRNG() : new PRNG(new BigInteger(ps.seed)));
	}
	
	/**
	 * Reads the guest tree with names and possibly lengths. There are many options,
	 * e.g. reading a tree with or without lengths, or randomly creating one.
	 * @param <S> Multialignment sequence type.
	 * @param <C> Multialignment compound type.
	 * @param ps parameters.
	 * @param gsMap guest-to-host leaf map.
	 * @param prng PRNG.
	 * @param alignment alignment of sequances.
	 * @return guest tree, names and branch lengths.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> Triple<RBTree, NamesMap, DoubleMap>
		getGuestTreeAndLengths(Parameters ps, GuestHostMap gsMap, PRNG prng, MultiAlignment<S, C> alignment) {
		try {
			RBTree g;
			NamesMap gNames;
			DoubleMap gLengths;
			if (ps.guestTree == null || ps.guestTree.equalsIgnoreCase("NJ")) {
				// "Randomly rooted" NJ tree. Produced lengths seem suspicious, so we won't use'em.
				NewickTree gRaw = NeighbourJoiningTreeGenerator.createNewickTree(alignment);
				g = new RBTree(gRaw, "GuestTree");
				gNames = gRaw.getVertexNamesMap(true, "GuestTreeNames");
				gLengths = new DoubleMap("branchlengths", g.getNoOfVertices(), 0.1);
			} else if (ps.guestTree.equalsIgnoreCase("UNIFORM")) {
				// Uniformly drawn tree.
				Pair<RBTree, NamesMap> gn = UniformRBTreeGenerator.createUniformTree("GuestTree", new ArrayList<String>(gsMap.getAllGuestLeafNames()), prng);
				g = gn.first;
				gNames = gn.second;
				gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
			} else {
				// Read tree from file.
				PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree(new File(ps.guestTree), true, false);
				g = new RBTree(GRaw, "GuestTree");
				gNames = GRaw.getVertexNamesMap(true, "GuestTreeNames");
				if (GRaw.hasProperty(MetaProperty.BRANCH_LENGTHS)) {
					gLengths = GRaw.getBranchLengthsMap("BranchLengths");
				} else {
					gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
				}
			}
			return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest tree parameter or file.", e);
		}
	}
	
	/**
	 * Returns an iteration object.
	 * @param ps parameters.
	 * @return iteration.
	 */
	public static Iteration getIteration(Parameters ps) {
		return new Iteration(ps.iterations.intValue());
	}
	
	/**
	 * Returns a thinning factor.
	 * @param ps parameters.
	 * @param iter iteration object associated with the thinning factor.
	 * @return thinning factor.
	 */
	public static Thinner getThinner(Parameters ps, Iteration iter) {
		return new ConstantThinner(iter, ps.thinning);
	}
	
	/**
	 * Reads the probability distribution used for iid rates over guest tree edges.
	 * @param ps parameters.
	 * @return probability distribution.
	 */
	public static Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> getEdgeRatePD(Parameters ps) {
		// TODO: Implement more PDs.
		
		boolean isUni = ps.edgeRatePD.equalsIgnoreCase("UNIFORM");
		double d1 = (ps.edgeRatePDMean != null ? Double.parseDouble(ps.edgeRatePDMean.replaceFirst("FIXED|Fixed|fixed", "")) : (isUni ? 0.0 : 0.1));
		double d2 = (ps.edgeRatePDCV != null ? Double.parseDouble(ps.edgeRatePDCV.replaceFirst("FIXED|Fixed|fixed", "")) : (isUni ? 5.0 : 0.7));
		DoubleParameter p1 = new DoubleParameter(isUni ? "EdgeRateLowerBound" : "EdgeRateMean", d1);
		DoubleParameter p2 = new DoubleParameter(isUni ? "EdgeRateUpperBound" : "EdgeRateCV", d2);
		
		Continuous1DPDDependent pd = null;
		if (ps.edgeRatePD.equalsIgnoreCase("GAMMA")) {
			pd = new GammaDistribution(p1, p2, GammaDistribution.ParameterSetup.MEAN_AND_CV);
		/*
		} else if (ps.edgeRatePD.equalsIgnoreCase("INVGAMMA")) {
			//pd = new InvGammaDistribution(p1, p2, InvGammaDistribution.ParameterSetup.MEAN_AND_CV);
		} else if (ps.edgeRatePD.equalsIgnoreCase("LOGN")) {
			//pd = new LogNDistribution(p1, p2, LogNDistribution.ParameterSetup.MEAN_AND_CV);
		*/
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
	 * @param times times of the host tree.
	 * @param G guest tree.
	 * @return the discretisation.
	 */
	public static RBTreeArcDiscretiser getDiscretizer(Parameters ps, RBTree S, TimesMap times, RBTree G) {
		if (ps.discStem == null) {
			// Try to find a small but sufficient number of stem points to accommodate all
			// duplications in the stem during G perturbation.
			int h = (int) Math.round(Math.log((double) G.getNoOfLeaves()) / Math.log(2.0));
			ps.discStem = Math.min(h + 5, 30);
		}
		return new RBTreeArcDiscretiser(S, times, ps.discMin, ps.discMax, ps.discTimestep, ps.discStem);
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
	public static Triple<DoubleParameter, DoubleParameter, DupLossProbs> getDupLossProbs(Parameters ps, MPRMap mpr, RBTree s, RBTree g, RBTreeArcDiscretiser times) {
						
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
		
		double lambda = (ps.dupRate == null ? dups / totTime + 1e-3 : Double.parseDouble(ps.dupRate.replaceFirst("FIXED|Fixed|fixed", "")));
		double mu = (ps.lossRate == null ? dups / totTime + 1e-3 : Double.parseDouble(ps.lossRate.replaceFirst("FIXED|Fixed|fixed", "")));
		DoubleParameter dr = new DoubleParameter("DuplicationRate", lambda);
		DoubleParameter lr = new DoubleParameter("LossRate", mu);
		DupLossProbs dlProbs = new DupLossProbs(s, times, dr, lr);
		return new Triple<DoubleParameter, DoubleParameter, DupLossProbs>(dr, lr, dlProbs);
	}
	
	/**
	 * Creates a proposer selector.
	 * @param ps parameters.
	 * @param prng PRNG.
	 * @return proposer selector.
	 */
	public static MultiProposerSelector getSelector(Parameters ps, PRNG prng) {
		MultiProposerSelector selector = new MultiProposerSelector(prng, SampleDoubleArray.toDoubleArray(ps.tuningProposerSelectorWeights));
		return selector;
	}

	/**
	 * Creates a proposal acceptor.
	 * @param ps parameters.
	 * @param prng PRNG.
	 * @return proposal acceptor.
	 */
	public static ProposalAcceptor getAcceptor(Parameters ps, PRNG prng) {
		if (ps.runtype.equalsIgnoreCase("MCMC")) {
			return new MetropolisHastingsAcceptor(prng);
		} else if (ps.runtype.equalsIgnoreCase("HILLCLIMBING")) {
			return new HillClimbingAcceptor();
		}
		throw new IllegalArgumentException("Invalid run type.");
	}
	
	/**
	 * Returns a Normal proposer.
	 * @param ps parameters.
	 * @param p MCMC parameter.
	 * @param iter iterations.
	 * @param prng PRNG.
	 * @param tuning tuning parameters as an array in string format.
	 * @return proposer.
	 */
	public static NormalProposer getNormalProposer(Parameters ps, RealParameter p, Iteration iter, PRNG prng, String tuning) {
		double[] tng = SampleDoubleArray.toDoubleArray(tuning);
		LinearTuningParameter t1 = new LinearTuningParameter(iter, tng[0], tng[1]);
		LinearTuningParameter t2 = new LinearTuningParameter(iter, tng[2], tng[3]);		
		NormalProposer proposer = new NormalProposer(p, new RealInterval(0, Double.POSITIVE_INFINITY, true, true), t1, t2, prng);
		proposer.setStatistics(new FineProposerStatistics(iter, 8));
		return proposer;
	}
	
	/**
	 * Returns a branch swapper proposer.
	 * @param tree tree.
	 * @param lengths branch lengths.
	 * @param iter iterations.
	 * @param prng PRNG.
	 * @return branch swapper.
	 */
	public static RBTreeBranchSwapper getBranchSwapper(RBTree tree, DoubleMap lengths, Iteration iter, PRNG prng) {
		RBTreeBranchSwapper mrGardener = new RBTreeBranchSwapper(tree, lengths, prng);
		mrGardener.setStatistics(new FineProposerStatistics(iter, 8));
		return mrGardener;
	}
	
	/**
	 * Returns a proposer weight.
	 * @param weights the two weights as an array in string format. 
	 * @param iter iteration.
	 * @return proposer weight.
	 */
	public static LinearProposerWeight getProposerWeight(String weights, Iteration iter) {
		double[] ws = SampleDoubleArray.toDoubleArray(weights);
		return new LinearProposerWeight(iter, ws[0], ws[1]);
	}
	
}
