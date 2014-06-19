package se.cbb.jprime.apps.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.analysis.ParameterParser;
import se.cbb.jprime.apps.pdlrs.Realisation;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.io.RBTreeSampleWrapper;
import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.io.SampleNewickTree;
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
import se.cbb.jprime.mcmc.UniformProposer;
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
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTree;
import se.cbb.jprime.topology.TimesMap;

import com.beust.jcommander.JCommander;


/**
 * Java version of the Delirious application (previously known as GSR and GSRf)
 * for simultaneous reconciliation and inference
 * of a guest tree evolving inside a dated host tree. Based on the DLRS model. 
 * 
 * @author Joel Sjöstrand
 * @author Owais Mahmudi
 */
public class Analysis implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "Analysis";
	}
	
	@Override
	public void main(String[] args) {
		BufferedWriter info = null;
		try {
			
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			
			Parameters params = new Parameters();
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
						"    java -jar jprime-X.Y.Z.jar Analysis [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			if(params.files.get(2) != null && params.inmcmcfile != null && params.check != false){
				// Check if all the samples in MCMC file and the Realisation file have one to one correspondence..
				File inrealfile1 = new File(params.files.get(2));
				File inmcmcfile1 = new File(params.inmcmcfile);
				String reals="";
				Scanner scr = new Scanner(inrealfile1);
				Scanner scm = new Scanner(inmcmcfile1);
				scr.nextLine(); scr.nextLine();
				scm.nextLine();

				while(scm.hasNextLine()){
					String rline = scr.nextLine();
					String mline = scm.nextLine();

					int rSampleNo = Integer.parseInt(rline.split("\t")[0]);
					int mSampleNo = Integer.parseInt(mline.split("\t")[0]);


					if(UnparsedRealisation.isReadable(rline.split("\t")[2] )){
						String realisation = rline.split("\t")[2];
						Realisation r = UnparsedRealisation.parseToRealisation(realisation, true);
						//				System.out.println(rSampleNo + "\t" + r.getGuestTree().toString());
						try {
							String realis = SampleNewickTree.toString(r.getGuestTree(), r.getNamesMap(), null);
							String mcmcgtree = mline.split("\t")[11];
							if(!realis.equalsIgnoreCase(mcmcgtree)){
								System.out.println("The two trees are not equal!");
								System.out.println(rSampleNo + "\t" +SampleNewickTree.toString(r.getGuestTree(), r.getNamesMap(), null));
								System.out.println(mSampleNo + "\t" + mline.split("\t")[11]);
							}
							//					System.out.println(rSampleNo + "\t" +SampleNewickTree.toString(r.getGuestTree(), r.getNamesMap(), null));
						} catch (NewickIOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//				System.out.println(mSampleNo + "\t" + mline.split("\t")[11]);
					}
				}
			}else
			{
			
			
			// ================ READ AND CREATE ALL PARAMETERS ================
			
			//RBTree sTree = ParameterParser.getHostTree(params, info);
			//System.out.println(sTree.getNoOfVertices() + " vertices in species tree" );
			
			String mapgenetreeproportion = "";
			boolean mapgenetreefound=false;
			double treesToSkip =0;
			
			if(params.inmcmcfile != null )
			{
				// Read the MCMC file
				File fmcmc = new File(params.inmcmcfile); 
	//			int noOfArgs = Integer.parseInt(params.inmcmcfile.get(1));
				double burnin = Double.parseDouble(params.guestTreeSetBurninProp);
				NewickRBTreeSamples trees = NewickRBTreeSamples.readTreesWithoutLengths(fmcmc, true, 1, burnin, 0.01);
				int totalTrees=trees.getTotalTreeCount();
				treesToSkip = burnin*totalTrees;
				int totalVertices = trees.getTree(0).getNoOfVertices();
				mapgenetreeproportion = "MAP gene tree proportion:" + "\t" + trees.getTreeCount(0) + "/"+ trees.getTotalTreeCount() + " (" + trees.getTreeCount(0)/(double)trees.getTotalTreeCount() + ") \n";
				System.out.println(mapgenetreeproportion);
				DoubleMap pgSwitches = new DoubleMap("Pseudogenization Switches", totalVertices);
				DoubleMap truePGSwitches = new DoubleMap("True Pseudogenization Switches", totalVertices);
				LinkedHashMap<String, Integer> gpgMap = ParameterParser.getGenePseudogeneMap(params);
				if(params.files.get(1) != null)
				{
					// Read the true tree with pseudo-switches
					if(params.files.get(0) != null)
					{
						File truetree = new File(params.files.get(0));
						Scanner sct = new Scanner(truetree);
						String realisation = sct.nextLine();
						Realisation trueReal = UnparsedRealisation.parseToRealisation(realisation, true);
						int vertices = trueReal.getGuestTree().getNoOfVertices();
						for (int vr = 0; vr < vertices; vr++)
						{
							if(trueReal.getPGPoint(vr) != 1)
								truePGSwitches.set(vr, trueReal.getPGPoint(vr));
							else
								truePGSwitches.set(vr, 1);
						}
						
						if(trueReal.getGuestTree().toString().equalsIgnoreCase(trees.getTree(0).toString()) )
							mapgenetreefound = true;
					}
					
					if(mapgenetreefound)
					{

						// Read the Real file
						File inrealfile = new File(params.files.get(1));

						String real1="";
						Scanner sc = new Scanner(inrealfile);

						int i = 0;
						String sTree="";
						if(sc.hasNextLine()){
							sTree = sc.nextLine(); sc.nextLine();}

						// Read S and t.
						Triple<RBTree, NamesMap, TimesMap> sNamesTimes = ParameterParser.getHostTree(sTree.substring(sTree.indexOf("# Host tree: ")+13, sTree.length()));

						String snmin = sTree.substring(sTree.indexOf("NMIN="), sTree.length());
						snmin = snmin.substring(5, (snmin.indexOf(" ")==-1)?snmin.indexOf("]"):snmin.indexOf(" ") );
						int nmin = Integer.parseInt(snmin);

						String snmax = sTree.substring(sTree.indexOf("NMAX="), sTree.length());
						snmax = snmax.substring(5, (snmax.indexOf(" ")==-1)?snmax.indexOf("]"):snmax.indexOf(" "));
						int nmax =  Integer.parseInt(snmax);

						String sstep = sTree.substring(sTree.indexOf("DELTAT="), sTree.length());
						sstep = sstep.substring(7, (sstep.indexOf(" ")==-1)?sstep.indexOf("]"):sstep.indexOf(" "));
						double step = Double.parseDouble(sstep);

						String sstem = sTree.substring(sTree.indexOf("NROOT="), sTree.length());
						sstem = sstem.substring(6, (sstem.indexOf(" ")==-1)?sstem.indexOf("]"):sstem.indexOf(" ") );
						int stem = Integer.parseInt(sstem);

						// Create discretisation of S.
						RBTreeArcDiscretiser dtimes = ParameterParser.getDiscretizer(sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, nmin, nmax, step, stem);

						try
						{
							int mapsamples=0, allsamples=0;
							PrintWriter outstats = new PrintWriter(new BufferedWriter(new FileWriter(params.outfile, false)));
							while (sc.hasNextLine()) {
								allsamples++;
								String line = sc.nextLine();
								
								int sampleNo = Integer.parseInt(line.split("\t")[0]);
								if(sampleNo == 532000)
									System.out.println();
								if(allsamples >= treesToSkip){
									if(UnparsedRealisation.isReadable(line.split("\t")[2] )){
										String realisation = line.split("\t")[2];
										Realisation r = UnparsedRealisation.parseToRealisation(realisation, true);
										if( trees.getTree(0).toString().equalsIgnoreCase(r.getGuestTree().toString())){
											mapsamples++;

											int vertices = r.getGuestTree().getNoOfVertices();
											for (int vr = 0; vr < vertices; vr++)
											{
												if(r.getPGPoint(vr) != 1)
													pgSwitches.set(vr, r.getPGPoint(vr));
												else
													pgSwitches.set(vr, 1);
											}
											int falseSample=0;
											List<Integer> leaves = r.getGuestTree().getLeaves();
											for(Integer l: leaves)
											{
												if(gpgMap.get(r.getVertexName(l.intValue()))==1)
												{
													int numberofswitches=0;
													int v=l.intValue();
													while(!r.getGuestTree().isRoot(v))
													{
														if(pgSwitches.get(v)!=1)
															numberofswitches++;
														v=r.getGuestTree().getParent(v);
													}
													if(r.getGuestTree().isRoot(v) && pgSwitches.get(v)!=1)
														numberofswitches++;
													if(numberofswitches ==0 )
														falseSample=1;	
													if(numberofswitches>1)
														falseSample=2;
												}
											}

											PrintWriter faultysamples = new PrintWriter(new BufferedWriter(new FileWriter(params.outfile+".faultysamples.txt", true)));

											if(falseSample > 0)
												faultysamples.printf("%7d \t %d\n", sampleNo, falseSample);
											faultysamples.close();


											double[] realisationDistance = new double[4];
											int countOfSwitches=0;
											double sumDistance=0; double sumTime=0;
											if(falseSample==0)
											{	
												for (int vr = 0; vr < vertices; vr++)
												{
													if(truePGSwitches.get(vr) != 1){
														countOfSwitches++;
														double[] distances = findTheDistance(vr, r, dtimes, truePGSwitches, pgSwitches);
//														System.out.println("Vertex no "+ vr);
//														for(int ds=0; ds<distances.length; ds++)
//															System.out.print("\t"+distances[ds]);
//														System.out.println();
														if((distances[1]<0 && distances[3] > 0) || (distances[1]>0 && distances[3] < 0))
															System.out.println();

														if(Math.abs(realisationDistance[0])<Math.abs(distances[0]))
															realisationDistance[0]=distances[0];
														if(Math.abs(realisationDistance[2])<Math.abs(distances[2]))
															realisationDistance[2]=distances[2];
														sumDistance+=(distances[1]);
														sumTime+=(distances[3]);
//														System.out.printf("%-3.4f \t %-3.4f \t %-3.4f \t %-3.4f\n", distances[0],distances[1],distances[2],distances[3]);
														//									System.out.println("max-edge distance = "+ distances[0] + " \navg-edge distance = " +distances[1]+" \nmax-time distance = "+distances[2]+" \navg-time distance = " + distances[3]);
													}
												}


												realisationDistance[1]=sumDistance/(double)countOfSwitches;
												realisationDistance[3]=sumTime/(double)countOfSwitches;
//												System.out.printf("%5d \t %-3.4f \t %-3.4f \t %-3.4f \t %-3.4f\n", sampleNo,realisationDistance[0],realisationDistance[1],realisationDistance[2],realisationDistance[3]);
												outstats.printf("%5d \t %-3.4f \t %-3.4f \t %-3.4f \t %-3.4f\n", sampleNo,realisationDistance[0],realisationDistance[1],realisationDistance[2],realisationDistance[3]);
											}		
											i++;
											real1 = r.toString();
										}
										//								else
										//								{
										//									if(Integer.parseInt(mapIDs.get(i)) == sampleNo)
										//										i++;
										//								}
									}
								}
							}
							
							System.out.println("All samples were "+ allsamples + ", while map samples were "+ mapsamples);
							outstats.close();
						}catch(IOException e)
						{
							e.printStackTrace();
						}

						// Print stats

						System.out.println(real1);
						for(int v=0;v<pgSwitches.getSize(); v++)
							System.out.println(v + " - " + pgSwitches.get(v));
						System.out.println();

					}else
					{
						try{
						PrintWriter maptree = new PrintWriter(new BufferedWriter(new FileWriter(params.outfile+".maptree.txt", true)));
						maptree.printf("%15s \t Not Matched\n", params.files.get(1) );
						System.out.printf("%15s \t Not Matched\n", params.files.get(1) );
						maptree.close();
						}catch(IOException e)
						{ e.printStackTrace();}
					}
				}
			}

			
			//TODO: Initialize switches count.. 
//			IntMap switchesCount = new IntMap("PseudoSwitches", sTree.getNoOfVertices());
			
			// Read the real file and update counts for each vertex
//			if(params.files.get(0) != null )
//			{
//				try{
//					// Read the Real file
//					File inrealfile = new File(params.files.get(1));
//					Scanner sc = new Scanner(inrealfile);
//					int i = 0;
//					if(sc.hasNextLine()){
//						// Skip two lines of header
//						sc.nextLine(); sc.nextLine();}
//					
//					while (sc.hasNextLine()) {
//						String line = sc.nextLine();
//						String realSample = line.split("\t")[2];
//						System.out.println(realSample);
//						Realisation r = UnparsedRealisation.parseToRealisation(realSample);
//						int vertices = r.getGuestTree().getNoOfVertices();
//						for (int vr = 0; vr < vertices; vr++)
//						{
//							System.out.println("Vertex: " + vr + " .. PG point = " + r.getPGPoint(vr));
//						}
//						
//						
//					}
//				}catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
			
			
//			System.out.println( sTree );
			
			// MCMC chain output and auxiliary info.
//			SampleWriter sampler = ParameterParser.getOut(params);
//			info = ParameterParser.getInfo(params);
//			info.write("# =========================================================================\n");
//			info.write("# ||                             PRE-RUN INFO                            ||\n");
//			info.write("# =========================================================================\n");
//			info.write("# Analysis\n");
//			info.write("# Arguments: " + Arrays.toString(args) + '\n');
//			Calendar cal = Calendar.getInstance();
//		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//			info.write("# Current time: " + df.format(cal.getTime()) + '\n');
//			
//			// Read S and t.
//			Triple<RBTree, NamesMap, TimesMap> sNamesTimes = ParameterParser.getHostTree(params, info);
//			
//			// Read guest-to-host leaf map.
//			GuestHostMap gsMap = ParameterParser.getGSMap(params);
//			
//			LinkedHashMap<String, Integer> gpgMap = ParameterParser.getGenePseudogeneMap(params);
//			
//			// Create structures necessary for creating substituion models
//			// YangCodon.createYangCodon(0.2, 0.2, 2);
//			// Kappa (transversion/transition ratio) parameter for substitution model
//			DoubleParameter kappa = ParameterParser.getKappa(params);
//			
//			// Omega (dN/dS) parameter for substitution model
//			DoubleParameter omega = ParameterParser.getOmega(params);
//			
//			// Pi calculated in prior and hard coded in substitution matrix 
//			
//			// Substitution model first, then sequence alignment D and site rates.
//			boolean allowStopCodons=true;
//			SubstitutionMatrixHandler Qp = SubstitutionMatrixHandlerFactory.createPseudogenizationModel(params.substitutionModel, kappa.getValue(), 1.0001, 4 * gsMap.getNoOfLeafNames(), allowStopCodons);
//			allowStopCodons=false;
//			SubstitutionMatrixHandler Q = SubstitutionMatrixHandlerFactory.createPseudogenizationModel(params.substitutionModel, kappa.getValue(), omega.getValue(), 4 * gsMap.getNoOfLeafNames(), allowStopCodons);
//
//			//SubstitutionMatrixHandler Q_arve = SubstitutionMatrixHandlerFactory.create(params.substitutionModel, 4 * gsMap.getNoOfLeafNames());
//			
//			LinkedHashMap<String, ? extends Sequence<? extends Compound>> sequences = ParameterParser.getMultialignment(params, Q.getSequenceType());
//			MSAData D = new MSAData(Q.getSequenceType(), sequences);
//			Pair<DoubleParameter, GammaSiteRateHandler> siteRates = ParameterParser.getSiteRates(params);
//			
//			// Pseudo-random number generator.
//			PRNG prng = ParameterParser.getPRNG(params);
//			
//			// Read/create G and l.
//			NewickRBTreeSamples guestTreeSamples = null;
//			if (params.guestTreeSet != null) {
//				Double burninProp = Double.parseDouble(params.guestTreeSetBurninProp);
//				Double minCvg = Double.parseDouble(params.guestTreeSetMinCvg);
//				if (params.guestTreeSetWithLengths) {
//					guestTreeSamples = NewickRBTreeSamples.readTreesWithLengths(new File(params.guestTreeSet), params.guestTreeSetFileHasHeader,
//							params.guestTreeSetFileRelColNo, burninProp, minCvg);
//				} else {
//					guestTreeSamples = NewickRBTreeSamples.readTreesWithoutLengths(new File(params.guestTreeSet), params.guestTreeSetFileHasHeader,
//							params.guestTreeSetFileRelColNo, burninProp, minCvg);
//				}
//			}
//			Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = ParameterParser.getGuestTreeAndLengths(params, gsMap, prng, sequences, info, guestTreeSamples, D);
//			System.out.println(gNamesLengths.second.toString());
//			
//			DoubleMap pgSwitches = new DoubleMap("G-PGSwitches", gNamesLengths.first.getNoOfVertices(), 1);
//			IntMap edgeModels = new IntMap("EdgeModels", gNamesLengths.first.getNoOfVertices(), 1);
//			List<Integer> leaves = gNamesLengths.first.getLeaves();
//			for(int leaf: leaves){
//				if (gpgMap.get(gNamesLengths.second.get(leaf)) == 1)
//				{
//					pgSwitches.set(leaf, 0.5);
//					edgeModels.set(leaf, 2);
//				}
//			}
//			
//			// A gene tree (RBT) along with pgSwitches, edgeModels, gNames, gpgMap defines a gene tree with pseudogenization events.
//			
//			//System.out.println("Gene tree have "+gNamesLengths.first.getNoOfVertices()+ " vertices");
//			
//			// Read number of iterations and thinning factor.
//			Iteration iter = ParameterParser.getIteration(params);
//			Thinner thinner = ParameterParser.getThinner(params, iter);
//			
//			// Sigma (mapping between G and S).
//			MPRMap mprMap = new MPRMap(gsMap, gNamesLengths.first, gNamesLengths.second, sNamesTimes.first, sNamesTimes.second);
//			
//			// Read probability distribution for iid guest tree edge rates (molecular clock relaxation). 
//			Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> edgeRatePD = ParameterParser.getEdgeRatePD(params);
//			
//			// Create discretisation of S.
//			RBTreeArcDiscretiser dtimes = ParameterParser.getDiscretizer(params, sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, gNamesLengths.first);
//			
//			// Create reconciliation helper.
//			ReconciliationHelper rHelper = ParameterParser.getReconciliationHelper(params, gNamesLengths.first, sNamesTimes.first, dtimes, mprMap);
//			
//			// Duplication-loss probabilities over discretised S.
//			Quadruple<DoubleParameter, DoubleParameter, DoubleParameter, DupLossProbs> dupLoss = ParameterParser.getDupLossProbs(params, mprMap, sNamesTimes.first, gNamesLengths.first, dtimes);
//
//			
//			// ================ CREATE MODELS, PROPOSERS, ETC. ================
//			
//			// Priors. We only have them for parameters which might cause issues.
//			RealInterval priorRange = new RealInterval(1e-16, 1e16, false, false);
//			RealParameterUniformPrior edgeRateMeanPrior = new RealParameterUniformPrior(edgeRatePD.first, priorRange);
//			RealParameterUniformPrior edgeRateCVPrior = new RealParameterUniformPrior(edgeRatePD.second, priorRange);
//			RealParameterUniformPrior lengthsPrior = new RealParameterUniformPrior(gNamesLengths.third, priorRange);
//			
//			// Substitution model. NOTE: Root arc is turned on!!!!
//			SubstitutionModel sm = new SubstitutionModel("SubstitutionModel", D, siteRates.second, Q, Qp, gNamesLengths.first, gNamesLengths.second, gNamesLengths.third, true, pgSwitches, edgeModels, kappa, omega);
//			
//			// DLR model.
//			DLRModel dlr = new DLRModel(gNamesLengths.first, sNamesTimes.first, rHelper, gNamesLengths.third, dupLoss.fourth, edgeRatePD.third, pgSwitches, edgeModels);
//			
//			// Realisation sampler.
//			RealisationSampler realisationSampler = ParameterParser.getRealisationSampler(params, iter, prng, dlr, gNamesLengths.second, pgSwitches);
//			
//			// Proposers.
//			NormalProposer kappaNProposer = ParameterParser.getNormalProposer(params, kappa, new RealInterval(0, 100, true, true), iter, prng, params.tuningKappaRate);
//			NormalProposer omegaNProposer = ParameterParser.getNormalProposer(params, omega, new RealInterval(0, 10, true, true), iter, prng, params.tuningOmegaRate);
////			UniformProposer kappaProposer = ParameterParser.getUniformProposer(params, kappa, iter, prng, params.tuningKappaRate);
////			UniformProposer omegaProposer = ParameterParser.getUniformProposer(params, omega, iter, prng, params.tuningOmegaRate);
//			NormalProposer dupRateProposer = ParameterParser.getNormalProposer(params, dupLoss.first, iter, prng, params.tuningDupRate);
//			NormalProposer lossRateProposer = ParameterParser.getNormalProposer(params, dupLoss.second, iter, prng, params.tuningLossRate);
//			NormalProposer pseudoRateProposer = ParameterParser.getNormalProposer(params, dupLoss.third, iter, prng, params.tuningPseudoRate);
//			NormalProposer edgeRateMeanProposer = ParameterParser.getNormalProposer(params, edgeRatePD.first, iter, prng, params.tuningEdgeRateMean);
//			NormalProposer edgeRateCVProposer = ParameterParser.getNormalProposer(params, edgeRatePD.second, iter, prng, params.tuningEdgeRateCV);
//			NormalProposer siteRateShapeProposer = ParameterParser.getNormalProposer(params, siteRates.first, iter, prng, params.tuningSiteRateShape);
//			Proposer guestTreeProposer = ParameterParser.getBranchSwapper(params, gNamesLengths.first, gNamesLengths.third, mprMap, iter, prng, guestTreeSamples, pgSwitches, edgeModels, gpgMap, gNamesLengths.second);
//			Proposer pseudogenizationProposer = ParameterParser.getPseudopointsPerturber(gNamesLengths.first, pgSwitches, edgeModels, gNamesLengths.second, gpgMap, iter, prng);
//			NormalProposer lengthsProposer = ParameterParser.getNormalProposer(params, gNamesLengths.third, iter, prng, params.tuningLengths);
//			double[] lengthsWeights = SampleDoubleArray.toDoubleArray(params.tuningLengthsSelectorWeights);
//			lengthsProposer.setSubParameterWeights(lengthsWeights);
//			
//			// Proposer selector.
//			MultiProposerSelector selector = ParameterParser.getSelector(params, prng);
//			selector.add(kappaNProposer, ParameterParser.getProposerWeight(params.tuningWeightKappa, iter));
//			selector.add(omegaNProposer, ParameterParser.getProposerWeight(params.tuningWeightOmega, iter));
//			selector.add(dupRateProposer, ParameterParser.getProposerWeight(params.tuningWeightDupRate, iter));
//			selector.add(lossRateProposer, ParameterParser.getProposerWeight(params.tuningWeightLossRate, iter));
//			selector.add(pseudoRateProposer, ParameterParser.getProposerWeight(params.tuningWeightPseudoRate, iter));
//			selector.add(edgeRateMeanProposer, ParameterParser.getProposerWeight(params.tuningWeightEdgeRateMean, iter));
//			selector.add(edgeRateCVProposer, ParameterParser.getProposerWeight(params.tuningWeightEdgeRateCV, iter));
//			selector.add(siteRateShapeProposer, ParameterParser.getProposerWeight(params.tuningWeightSiteRateShape, iter));
//			selector.add(guestTreeProposer, ParameterParser.getProposerWeight(params.tuningWeightG, iter));
//			selector.add(pseudogenizationProposer, ParameterParser.getProposerWeight(params.tuningWeightPgPoints, iter));
//			selector.add(lengthsProposer, ParameterParser.getProposerWeight(params.tuningWeightLengths, iter));
//			
//			// Inactivate fixed proposers.
//			if (params.dupRate != null        && params.dupRate.matches("FIXED|Fixed|fixed"))        { dupRateProposer.setEnabled(false); }
//			if (params.lossRate != null       && params.lossRate.matches("FIXED|Fixed|fixed"))       { lossRateProposer.setEnabled(false); }
//			if (params.pseudoRate != null       && params.pseudoRate.matches("FIXED|Fixed|fixed"))       { pseudoRateProposer.setEnabled(false); }
//			if (params.edgeRatePDMean != null && params.edgeRatePDMean.matches("FIXED|Fixed|fixed")) { edgeRateMeanProposer.setEnabled(false); }
//			if (params.edgeRatePDCV != null   && params.edgeRatePDCV.matches("FIXED|Fixed|fixed"))   { edgeRateCVProposer.setEnabled(false); }
//			if (params.siteRateCats == 1      || params.siteRateShape.matches("FIXED|Fixed|fixed"))  { siteRateShapeProposer.setEnabled(false); }
//			if (params.guestTreeFixed)                                                               { guestTreeProposer.setEnabled(false); }
//			if (params.lengthsFixed)                                                                 { lengthsProposer.setEnabled(false); }
//			pseudogenizationProposer.setEnabled(true);
//			
//			
//			// Proposal acceptor.
//			ProposalAcceptor acceptor = ParameterParser.getAcceptor(params, prng);
//			
//			// Overall statistics.
//			FineProposerStatistics stats = new FineProposerStatistics(iter, 8);
//			
//			// ================ SETUP MCMC HIERARCHY ================
//			
//			MCMCManager manager = new MCMCManager(iter, thinner, selector, acceptor, sampler, prng, stats);
//			manager.setDebugMode(params.debug);
//			
//			manager.addModel(edgeRateMeanPrior);
//			manager.addModel(edgeRateCVPrior);
//			manager.addModel(lengthsPrior);
//			manager.addModel(sm);
//			manager.addModel(dlr);
//			
//			manager.addSampleable(iter);
//			manager.addSampleable(manager);			// Overall likelihood.
//			//manager.addSampleable(edgeRateMeanPrior);
//			//manager.addSampleable(edgeRateCVPrior);
//			//manager.addSampleable(lengthsPrior);
//			manager.addSampleable(sm);
//			manager.addSampleable(dlr);
//			manager.addSampleable(dupLoss.first);
//			manager.addSampleable(dupLoss.second);
//			manager.addSampleable(edgeRatePD.first);
//			manager.addSampleable(edgeRatePD.second);
//			
//			if(pgSwitches!= null )
//			{
//				manager.addSampleable(dupLoss.third);
//				manager.addSampleable(kappa);
//				manager.addSampleable(omega);
//			}
//			if (siteRateShapeProposer.isEnabled()) {
//				manager.addSampleable(siteRates.first);
//			}
//			manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second, null, pgSwitches));
//			if (params.outputLengths) {
//				manager.addSampleable(new RBTreeSampleWrapper(gNamesLengths.first, gNamesLengths.second, gNamesLengths.third));
//			}
//			if (realisationSampler != null) {
//				manager.addSampleable(realisationSampler);
//			}
//			
//			// ================ WRITE PRE-INFO ================
//			info.write("# MCMC manager:\n");
//			info.write(manager.getPreInfo("# \t"));
//			info.flush();   // Don't close, maybe use stdout for both sampling and info...
//			
//			// ================ RUN ================
//			manager.run();
//			
//			// ================ WRITE POST-INFO ================
//			info.write("# =========================================================================\n");
//			info.write("# ||                             POST-RUN INFO                           ||\n");
//			info.write("# =========================================================================\n");
//			info.write("# pDELIRIOUS\n");
//			info.write("# MCMC manager:\n");
//			info.write(manager.getPostInfo("# \t"));
//			info.flush();
//			sampler.close();
//			info.close();
//			if (realisationSampler != null) { realisationSampler.close(); }
			}
			
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
	
	
	public double[] findTheDistance(int trueNode, Realisation r, RBTreeArcDiscretiser dtimes, DoubleMap truePGs, DoubleMap sampledPGs) {
		RootedBifurcatingTree tree = r.getGuestTree();
		int FOURDISTANCES=4;
		double[] distances= new double[FOURDISTANCES]; // {max-edge distance, avg-edge distance, max-time distance, avg-time distance}
//		System.out.println(r.toString());
		// Check if parents have the sampled pg-switch?
		int nodeNumber = checkParents(trueNode, r);
		if (nodeNumber == -1)
		{
			distances = checkDescendants(nodeNumber, r, trueNode, dtimes, truePGs, sampledPGs);
		}else
		{
			// Find the average and max of the distances 1) timewise 2) distancewise
			double edgeDistance = getEdgeDistance(nodeNumber, r, trueNode);
			double edgeTime = getTime(nodeNumber, r, trueNode, dtimes, truePGs, sampledPGs);
			distances[0] = Math.abs(edgeDistance);	// conversion to absolute values
			distances[1] = Math.abs(edgeDistance);	// conversion to absolute values
			distances[2] = Math.abs(edgeTime);		// conversion to absolute values
			distances[3] = Math.abs(edgeTime);		// conversion to absolute values
		}
			
		return distances;
	}
	
	
	/**
	 * Returns the node number of ancestors if they have pseudogenization switch
	 * @param node
	 * @param realisation
	 * @return node number or -1 otherwise
	 */
	public int checkParents(int node, Realisation r){
		int u = node;
		int distance = 0;
		RootedBifurcatingTree tree = r.getGuestTree();
		if(!tree.isRoot(u))
		{
			
			while(!tree.isRoot(u) && r.getPGPoint(u)==1){
				u = tree.getParent(u);
			}
			if(tree.isRoot(u) ){
				if (r.getPGPoint(u)==1)
					return -1;
				else
					return u;
			}
			else
				return u;

		}else
		{
			if(r.getPGPoint(u)==1)
				return -1;
			else
				return u;
		}
	}
	
	/**
	 * Returns the distance and time if the descendants have pseudogenization switch
	 * @param node
	 * @param realisation
	 * @return node number or -1 otherwise
	 */
	public double[] checkDescendants(int node, Realisation r, int trueNode, RBTreeArcDiscretiser dtimes, DoubleMap truePGs, DoubleMap sampledPGs){
		int FOURDISTANCES=4;	// {max-edge distance, avg-edge distance, max-time distance, avg-time distance}
		double[] distances = new double[FOURDISTANCES];
		RootedBifurcatingTree tree = r.getGuestTree();
		List<Integer> list = tree.getDescendants(trueNode, true);
		double maxTime=0, maxEdgeDistance=0, sumOfDistances=0;
		double avgTime=0, avgEdgeDistance=0, sumOfTimes=0;
		int countOfDescendantEdges=0;
		
		for (Integer element : list)
		{
			if(r.getPGPoint(element.intValue()) != 1)
			{
				countOfDescendantEdges++;
				double d = getEdgeDistance(element.intValue(), r, trueNode);
				double t = getTime(element.intValue(), r, trueNode, dtimes, truePGs, sampledPGs);
				if(Math.abs(maxTime)<Math.abs(t))
					maxTime=Math.abs(t);			// conversion to absolute values
				if(Math.abs(maxEdgeDistance)<Math.abs(d))
					maxEdgeDistance=Math.abs(d);	// conversion to absolute values
				
				sumOfDistances+=Math.abs(d);		// conversion to absolute values
				sumOfTimes+=Math.abs(t);			// conversion to absolute values
			}
				
		}
		avgEdgeDistance = sumOfDistances/countOfDescendantEdges;
		avgTime = sumOfTimes/countOfDescendantEdges;
		distances[0]=maxEdgeDistance;
		distances[1]=avgEdgeDistance;
		distances[2]=maxTime;
		distances[3]=avgTime;
		return distances;
	}
	
	/**
	 * Returns the edge distance 
	 * @param node
	 * @param realisation
	 * @param trueNode
	 * @return edge distance
	 */
	public double getEdgeDistance(int node, Realisation r, int trueNode){
		double distance=0;
		RootedBifurcatingTree tree = r.getGuestTree();
		
		if(tree.getDescendants(trueNode, true).contains(node))
			distance=getDistance(node, trueNode, r);
		else
			distance=getDistance(trueNode, node, r)*(-1);
		return distance;
	}
	
	/**
	 * Returns the time 
	 * @param node
	 * @param realisation
	 * @param trueNode
	 * @return edge distance
	 */
	public double getTime(int node, Realisation r, int trueNode, RBTreeArcDiscretiser dtimes, DoubleMap truePGs, DoubleMap sampledPGs){
		double time=0;
		RootedBifurcatingTree tree = r.getGuestTree();
		

		// Get the time point of true node							tx
		int[] pl_trueNode = r.getDiscPt(trueNode);
		double trueNode_time = dtimes.getDiscretisationTime(pl_trueNode[0], pl_trueNode[1]);
		// get the time point of parent node of the true node		p(tx)
		int[] pl_parent = r.getDiscPt(tree.getParent(trueNode));
		double trueNode_parentstime = dtimes.getDiscretisationTime(pl_parent[0], pl_parent[1]);
		// Get the time point of the pg-point						t_pg
		double diff = trueNode_parentstime - trueNode_time;
		double truePG_time = trueNode_time + diff*truePGs.get(trueNode);
		
		// Get the time point of the sampled node					ty
		int[] pl_node = r.getDiscPt(node);
		double pl_nodetime = dtimes.getDiscretisationTime(pl_node[0], pl_node[1]);
		// get the time point of parent node of the sampled node	p(ty)
		
		int[] pl_parentnode = new int[2];
		if(!tree.isRoot(node))
			pl_parentnode = r.getDiscPt(tree.getParent(node));
		else
		{
			pl_parentnode[0] = pl_node[0];
			pl_parentnode[1] = dtimes.getNoOfSlicesForRootPath(pl_node[0]);
		}
		double pl_parentnodetime = dtimes.getDiscretisationTime(pl_parentnode[0], pl_parentnode[1]);
		// Get the time point of the pg-point						t'_pg
		double diff2 = pl_parentnodetime - pl_nodetime;
		double sampledPG_time = pl_nodetime + diff2*sampledPGs.get(node);
			
			
		if(tree.getDescendants(trueNode, true).contains(node))
		{	
			// return t_pg - t'_pg
			time = truePG_time - sampledPG_time;
			
		}else if(tree.getDescendants(node, true).contains(trueNode))
		{
			// Get the time point of true node							tx
			// get the time point of parent node of the true node		p(tx)
			// Get the time point of the pg-point						t_pg
			
			// Get the time point of the sampled node					ty
			// get the time point of parent node of the sampled node	p(ty)
			// Get the time point of the pg-point						t'_pg
			
			// return t'_pg - t_pg	
			time = truePG_time - sampledPG_time ;
		}else
		{
			time = truePG_time - sampledPG_time;
				
		}
		return time;
	}
	
	/**
	 * Returns the distance 
	 * @param node
	 * @param realisation
	 * @param trueNode
	 * @return edge distance
	 */
	public double getDistance(int u, int v, Realisation r){
		double distance=0;
		RootedBifurcatingTree tree = r.getGuestTree();
		if(u==v) return distance;
		
		while(u!=v)
		{
			if(tree.isRoot(u))
			{
				distance=-1;
				break;
			}
			u=tree.getParent(u);
			distance++;
		}
		return distance;
	}
	
	/**
	 * Checks if the realisation is a legal pseudogenization
	 * @param r
	 * @return true or false
	 */
	public boolean isLegalRealisation(Realisation r, LinkedHashMap<String, Integer> gpgMap)
	{
		
		int vertices = r.getGuestTree().getNoOfVertices();
		DoubleMap pgSwitches = new DoubleMap("Pseudogenization Switches", vertices);
		for (int vr = 0; vr < vertices; vr++)
		{
			if(r.getPGPoint(vr) != 1)
				pgSwitches.set(vr, r.getPGPoint(vr));
			else
				pgSwitches.set(vr, 1);
		}
		int falseSample=0;
		List<Integer> leaves = r.getGuestTree().getLeaves();
		for(Integer l: leaves)
		{
			if(gpgMap.get(r.getVertexName(l.intValue()))==1)
			{
				int numberofswitches=0;
				int v=l.intValue();
				while(!r.getGuestTree().isRoot(v))
				{
					if(pgSwitches.get(v)!=1)
						numberofswitches++;
					v=r.getGuestTree().getParent(v);
				}
				if(r.getGuestTree().isRoot(v) && pgSwitches.get(v)!=1)
					numberofswitches++;
				if(numberofswitches ==0 )
					falseSample=1;	
				if(numberofswitches>1)
					falseSample=2;
			}
		}		
		if(falseSample == 0)
			return true;
		else
			return false;
	}
}
