package se.cbb.jprime.apps;

import java.util.List;
import com.beust.jcommander.Parameter;

import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;

/**
 * Abstract class for storing annotated jcommander parameters extended by age, dlrs, dltrs and pdlrs 
 * as these four have much parameter handling in common. Should be extended if implementing an app 
 * with similar functionality to these.
 */

public abstract class Parameters {
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;

	/** Output location. */
	@Parameter(names = {"-o", "--outfile"}, description = "Output file. Default: stdout.")
	public String outfile = null;

	/** Info output location. */
	@Parameter(names = {"-info", "--infofile"}, description = "Info output file. Default: <outfile>.info when -o has been specified, " +
			"stdout when -o has not been specified, suppressed if -info NONE is specified.")
	public String infofile = null;

	/** Run type. */
	@Parameter(names = {"-run", "--runtype"}, description = "Type of run. Valid values are MCMC and HILLCLIMBING.")
	public String runtype = "MCMC";

	/** PRNG seed. */
	@Parameter(names = {"-s", "--seed"}, description = "PRNG seed. Default: Random seed.")
	public String seed = null;

	/** Iterations. */
	@Parameter(names = {"-i", "--iterations"}, description = "Number of iterations (attempted state changes).")
	public Integer iterations = 1000000;

	/** Thinning. */
	@Parameter(names = {"-t", "--thinning"}, description = "Thinning factor, i.e., sample output every n-th iteration.")
	public Integer thinning = 100;

	/** Substitution model. */
	@Parameter(names = {"-sm", "--substitutionmodel"}, description = SubstitutionMatrixHandlerFactory.USER_MESSAGE)
	public String substitutionModel = "JC69";

	/** Duplication rate. */
	@Parameter(names = {"-dup", "--duplicationrate"}, description = "Initial duplication rate. Append with FIXED for no " +
			"perturbation, e.g. 0.1FIXED. Default: Simple rule-of-thumb.")
	public String dupRate = null;

	/** Loss rate. */
	@Parameter(names = {"-loss", "--lossrate"}, description = "Initial loss rate. Append with FIXED for no " +
			"perturbation, e.g. 0.1FIXED. Default: Simple rule-of-thumb.")
	public String lossRate = null;

	/** Edge rate distribution. */
	@Parameter(names = {"-erpd", "--edgeratepd"}, description = "Probability distribution underlying relaxed molecular clock through IID" +
			" substitution rates over guest tree edges. Valid values are currently GAMMA and UNIFORM.")
	public String edgeRatePD = "GAMMA";

	/** Edge rate distribution parameter 1. */
	@Parameter(names = {"-erpdm", "--edgeratepdmean"}, description = "Mean for relaxed clock probability distribution. If UNIFORM, " +
			"refers to lower bound (a,...) instead. Append with FIXED for no perturbation, e.g. 0.1FIXED. Default: 0.5 or 0 if UNIFORM.")
	public String edgeRatePDMean = null;

	/** Edge rate distribution parameter 2. */
	@Parameter(names = {"-erpdcv", "--edgeratepdcv"}, description = "Coefficient of variation (CV) for relaxed clock probability distribution. If UNIFORM," +
			"refers to upper bound (...,b) instead. Append with FIXED for no perturbation, e.g. 1.2FIXED. Default: 1.0, or 5.0 if UNIFORM.")
	public String edgeRatePDCV = null;

	/** Gamma site rate categories. */
	@Parameter(names = {"-srcats", "--siteratecategories"}, description = "Number of categories for discretised gamma distribution" +
			" for rate variation across sites, e.g., 4.")
	public Integer siteRateCats = 1;

	/** Edge rate distribution parameter 1. */
	@Parameter(names = {"-srshape", "--siterateshape"}, description = "Shape parameter for discretised gamma distribution" +
			" for rate variation across sites. Only applicable if number of categories > 1. Append with FIXED for no perturbation, e.g. 1.0FIXED.")
	public String siteRateShape = "0.8";

	/** No of discretisation intervals for stem. */
	@Parameter(names = {"-dstem", "--discretisationstem"}, description = "No. of discretisation intervals on the host edge predating the root." +
			" Default: Simple rule-of-thumb.")
	public Integer discStem = null;

	/** Guest tree. */
	@Parameter(names = {"-g", "--guesttree"}, description = "Initial guest tree topology. May be either <file>, UNIFORM or NJ." +
			" If a file is specified and this has branch lengths, these lengths are used as initial values.")
	public String guestTree = "NJ";

	/** Fix guest tree. */
	@Parameter(names = {"-gfix", "--guesttreefixed"}, description = "Fix topology of guest tree.")
	public Boolean guestTreeFixed = false;

	/** Fix guest tree branch lengths. */
	@Parameter(names = {"-lfix", "--lengthsfixed"}, description = "Fix branch lengths of guest tree.")
	public Boolean lengthsFixed = false;

