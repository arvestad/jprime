package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.math.NormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.math.RealInterval.Type;

/**
 * Represents a normal proposal distribution. That is, given a current state parameter value m,
 * it draws a new value Y ~ N(m,v). It is also possible to limit the domain to [A,B], (A,inf) and
 * so forth, thus creating a truncated normal distribution.
 * <p/>
 * The proposer can perturb both singleton parameters and arrays. In the latter case, sub-parameters are treated
 * individually, and the user can control how many of them should be affected.
 * By default, this is set to one sub-parameter only, but it may be changed by invoking
 * <code>setSubParameterWeights(...)</code>.
 * <p/>
 * The proposer relies on two user-defined tuning parameters, t1 and t2, which define the variance v of
 * the proposal distribution: v is chosen so that for m > 0, Pr[(1-t1)*m < Y < (1+t1)*m] = t2. For instance, with
 * t1 = 0.5 and t2 = 0.6, the proposed value Y will be at most 50% greater or smaller
 * than the previous value m in 60% of the cases. For m < 0, the case is analogous, whereas for m = 0, a
 * small epsilon proposal variance
 * is used. For bounded domains, v is chosen as if not truncated and sampling is repeated until within the domain. 
 * 
 * @author Joel Sjöstrand.
 */
public class NormalProposer implements Proposer {

	/** Fixed distribution used for making certain computations. */
	private static final NormalDistribution N = new NormalDistribution(0.0, 1.0);
	
	/** Perturbed parameter. */
	private RealParameter param;
	
	/** Domain of parameter and proposal distribution. */
	private RealInterval interval;
	
	/** Pseudo-random number generator. */
	private PRNG prng;
	
	/** First tuning parameter. Governs width from old state value. */
	private TuningParameter t1;
	
	/** Second tuning parameter. Governs probability of staying in +-(1+t1)*old value. */
	private TuningParameter t2;

	/** Current number of perturbed sub-parameters. */
	int noPerturbed;
	
	/** Statistics. */
	private ProposerStatistics stats = null;
	
	/**
	 * Cumulative sub-parameter weights. Controls the probability of perturbing
	 * 0,1,2,... sub-parameters. Sums up to 1.0.
	 */
	private double[] cumSubParamWeights;
	
	/** On/off switch. */
	private boolean isEnabled;
	
	/**
	 * Constructor. Creates a normal proposal distribution.
	 * @param param state parameter perturbed by this proposer.
	 * @param interval domain of state parameter and proposal distribution.
	 * @param t1 tuning parameter governing factor from old state.
	 * @param t2 tuning parameter governing probability of staying within +-(1+t1)*old state.
	 * @param prng pseudo-random number generator.
	 */
	public NormalProposer(RealParameter param, RealInterval interval, TuningParameter t1, TuningParameter t2, PRNG prng) {
		if (t1.getMinValue() <= 0) {
			throw new IllegalArgumentException("First tuning parameter for normal proposer must be in (0,inf).");
		}
		if (t2.getMinValue() <= 0 || t2.getMaxValue() >= 1.0) {
			throw new IllegalArgumentException("Second tuning parameter for normal proposer must be in (0,1).");
		}
		RealInterval.Type t = interval.getType();
		if (t == Type.DEGENERATE || t == Type.EMPTY) {
			throw new IllegalArgumentException("Invalid interval for normal proposer.");
		}
		this.param = param;
		this.interval = interval;
		this.t1 = t1;
		this.t2 = t2;
		this.noPerturbed = 0;
		this.prng = prng;
		this.cumSubParamWeights = new double[] { 1.0 };
		this.isEnabled = true;
	}
	
	/**
	 * Constructor. Creates an unbounded proposal distribution, i.e. defined over (-inf,+inf).
	 * @param param state parameter perturbed by this proposer.
	 * @param t1 tuning parameter governing factor from old state.
	 * @param t2 tuning parameter governing probability of staying within +-(1+t1)*old state.
	 * @param prng random number generator.
	 */
	public NormalProposer(RealParameter param, TuningParameter t1, TuningParameter t2, PRNG prng) {
		this(param, new RealInterval(), t1, t2, prng);
	}
	
