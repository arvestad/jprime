package se.cbb.jprime.apps.age;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.dltrs.Realisation;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.io.UnparsedRealisation;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.seqevo.MSAData;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandler;
import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTree;
import se.cbb.jprime.topology.TimesMap;

import com.beust.jcommander.JCommander;

/**
 * Estimates age of the internal vertices of the MAP gene tree.
 * 
 * @author Joel Sjöstrand.
 * @author Owais Mahmudi
 */
public class Age implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "Age";
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
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar Age [options] <args>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// ================ READ AND CREATE ALL PARAMETERS ================
			
			// MCMC chain output and auxiliary info.
//			SampleWriter sampler = ParameterParser.getOut(params);
			info = ParameterParser.getInfo(params);
			info.write("# =========================================================================\n");
			info.write("# ||                             PRE-RUN INFO                            ||\n");
			info.write("# =========================================================================\n");
			info.write("# Age\n");
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
			Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = ParameterParser.getGuestTreeAndLengths(params, gsMap, prng, sequences, info, guestTreeSamples, D);
			// Create discretisation of S.
			RBTreeArcDiscretiser dtimes = ParameterParser.getDiscretizer(params, sNamesTimes.first, sNamesTimes.second, sNamesTimes.third, gNamesLengths.first);
			info.close();

			String mapgenetreeproportion = "";
			if(params.inmcmcfile != null )
			{
				// Read the MCMC file
				File fmcmc = new File(params.inmcmcfile); 
	//			int noOfArgs = Integer.parseInt(params.inmcmcfile.get(1));
				double burnin = Double.parseDouble(params.guestTreeSetBurninProp);
				NewickRBTreeSamples trees = NewickRBTreeSamples.readTreesWithoutLengths(fmcmc, true, 1, burnin, 0.01);
				//System.out.println("Found " + trees.getNoOfTrees() + " trees");
				mapgenetreeproportion = "MAP gene tree proportion:" + "\t" + trees.getTreeCount(0) + "/"+ trees.getTotalTreeCount() + " (" + trees.getTreeCount(0)/(double)trees.getTotalTreeCount() + ") \n";
				ArrayList<String> mapIDs = trees.getMAPTreeSampleIDs();
				
//				for(int i=0; i<mapIDs.size(); i++)
//					System.out.println( mapIDs.get(i));
				
				if(params.outmcmcfile != null){ 						// At the moment it writes gene trees above certain threshold
					try {
					    PrintWriter outgenetrees = new PrintWriter(new BufferedWriter(new FileWriter(params.outmcmcfile, false)));
					    for (int i =0; i < trees.getNoOfTrees(); i++){
					    	outgenetrees.write(trees.getTreeNewickString(i) + "\n");
					    }
				    	outgenetrees.close();
					} catch (IOException e) {
					    System.out.println("Problem with accessing file " + params.outmcmcfile);
					}
				}
				
				if(params.outrealfile != null && params.inrealfile != null)
				{
					// Read the Real file
					File inrealfile = new File(params.inrealfile);
					PrintWriter outrealwriter = new PrintWriter(new BufferedWriter(new FileWriter(params.outrealfile, false)));
					Scanner sc = new Scanner(inrealfile);
					int i = 0;
					if(sc.hasNextLine()){
						sc.nextLine(); sc.nextLine();}
					while (sc.hasNextLine()) {
						String line = sc.nextLine();
						int sampleNo = Integer.parseInt(line.split("\t")[0]);
						if(Integer.parseInt(mapIDs.get(i)) == sampleNo){
							outrealwriter.write(line+"\n"); i++;}
					}
					outrealwriter.close();
				}
			}
			
			if(params.outrealfile != null && params.inrealfile != null)
			{
				// Read the out real file
				File inrealfile = new File(params.outrealfile);
//				inrealfile = new File("/Users/mahmudi/Dropbox/Office/GeneAge/Data/sample_files/CDHfinaltiny.real.out");
				Scanner sc = new Scanner(inrealfile);
				List<String> realisations =  new ArrayList<String>();;
				LinkedHashMap<String, Integer> realMap= new LinkedHashMap<String, Integer>(4096);
				// Node number of the gene tree vertex mapped to the discretization times in double (done for computing expectations)
				LinkedHashMap<Integer, List<Double>> verticesMap= new LinkedHashMap<Integer, List<Double>>(4096);
				List<String> leaves = new ArrayList<String>();
				LinkedHashMap<Integer, List<String>> leavesVerticesMap= new LinkedHashMap<Integer, List<String>>();
				int total_samples=0;
				while (sc.hasNextLine()) {
					total_samples++;
					String line = sc.nextLine();
					String real = line.split("\t")[2];
//					int sampleNo = Integer.parseInt(line.split("\t")[0]);
					
					if(total_samples == 1){
						Realisation r=UnparsedRealisation.parseRealisation(real);
						RootedBifurcatingTree t =  r.getTree();
						int noofvertices = t.getNoOfVertices();
						for (int i=0; i<noofvertices; i++){
							leaves = r.getNameOfLeaves(i);
							leavesVerticesMap.put(i, leaves);
						}
					}
					
					//Document the realisation points for all vertices of gene tree
					UnparsedRealisation.verticesDistribution(real, verticesMap, dtimes);
					
					if(realMap.size()>=1){
						boolean found = false;
						for( int j=0; j<realMap.size(); j++){
							if(UnparsedRealisation.compareRealisation(real, realisations.get(j))){
								found = true;
								realMap.put(realisations.get(j), realMap.get(realisations.get(j))+1);
								break;
							}
						}
						if(!found){
							realMap.put(real, 1);
							realisations.add(real);
						}
					}else{
						realisations.add(real);
						realMap.put(real,1);
					}
				}

				// Compute the expected age of the nodes of gene tree vertices.
				// Double array of size of the gene tree vertices
				List<Double> ageEstimates = new ArrayList<Double>();
				for(int i=0; i<verticesMap.size(); i++){
					double sum=0.0;
					List<Double> estimates = verticesMap.get(i);
					for(int j=0; j<estimates.size(); j++) sum=sum+estimates.get(j);
					ageEstimates.add(sum/(double)estimates.size());
				}
				
				// Write down age estimates to an output file specified
				if(params.ageestimatesfile != null){
					PrintWriter agewriter = new PrintWriter(new BufferedWriter(new FileWriter(params.ageestimatesfile, false)));
					agewriter.write(mapgenetreeproportion);
					double rootage = Double.parseDouble(params.rootage);
					for(int i=0;i<ageEstimates.size(); i++){
						agewriter.write(i + "\t" + ageEstimates.get(i)*rootage + "\t" + leavesVerticesMap.get(i)+"\n");
					}
					agewriter.close();
				}
				
				// Sort according to realisation frequency.
				ArrayList<Integer> reals = new ArrayList<Integer>(realMap.values());
				Collections.sort(reals, Collections.reverseOrder());
				String map_realisation = "";
				for (int i=0; i<realMap.size(); i++){
					if(reals.get(0) == realMap.get(realisations.get(i))){
						map_realisation = realisations.get(i);
						break;
					}
				}
				
				if(params.outfilemaprealisation != null){											// Write the statistics of MAP realisation
					PrintWriter mrwriter = new PrintWriter(new BufferedWriter(new FileWriter(params.outfilemaprealisation, false)));
					mrwriter.write(map_realisation+"\n");
					mrwriter.write("Total Samples : " + total_samples+"\n");
					mrwriter.write("Total Realisations : " + realMap.size()+"\n");
					mrwriter.write("MAP Samples : " + reals.get(0)+"\n");
					mrwriter.write("Percentage : " + reals.get(0)/(double)total_samples);
					mrwriter.close();
				}else{
					System.out.println(map_realisation);
				}
				
				sc.close();
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
	

}
