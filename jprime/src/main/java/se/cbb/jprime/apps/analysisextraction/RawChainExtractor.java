package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * Parses contents of raw MCMC chain files.
 * At the moment, this 
 * 
 * @author Joel Sjöstrand.
 */
public class RawChainExtractor {

	/**
	 * Retrieves parameter info.
	 * @param params the parameters of interest.
	 * @param filenames the filenames.
	 * @throws FileNotFoundException.
	 */
	public static void parse(Collection<Parameter> params, Collection<String> filenames) throws FileNotFoundException {
		// Create readers for all files.
		ArrayList<Scanner> ins = new ArrayList<Scanner>(filenames.size());
		for (String f : filenames) {
			ins.add(new Scanner(new File(f)));
		}
		
		// Retrieve parameter names from first file. Ignore L and N.
		String ln;
		while (!(ln = ins.get(0).nextLine()).startsWith("# L N")) {}
		String[] parts = ln.substring(6).split("[ \t;]+|\\([A-Za-z_-]+\\)");
		ArrayList<String> paramMap = new ArrayList<String>();
		for (String p : parts) {
			if (!p.trim().equals("")) {
				paramMap.add(p);
			}
		}
		
		// Examine all file optima to find the true one.
		double optDens = Double.NEGATIVE_INFINITY;
		String optVals = null;
		for (Scanner in : ins) {
			while (!(ln = in.nextLine()).startsWith("# local optimum")) {}
			double opt = Double.parseDouble(ln.split(" = ")[1]);
			if (opt > optDens) {
				optDens = opt;
				optVals = in.nextLine();
			}
		}
		
		// Close all files.
		for (Scanner sc : ins) {
			sc.close();
		}
		
		// Extract pertinent parameters from optimum.
		// L N are already excluded.
		String[] vals = optVals.substring(13).split(";[ \t\n]+");
		for (Parameter p : params) {
			for (int i = 0; i < paramMap.size(); ++i) {
				if (p.name.equals(paramMap.get(i))) {
					if (p.getClass() == TreeParameter.class) {
						((TreeParameter) p).bestStateTree = vals[i];
					} else if (p.getClass() == FloatParameter.class) {
						((FloatParameter) p).bestState = Double.parseDouble(vals[i]);
					}
				}
			}
		}
	}
	
}
