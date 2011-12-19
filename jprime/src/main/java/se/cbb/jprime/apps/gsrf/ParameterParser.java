package se.cbb.jprime.apps.gsrf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.biojava3.core.sequence.template.AbstractSequence;
import org.biojava3.core.sequence.template.Compound;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.PrIMENewickTreeVerifier;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.math.Continuous1DPD;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.math.NormalDistribution.ParameterSetup;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ConstantThinner;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.Iteration;
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
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.UniformRBTreeGenerator;

/**
 * For more complex parameters, performs appropriate type casts, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class ParameterParser {

	public static Triple<RBTree, NamesMap, TimesMap> getHostTree(Parameters ps) {
		try {
			PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(new File(ps.files.get(0)), true, true);
			RBTree s = new RBTree(sRaw, "S");
			NamesMap sNames = sRaw.getVertexNamesMap(true, "S.names");
			TimesMap sTimes = sRaw.getTimesMap("S.times");
			double stemTime = sTimes.getArcTime(s.getRoot());
			if (Double.isNaN(stemTime) || stemTime <= 0.0) {
				throw new IllegalArgumentException("Missing time for stem in host tree (i.e., \"arc\" predating root).");
			}
			double leafTime = sTimes.get(s.getLeaves().get(0));
			if (leafTime <= 0.0) {
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
	
	public static Thinner getThinner(Parameters ps, Iteration iter) {
		return new ConstantThinner(iter, ps.thinning);
	}
	
	public static <S extends AbstractSequence<C>, C extends Compound> Triple<RBTree, NamesMap, DoubleMap>
	getGuestTreeAndLengths(Parameters ps, GuestHostMap gsMap, PRNG prng, MultiAlignment<S, C> alignment) {
		try {
			RBTree g;
			NamesMap gNames;
			DoubleMap gLengths;
			if (ps.guestTree == null || ps.guestTree.equalsIgnoreCase("NJ")) {
				// NJ tree. Produced lengths seem fishy, so we won't use'em.
				NewickTree gRaw = NeighbourJoiningTreeGenerator.createNewickTree(alignment);
				g = new RBTree(gRaw, "G");
				gNames = gRaw.getVertexNamesMap(true, "G.names");
				gLengths = new DoubleMap("G.lengths", g.getNoOfVertices(), 0.1);
			} else if (ps.guestTree.equalsIgnoreCase("UNIFORM")) {
				// Uniformly drawn tree.
				Pair<RBTree, NamesMap> gn = UniformRBTreeGenerator.createUniformTree("G", new ArrayList<String>(gsMap.getAllGuestLeafNames()), prng);
				g = gn.first;
				gNames = gn.second;
				gLengths = new DoubleMap("G.lengths", g.getNoOfVertices(), 0.1);
			} else {
				// Read tree from file.
				PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree(new File(ps.guestTree), true, false);
				g = new RBTree(GRaw, "G");
				gNames = GRaw.getVertexNamesMap(true, "G.names");
				if (GRaw.hasProperty(MetaProperty.BRANCH_LENGTHS)) {
					gLengths = GRaw.getBranchLengthsMap("G.lengths");
				} else {
					gLengths = new DoubleMap("G.lengths", g.getNoOfVertices(), 0.1);
				}
			}
			return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid guest tree file or parameter.", e);
		}
	}
	
	public static Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> getEdgeRateDB(Parameters ps) {
		// TODO: Implement.
		boolean isUni = ps.edgeRateDistrib.equalsIgnoreCase("UNIFORM");
		DoubleParameter p1 = new DoubleParameter(isUni ? "EdgeRate.A" : "EdgeRate.mean", isUni ? 0.0 : 0.1);
		DoubleParameter p2 = new DoubleParameter(isUni ? "EdgeRate.B" : "EdgeRate.CV", isUni ? 1.5 : Math.sqrt(0.005)/0.1);
		Continuous1DPDDependent pd = null;
		if (ps.edgeRateDistrib.equalsIgnoreCase("GAMMA")) {
			pd = new GammaDistribution(p1, p2, GammaDistribution.ParameterSetup.MEAN_AND_CV);
/*
		} else if (ps.edgeRateDistrib.equalsIgnoreCase("INVGAMMA")) {
			//pd = new InvGammaDistribution(p1, p2, InvGammaDistribution.ParameterSetup.MEAN_AND_CV);
		} else if (ps.edgeRateDistrib.equalsIgnoreCase("LOGN")) {
			//pd = new LogNDistribution(p1, p2, LogNDistribution.ParameterSetup.MEAN_AND_CV);
*/
		} else if (ps.edgeRateDistrib.equalsIgnoreCase("UNIFORM")) {
			//pd = new UniformDistribution(p1, p2);
		} else {
			throw new IllegalArgumentException("Invalid edge rate distribution.");
		}
		return new Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent>(p1, p2, pd);
	}
	
	public static RBTreeArcDiscretiser getDiscretizer(Parameters ps, RBTree S, TimesMap times, RBTree G) {
		if (ps.discStem == null) {
			// Try to find a small but sufficient number of stem points to accommodate all
			// duplications in the stem during G perturbation.
			int h = (int) Math.round(Math.log((double) G.getNoOfLeaves()) / Math.log(2.0));
			ps.discStem = Math.min(h + 5, 30);
		}
		return new RBTreeArcDiscretiser(S, times, ps.discMin, ps.discMax, ps.discTimestep, ps.discStem);
	}
	
	public static Triple<DoubleParameter, DoubleParameter, DupLossProbs> getDupLossProbs(Parameters ps, MPRMap mpr, RBTree s, TimesMap times) {
		// Set initial duplication rate as number of inferred MPR duplications divided by total time tree span.
		// Then set loss rate to the same amount.
		int dups = 0;
		double totTime = 0.0;
		for (int x = 0; x < times.getSize(); ++x) {
			if (mpr.isDuplication(x)) {
				++dups;
			}
			totTime += times.getArcTime(x);
		}
		DoubleParameter dr = new DoubleParameter("DuplicationRate", dups / totTime + 1e-3);
		DoubleParameter lr = new DoubleParameter("LossRate", dups / totTime + 1e-3);
		// TODO: Implement.
		DupLossProbs dlProbs = null; //new DupLossProbs(s, dr, lr);
		return new Triple<DoubleParameter, DoubleParameter, DupLossProbs>(dr, lr, dlProbs);
	}
	
}