	/** MSA that will be used to generate a starting gene tree */
	@Parameter(names = {"-fp", "--fastphylo"}, description = "Generate a good starting tree by calling FastPhylo on a specified MSA <file>." + 
			" If --guesttree is specified it inhibits this parameter.")
	public boolean msaFastPhyloTree = false;

	/** Sample (output) branch lengths in additional Newick tree. */
	@Parameter(names = {"-lout", "--outputlengths"}, description = "When sampling, output an additional Newick guest tree with branch lengths.")
	public Boolean outputLengths = false;

	/** Sample guest trees among a fixed set of Newick trees. */
	@Parameter(names = {"-gtset", "--guesttreeset"}, description = "Sample guest trees among a fixed set of Newick trees in the specified <file>.")
	public String guestTreeSet = null;

	/** Take the branch lengths into account for a guest tree set. */
	@Parameter(names = {"-gtsetl", "--guesttreesetwithlengths"}, description = "When sampling guest trees from a fixed set, take the branch lengths into account.")
	public Boolean guestTreeSetWithLengths = false;

	/** Samples uniformly among unique topologies rather than by topology prevalence. */
	@Parameter(names = {"-gtsete", "--guesttreesetequaltopochance"}, description = "When sampling guest trees from a fixed set,"+
			"samples uniformly among unique topologies rather than by topology prevalence.")
	public Boolean guestTreeSetEqualTopoChance = false;

	/** If the file that contains the set of guest trees has a header. */
	@Parameter(names = {"-gtseth", "--guesttreesetfilehasheader"}, description = "The file that contains the set of guest trees has a header.")
	public Boolean guestTreeSetFileHasHeader = false;

	/** Relative column number containing trees. */
	@Parameter(names = {"-gtsetc", "--guesttreesetfilerelcolno"}, description = "The relative column number containing trees in the file containing the set of guest trees.")
	public Integer guestTreeSetFileRelColNo = null;

	/** Proportion of samples to discard as burn-in. */
	@Parameter(names = {"-gtsetb", "--guesttreesetburninprop"}, description = "Proportion of samples to discard as burn-in in the guest tree set, e.g. 0.25 for 25%..")
	public String guestTreeSetBurninProp = "0.25";

	/** Minimum coverage for a topology to be included among the samples */
	@Parameter(names = {"-gtsetcvg"}, description = "Minimum coverage for a topology to be included among the samples in the guest tree set, e.g. 0.01.")
	public String guestTreeSetMinCvg = "0.01";

	/** Tuning parameter: branch lengths proposal distribution variance. */
	@Parameter(names = {"-tngl", "--tuninglengths"}, description = "Tuning parameter: Governs branch lengths proposal distribution's CV.")
	public String tuningLengths = "[0.6,0.6]";

	/** Tuning parameter: branch lengths selector weights. */
	@Parameter(names = {"-tnglw", "--tuninglengthsweights"}, description = "Tuning parameter: Governs how often 1,2,... branch lengths " +
			"will be perturbed simultaneously, e.g., [0.5,0.5] for an equal chance of 1 or 2 branch lengths.")
	public String tuningLengthsSelectorWeights = "[0.4,0.3,0.2,0.1]";

	/** Tuning parameter: proposer selector weights. */
	@Parameter(names = {"-tngpw", "--tuningproposerweights"}, description = "Tuning parameter: Governs how often 1,2,... simultaneous proposers " +
			"(a.k.a. operators or kernels) will be activated for performing a state change, e.g., [0.5,0.5] for an equal chance of 1 or 2 proposers. No more than 4 may be specified.")
	public String tuningProposerSelectorWeights = "[0.7,0.2,0.1]";

	/** Tuning parameter: duplication rate proposer weight. */
	@Parameter(names = {"-tngwdup", "--tuningweightduplicationrate"}, description = "Tuning parameter: Relative activation weight for duplication rate proposer" +
			" as [w_start,w_end], where start and end refer to the first and last iteration respectively.")
	public String tuningWeightDupRate = "[1.0,1.0]";

	/** Tuning parameter: loss rate proposer weight. */
	@Parameter(names = {"-tngwloss", "--tuningweightlossrate"}, description = "Tuning parameter: Relative activation weight for loss rate proposer.")
	public String tuningWeightLossRate = "[1.0,1.0]";

	/** Tuning parameter: edge rate mean proposer weight. */
	@Parameter(names = {"-tngwerm", "--tuningweightedgeratemean"}, description = "Tuning parameter: Relative activation weight for edge rate mean proposer.")
	public String tuningWeightEdgeRateMean = "[1.0,1.0]";

