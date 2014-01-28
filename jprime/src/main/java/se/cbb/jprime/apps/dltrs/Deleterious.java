package se.cbb.jprime.apps.dltrs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.dltrs.ParameterParser;
import se.cbb.jprime.apps.dltrs.RealisationSampler;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.io.RBTreeSampleWrapper;
import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.io.UnparsedRealisation;
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
import se.cbb.jprime.misc.Quadruple;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.GammaSiteRateHandler;
import se.cbb.jprime.seqevo.MSAData;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandler;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;
import se.cbb.jprime.seqevo.SubstitutionModel;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.LeafLeafMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;

import com.beust.jcommander.JCommander;

/**
 * Java version of the Deleterious application
 * for simultaneous reconciliation and inference
 * of a guest tree evolving inside a dated host tree. Based on the DLTRS model.
 * 
 * @author Joel Sjöstrand.
 * @author Mehmood Alam Khan
 */
public class Deleterious implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "Deleterious";
	}
	
	@Override
	public void main(String[] args) {
		BufferedWriter info = null;
		Parameters params = null;
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			
			params = new Parameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"JPrIME-DLTRS (colloquially \"Deleterious\") is a phylogenetic tool to simul-\n" +
						"taneously infer and reconcile a gene tree given a species tree. It accounts for\n" +
						"duplication, loss and lateral transfer events, a relaxed molecular clock, and is\n" +
						"intended for the study of homologous gene families, for example in a comparative\n" +
						"genomics setting involving multiple species. It uses a Bayesian MCMC framework,\n" +
						"where the input is a known species tree with divergence times and a multiple\n" +
						"sequence alignment, and the output is a posterior distribution over gene trees\n" +
						"and model parameters.\n\n" +
						"References:\n" +
						"    TBA.\n\n" +
						"Releases, source code and tutorial: http://code.google.com/p/jprime/wiki/DLTRS\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar Deleterious [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// ================ READ AND CREATE ALL PARAMETERS ================
			
			// MCMC chain output and auxiliary info.
			SampleWriter sampler = ParameterParser.getOut(params);
			info = ParameterParser.getInfo(params);
			info.write("# =========================================================================\n");
			info.write("# ||                             PRE-RUN INFO                            ||\n");
			info.write("# =========================================================================\n");
			info.write("# DELETERIOUS\n");
			info.write("# Arguments: " + Arrays.toString(args) + '\n');
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			info.write("# Current time: " + df.format(cal.getTime()) + '\n');
			
			// Read S and t.
			Triple<RBTree, NamesMap, TimesMap> sNamesTimes = ParameterParser.getHostTree(params, info);
			
			// Read guest-to-host leaf map.
			GuestHostMap gsMap = ParameterParser.getGSMap(params);
			
			// Substitution model first, then sequence alignment D and site rates.
			SubstitutionMatrixHandler Q = SubstitutionMatrixHandlerFactory.create(params.substitutionModel, 4 * gsMap.getNoOfLeafNames());
			LinkedHashMap<String, ? extends Sequence<? extends Compound>> sequences = ParameterParser.getMultialignment(params, Q.getSequenceType());
			MSAData D = new MSAData(Q.getSequenceType(), sequences);
			Pair<DoubleParameter, GammaSiteRateHandler> siteRates = ParameterParser.getSiteRates(params);
			
			// Pseudo-random number generator.
			PRNG prng = ParameterParser.getPRNG(params);
			
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
			Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = ParameterParser.getGuestTreeAndLengths(params, gsMap, prng, sequences, info, guestTreeSamples);
			
			// Read number of iterations and thinning factor.
			Iteration iter = ParameterParser.getIteration(params);
			Thinner thinner = ParameterParser.getThinner(params, iter);
			
			// Read probability distribution for iid guest tree edge rates (molecular clock relaxation). 
			Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> edgeRatePD = ParameterParser.getEdgeRatePD(params);
			
			// Create discretisation of S.
			RBTreeEpochDiscretiser dtimes = ParameterParser.getDiscretizer(params, sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, gNamesLengths.first);
			
			// Create reconciliation helper.
			ReconciliationHelper rHelper = ParameterParser.getReconciliationHelper(params, gNamesLengths.first, sNamesTimes.first, dtimes,
					new LeafLeafMap(gsMap, gNamesLengths.first, gNamesLengths.second, sNamesTimes.first, sNamesTimes.second));
			
			// Duplication-loss probabilities over discretised S.
			Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs> dlt = ParameterParser.getDLTProbs(params, sNamesTimes.first, sNamesTimes.second,
					gNamesLengths.first, gNamesLengths.second, gsMap, dtimes);
			
			// ================ CREATE MODELS, PROPOSERS, ETC. ================
			
			// Priors. We only have them for parameters which might cause issues.
			RealInterval priorRange = new RealInterval(1e-16, 1e16, false, false);
			RealParameterUniformPrior edgeRateMeanPrior = new RealParameterUniformPrior(edgeRatePD.first, priorRange);
			RealParameterUniformPrior edgeRateCVPrior = new RealParameterUniformPrior(edgeRatePD.second, priorRange);
			RealParameterUniformPrior lengthsPrior = new RealParameterUniformPrior(gNamesLengths.third, priorRange);
			
			// Substitution model. NOTE: Root arc is turned on!!!!
			SubstitutionModel sm = new SubstitutionModel("SubstitutionModel", D, siteRates.second, Q, gNamesLengths.first, gNamesLengths.second, gNamesLengths.third, true);
			
			// DLTR model.
			DLTRModel dltr = new DLTRModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dlt.fourth, edgeRatePD.third);
			// mehmood's addtition here Ma7 24 2013
			// Sigma (mapping between G and S).	
			// DLTRMaxSampling model.
			DLTRMAPModel dltrMs = new DLTRMAPModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dlt.fourth, edgeRatePD.third);
			
			// Realisation sampler.
			RealisationSampler realisationSampler = ParameterParser.getRealisationSampler(params, iter, prng, dltr, dltrMs, gNamesLengths.second, params.maxRealizationFlag);
			
			// Proposers.
			NormalProposer dupRateProposer = ParameterParser.getNormalProposer(params, dlt.first, iter, prng, params.tuningDupRate);
			NormalProposer lossRateProposer = ParameterParser.getNormalProposer(params, dlt.second, iter, prng, params.tuningLossRate);
			NormalProposer transRateProposer = ParameterParser.getNormalProposer(params, dlt.third, iter, prng, params.tuningTransferRate);
			NormalProposer edgeRateMeanProposer = ParameterParser.getNormalProposer(params, edgeRatePD.first, iter, prng, params.tuningEdgeRateMean);
			NormalProposer edgeRateCVProposer = ParameterParser.getNormalProposer(params, edgeRatePD.second, iter, prng, params.tuningEdgeRateCV);
			NormalProposer siteRateShapeProposer = ParameterParser.getNormalProposer(params, siteRates.first, iter, prng, params.tuningSiteRateShape);
			Proposer guestTreeProposer = ParameterParser.getBranchSwapper(params, gNamesLengths.first, gNamesLengths.third, iter, prng, guestTreeSamples);
			NormalProposer lengthsProposer = ParameterParser.getNormalProposer(params, gNamesLengths.third, iter, prng, params.tuningLengths);
			double[] lengthsWeights = SampleDoubleArray.toDoubleArray(params.tuningLengthsSelectorWeights);
			lengthsProposer.setSubParameterWeights(lengthsWeights);
			
			// Proposer selector.
			MultiProposerSelector selector = ParameterParser.getSelector(params, prng);
			selector.add(dupRateProposer, ParameterParser.getProposerWeight(params.tuningWeightDupRate, iter));
			selector.add(lossRateProposer, ParameterParser.getProposerWeight(params.tuningWeightLossRate, iter));
			selector.add(transRateProposer, ParameterParser.getProposerWeight(params.tuningWeightTransferRate, iter));
			selector.add(edgeRateMeanProposer, ParameterParser.getProposerWeight(params.tuningWeightEdgeRateMean, iter));
			selector.add(edgeRateCVProposer, ParameterParser.getProposerWeight(params.tuningWeightEdgeRateCV, iter));
			selector.add(siteRateShapeProposer, ParameterParser.getProposerWeight(params.tuningWeightSiteRateShape, iter));
			selector.add(guestTreeProposer, ParameterParser.getProposerWeight(params.tuningWeightG, iter));
			selector.add(lengthsProposer, ParameterParser.getProposerWeight(params.tuningWeightLengths, iter));
			
			// Inactivate fixed proposers.
			if (params.dupRate != null        && params.dupRate.matches("FIXED|Fixed|fixed"))        { dupRateProposer.setEnabled(false); }
			if (params.lossRate != null       && params.lossRate.matches("FIXED|Fixed|fixed"))       { lossRateProposer.setEnabled(false); }
			if (params.transRate != null      && params.transRate.matches("FIXED|Fixed|fixed"))      { transRateProposer.setEnabled(false); }
			if (params.edgeRatePDMean != null && params.edgeRatePDMean.matches("FIXED|Fixed|fixed")) { edgeRateMeanProposer.setEnabled(false); }
			if (params.edgeRatePDCV != null   && params.edgeRatePDCV.matches("FIXED|Fixed|fixed"))   { edgeRateCVProposer.setEnabled(false); }
			if (params.siteRateCats == 1      || params.siteRateShape.matches("FIXED|Fixed|fixed"))  { siteRateShapeProposer.setEnabled(false); }
			if (params.guestTreeFixed)                                                               { guestTreeProposer.setEnabled(false); }
			if (params.lengthsFixed)                                                                 { lengthsProposer.setEnabled(false); }
			
			// Proposal acceptor.
			ProposalAcceptor acceptor = ParameterParser.getAcceptor(params, prng);
			
			// Overall statistics.
			FineProposerStatistics stats = new FineProposerStatistics(iter, 8);
			
			// ================ SETUP MCMC HIERARCHY ================
			
			MCMCManager manager = new MCMCManager(iter, thinner, selector, acceptor, sampler, prng, stats);
			manager.setDebugMode(params.debug);
			
			manager.addModel(edgeRateMeanPrior);
			manager.addModel(edgeRateCVPrior);
			manager.addModel(lengthsPrior);
			manager.addModel(sm);
			
			if (params.maxRealizationFlag == false){
				manager.addModel(dltr);
				manager.addSampleable(dltr);
			}else{
				manager.addModel(dltrMs);
				manager.addSampleable(dltrMs);
			}
			
			
			
			manager.addSampleable(iter);
			manager.addSampleable(manager);			// Overall likelihood.
			//manager.addSampleable(edgeRateMeanPrior);
			//manager.addSampleable(edgeRateCVPrior);
			//manager.addSampleable(lengthsPrior);
			manager.addSampleable(sm);
			
			
			
			manager.addSampleable(dlt.first);
			manager.addSampleable(dlt.second);
			manager.addSampleable(dlt.third);
			manager.addSampleable(edgeRatePD.first);
			manager.addSampleable(edgeRatePD.second);
			if (siteRateShapeProposer.isEnabled()) {
				manager.addSampleable(siteRates.first);
			}
			manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second));
			if (params.outputLengths) {
				manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second, gNamesLengths.third));
			}
			// mehmood's addition here
			if (realisationSampler != null) {
				manager.addSampleable(realisationSampler);
			}
			
			// ================ WRITE PRE-INFO ================
			info.write("# MCMC manager:\n");
			info.write(manager.getPreInfo("# \t"));
			info.flush();   // Don't close, maybe using stdout for both sampling and info...
			
			// ================ RUN ================
			manager.run();
			
