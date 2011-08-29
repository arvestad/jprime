package se.cbb.jprime.apps.gsrf;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * Handles all application parameters.
 * 
 * @author Joel Sjöstrand.
 */
public class Parameters {

	/** Required parameters: D, S and GS. */
	@Parameter(description = "<Multialignment> <Host tree> <Guest-to-host leaf map>.")
	public List<String> files = new ArrayList<String>();
	
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** Output location. */
	@Parameter(names = {"-o", "--outfile"}, description = "Output file. Default: stdout.")
	public String outfile = null;
	
	/** Post scriptum location. */
	@Parameter(names = {"-ps", "--postscriptumfile"}, description = "Post scriptum file. Default: <outfile>.ps when -o has been specified, " +
			"stdout when -o has not been specified, suppressed if -ps NONE is specified.")
	public String psfile = null;
	
	/** PRNG seed. */
	@Parameter(names = {"-s", "--seed"}, description = "PRNG seed. Default: Random seed.")
	public Integer seed = null;
	
	/** Iterations. */
	@Parameter(names = {"-i", "--iterations"}, description = "Number of iterations (attempted state changes).")
	public Integer iterations = 1000000;
	
	/** Thinning. */
	@Parameter(names = {"-t", "--thinning"}, description = "Thinning factor, i.e., sample output every n-th iteration.")
	public Integer thinning = 100;
	
	/** Substitution model. */
	@Parameter(names = {"-sm", "--substitutionmodel"}, description = "Substitution model. Only JTT supported at the moment.")
	public String substitutionModel = "JTT";
	
	/** Gamma site rate categories. */
	@Parameter(names = {"-cats", "--siteratecategories"}, description = "Number of categories for discretised Gamma distribution" +
			" for rate variation across sites.")
	public Integer gammaCategoriesOverSites = 1;

	/** Edge rate distribution. */
	@Parameter(names = {"-erd", "--edgeratedistribution"}, description = "Distribution underlying relaxed molecular clock through IID" +
			" substitution rates across lineages. Valid values are GAMMA, INVGAMMA, LOGN and UNIFORM.")
	public String edgeRateDistrib = "GAMMA";
	
	/** Edge rate distribution parameters. */
	@Parameter(names = {"-erdmv", "--edgeratedistributionmeanvar"}, arity = 2, description = "Mean and variance for relaxed clock. Refers to range (a,b)" +
			" if UNIFORM. Default: Simple rule-of-thumb.")
	public List<String> edgeRateDistribMeanVar = null;
	
	/** Fix edge rate distribution. */
	@Parameter(names = {"-erdfix", "--edgeratedistributionfixed"}, description = "Fix relaxed molecular clock parameters.")
	public Boolean edgeRateDistribFixed = false;
	
	/** Duplication rate. */
	@Parameter(names = {"-dup", "--duplicationrate"}, description = "Initial duplication rate.")
	public Double dupRate = 1.0;
	
	/** Fix duplication rate. */
	@Parameter(names = {"-dupfix", "--duplicationratefixed"}, description = "Fix duplication rate.")
	public Boolean dupRateFixed = false;
	
	/** Loss rate. */
	@Parameter(names = {"-loss", "--lossrate"}, description = "Initial loss rate.")
	public Double lossRate = 1.0;
	
	/** Fix loss rate. */
	@Parameter(names = {"-lossfix", "--lossratefixed"}, description = "Fix loss rate.")
	public Boolean lossRateFixed = false;
	
	/** Discretisation timestep. */
	@Parameter(names = {"-dts", "--discretisationtimestep"}, description = "Discretisation timestep upper bound. E.g. 0.02 yields" +
			" timesteps of max size 0.02 on each host edge.")
	public Double discTimestep = 0.02;
	
	/** Min. no of discretisation intervals. */
	@Parameter(names = {"-dmin", "--discretisationmin"}, description = "Min. no. of discretisation intervals on each host edge.")
	public Integer discMin = 3;
	
	/** Max. no of discretisation intervals. */
	@Parameter(names = {"-dmax", "--discretisationmax"}, description = "Max. no. of discretisation intervals on each host edge.")
	public Integer discMax = 10;
	
	/** No of discretisation intervals for stem. */
	@Parameter(names = {"-dstem", "--discretisationstem"}, description = "No. of discretisation intervals on the host edge predating the root." +
			" Default: Simple rule-of-thumb.")
	public Integer discStem = null;
	
	/** Guest tree. */
	@Parameter(names = {"-g", "--guesttree"}, description = "Initial guest tree topology. May be either <file>, UNIFORM or NJ.")
	public String guestTree = "NJ";
	
	/** Fix guest tree. */
	@Parameter(names = {"-gfix", "--guesttreefixed"}, description = "Fix guest tree topology.")
	public Boolean guestTreeFixed =false;
	
	/** Debug flag. */
	@Parameter(names = {"-d", "--debug"}, description = "Output debugging info.")
	public Boolean debug = false;
	
}
