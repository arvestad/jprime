package se.cbb.jprime.apps.dlrs;

import java.util.ArrayList;
import java.util.List;
import se.cbb.jprime.apps.Parameters;

import com.beust.jcommander.Parameter;

/**
 * Handles all regular application parameters.
 * 
 * @author Joel Sjöstrand.
 * @author Vincent Llorens.
 */
public class DLRSParameters extends Parameters {

	/** Required parameters: S, D and GS. */
	@Parameter(description = "<Host tree> <Multialignment> <Guest-to-host leaf map>.")
	public List<String> files = new ArrayList<String>();

	/** Citation info */
	@Parameter(names = { "--cite" }, description = "Output citation info (BibTeX) and exit.")
	public Boolean cite = false;

	/** Discretisation timestep. */
	@Parameter(names = { "-dts",
			"--discretisationtimestep" }, description = "Discretisation timestep upper bound. E.g. 0.02 yields"
					+ " timesteps of max size 0.02 on each host edge.")
	public String discTimestep = "0.02";

	/** Min. no of discretisation intervals. */
	@Parameter(names = { "-dmin",
			"--discretisationmin" }, description = "Min. no. of discretisation intervals on each host edge.")
	public Integer discMin = 3;

	/** Max. no of discretisation intervals. */
	@Parameter(names = { "-dmax",
			"--discretisationmax" }, description = "Max. no. of discretisation intervals on each host edge.")
	public Integer discMax = 10;

	/** Maximum no of implied losses for a viable placement. */
	@Parameter(names = { "-maxlosses",
			"--maximpliedlosses" }, description = "Maximum allowed no. of implied losses for a duplication placement to "
					+ "be considered viable.")
	public Integer maxLosses = 3;

	/** Sample realisations. */
	@Parameter(names = { "-real",
			"--samplerealisations" }, arity = 2, description = "When sampling, output dated reconciliations to a file. "
					+ "Takes two arguments: <file> <no of realisations per sample>.")
	public List<String> sampleRealisations = null;

	/** Biased branch-swapper. */
	@Parameter(names = { "-gbiased",
			"--guesttreebiasedbranchswapper" }, description = "Use parsimony-biased branch-swapper "
					+ "for better exploration of guest tree space. Value indicates proportion of biased moves.")
	public String guestTreeBiasedSwapping = null;

	/** Tuning parameter: duplication rate proposal distribution variance. */
	@Parameter(names = { "-tngdup",
			"--tuningduplicationrate" }, description = "Tuning parameter: Governs duplication rate proposal distribution's CV as "
					+ " [CV_start,CV_end], where start and end refer to values at first and last iteration respectively.")
	public String tuningDupRate = "[0.75,0.75]";

	/** Tuning parameter: duplication rate proposal distribution variance. */
	@Parameter(names = { "-tngloss",
			"--tuninglossrate" }, description = "Tuning parameter: Governs loss rate proposal distribution's CV.")
	public String tuningLossRate = "[0.75,0.75]";

	/** Tuning parameter: edge rate mean proposal distribution variance. */
	@Parameter(names = { "-tngerm",
			"--tuningedgeratemean" }, description = "Tuning parameter: Governs edge rate mean proposal distribution's CV.")
	public String tuningEdgeRateMean = "[0.25,0.25]";

	/** Tuning parameter: edge rate CV proposal distribution variance. */
	@Parameter(names = { "-tngercv",
			"--tuningedgeratecv" }, description = "Tuning parameter: Governs edge rate CV proposal distribution's CV.")
	public String tuningEdgeRateCV = "[0.25,0.25]";

	/** Tuning parameter: site rate shape proposal distribution variance. */
	@Parameter(names = { "-tngsrshape",
			"--tuningsiterateshape" }, description = "Tuning parameter: Governs site rate shape proposal distribution's CV.")
	public String tuningSiteRateShape = "[0.10,0.10]";

	/** Tuning parameter: guest tree move weights. */
	@Parameter(names = { "-tnggw",
			"--tuningguesttreeweights" }, description = "Tuning parameter: Governs how often a particular "
					+ "branch swap operation is carried out as [NNI,SPR,Rerooting].")
	public String tuningGuestTreeMoveWeights = "[0.70,0.25,0.05]";

	/**
	 * Normalizing weight parameter for branch lengths of gene tree.
	 * Particularly useful when gene trees are generated using a different
	 * concept of branch lengths e.g. ALF (PAM distance).
	 */
	@Parameter(names = { "-normp",
			"--normalizationparam" }, description = "Normalizing ratio for branch lengths of gene tree.")
	public String normp = "1.0";

	public List<String> getFiles() {
		return files;
	}

	public Boolean getCite() {
		return cite;
	}

	public String getDiscTimestep() {
		return discTimestep;
	}

	public Integer getDiscMin() {
		return discMin;
	}

	public Integer getDiscMax() {
		return discMax;
	}

	public Integer getMaxLosses() {
		return maxLosses;
	}

	public List<String> getSampleRealisations() {
		return sampleRealisations;
	}

	public String getGuestTreeBiasedSwapping() {
		return guestTreeBiasedSwapping;
	}

	public String getTuningDupRate() {
		return tuningDupRate;
	}

	public String getTuningLossRate() {
		return tuningLossRate;
	}

	public String getTuningEdgeRateMean() {
		return tuningEdgeRateMean;
	}

	public String getTuningEdgeRateCV() {
		return tuningEdgeRateCV;
	}

	public String getTuningSiteRateShape() {
		return tuningSiteRateShape;
	}

	public String getTuningGuestTreeMoveWeights() {
		return tuningGuestTreeMoveWeights;
	}

	public String getNormp() {
		return normp;
	}
}