//			Code for checking if the encoding and decoding of heatmap matrix is working fine.. 			
//			int index =0; 
//			int epoch = 0;
//			int dscPt = 0;
//			// Assign values to the heatmap
//			for(int i =0; i< dtimes.getTotalNoOfPoints(); i++, dscPt++){
//				if(dscPt >= dtimes.getEpoch(epoch).getNoOfPoints())
//				{ dscPt = 0; epoch++;}
//				
//				int idx = 0;
//				for(int i1 =0; i1< epoch; i1++){
//					idx += dtimes.getEpoch(i1).getNoOfPoints();
//				}
//				idx += dscPt;
//				
//				System.out.println(i+ "  :  " + epoch + ":"+dscPt + "   :   " + idx );
////				index += dtimes.getEpoch(i).getNoOfPoints();
//			}
//			
//			int maximumNoofArcsAtOneEpoch1 = dtimes.getEpoch(0).getNoOfArcs();
//			int fr = 0, t=0;
//			for(int i =0; i< maximumNoofArcsAtOneEpoch1*(maximumNoofArcsAtOneEpoch1-1); i++){
//				if(t >=maximumNoofArcsAtOneEpoch1-1)
//				{
//					t=0; fr++;
//				}
//				int index2 = fr *(maximumNoofArcsAtOneEpoch1-1) + t;
//				System.out.println(i + "  :  " + fr+"."+((t>=fr)?t+1:t) + "    :   "+ index2);
//				t++;
//			}
//			
//			System.out.println("maximumNoofArcsAtOneEpoch" + maximumNoofArcsAtOneEpoch1);
		

			// ================ WRITE POST-INFO ================
			info.write("# =========================================================================\n");
			info.write("# ||                             POST-RUN INFO                           ||\n");
			info.write("# =========================================================================\n");
			info.write("# DELETERIOUS\n");
			info.write("# MCMC manager:\n");
			info.write(manager.getPostInfo("# \t"));
			info.flush();
			sampler.close();
			info.close();
			
			

			
			// mehmood's addition here
			if (realisationSampler != null) { realisationSampler.close(); }
			
				if(params.heatmap != null)
				{
					int maximumNoofArcsAtOneEpoch = dtimes.getEpoch(0).getNoOfArcs();
					int possibleCombinations = maximumNoofArcsAtOneEpoch *(maximumNoofArcsAtOneEpoch -1);
					int [][] heatmap = new int[dtimes.getTotalNoOfPoints()][possibleCombinations];
					
					
					// Read the realization file
					File f = new File(params.sampleRealisations.get(0)); 
	//				File f = new File("/Users/mahmudi/Documents/samplerealization.txt");
					byte[] buffer = new byte[(int) f.length()];
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(f);
						fis.read(buffer);
						
					} finally {
						if (fis != null) try { fis.close(); } catch (IOException ex) {}
					}
				    String str = new String(buffer);
				    String[] strsplit = str.split("\t|\n");
				    for(int c=6;c<strsplit.length;c+=3)
				    {
				    	if(c % 3 == 0)
				    	{
				    		// map transfers of a realization to the corresponding places...
				    		System.out.println(strsplit[c]);
				    		Realisation realisation = UnparsedRealisation.parseRealisation(strsplit[c]);
				    		StringMap fromTos = realisation.getFromTos();
				    		BooleanMap isTrans = realisation.getTransfers();
				    		StringMap placements = realisation.getPlacements();
				    		
				    		for(int v =0; v< realisation.getTree().getNoOfVertices(); v++){
				    			if(isTrans.get(v)==true){
				    				int from , to;
				    				boolean special_transfer=false;
				    				from = Integer.parseInt(fromTos.get(v).substring( fromTos.get(v).indexOf("(")+1 , fromTos.get(v).indexOf(",")  ) );
				    				to = Integer.parseInt(fromTos.get(v).substring( fromTos.get(v).indexOf(",")+1 , fromTos.get(v).lastIndexOf(",")  ) );
				    				int sp_transfer = Integer.parseInt(fromTos.get(v).substring( fromTos.get(v).lastIndexOf(",")+1 , fromTos.get(v).lastIndexOf(")")  ) );
				    				special_transfer = (sp_transfer == -1)?false:true;
				    				int epochNo = Integer.parseInt(placements.get(v).substring( placements.get(v).indexOf("(")+1, placements.get(v).indexOf(",")));
				    				int discPt = Integer.parseInt(placements.get(v).substring( placements.get(v).indexOf(",")+1, placements.get(v).indexOf(")")));
	
				    				int idx = 0;
				    				for(int i =0; i< epochNo; i++){
				    					idx += dtimes.getEpoch(i).getNoOfPoints();
				    				}
				    				idx += discPt;
				    				int idx2 = from *(maximumNoofArcsAtOneEpoch-1) + ((from<to)?to-1:to);
				    				heatmap[idx][idx2]++;
				    			}
				    		}
				    		
				    	}
				    }
						
				    try {
					    PrintWriter heatmapout = new PrintWriter(new BufferedWriter(new FileWriter(params.heatmap.get(0), false)));
					    heatmapout.println("#HeatMap: [colums: epochs+disc_points x rows:transfers_from_to ] (time points x Species Edge/Vertex No) = value (count(realized vertices))");
				    	for(int i=0; i< dtimes.getTotalNoOfPoints(); i++){
				    		for(int j=0; j< possibleCombinations; j++)
				    			heatmapout.print(heatmap[i][j] + "\t");
				    		heatmapout.println();
				    	}
				    	heatmapout.close();
					} catch (IOException e) {
					    System.out.println("Problem with accessing file " + params.heatmap);
					}
				    
				    
				}
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
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
			if (params != null && params.uncatch) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
}
