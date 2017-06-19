package se.cbb.jprime.apps.pdlrs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.Sequence;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.io.RBTreeSampleWrapper;
import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.FineProposerStatistics;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.MCMCManager;
import se.cbb.jprime.mcmc.MultiProposerSelector;
import se.cbb.jprime.mcmc.NormalProposer;
import se.cbb.jprime.mcmc.ProposalAcceptor;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.RealParameterUniformPrior;
import se.cbb.jprime.mcmc.Thinner;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.GammaSiteRateHandler;
import se.cbb.jprime.seqevo.MSAData;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandler;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;
import se.cbb.jprime.seqevo.SubstitutionModel;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.TimesMap;

import com.beust.jcommander.JCommander;

/**
 * Java version of the Delirious application (previously known as GSR and GSRf)
 * for simultaneous reconciliation and inference
 * of a guest tree evolving inside a dated host tree. Based on the DLRS model. 
 * 
 * @author Joel Sjöstrand.
 */
public class pDelirious implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "pDelirious";
	}
	
	@Override
	public void main(String[] args) {
		BufferedWriter info = null;
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			
			PDLRSParameters params = new PDLRSParameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"JPrIME-pDLRS (colloquially \"pseudogenized-Delirious\") is a phylogenetic tool to\n" +
						"simultaneously infer and reconcile a gene tree given a species tree. It accounts \n" +
						"for duplication and loss events, a relaxed molecular clock, and is intended for \n" +
						"the study of homologous gene families, for example in a comparative genomics setting\n" +
						"involving multiple species. It uses a Bayesian MCMC framework, where the input\n" +
						"is a known species tree with divergence times and a multiple sequence alignment,\n" +
						"and the output is a posterior distribution over gene trees and model parameters.\n\n" +
						"References:\n" +
						"    DLRS: Gene tree evolution in light of a species tree,\n" +
						"    Sjostrand et al., Bioinformatics, 2012, doi: 10.1093/bioinformatics/bts548.\n\n" +
						"    Simultaneous Bayesian gene tree reconstruction and reconciliation analysis,\n" +
						"    Akerborg et al., PNAS, 2009, doi: 10.1073/pnas.0806251106.\n\n" +
						"Releases, source code and tutorial: http://code.google.com/p/jprime/wiki/DLRS\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar pDelirious [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// ================ READ AND CREATE ALL PARAMETERS ================
			
			// MCMC chain output and auxiliary info.
			SampleWriter sampler = PDLRSParameterParser.getOut(params);
			info = PDLRSParameterParser.getInfo(params);
			info.write("# =========================================================================\n");
			info.write("# ||                             PRE-RUN INFO                            ||\n");
			info.write("# =========================================================================\n");
			info.write("# pDELIRIOUS\n");
			info.write("# Arguments: " + Arrays.toString(args) + '\n');
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			info.write("# Current time: " + df.format(cal.getTime()) + '\n');
			
			// Read S and t.
			Triple<RBTree, NamesMap, TimesMap> sNamesTimes = PDLRSParameterParser.getHostTree(params, info);
			
			// Read guest-to-host leaf map.
			GuestHostMap gsMap = PDLRSParameterParser.getGSMap(params);
			
			
			// Substitution model first, then sequence alignment D and site rates.
			SubstitutionMatrixHandler Q = SubstitutionMatrixHandlerFactory.create(params.substitutionModel, 4 * gsMap.getNoOfLeafNames());
			LinkedHashMap<String, ? extends Sequence<? extends Compound>> sequences = PDLRSParameterParser.getMultialignment(params, Q.getSequenceType());
			MSAData D = new MSAData(Q.getSequenceType(), sequences);
			Pair<DoubleParameter, GammaSiteRateHandler> siteRates = PDLRSParameterParser.getSiteRates(params);
			
			// Pseudo-random number generator.
			PRNG prng = PDLRSParameterParser.getPRNG(params);
			
			// Read/create G and l.
			NewickRBTreeSamples guestTreeSamples = null;
			if (params.guestTreeSet != null) {
				Double burninProp = Double.parseDouble(params.guestTreeSetBurninProp);
				Double minCvg = Double.parseDouble(params.guestTreeSetMinCvg);
				if (params.guestTreeSetWithLengths) {
					guestTreeSamples = NewickRBTreeSamples.readTreesWithLengths(new File(params.guestTreeSet), params.guestTreeSetFileHasHeader,
							params.guestTreeSetFileRelColNo, burninProp, minCvg);
				} else {
					guestTreeSamples = NewickRBTreeSamples.readTreesWithoutLengths(new File(params.guestTreeSet), params.guestTreeSetFileHasHeader,
							params.guestTreeSetFileRelColNo, burninProp, minCvg);
				}
			}
			Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = PDLRSParameterParser.getGuestTreeAndLengths(params, gsMap, prng, sequences, info, guestTreeSamples, D);
			
			DoubleMap pgSwitches = new DoubleMap("G-PGSwitches", gNamesLengths.first.getNoOfVertices(), 1);
			IntMap edgeModels = new IntMap("EdgeModels", gNamesLengths.first.getNoOfVertices(), 1);
			List<Integer> leaves = gNamesLengths.first.getLeaves();
			for(int leaf: leaves){
				pgSwitches.set(leaf, 0.5);
				edgeModels.set(leaf, 2);
			}
			System.out.println("Gene tree have "+gNamesLengths.first.getNoOfVertices()+ " vertices");
			
			
			// Read number of iterations and thinning factor.
			Iteration iter = PDLRSParameterParser.getIteration(params);
			Thinner thinner = PDLRSParameterParser.getThinner(params, iter);
			
			// Sigma (mapping between G and S).
			MPRMap mprMap = new MPRMap(gsMap, gNamesLengths.first, gNamesLengths.second, sNamesTimes.first, sNamesTimes.second);
			
			// Read probability distribution for iid guest tree edge rates (molecular clock relaxation). 
			Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> edgeRatePD = PDLRSParameterParser.getEdgeRatePD(params);
			
			// Create discretisation of S.
			RBTreeArcDiscretiser dtimes = PDLRSParameterParser.getDiscretizer(params, sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, gNamesLengths.first);
			
			// Create reconciliation helper.
			ReconciliationHelper rHelper = PDLRSParameterParser.getReconciliationHelper(params, gNamesLengths.first, sNamesTimes.first, dtimes, mprMap);
			
			// Duplication-loss probabilities over discretised S.
			Triple<DoubleParameter, DoubleParameter, DupLossProbs> dupLoss = PDLRSParameterParser.getDupLossProbs(params, mprMap, sNamesTimes.first, gNamesLengths.first, dtimes);
			
			// ================ CREATE MODELS, PROPOSERS, ETC. ================
			
			// Priors. We only have them for parameters which might cause issues.
			RealInterval priorRange = new RealInterval(1e-16, 1e16, false, false);
			RealParameterUniformPrior edgeRateMeanPrior = new RealParameterUniformPrior(edgeRatePD.first, priorRange);
			RealParameterUniformPrior edgeRateCVPrior = new RealParameterUniformPrior(edgeRatePD.second, priorRange);
			RealParameterUniformPrior lengthsPrior = new RealParameterUniformPrior(gNamesLengths.third, priorRange);
			
			// Substitution model. NOTE: Root arc is turned on!!!!
			SubstitutionModel sm = new SubstitutionModel("SubstitutionModel", D, siteRates.second, Q, gNamesLengths.first, gNamesLengths.second, gNamesLengths.third, true);
			
			// DLR model.
			DLRModel dlr = new DLRModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dupLoss.third, edgeRatePD.third);
			
			// Realisation sampler.
			RealisationSampler realisationSampler = PDLRSParameterParser.getRealisationSampler(params, iter, prng, dlr, gNamesLengths.second);
			
			// Proposers.
			NormalProposer dupRateProposer = PDLRSParameterParser.getNormalProposer(params, dupLoss.first, iter, prng, params.tuningDupRate);
			NormalProposer lossRateProposer = PDLRSParameterParser.getNormalProposer(params, dupLoss.second, iter, prng, params.tuningLossRate);
			NormalProposer edgeRateMeanProposer = PDLRSParameterParser.getNormalProposer(params, edgeRatePD.first, iter, prng, params.tuningEdgeRateMean);
			NormalProposer edgeRateCVProposer = PDLRSParameterParser.getNormalProposer(params, edgeRatePD.second, iter, prng, params.tuningEdgeRateCV);
			NormalProposer siteRateShapeProposer = PDLRSParameterParser.getNormalProposer(params, siteRates.first, iter, prng, params.tuningSiteRateShape);
			Proposer guestTreeProposer = PDLRSParameterParser.getBranchSwapper(params, gNamesLengths.first, gNamesLengths.third, mprMap, iter, prng, guestTreeSamples);
			NormalProposer lengthsProposer = PDLRSParameterParser.getNormalProposer(params, gNamesLengths.third, iter, prng, params.tuningLengths);
			double[] lengthsWeights = SampleDoubleArray.toDoubleArray(params.tuningLengthsSelectorWeights);
			lengthsProposer.setSubParameterWeights(lengthsWeights);
			
			// Proposer selector.
			MultiProposerSelector selector = PDLRSParameterParser.getSelector(params, prng);
			selector.add(dupRateProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightDupRate, iter));
			selector.add(lossRateProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightLossRate, iter));
			selector.add(edgeRateMeanProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightEdgeRateMean, iter));
			selector.add(edgeRateCVProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightEdgeRateCV, iter));
			selector.add(siteRateShapeProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightSiteRateShape, iter));
			selector.add(guestTreeProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightG, iter));
			selector.add(lengthsProposer, PDLRSParameterParser.getProposerWeight(params.tuningWeightLengths, iter));
			
			// Inactivate fixed proposers.
			if (params.dupRate != null        && params.dupRate.matches("FIXED|Fixed|fixed"))        { dupRateProposer.setEnabled(false); }
			if (params.lossRate != null       && params.lossRate.matches("FIXED|Fixed|fixed"))       { lossRateProposer.setEnabled(false); }
			if (params.edgeRatePDMean != null && params.edgeRatePDMean.matches("FIXED|Fixed|fixed")) { edgeRateMeanProposer.setEnabled(false); }
			if (params.edgeRatePDCV != null   && params.edgeRatePDCV.matches("FIXED|Fixed|fixed"))   { edgeRateCVProposer.setEnabled(false); }
			if (params.siteRateCats == 1      || params.siteRateShape.matches("FIXED|Fixed|fixed"))  { siteRateShapeProposer.setEnabled(false); }
			if (params.guestTreeFixed)                                                               { guestTreeProposer.setEnabled(false); }
			if (params.lengthsFixed)                                                                 { lengthsProposer.setEnabled(false); }
			
			// Proposal acceptor.
			ProposalAcceptor acceptor = PDLRSParameterParser.getAcceptor(params, prng);
			
			// Overall statistics.
			FineProposerStatistics stats = new FineProposerStatistics(iter, 8);
			
			// ================ SETUP MCMC HIERARCHY ================
			
			MCMCManager manager = new MCMCManager(iter, thinner, selector, acceptor, sampler, prng, stats);
			manager.setDebugMode(params.debug);
			
			manager.addModel(edgeRateMeanPrior);
			manager.addModel(edgeRateCVPrior);
			manager.addModel(lengthsPrior);
			manager.addModel(sm);
			manager.addModel(dlr);
			
			manager.addSampleable(iter);
			manager.addSampleable(manager);			// Overall likelihood.
			//manager.addSampleable(edgeRateMeanPrior);
			//manager.addSampleable(edgeRateCVPrior);
			//manager.addSampleable(lengthsPrior);
			manager.addSampleable(sm);
			manager.addSampleable(dlr);
			manager.addSampleable(dupLoss.first);
			manager.addSampleable(dupLoss.second);
			manager.addSampleable(edgeRatePD.first);
			manager.addSampleable(edgeRatePD.second);
			if (siteRateShapeProposer.isEnabled()) {
				manager.addSampleable(siteRates.first);
			}
			manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second));
			if (params.outputLengths) {
				manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second, gNamesLengths.third));
			}
			if (realisationSampler != null) {
				manager.addSampleable(realisationSampler);
			}
			
			// ================ WRITE PRE-INFO ================
			info.write("# MCMC manager:\n");
			info.write(manager.getPreInfo("# \t"));
			info.flush();   // Don't close, maybe use stdout for both sampling and info...
			
			// ================ RUN ================
			manager.run();
			
			// ================ WRITE POST-INFO ================
			info.write("# =========================================================================\n");
			info.write("# ||                             POST-RUN INFO                           ||\n");
			info.write("# =========================================================================\n");
			info.write("# pDELIRIOUS\n");
			info.write("# MCMC manager:\n");
			info.write(manager.getPostInfo("# \t"));
			info.flush();
			sampler.close();
			info.close();
			if (realisationSampler != null) { realisationSampler.close(); }
			
			
		} catch (Exception e) {
		    //			e.printStackTrace(System.err);
		    String msg = e.getMessage();
			System.err.print("\nERROR: " + msg + "\n\nUse option -h or --help to show usage.\nSee .info file for more information.\n");
			if (info != null) {
				Writer w = new StringWriter();
			    PrintWriter pw = new PrintWriter(w);
 			    e.printStackTrace(pw);
				try {
					info.write("# Run failed. Reason:\n" + w.toString());
					info.close();
					pw.close();
					w.close();
				} catch (IOException f) {
				}
			}
		}
	}
	
	
}