	/** Tuning parameter: edge rate CV proposer weight. */
	@Parameter(names = {"-tngwercv", "--tuningweightedgeratecv"}, description = "Tuning parameter: Relative activation weight for edge rate CV proposer.")
	public String tuningWeightEdgeRateCV = "[1.0,1.0]";

	/** Tuning parameter: site rate shape proposer weight. */
	@Parameter(names = {"-tngwsrshape", "--tuningweightsiterateshape"}, description = "Tuning parameter: Relative activation weight for site rate shape proposer.")
	public String tuningWeightSiteRateShape = "[0.25,0.25]";

	/** Tuning parameter: guest tree proposer weight. */
	@Parameter(names = {"-tngwg", "--tuningweightguesttree"}, description = "Tuning parameter: Relative activation weight for guest tree topology proposer.")
	public String tuningWeightG = "[2.0,2.0]";

	/** Tuning parameter: branch lengths. */
	@Parameter(names = {"-tngwl", "--tuningweightlengths"}, description = "Tuning parameter: Relative activation weight for branch lengths proposer.")
	public String tuningWeightLengths = "[10.0,10.0]";

	/** Debug flag. */
	@Parameter(names = {"-dbg", "--debug"}, description = "Output debugging info.")
	public Boolean debug = false;

	public Boolean getHelp() {
		return help;
	}

	public String getOutfile() {
		return outfile;
	}

	public String getInfofile() {
		return infofile;
	}

	public String getRuntype() {
		return runtype;
	}

	public String getSeed() {
		return seed;
	}

	public Integer getIterations() {
		return iterations;
	}

	public Integer getThinning() {
		return thinning;
	}

	public String getSubstitutionModel() {
		return substitutionModel;
	}

	public String getDupRate() {
		return dupRate;
	}

	public String getLossRate() {
		return lossRate;
	}

	public String getEdgeRatePD() {
		return edgeRatePD;
	}

	public String getEdgeRatePDMean() {
		return edgeRatePDMean;
	}

	public String getEdgeRatePDCV() {
		return edgeRatePDCV;
	}

	public Integer getSiteRateCats() {
		return siteRateCats;
	}

	public String getSiteRateShape() {
		return siteRateShape;
	}

	public Integer getDiscStem() {
		return discStem;
	}

	public String getGuestTree() {
		return guestTree;
	}

	public Boolean getGuestTreeFixed() {
		return guestTreeFixed;
	}

	public Boolean getLengthsFixed() {
		return lengthsFixed;
	}

	public String getGuestTreeSet() {
		return guestTreeSet;
	}

	public Boolean getGuestTreeSetWithLengths() {
		return guestTreeSetWithLengths;
	}

	public Boolean getGuestTreeSetEqualTopoChance() {
		return guestTreeSetEqualTopoChance;
	}

	public Boolean getGuestTreeSetFileHasHeader() {
		return guestTreeSetFileHasHeader;
	}

	public Integer getGuestTreeSetFileRelColNo() {
		return guestTreeSetFileRelColNo;
	}

	public String getGuestTreeSetBurninProp() {
		return guestTreeSetBurninProp;
	}

	public String getGuestTreeSetMinCvg() {
		return guestTreeSetMinCvg;
	}

	public boolean isMsaFastPhyloTree() {
		return msaFastPhyloTree;
	}

	public Boolean getOutputLengths() {
		return outputLengths;
	}

	public String getTuningLengths() {
		return tuningLengths;
	}

	public abstract String getTuningGuestTreeMoveWeights();

	public String getTuningLengthsSelectorWeights() {
		return tuningLengthsSelectorWeights;
	}

	public String getTuningProposerSelectorWeights() {
		return tuningProposerSelectorWeights;
	}

	public String getTuningWeightDupRate() {
		return tuningWeightDupRate;
	}

	public String getTuningWeightLossRate() {
		return tuningWeightLossRate;
	}

	public String getTuningWeightEdgeRateMean() {
		return tuningWeightEdgeRateMean;
	}

	public String getTuningWeightEdgeRateCV() {
		return tuningWeightEdgeRateCV;
	}

	public String getTuningWeightSiteRateShape() {
		return tuningWeightSiteRateShape;
	}

	public String getTuningWeightG() {
		return tuningWeightG;
	}

	public String getTuningWeightLengths() {
		return tuningWeightLengths;
	}

	public Boolean getDebug() {
		return debug;
	}
	
	public void setDiscStem(int x) {
		discStem = x;
	}
	
	public abstract String getDiscTimestep();

	public abstract Integer getDiscMin();

	public abstract Integer getDiscMax();

	public abstract List<String> getFiles();
	
	public abstract String getGuestTreeBiasedSwapping();

	public abstract String getTuningDupRate();

	public abstract String getTuningLossRate();

	public abstract String getTuningEdgeRateMean();

	public abstract String getTuningEdgeRateCV();

	public abstract String getTuningSiteRateShape();
}
