package se.cbb.jprime.apps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.Sequence;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.MSAFastPhyloTree;
import se.cbb.jprime.io.NewickRBTreeSamples;
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
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.RealParameter;
import se.cbb.jprime.mcmc.Thinner;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.GammaSiteRateHandler;
import se.cbb.jprime.seqevo.MSAData;
import se.cbb.jprime.seqevo.MultiAlignment;
import se.cbb.jprime.seqevo.SequenceType;
import se.cbb.jprime.topology.BiasedRBTreeBranchSwapper;
import se.cbb.jprime.topology.BifurcateTree;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.NeighbourJoiningTreeGenerator;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RBTreeBranchSwapper;
import se.cbb.jprime.topology.RBTreeBranchSwapperSampler;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TreeSequenceIdentity;
import se.cbb.jprime.topology.UniformRBTreeGenerator;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * Super class for parameter parsers of age, dlrs, dltrs and pdlrs as these four have 
 * much parameter handling in common. Should be extended if implementing an app with similar
 * functionality to these.
 * A bit messy, code-wise. 
 * 
 * @author Joel Sjöstrand.
 * @author Vincent Llorens.
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
	public static <P extends Parameters> Triple<RBTree, NamesMap, TimesMap> getHostTree(P ps, BufferedWriter info) {
		try {
			PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(new File(ps.getFiles().get(0)), false, true);			
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
				info.append("# Host tree rescaling factor: ").append("" + (1.0/rootTime)).append('\n');
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
	
	/**
	 * Reads the MSA.
	 * @param ps parameters.
	 * @param seqType sequence type.
	 * @return MSA.
	 * @throws Exception.
	 */
	public static <P extends Parameters> LinkedHashMap<String, ? extends Sequence<? extends Compound>> getMultialignment(P ps, SequenceType seqType) throws Exception {
		File f = new File(ps.getFiles().get(1));
		if (seqType == SequenceType.DNA || seqType == SequenceType.CODON) {
			return FastaReaderHelper.readFastaDNASequence(f);
		} else {
			return FastaReaderHelper.readFastaProteinSequence(f);
		}
	}
	
	/**
	 * Reads a guest-to-host leaf map.
	 * @param ps parameters.
	 * @return map.
	 */
	public static <P extends Parameters> GuestHostMap getGSMap(P ps) {
		try {
			return GuestHostMapReader.readGuestHostMap(new File(ps.getFiles().get(2)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest-to-host leaf map.", e);
		}
	}
	
	/**
	 * Returns site rates.
	 * @param ps parameters.
	 * @return site rates.
	 */
	public static <P extends Parameters> Pair<DoubleParameter, GammaSiteRateHandler> getSiteRates(P ps) {
		DoubleParameter k = new DoubleParameter("SiteRateShape", Double.parseDouble(ps.getSiteRateShape()));
		GammaSiteRateHandler sr = new GammaSiteRateHandler(k, ps.getSiteRateCats());
		return new Pair<DoubleParameter, GammaSiteRateHandler>(k, sr);
	}
	
	/**
	 * Creates an output stream for the MCMC chain. If no parameter is found, stdout is used.
	 * @param ps parameters.
	 * @return output stream.
	 */
	public static <P extends Parameters> SampleWriter getOut(P ps) {
		try {
			return (ps.getOutfile() == null ? new SampleWriter() : new SampleWriter(new File(ps.getOutfile()), 10));
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
	public static <P extends Parameters> BufferedWriter getInfo(P ps) {
		try {
			if (ps.getInfofile() == null) {
				if (ps.getOutfile() == null) {
					// stdout.
					return new BufferedWriter(new OutputStreamWriter(System.out));
				} else {
					// <outfile>.info.
					return new BufferedWriter(new FileWriter(ps.getOutfile().trim() + ".info"));
				}
				
			} else {
				if (ps.getInfofile().equalsIgnoreCase("NONE")) {
					// No info file.
					return null;
				}
				// User-defined info file.
				return new BufferedWriter(new FileWriter(ps.getInfofile()));
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
	public static <P extends Parameters> PRNG getPRNG(P ps) {
		return (ps.getSeed() == null ? new PRNG() : new PRNG(new BigInteger(ps.getSeed())));
	}
	
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
	public static <P extends Parameters> Triple<RBTree, NamesMap, DoubleMap>
		getGuestTreeAndLengths(P ps, GuestHostMap gsMap, PRNG prng, LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs, BufferedWriter info, NewickRBTreeSamples samples, MSAData msa) {
		Triple<RBTree, NamesMap, DoubleMap> guestTreeAndLengths = null;
		if (ps.isMsaFastPhyloTree() && ps.getGuestTreeSet() != null) {
			throw new IllegalArgumentException("Cannot use a fixed guest tree set and Fast Phylo at the same time.");
		} else if (ps.isMsaFastPhyloTree()) {
			guestTreeAndLengths = getGuestTreeAndLengthsFastPhylo(ps, gsMap, info);
		} else if (ps.getGuestTreeSet() != null) {
			guestTreeAndLengths = getGuestTreeAndLengthsFromSet(ps, prng, info, samples);
		} else {
			guestTreeAndLengths = getGuestTreeAndLengthsSimple(ps, gsMap, prng, seqs, info, msa);
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
	private static <P extends Parameters> Triple<RBTree, NamesMap, DoubleMap> getGuestTreeAndLengthsSimple(P ps, GuestHostMap gsMap, PRNG prng, LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs,
			BufferedWriter info, MSAData msa) {
		RBTree g = null;
		NamesMap gNames = null;
		DoubleMap gLengths = null;
		try {
			if (ps.getGuestTree() == null || ps.getGuestTree().equalsIgnoreCase("NJ")) {
				// "Randomly rooted" NJ tree. Produced lengths seem suspicious, so we won't use'em.
				info.append("# Initial guest tree: Produced with NJ on sequence identity (arbitrarily rooted).\n");
				@SuppressWarnings({ "unchecked", "rawtypes" })
				NewickTree gRaw = NeighbourJoiningTreeGenerator.createNewickTree(new MultiAlignment(seqs, false));
				g = new RBTree(gRaw, "GuestTree");
				gNames = gRaw.getVertexNamesMap(true, "GuestTreeNames");
				gLengths = getBranchLengths(g, gNames, msa);
			} else if (ps.getGuestTree().equalsIgnoreCase("UNIFORM")) {
				// Uniformly drawn tree.
				info.append("# Initial guest tree: Uniformly selected random unlabelled tree.\n");
				Pair<RBTree, NamesMap> gn = UniformRBTreeGenerator.createUniformTree("GuestTree", new ArrayList<String>(gsMap.getAllGuestLeafNames()), prng);
				g = gn.first;
				gNames = gn.second;
				gLengths = getBranchLengths(g, gNames, msa);
			} else {
				// Read tree from file.
				info.append("# Initial guest tree: User-specified from file ").append(ps.getGuestTree()).append(".\n");
				PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree(new File(ps.getGuestTree()), true, false);
				g = new RBTree(GRaw, "GuestTree");
				gNames = GRaw.getVertexNamesMap(true, "GuestTreeNames");
				if (GRaw.hasProperty(MetaProperty.BRANCH_LENGTHS)) {
					gLengths = GRaw.getBranchLengthsMap("BranchLengths");
				} else {
					gLengths = getBranchLengths(g, gNames, msa);
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest tree parameter or file.", e);
		}
		return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
	}
	
	/**
	 * Heuristic for branch lengths.
	 * @param g guest tree.
	 * @param gNames leaf names.
	 * @param msa data.
	 * @return branch lengths.
	 */
	private static <P extends Parameters> DoubleMap getBranchLengths(RBTree g, NamesMap gNames, MSAData msa) {
		TreeSequenceIdentity id = new TreeSequenceIdentity(g, gNames, msa);
		DoubleMap pids = id.getPercentIdentityLengths();
		DoubleMap gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
		for (int x = 0; x < gLengths.getSize(); ++x) {
			double pid = pids.get(x);
			if (pid > 0.9) {
				gLengths.set(x, 1.0 - pid + 1e-6);
			}
		}
		return gLengths;
	}
	
	/**
	 * Helper for getGuestTreeAndLengths when using Fast Phylo to compute the tree.
	 * @param ps parameters.
	 * @param gsMap guest-to-host leaf map.
	 * @param info information output.
	 * @return guest tree, names and branch lengths.
	 */
	protected static <P extends Parameters> Triple<RBTree, NamesMap, DoubleMap> getGuestTreeAndLengthsFastPhylo(P ps, GuestHostMap gsMap, BufferedWriter info) {
		RBTree g = null;
		NamesMap gNames = null;
		DoubleMap gLengths = null;
		try {
			info.append("# Initial guest tree: Tree computated by FastPhylo.\n");
			PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(new File(ps.getFiles().get(0)), false, true);
			NewickTree GRaw = MSAFastPhyloTree.getTreeFromFastPhylo(ps.getFiles().get(1), ps.getSubstitutionModel());
			Pair<RBTree, NamesMap> bestBifurcatingTree = BifurcateTree.bestBifurcatingTree(GRaw, sRaw, gsMap);
			g = bestBifurcatingTree.first;
			gNames = bestBifurcatingTree.second;
			gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error when using Fast Phylo to get a guest tree.", e);
		}
		return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
	}
	
	/**
	 * Helper for getGuestTreeAndLengths when using a guest tree from a specific set of trees.
	 * @param ps parameters.
	 * @param prng PRNG.
	 * @param info information output.
	 * @param samples set of trees.
	 * @return guest tree, names and branch lengths.
	 */
	protected static <P extends Parameters> Triple<RBTree, NamesMap, DoubleMap> getGuestTreeAndLengthsFromSet(P ps, PRNG prng, BufferedWriter info, NewickRBTreeSamples samples) {
		RBTree g = null;
		NamesMap gNames = null;
		DoubleMap gLengths = null;
		try {
			info.append("# Initial guest tree: Tree picked at random in the fixed guest tree set.\n");
			// Picking a tree at random in the samples
			int idxTree = prng.nextInt(samples.getNoOfTrees());
			int idxLengthMap = prng.nextInt(samples.getTreeCount(idxTree));
			g = samples.getTree(idxTree);
			gNames = samples.getTemplateNamesMap();
			if (ps.getGuestTreeSetWithLengths()) {
				gLengths = samples.getTreeBranchLengths(idxTree).get(idxLengthMap);
			} else {
				gLengths = new DoubleMap("BranchLengths", g.getNoOfVertices(), 0.1);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid fixed guest tree set parameter or file.", e);
		}
		return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
	}
	
	/**
	 * Returns an iteration object.
	 * @param ps parameters.
	 * @return iteration.
	 */
	public static <P extends Parameters> Iteration getIteration(P ps) {
		return new Iteration(ps.getIterations().intValue());
	}
	
	/**
	 * Returns a thinning factor.
	 * @param ps parameters.
	 * @param iter iteration object associated with the thinning factor.
	 * @return thinning factor.
	 */
	public static <P extends Parameters> Thinner getThinner(P ps, Iteration iter) {
		return new ConstantThinner(iter, ps.getThinning());
	}
	
	/**
	 * Reads the probability distribution used for iid rates over guest tree edges.
	 * @param ps parameters.
	 * @return probability distribution.
	 */
	public static <P extends Parameters> Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> getEdgeRatePD(P ps) {
		// TODO: Implement more PDs.
		
		boolean isUni = ps.getEdgeRatePD().equalsIgnoreCase("UNIFORM");
		double d1 = (ps.getEdgeRatePDMean() != null ? Double.parseDouble(ps.getEdgeRatePDMean().replaceFirst("FIXED|Fixed|fixed", "")) : (isUni ? 0.0 : 0.5));
		double d2 = (ps.getEdgeRatePDCV() != null ? Double.parseDouble(ps.getEdgeRatePDCV().replaceFirst("FIXED|Fixed|fixed", "")) : (isUni ? 5.0 : 1.0));
		DoubleParameter p1 = new DoubleParameter(isUni ? "EdgeRateLowerBound" : "EdgeRateMean", d1);
		DoubleParameter p2 = new DoubleParameter(isUni ? "EdgeRateUpperBound" : "EdgeRateCV", d2);
		
		Continuous1DPDDependent pd = null;
		if (ps.getEdgeRatePD().equalsIgnoreCase("GAMMA")) {
			pd = new GammaDistribution(p1, p2);
		/*
		} else if (ps.edgeRatePD.equalsIgnoreCase("INVGAMMA")) {
			//pd = new InvGammaDistribution(p1, p2);
		} else if (ps.edgeRatePD.equalsIgnoreCase("LOGN")) {
			//pd = new LogNDistribution(p1, p2);
		*/
		} else if (ps.getEdgeRatePD().equalsIgnoreCase("UNIFORM")) {
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
	public static <P extends Parameters> RBTreeArcDiscretiser getDiscretizer(P ps, RBTree S, NamesMap names, TimesMap times, RBTree G) {
		if (ps.getDiscStem() == null) {
			// Try to find a small but sufficient number of stem points to accommodate all
			// duplications in the stem during G perturbation.
			int k = G.getNoOfLeaves();
			int h = (int) Math.round(Math.log((double) k) / Math.log(2.0)); // Height of balanced tree...
			ps.setDiscStem(Math.min(Math.min(h + 12, k), 30));
		}
		return new RBTreeArcDiscretiser(S, names, times, ps.getDiscMin(), ps.getDiscMax(), Double.parseDouble(ps.getDiscTimestep()), ps.getDiscStem());
	}
	
	/**
	 * Creates a proposer selector.
	 * @param ps parameters.
	 * @param prng PRNG.
	 * @return proposer selector.
	 */
	public static <P extends Parameters> MultiProposerSelector getSelector(P ps, PRNG prng) {
		MultiProposerSelector selector = new MultiProposerSelector(prng, SampleDoubleArray.toDoubleArray(ps.getTuningProposerSelectorWeights()));
		return selector;
	}

	/**
	 * Creates a proposal acceptor.
	 * @param ps parameters.
	 * @param prng PRNG.
	 * @return proposal acceptor.
	 */
	public static <P extends Parameters> ProposalAcceptor getAcceptor(P ps, PRNG prng) {
		if (ps.getRuntype().equalsIgnoreCase("MCMC")) {
			return new MetropolisHastingsAcceptor(prng);
		} else if (ps.getRuntype().equalsIgnoreCase("HILLCLIMBING")) {
			return new HillClimbingAcceptor(100);
		}
		throw new IllegalArgumentException("Invalid run type.");
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
	public static <P extends Parameters> NormalProposer getNormalProposer(P ps, RealParameter p, Iteration iter, PRNG prng, String tuning) {
		double[] tng = SampleDoubleArray.toDoubleArray(tuning);
		LinearTuningParameter tcv = new LinearTuningParameter(iter, tng[0], tng[1]);
		NormalProposer proposer = new NormalProposer(p, new RealInterval(0, Double.POSITIVE_INFINITY, true, true), tcv, prng);
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
	public static <P extends Parameters> Proposer getBranchSwapper(P ps, RBTree tree, DoubleMap lengths, MPRMap mpr, Iteration iter, PRNG prng, NewickRBTreeSamples samples) {
		Proposer mrGardener;
		if (ps.getGuestTreeSet() == null) {
			if (ps.getGuestTreeBiasedSwapping() == null) {
				mrGardener = new RBTreeBranchSwapper(tree, lengths, prng);
			} else {
				double p = Double.parseDouble(ps.getGuestTreeBiasedSwapping());
				mrGardener = new BiasedRBTreeBranchSwapper(tree, lengths, null, prng, mpr, 1, 1, p);
			}
			double[] moves = SampleDoubleArray.toDoubleArray(ps.getTuningGuestTreeMoveWeights());
			((RBTreeBranchSwapper) mrGardener).setOperationWeights(moves[0], moves[1], moves[2]);
		} else {
			if (ps.getGuestTreeSetWithLengths()) {
				mrGardener = new RBTreeBranchSwapperSampler(tree, lengths, prng, samples, ps.getGuestTreeSetEqualTopoChance());
			} else {
				mrGardener = new RBTreeBranchSwapperSampler(tree, prng, samples, ps.getGuestTreeSetEqualTopoChance());
			}
		}
		mrGardener.setStatistics(new FineProposerStatistics(iter, 8));
		return mrGardener;
	}
	
	/**
	 * Returns a proposer weight.
	 * @param weights the two weights as an array in string format. 
	 * @param iter iteration.
	 * @return proposer weight.
	 */
	public static <P extends Parameters> LinearProposerWeight getProposerWeight(String weights, Iteration iter) {
		double[] ws = SampleDoubleArray.toDoubleArray(weights);
		return new LinearProposerWeight(iter, ws[0], ws[1]);
	}
}
