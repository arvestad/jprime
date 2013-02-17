package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.TopologyException;

import com.beust.jcommander.Parameter;

/**
 * Contains user settings.
 * 
 * @author Joel Sjöstrand.
 */
public class BranchRelaxerParameters {

	/** Required parameters: S, D and GS. */
	@Parameter(description = "<Tree file or tree string> <Model> <Model arg 1> <Model arg 2> ...")
	public List<String> args = new ArrayList<String>();
		
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** Output. */
	@Parameter(names = {"-o", "--output-file"}, description = "Output relaxed tree to a file. Also writes used model parameters to a file named <filename>.info.")
	public String outputfile = null;
	
	/** PRNG seed. */
	@Parameter(names = {"-s", "--seed"}, description = "PRNG seed. Default: Random seed.")
	public String seed = null;
	
	public PRNG prng = null;

	// TODO: Implement.
//	/** Root constraints. */
//	@Parameter(names = {"-r", "--root-constraint"}, description = "Enables special treatment of the rate r(...) of ingoing edge e and outgoing edges f and g of the root. 'PC' enforces r(e)=r(f) or r(e)=r(g) with equal probability, " +
//			"'PCC' enforces r(e)=r(f)=r(g), 'CC' enforces r(f)=r(g).")
//	public String rootConst = "NONE";
	
	/** Do meta. */
	@Parameter(names = {"-x", "--auxiliary-tags"}, description = "Include auxiliary PrIME tags in output tree.")
	public Boolean doMeta = false;
	
	/**
	 * Return rate model.
	 * @return model.
	 */
	public RateModel getRateModel() {
		if (this.prng == null) {
			this.prng = (this.seed == null ? new PRNG() : new PRNG(new BigInteger(this.seed)));
		}
		// TODO: This could be solved with reflection, but right now, why bother...
		String model = args.get(1);
		if (model.equalsIgnoreCase("Constant")) {
			return new ConstantRateModel(Double.parseDouble(args.get(2)));
		} else if (model.equalsIgnoreCase("IIDExponential")) {
			return new IIDExponentialRateModel(Double.parseDouble(args.get(2)), this.prng);
		} else if (model.equalsIgnoreCase("IIDGamma")) {
			return new IIDGammaRateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("IIDLogNormal")) {
			return new IIDLogNormalRateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("IIDNormal")) {
			return new IIDNormalRateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("IIDSamplesFromFile")) {
			return new IIDSamplesFromFileRateModel(new File(args.get(2)), this.prng);
		}  else if (model.equalsIgnoreCase("IIDUniform")) {
			return new IIDUniformRateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		}  else if (model.equalsIgnoreCase("ACTK98")) {
			return new ACThorneKishino98RateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		}  else if (model.equalsIgnoreCase("ACRY07")) {
			return new ACRannalaYang07RateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		}  
		else {
			throw new IllegalArgumentException("Invalid rate model identifier: ." + model);
		}
	}
	
	public String getModelsHelpMsg() {
		return "Supported models:\n" +
				"    Constant <rate>                 -  Constant rates.\n" +
				"    IIDGamma <k> <theta>            -  IID rates from Gamma(k, theta).\n" +
				"    IIDLogNormal <mu> <sigma2>      -  IID rates from ln N(mu, sigma^2).\n" +
				"    IIDNormal <mu> <sigma2>         -  IID rates from N(mu, sigma^2).\n" +
				"    IIDUniform <a> <b>              -  IID rates from Unif([a,b]).\n" +
				"    IIDExponential <lambda>         -  IID rates from Exp(lambda).\n" +
				"    IIDSamplesFromFile <filename>   -  IID rates drawn uniformly (with replacement)\n" +
				"                                       from a file with a column of samples.\n" +
				"    ACTK98 <start rate> <v>         -  Autocorrelated rates in accordance with\n" +
				"                                       Thorne-Kishino '98/'01/'02 but corrected\n" +
				"                                       to not yield increasing average rates\n" +
				"                                       in root-to-leaf direction.\n" +
				"    ACRY07 <start rate> <sigma2>    -  Autocorrelated model in accordance with\n" +
				"                                       Rannala-Yang '07. The start rate refers to\n" +
				"                                       tip of host tree in case there is a stem edge."
				;
	}
	
	/**
	 * Returns the tree.
	 * @param doStrict require clock-like lengths.
	 * @return the tree.
	 * @throws NewickIOException
	 * @throws IOException
	 * @throws TopologyException
	 */
	public PrIMENewickTree getTree(boolean doStrict) throws NewickIOException, IOException, TopologyException {
		File f = new File(this.args.get(0));
		if (f.exists()) {
			// We do allow non-ultrametric trees to, if relaxing in multiple rounds.
			return PrIMENewickTreeReader.readTree(f, false, doStrict);
		} else {
			return PrIMENewickTreeReader.readTree(args.get(0), false, doStrict);
		}
	}
	
	/**
	 * Returns output and info streams.
	 * @return streams.
	 */
	public Pair<BufferedWriter,BufferedWriter> getOutputFiles() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.outputfile.trim()));
			BufferedWriter info = new BufferedWriter(new FileWriter(this.outputfile.trim() + ".info"));
			return new Pair<BufferedWriter, BufferedWriter>(out, info);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid output file.", e);
		}
	}
}
