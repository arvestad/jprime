package se.cbb.jprime.apps.dltrs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Scanner;
import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.Sequence;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.dltrs.DLTRSParameterParser;
import se.cbb.jprime.apps.dltrs.RealisationSampler;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.io.Sampleable.SamplingMode;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.Thinner;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Quadruple;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.GammaSiteRateHandler;
import se.cbb.jprime.seqevo.MSAData;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandler;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;
import se.cbb.jprime.seqevo.SubstitutionModel;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.LeafLeafMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;
import com.beust.jcommander.JCommander;

/**
 * 
 * 
 * @author Mehmood Alam Khan
 */
public class SampRealFromPosterior implements JPrIMEApp {

	
	private Triple<RBTree, NamesMap, DoubleMap> getGeneTreeAndLengthsFromString(String geneTreeString, Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> edgeRatePD, PRNG prng) {
		RBTree g = null;
		NamesMap gNames = null;
		DoubleMap gLengths = null;
		int ATTEMPTS=0;
		boolean FLAG=false;
		do{
		try {
			PrIMENewickTree pg= PrIMENewickTreeReader.readTree(geneTreeString, true, false);
			NewickTree newickGTree= NewickTreeReader.readTree(geneTreeString, true);
			g= new RBTree(newickGTree, "sahi da") ;
			gNames= newickGTree.getVertexNamesMap(true, "haji bass ka");
	
			gLengths =  newickGTree.getBranchLengthsMap("Lengths");
			
			for (int v=0; v < g.getNoOfVertices(); v++){
				if(!g.isRoot(v))
				{
					double parentBLen= gLengths.get(g.getParent(v));
					gLengths.set(v, (parentBLen - gLengths.get(v))* edgeRatePD.third.sampleValue(prng));
				}else{
					gLengths.set(v, 0.2 * edgeRatePD.third.sampleValue(prng));
				}
				
			}
			
			
		} catch (Exception e) {
			ATTEMPTS+=1;
			FLAG=true;
			System.out.println("Attempts: "+  ATTEMPTS+ "  edgeRatePD.third.sampleValue(seed): "+ edgeRatePD.third.sampleValue(prng));
			prng= new PRNG();
			if (ATTEMPTS  > 10){
				return null;
				//throw new IllegalArgumentException("Invalid fixed guest tree set parameter or file.", e);
			}
		}
		}while(FLAG==true);
		
		return new Triple<RBTree, NamesMap, DoubleMap>(g, gNames, gLengths);
	}
	
	@Override
	public String getAppName() {
		return "SampRealFromPosterior";
	}

	@Override
	public void main(String[] args) {
		BufferedWriter info = null;
		DLTRSParameters params = null;
		BufferedWriter bw= null;
		Scanner sc = null;
		try {

			// ================ PARSE USER OPTIONS AND ARGUMENTS ================

			params = new DLTRSParameters();
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
				"    java -jar jprime-X.Y.Z.jar SampleRealFromPosterior [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}

			// ================ READ AND CREATE ALL PARAMETERS ================

			// MCMC chain output and auxiliary info.
			SampleWriter sampler = DLTRSParameterParser.getOut(params);
			info = DLTRSParameterParser.getInfo(params);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			
			
			
			// Read S and t.
			Triple<RBTree, NamesMap, TimesMap> sNamesTimes = DLTRSParameterParser.getHostTree(params, info);

			// Read guest-to-host leaf map.
			GuestHostMap gsMap = DLTRSParameterParser.getGSMap(params);

			// Substitution model first, then sequence alignment D and site rates.
			SubstitutionMatrixHandler Q = SubstitutionMatrixHandlerFactory.create(params.substitutionModel, 4 * gsMap.getNoOfLeafNames());
			LinkedHashMap<String, ? extends Sequence<? extends Compound>> sequences = DLTRSParameterParser.getMultialignment(params, Q.getSequenceType());
			MSAData D = new MSAData(Q.getSequenceType(), sequences);
			Pair<DoubleParameter, GammaSiteRateHandler> siteRates = DLTRSParameterParser.getSiteRates(params);

			// Pseudo-random number generator.
			PRNG prng = DLTRSParameterParser.getPRNG(params);

			// posterior mcmc file
			
			try{
				
				
				sc = new Scanner(new File(params.runSampRealFromPosterior.get(0)));
				sc.nextLine();
				while (sc.hasNextLine()) {
					String ln = sc.nextLine().trim();
					if (ln.equals("")) {
						continue;
					}
					String[] parts = ln.split("\t");
					String geneTreeString= parts[parts.length-1];
					
					String iterations= parts[0];
					double d1= Double.parseDouble(parts[7]);
					double d2= Double.parseDouble(parts[8]);
					double d=  Double.parseDouble(parts[4]);
					double l=  Double.parseDouble(parts[5]);
					double t=  Double.parseDouble(parts[6]);
					
					// Read probability distribution for iid guest tree edge rates (molecular clock relaxation). 
					Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> edgeRatePD = DLTRSParameterParser.getEdgeRatePD(params, d1, d2);
					
					Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = this.getGeneTreeAndLengthsFromString(geneTreeString, edgeRatePD, prng);
					if (gNamesLengths == null){
						continue;
					}
					// Read number of iterations and thinning factor.
					Iteration iter = DLTRSParameterParser.getIteration(params);
					Thinner thinner = DLTRSParameterParser.getThinner(params, iter);

					
					
					
					// Create discretisation of S.
					RBTreeEpochDiscretiser dtimes = DLTRSParameterParser.getDiscretizer(params, sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, gNamesLengths.first);
					
					// Create reconciliation helper.
					ReconciliationHelper rHelper = DLTRSParameterParser.getReconciliationHelper(params, gNamesLengths.first, sNamesTimes.first, dtimes,
							new LeafLeafMap(gsMap, gNamesLengths.first, gNamesLengths.second, sNamesTimes.first, sNamesTimes.second));

					// Duplication-loss probabilities over discretised S.
					Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, EpochDLTProbs> dlt = DLTRSParameterParser.getDLTProbs( dtimes, d,l, t);
					
					// Substitution model. NOTE: Root arc is turned on!!!!
					SubstitutionModel sm = new SubstitutionModel("SubstitutionModel", D, siteRates.second, Q, gNamesLengths.first, gNamesLengths.second, gNamesLengths.third, true);

					// DLTR model.
					DLTRModel dltr = new DLTRModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dlt.fourth, edgeRatePD.third);
					// Sigma (mapping between G and S).	
					// DLTRMaxSampling model.
					DLTRMAPModel dltrMs = new DLTRMAPModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dlt.fourth, edgeRatePD.third);

					// Realisation sampler.
					RealisationSampler realisationSampler = DLTRSParameterParser.getRealisationSampler(params, iter, prng, dltr, dltrMs, gNamesLengths.second, params.maxRealizationFlag);
					
					
					info.write(iterations+"\t"+realisationSampler.getSampleValue(SamplingMode.ORDINARY) + "\n");
					
					//if (realisationSampler != null) { realisationSampler.close(); }
				}
				info.flush();
				sampler.close();
				info.close();
				sc.close();
			} catch (IOException e) {
				sc.close();
				System.out.println("Problem with accessing file " + params.runSampRealFromPosterior.get(0));
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
					//bw.close();
					w.close();
					sc.close();
				} catch (IOException f) {
				}
			}
			if (params != null && params.uncatch) {
				throw new RuntimeException(e);
			}
		}
	}


}
