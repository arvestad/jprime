package se.cbb.jprime.apps.analysisextraction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



/**
 * Starter used for assembling DTLSR info for one family.
 * Input is all files (MCMC input/output) related to the family.
 * 
 * @author Joel Sjöstrand.
 */
public class DTLSRSummaryAssembler {
	
	/**
	 * Parses info all kinds of files that are recognized.
	 * @param filenames the files (sequence file, chains, etc.).
	 * File type is deduced from suffix.
	 */
	public static void main(String[] filenames) {
		
		// Resolve the various files.
		String seqFile = null;
		String gsFile = null;
		String hostTreeFile = null;
		String guestTreeFile = null;
		String trueFile = null;
		String eventsFile = null;
		ArrayList<String> chainFiles = new ArrayList<String>();
		for (String f : filenames) {
			if (f.endsWith(".fasta") || f.endsWith(".fa") || f.endsWith(".aln") || f.endsWith(".aln-gb")) {
				seqFile = f;
			} else if (f.endsWith(".sigma") || f.endsWith(".gs")) {
				gsFile = f;
			} else if (f.endsWith(".tree") || f.endsWith(".stree") || f.endsWith(".prime") || f.endsWith(".nw")) {
				hostTreeFile = f;
			} else if (f.endsWith(".gtree")) {
				guestTreeFile = f;
			} else if (f.endsWith(".true")) {
				trueFile = f;
			} else if (f.endsWith(".events")) {
				eventsFile = f;
			} else if (f.endsWith(".out") || f.endsWith(".mcmc")) {
				chainFiles.add(f);
			}
		}
	    
		try {
			// Settings.
			double burnIn = 0.25;
			
			// Parameters to retrieve info for.
			ArrayList<Parameter> params = new ArrayList<Parameter>();
			params.add(new TreeParameter("Tree_Model1.Tree"));
			params.add(new FloatParameter("molli_DupLossTrans0.birthRate"));
			params.add(new FloatParameter("molli_DupLossTrans0.transferRate"));
			params.add(new FloatParameter("molli_DupLossTrans0.deathRate"));
			params.add(new FloatParameter("Density2.mean"));
			params.add(new FloatParameter("Density2.variance"));
			
			// Parse sequence file.
			if (seqFile != null) {
				List<Integer> v = SequenceExtractor.parse(seqFile);
				System.out.println("Number of sequences: " + v.get(0));
				System.out.println("Multialignment length: " + v.get(1));
			}
			
			// Parse G-S map file.
			if (gsFile != null) {
				List<Integer> v = GSMapExtractor.parse(gsFile);
				System.out.println("Number of species: " + v.get(0));
				System.out.println("Max species member size: " + v.get(1));
			}
			
			// Parse host tree file.
			if (hostTreeFile != null) {
				List<Object> v = HostTreeExtractor.parse(hostTreeFile);
				System.out.println("Host tree: " + v.get(0));
				System.out.println("Host tree height: " + v.get(1));
				System.out.println("Host tree total time span: " + v.get(2));
				System.out.println("Host tree total time span excl. top time: " + v.get(3));
			}
			
			// Parse guest tree file.
			if (guestTreeFile != null) {
				String g = GuestTreeExtractor.parse(guestTreeFile);
				System.out.println("True guest tree: " + g);
			}
			
			// Parse sequence-generated true file.
			if (trueFile != null) {
				TreeMap<String, String> vals = SequenceTrueFileExtractor.parse(trueFile);
				System.out.println("True edge rate mean: " + vals.get("EdgeRates.mean(float)"));
				System.out.println("True edge rate variance: " + vals.get("EdgeRates.variance(float)"));
			}
			
			// Parse events file (Ali's and my meta info).
			if (eventsFile != null) {
				TreeMap<String, String> map = AlixFileExtractor.parse(eventsFile, ":");
				for (Map.Entry<String, String> e : map.entrySet()) {
					System.out.println(e.getKey() + ": " + e.getValue());
				}
			}
			
			// Parse raw chain files, both raw contents and using mcmc_analysis.
			if (!chainFiles.isEmpty()) {
				RawChainExtractor.parse(params, chainFiles);
				boolean isSingle = (chainFiles.size() == 1);
				String mcmcAnalysisCmd = "mcmc_analysis " + (isSingle ? "" : "-P ")
					+ "-b " + burnIn + " -i Tree_weights3.Tree_Lengths";
				MCMCAnalysisExtractor.parse(params, mcmcAnalysisCmd, chainFiles);
				for (Parameter p : params) {
					System.out.print(p);
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
