package se.cbb.jprime.apps.dltrs;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * Handles all regular application parameters.
 * 
 * @author Joel Sjöstrand.
 * @author Mehmood Alam Khan
 * @author Vincent Llorens.
 */
public class DLTRSParameters extends se.cbb.jprime.apps.Parameters {

	/** Required parameters: S, D and GS. */
	@Parameter(description = "<Host tree> <Multialignment> <Guest-to-host leaf map>.")
	public List<String> files = new ArrayList<String>();

	/** Citation info */
	@Parameter(names = { "--cite" }, description = "Output citation info (BibTeX) and exit.")
	public Boolean cite = false;

	/** Compute sample realization. */
	@Parameter(names = { "-real", "--samplerealisations" }, description = "Sample Realizations.")
	public Boolean sampleRealisations = false;

	/** Compute Max realization. */
	@Parameter(names = { "-mr", "--maxrealization" }, description = "Compute Max Realizations.")
	public Boolean maxRealizationFlag = false;

	/** Transfer rate. */
	@Parameter(names = { "-trans", "--transferrate" }, description = "Initial transfer rate. Append with FIXED for no "
			+ "perturbation, e.g. 0.1FIXED. Default: Simple rule-of-thumb.")
	public String transRate = null;

	/** Discretisation timestep. */
	@Parameter(names = { "-dts",
			"--discretisationtimestep" }, description = "Discretisation timestep upper bound. E.g. 0.05 yields"
					+ " timesteps of max size 0.05 on each host edge generation.")
	public String discTimestep = "0.05";

	/** Min. no of discretisation intervals. */
	@Parameter(names = { "-dmin",
			"--discretisationmin" }, description = "Min. no. of discretisation intervals on each host edge generation.")
	public Integer discMin = 2;

	/** Max. no of discretisation intervals. */
	@Parameter(names = { "-dmax",
			"--discretisationmax" }, description = "Max. no. of discretisation intervals on each host edge generation.")
	public Integer discMax = 5;

	// TODO: Investigate!
	/// ** Adjust for contemporary edges. */
	// @Parameter(names = {"-adjust", "--adjusttransfer"}, description = "In the
	// model, adjust probability of transfer by normalising with number of
	// disjoint contemporary host tree edges.")
	// public Boolean adjust = false;

	/** Tuning parameter: duplication rate proposal distribution variance. */
	@Parameter(names = { "-tngdup",
			"--tuningduplicationrate" }, description = "Tuning parameter: Governs duplication rate proposal distribution's CV as "
					+ " [CV_start,CV_end], where start and end refer to values at first and last iteration respectively.")
	public String tuningDupRate = "[0.8,0.8]";

	/** Tuning parameter: duplication rate proposal distribution variance. */
	@Parameter(names = { "-tngloss",
			"--tuninglossrate" }, description = "Tuning parameter: Governs loss rate proposal distribution's CV.")
	public String tuningLossRate = "[0.8,0.8]";

	/** Tuning parameter: transfer rate proposal distribution variance. */
	@Parameter(names = { "-tngtrans",
			"--tuningtransferrate" }, description = "Tuning parameter: Governs transfer rate proposal distribution's CV.")
	public String tuningTransferRate = "[0.7,0.7]";

	/** Tuning parameter: edge rate mean proposal distribution variance. */
	@Parameter(names = { "-tngerm",
			"--tuningedgeratemean" }, description = "Tuning parameter: Governs edge rate mean proposal distribution's CV.")
	public String tuningEdgeRateMean = "[0.2,0.2]";

	/** Tuning parameter: edge rate CV proposal distribution variance. */
	@Parameter(names = { "-tngercv",
			"--tuningedgeratecv" }, description = "Tuning parameter: Governs edge rate CV proposal distribution's CV.")
	public String tuningEdgeRateCV = "[0.2,0.2]";

	/** Tuning parameter: site rate shape proposal distribution variance. */
	@Parameter(names = { "-tngsrshape",
			"--tuningsiterateshape" }, description = "Tuning parameter: Governs site rate shape proposal distribution's CV.")
	public String tuningSiteRateShape = "[0.1,0.1]";

	/** Tuning parameter: guest tree move weights. */
	@Parameter(names = { "-tnggw",
			"--tuningguesttreeweights" }, description = "Tuning parameter: Governs how often a particular "
					+ "branch swap operation is carried out as [NNI,SPR,Rerooting].")
	public String tuningGuestTreeMoveWeights = "[0.5,0.3,0.2]";

	/** Tuning parameter: transfer rate proposer weight. */
	@Parameter(names = { "-tngwtrans",
			"--tuningweighttransferrate" }, description = "Tuning parameter: Relative activation weight for transfer rate proposer.")
	public String tuningWeightTransferRate = "[1.0,1.0]";

	/** Exceptions flag. */
	@Parameter(names = { "-uncatch",
			"--uncatchexception" }, description = "Allows application exceptions to propagate all the way to the terminal without being caught.")
	public Boolean uncatch = false;

	/** offline sampling from posterior. */
	@Parameter(names = { "-posterior",
			"--sampleRealFromPosterior" }, arity = 1, description = "Perform offline sampling from posterior "
					+ "Takes one arguments: <file>.")

	public List<String> runSampRealFromPosterior = null;

	public List<String> getFiles() {
		return files;
	}

	public Boolean getCite() {
		return cite;
	}

	public Boolean getSampleRealisations() {
		return sampleRealisations;
	}

	public Boolean getMaxRealizationFlag() {
		return maxRealizationFlag;
	}

	public String getTransRate() {
		return transRate;
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

	public String getTuningDupRate() {
		return tuningDupRate;
	}

	public String getTuningLossRate() {
		return tuningLossRate;
	}

	public String getTuningTransferRate() {
		return tuningTransferRate;
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

	public String getTuningWeightTransferRate() {
		return tuningWeightTransferRate;
	}

	public Boolean getUncatch() {
		return uncatch;
	}

	public List<String> getRunSampRealFromPosterior() {
		return runSampRealFromPosterior;
	}

	@Override
	public String getGuestTreeBiasedSwapping() {
		// TODO Auto-generated method stub
		return null;
	}

}
