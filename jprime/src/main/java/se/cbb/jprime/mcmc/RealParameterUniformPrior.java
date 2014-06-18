package se.cbb.jprime.mcmc;

import java.util.Map;

import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.math.PRNG;
/**
 * Represents a uniform (non-informative) prior for a real-valued
 * parameters (singletons or arrays).
 * This is typically used to yield a prior probability 0 if values get too small or large,
 * which could ruin convergence.
 * <p/>
 * Important note: By default, a prior probability of 1 will be returned if the current value
 * is within the interval, e.g. [A,B]. This is to prevent a typically unnecessary decrease
 * of the overall likelihood, which could lead to loss of precision.
 * If you instead want to return 1/(B-A)^noOfSubParams, this can be switched on with
 * <code>returnActualPriorProbability(true)</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class RealParameterUniformPrior implements InferenceModel {
	
	/** Parameter for prior. */
	private RealParameter param;
	
	/** Allowed interval. */
	private RealInterval interval;
	
	/** Returns actual prior probability if true. */
	private boolean doUseActual;
	
	/** Prior. */
	private LogDouble priorProbability;
	
	/** Cache. */
	private LogDouble priorProbabilityCache = null;
	
	/**
	 * Constructor.
	 * @param param parameter on which prior acts.
	 * @param priorInterval allowed interval.
	 */
	public RealParameterUniformPrior(RealParameter param, RealInterval priorInterval) {
		this.param = param;
		this.interval = priorInterval;
		this.doUseActual = false;
		this.update();
	}
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.param };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		this.priorProbabilityCache = new LogDouble(this.priorProbability);
		this.update();
		changeInfos.put(this, new ChangeInfo(this, "Full uniform prior update."));
	}
	
	/**
	 * Updates the prior dataProbability.
	 */
	private void update() {
		// At the moment, we go through the lot of subparameters, even if only parts have changed.
		this.priorProbability = new LogDouble(1.0);
		if (this.doUseActual) {
			double density = 1.0 / this.interval.getWidth();
			for (int i = 0; i < this.param.getNoOfSubParameters(); ++i) {
				if (!this.interval.isWithin(this.param.getValue(i))) {
					this.priorProbability = new LogDouble(0.0);
						break;
				} else {
					this.priorProbability.mult(density);
				}
			}
		} else {
			for (int i = 0; i < this.param.getNoOfSubParameters(); ++i) {
				if (!this.interval.isWithin(this.param.getValue(i))) {
					this.priorProbability = new LogDouble(0.0);
					break;
				}
			}
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.priorProbabilityCache = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.priorProbability = this.priorProbabilityCache;
		this.priorProbabilityCache = null;
	}

	@Override
	public Class<?> getSampleType() {
		return LogDouble.class;
	}

	@Override
	public String getSampleHeader() {
		return (this.param.getName() + "UniformPriorLikelihood");
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		return this.priorProbability.toString();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(prefix).append("REAL-PARAMETER UNIFORM PRIOR\n");
		sb.append(prefix).append("Parameter: ").append(this.param.getName()).append('\n');
		sb.append(prefix).append("Interval: ").append(this.interval.toString()).append('\n');
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(prefix).append("REAL-PARAMETER UNIFORM PRIOR\n");
		sb.append(prefix).append("Parameter: ").append(this.param.getName()).append('\n');
		return sb.toString();
	}

	@Override
	public LogDouble getDataProbability() {
		return this.priorProbability;
	}
	
	/**
	 * If current value(s) within the interval, e.g. [A,B], governs whether
	 * 1 is returned or 1/(B-A)^noOfSubParams.
	 * @param doUseActual false to return 1; true to return 1/(B-A)^noOfSubParams.
	 */
	public void returnActualPriorProbability(boolean doUseActual) {
		this.doUseActual = doUseActual;
	}

	@Override
	public String getModelName() {
		return "RealParameterUniformPrior";
	}

}
