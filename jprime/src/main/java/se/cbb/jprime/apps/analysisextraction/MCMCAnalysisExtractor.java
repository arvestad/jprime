package se.cbb.jprime.apps.analysisextraction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Parses contents of output generated with mcmc_analysis
 * shell command.
 * 
 * @author Joel Sjöstrand.
 */
public class MCMCAnalysisExtractor {

	/**
	 * Parses output resulting from a call to mcmc_analysis.
	 * @param params the parameters for which to parse info.
	 * @param mcmcAnalysisCmd the shell command for executing mcmc_analysis.
	 * @param filenames the input files.
	 * @throws IOException.
	 * @throws InterruptedException.
	 */
	public static void parse(Collection<Parameter> params, String mcmcAnalysisCmd, Collection<String> filenames)
			throws IOException, InterruptedException {
		// Execute mcmc_analysis.
		StringBuilder filenamesConcat = new StringBuilder();
		for (String f : filenames) {
			filenamesConcat.append(" ").append(f);
		}
		Process pr = Runtime.getRuntime().exec(mcmcAnalysisCmd + filenamesConcat.toString());
		//pr.waitFor();
		BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		
		// Parse output.
		String ln;
		while ((ln = in.readLine()) != null) {
			for (Parameter p : params) {
				if (ln.startsWith(p.name)) {
					if (p.getClass() == TreeParameter.class) {
						parseTree((TreeParameter) p, in);
					} else if (p.getClass() ==  FloatParameter.class) {
						parseFloat((FloatParameter) p, ln, in);
					}
				}
			}
		}
		in.close();
	}
	
	/**
	 * Parses tree parameter info.
	 * @param param the parameter.
	 * @param in the mcmc_analysis output following the parameter name.
	 * @throws IOException.
	 */
	private static void parseTree(TreeParameter param, BufferedReader in) throws IOException {
		String ln = in.readLine();
		ln = in.readLine();
		String[] parts = ln.trim().split("\t ?");
		param.mapTree = parts[2];
		param.mapTreeProbability = new Double(parts[1]);	
	}
	
	/**
	 * Parses float parameter info.
	 * @param param the parameter.
	 * @param ln the parameter name line, i.e. preceding the contents of 'in'.
	 * @param in the mcmc_analysis output following the parameter name line.
	 * @throws IOException.
	 */
	private static void parseFloat(FloatParameter param, String ln, BufferedReader in) throws IOException {
		String[] parts = ln.trim().split("[ \t,=]+");
		param.mean = new Double(parts[2]);
		param.stdDev = new Double(parts[4]);
		ln = in.readLine();
		parts = ln.trim().split("[ \t,=]+");
		param.max = new Double(parts[1]);
		param.min = new Double(parts[3]);
	}

}
