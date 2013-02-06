package se.cbb.jprime.apps.genphylodata;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.TopologyException;

import com.beust.jcommander.Parameter;

/**
 * Contains user settings.
 * 
 * @author Joel Sjöstrand.
 */
public class BranchLengthRelaxerParameters {

	/** Required parameters: S, D and GS. */
	@Parameter(description = "<Tree file or tree string> <Model> <Model arg 1> <Model arg 2> ...")
	public List<String> args = new ArrayList<String>();
		
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** PRNG seed. */
	@Parameter(names = {"-s", "--seed"}, description = "PRNG seed. Default: Random seed.")
	public String seed = null;
	
	public PRNG prng = null;
	
	/**
	 * Return rate model.
	 * @return model.
	 */
	public RateModel getRateModel() {
		if (this.prng == null) {
			this.prng = (this.seed == null ? new PRNG() : new PRNG(new BigInteger(this.seed)));
		}
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
		} else {
			throw new IllegalArgumentException("Invalid rate model identifier.");
		}
	}
	
	/**
	 * Returns the tree
	 * @return
	 * @throws NewickIOException
	 * @throws IOException
	 * @throws TopologyException
	 */
	public PrIMENewickTree getTree() throws NewickIOException, IOException, TopologyException {
		File f = new File(this.args.get(0));
		if (f.exists()) {
			// We do allow non-ultrametric trees to, if relaxing in multiple rounds.
			return PrIMENewickTreeReader.readTree(f, false, false);
		} else {
			return PrIMENewickTreeReader.readTree(args.get(0), false, false);
		}
	}
}