	@Override
	public Set<StateParameter> getParameters() {
		TreeSet<StateParameter> s = new TreeSet<StateParameter>();
		s.add(this.param);
		return s;
	}

	@Override
	public int getNoOfParameters() {
		return 1;
	}

	@Override
	public int getNoOfSubParameters() {
		return 1;
	}

	/**
	 * For a parameter containing multiple sub-parameters, it is possible to allow for multiple
	 * sub-parameters to be changed each proposal. This achieved by specifying a set of weights
	 * for the probabilities, e.g. [0.5,0.3,0.1] for perturbing 1 sub-parameter 50% of the time,
	 * 2 sub-parameters 30% of the time, and 3 sub-parameters 10% of the time. The sub-parameters are chosen uniformly.
	 * @param weights the weights.
	 */
	public void setSubParameterWeights(double[] weights) {
		this.cumSubParamWeights = new double[Math.min(weights.length, this.param.getNoOfSubParameters())];
		double tot = 0.0;
		for (int i = 0; i < this.cumSubParamWeights.length; ++i) {
			if (weights[i] < 0.0) {
				throw new IllegalArgumentException("Cannot assign negative weight in truncated normal proposer.");
			}
			tot += weights[i];
			this.cumSubParamWeights[i] = tot;
		}
		for (int i = 0; i < this.cumSubParamWeights.length; ++i) {
			this.cumSubParamWeights[i] /= tot;
		}
		this.cumSubParamWeights[this.cumSubParamWeights.length - 1] = 1.0;   // For numeric safety.
	}
	
	@Override
	public ProposerStatistics getStatistics() {
		return this.stats;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		ArrayList<TuningParameter> l = new ArrayList<TuningParameter>(2);
		l.add(this.t1);
		l.add(this.t2);
		return l;
	}

	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		
		int k = this.param.getNoOfSubParameters();
		int m = this.cumSubParamWeights.length;
		
		// Determine desired number of sub-parameters and select them.
		// Some special cases for better speed.
		int[] indices;
		if (k == 1) {
			// Only one to choose from.
			indices = new int[] { 0 };
		} else if (m == 1) {
			// Only one to choose.
			indices = new int[1];
			indices[0] = this.prng.nextInt(k);
		} else if (m == k && this.cumSubParamWeights[m-2] == 0.0) {
			// All should be chosen.
			indices = new int[k];
			for (int i = 0; i < k; ++i) { indices[i] = i; }
		} else {
			// Remaining cases.
			int no = 1;
			double d = this.prng.nextDouble();
			while (d > this.cumSubParamWeights[no-1]) { ++no; }
			indices = new int[no];
			ArrayList<Integer> l = new ArrayList<Integer>(k);
			for (int i = 0; i < k; ++i) { l.add(i); }
			for (int i = 0; i < no; ++i) {
				indices[i] = l.remove(this.prng.nextInt(l.size()));				
			}
		}
		
		// Cache.
		this.param.cache(indices);
		
		// Get size factor w.r.t. N(0,1) given t2.
		double normFact = N.getQuantile((1.0 + this.t2.getValue()) / 2);
		
