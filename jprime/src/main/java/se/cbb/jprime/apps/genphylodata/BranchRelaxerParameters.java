package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.GuestHostMap;
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

	// TODO: Implement some day perhaps.
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
	 * @throws TopologyException 
	 * @throws IOException 
	 * @throws NewickIOException 
	 */
	public RateModel getRateModel() throws NewickIOException, IOException, TopologyException {
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
		} else if (model.equalsIgnoreCase("IIDUniform")) {
			return new IIDUniformRateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("ACTK98")) {
			return new ACThorneKishino98RateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("ACRY07")) {
			return new ACRannalaYang07RateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), this.prng);
		} else if (model.equalsIgnoreCase("ACABY02")) {
			return new ACArisBrosouYang02RateModel(Double.parseDouble(args.get(2)), this.prng);
		} else if (model.equalsIgnoreCase("ACLBPL07")) {
			return new ACLepageBryantPhillipeLartillot07RateModel(Double.parseDouble(args.get(2)), Double.parseDouble(args.get(3)), Double.parseDouble(args.get(4)), Double.parseDouble(args.get(5)), this.prng);
		} else if (model.equalsIgnoreCase("IIDRK07")) {
			PrIMENewickTree g = getTree(true);
			PrIMENewickTree s = getTree(args.get(2), true);
			GuestHostMap gs = GuestHostMapReader.readGuestHostMap(new File(args.get(3)));
			return new IIDRasmussenKellis07RateModel(s, g, gs, this.prng);
		} else {
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
				"    ACTK98 <start rate> <v>         -  Autocorrelated lognormal rates in accordance w.\n" +
				"                                       Thorne-Kishino '98/'01/'02 but corrected\n" +
				"                                       to not yield increasing average rates\n" +
				"                                       in root-to-leaf direction.\n" +
				"    ACRY07 <start rate> <sigma2>    -  Autocorrelated lognormal rate in accordance w.\n" +
				"                                       Rannala-Yang '07. The start rate refers to\n" +
				"                                       tip of tree in case there is a stem edge.\n" +
				"    ACABY02 <start rate>            -  Autocorrelated exponential rates in accordance w.\n" +
				"                                       Aris-Brosou-Yang '02.\n" +
				"    ACLBPL07 <start rate> <mu> <theta> <sigma>\n" +
				"                                    -  Autocorrelated CIR rates in accordance w.\n" +
				"                                       Lepage-Bryant-Phillipe-Lartillot '07. The\n" +
				"                                       process is simulated using a discretisation\n" +
				"                                       across every branch. The start rate refers to\n" +
				"                                       tip of tree in case there is a stem edge.\n" +
				"    IIDRK07 <host tree> <guest-to-host map>\n" +
				"                                    -  IID gamma rates governed by host tree in\n" +
				"                                       accordance w. Rasmussen-Kellis '07/'11.\n" +
				"                                       Every guest branch rate is created from a gamma\n" +
				"                                       distribution specific for each host edge the\n" +
				"                                       branch passes over. Parameters are stored in the\n" +
				"                                       host tree thus: (A:0.4[&&PRIME PARAMS=(<k>,<theta>)],...\n" +
				"                                       Guest and host tree must be temporally compatible\n" +
				"                                       and have no lateral transfer events." +
				"                                       "
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
		return this.getTree(args.get(0), doStrict);
	}
	
	public PrIMENewickTree getTree(String s, boolean doStrict) throws NewickIOException, IOException, TopologyException {
		File f = new File(s);
		if (f.exists()) {
			// We do allow non-ultrametric trees to, if relaxing in multiple rounds.
			return PrIMENewickTreeReader.readTree(f, false, doStrict);
		} else {
			return PrIMENewickTreeReader.readTree(s, false, doStrict);
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