		// Perturb all chosen sub-parameters.
		LogDouble forward = new LogDouble(1.0);
		LogDouble backward = new LogDouble(1.0);
		for (int i = 0; i < indices.length; ++i) {
			
			// Compute variance for current proposal distribution.
			double xOld = this.param.getValue(indices[i]);
			double stdev = (xOld == 0.0 ? 1e-10 : Math.abs(xOld * this.t1.getValue()) / normFact);
			
			// Sample a new value.
			NormalDistribution pd = new NormalDistribution(xOld, Math.pow(stdev, 2));
			double x = Double.NaN;
			int tries = 0;
			do {
				x = pd.sampleValue(this.prng);
				this.param.setValue(indices[i], x);
				++tries;
				if (tries > 100) {
					// Abort with invalid proposal.
					return new MetropolisHastingsProposal(this, this.param);
				}
			} while (!this.interval.isWithin(x));
			
			// Obtain "forward" density.
			double a = this.interval.getLowerBound();
			double b = this.interval.getUpperBound();
			double nonTails = 1.0;
			if (!Double.isInfinite(a)) {
				nonTails -= pd.getCDF(a);
			}
			if (!Double.isInfinite(b)) {
				nonTails -= (1.0 - pd.getCDF(b));
			}
			forward.mult(new LogDouble(Math.max(pd.getPDF(x) / nonTails, 0.0)));
			
			// Obtain "backward" density.
			stdev = (x == 0.0 ? 1e-10 : Math.abs(x * this.t1.getValue()) / normFact);
			pd.setMean(x);
			pd.setStandardDeviation(stdev);
			nonTails = 1.0;
			if (!Double.isInfinite(a)) {
				nonTails -= pd.getCDF(a);
			}
			if (!Double.isInfinite(b)) {
				nonTails -= (1.0 - pd.getCDF(b));
			}
			backward.mult(new LogDouble(Math.max(pd.getPDF(xOld) / nonTails, 0.0)));
		}
		this.noPerturbed = indices.length;
		
		// Set change info.
		changeInfos.put(this.param, new ChangeInfo(this.param, "Perturbed by NormalProposer", indices));
		
		// Generate proposal object.
		return new MetropolisHastingsProposal(this, forward, backward, this.param, indices.length);
	}

	@Override
	public void clearCache() {
		if (this.stats != null) {
			if (this.param.getNoOfSubParameters() > 1) {
				this.stats.increment(true, "" + this.noPerturbed + " perturbed sub-parameters");
			} else {
				this.stats.increment(true);
			}
		}
		this.param.clearCache();
	}

	@Override
	public void restoreCache() {
		if (this.stats != null) {
			if (this.param.getNoOfSubParameters() > 1) {
				this.stats.increment(false, "" + this.noPerturbed + " perturbed sub-parameters");
			} else {
				this.stats.increment(false);
			}
		}
		this.param.restoreCache();
	}

	@Override
	public boolean isEnabled() {
		return this.isEnabled;
	}

	@Override
	public void setEnabled(boolean isActive) {
		this.isEnabled = isActive;
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("NORMAL-DISTRIBUTED PROPOSER\n");
		sb.append(prefix).append("Perturbed parameter: ").append(this.param.getName()).append('\n');
		sb.append(prefix).append("Is active: ").append(this.isEnabled).append("\n");
		sb.append(prefix).append("Domain: ").append(this.interval.toString()).append('\n');
		sb.append(prefix).append("Cumulative sub-parameter weights: ").append(Arrays.toString(this.cumSubParamWeights)).append("\n");
		sb.append(prefix).append("Tuning parameter 1:\n").append(this.t1.getPreInfo(prefix + '\t'));
		sb.append(prefix).append("Tuning parameter 2:\n").append(this.t2.getPreInfo(prefix + '\t'));
		if (this.stats != null) {
			sb.append(prefix).append("Statistics:\n").append(this.stats.getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("NORMAL-DISTRIBUTED PROPOSER\n");
		sb.append(prefix).append("Perturbed parameter: ").append(this.param.getName()).append('\n');
		sb.append(prefix).append("Tuning parameter 1:\n").append(this.t1.getPostInfo(prefix + '\t'));
		sb.append(prefix).append("Tuning parameter 2:\n").append(this.t2.getPostInfo(prefix + '\t'));
		if (this.stats != null) {
			sb.append(prefix).append("Statistics:\n").append(this.stats.getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public void setStatistics(ProposerStatistics stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		return "NormalProposer perturbing " + this.param.getName();
	}
	
}
