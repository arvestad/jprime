package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.cbb.jprime.math.NormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.RealInterval;
import se.cbb.jprime.math.RealInterval.Type;
import se.cbb.jprime.math.UniformDistribution;

/**
 * TODO: Write description of a uniform proposal distribution.
 * Represents a normal proposal distribution. That is, given a current state parameter value m,
 * it draws a new value Y ~ N(m,v). It is also possible to limit the domain to [A,B], (A,inf) and
 * so forth, thus creating a truncated normal distribution.
 * <p/>
 * The proposer can perturb both singleton parameters and arrays. In the latter case, sub-parameters are treated
 * individually, and the user can control how many of them should be affected.
 * By default, this is set to one sub-parameter only, but it may be changed by invoking
 * <code>setSubParameterWeights(...)</code>.
 * <p/>
 * The proposer relies on a user-defined tuning parameter, which governs the proposal distribution's
 * coefficient of variation (CV). For m = 0, a small epsilon proposal variance
 * is used instead.
 * For bounded domains, v is chosen as if not truncated and sampling is repeated until within the domain. 
 * 
 * @author Joel Sjöstrand.
 * @author Owais Mahmudi
 */
public class UniformProposer implements Proposer {
	
	/** Perturbed parameter. */
	private RealParameter param;
	
	/** Domain of parameter and proposal distribution. */
	private RealInterval interval;
	
	/** Pseudo-random number generator. */
	private PRNG prng;
	
	/** Tuning parameter. Governs proposal distribution's coefficient of variation. */
	private TuningParameter proposalCV;
	
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
	 * @param proposalCV tuning parameter governing proposal distribution's CV.
	 * @param prng pseudo-random number generator.
	 */
	public UniformProposer(RealParameter param, RealInterval interval, TuningParameter proposalCV, PRNG prng) {
		if (proposalCV.getMinValue() <= 0) {
			throw new IllegalArgumentException("Illegal tuning parameter for normal proposer for parameter " + param.getName() + ". Value must be in (0,inf).");
		}
		RealInterval.Type t = interval.getType();
		if (t == Type.DEGENERATE || t == Type.EMPTY) {
			throw new IllegalArgumentException("Invalid interval for normal proposer.");
		}
		this.param = param;
		this.interval = interval;
		this.proposalCV = proposalCV;
		this.noPerturbed = 0;
		this.prng = prng;
		this.cumSubParamWeights = new double[] { 1.0 };
		this.isEnabled = true;
	}
	
	/**
	 * Constructor. Creates an unbounded proposal distribution, i.e. defined over (-inf,+inf).
	 * @param param state parameter perturbed by this proposer.
	 * @param proposalCV tuning parameter governing proposal distribution's CV.
	 * @param prng random number generator.
	 */
	public UniformProposer(RealParameter param, TuningParameter proposalCV, PRNG prng) {
		this(param, new RealInterval(), proposalCV, prng);
	}
	
	@Override
	//public Set<StateParameter> getParameters() {
	public ArrayList<StateParameter> getParameters() {
//		HashSet<StateParameter> ps = new HashSet<StateParameter>(1);
//		ps.add(this.param);
//		return ps;
		ArrayList<StateParameter> ps = new ArrayList<StateParameter>(1);
		ps.add(this.param);
		return ps;
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
		l.add(this.proposalCV);
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
		
		// Perturb all chosen sub-parameters.
		LogDouble forward = new LogDouble(1.0);
		LogDouble backward = new LogDouble(1.0);
		for (int i = 0; i < indices.length; ++i) {
			
			// Compute variance for current proposal distribution.
//			double xOld = this.param.getValue(indices[i]);
//			double stdev = Math.max(Math.abs(xOld * this.proposalCV.getValue()), 1e-16);
			
			// Sample a new value.
			boolean open = true;
			UniformDistribution pd = new UniformDistribution(0.0, 1.0, open, open);
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
			
//			// Obtain "forward" density.
//			double a = this.interval.getLowerBound();
//			double b = this.interval.getUpperBound();
//			double nonTails = 1.0;
//			if (!Double.isInfinite(a)) {
//				nonTails -= pd.getCDF(a);
//			}
//			if (!Double.isInfinite(b)) {
//				nonTails -= (1.0 - pd.getCDF(b));
//			}
//			forward.mult(new LogDouble(Math.max(pd.getPDF(x) / nonTails, 0.0)));
//			
//			// Obtain "backward" density.
//			stdev = Math.max(Math.abs(x * this.proposalCV.getValue()), 1e-16);
//			pd.setMean(x);
//			pd.setStandardDeviation(stdev);
//			nonTails = 1.0;
//			if (!Double.isInfinite(a)) {
//				nonTails -= pd.getCDF(a);
//			}
//			if (!Double.isInfinite(b)) {
//				nonTails -= (1.0 - pd.getCDF(b));
//			}
//			backward.mult(new LogDouble(Math.max(pd.getPDF(xOld) / nonTails, 0.0)));
			forward = new LogDouble(1.0);
			backward = new LogDouble(1.0);
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
		if (this.param.getNoOfSubParameters() > 1) {
			sb.append(prefix).append("Cumulative sub-parameter weights: ").append(Arrays.toString(this.cumSubParamWeights)).append("\n");
		}
		sb.append(prefix).append("Tuning parameter governing proposal CV:\n").append(this.proposalCV.getPreInfo(prefix + '\t'));
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
		sb.append(prefix).append("Tuning parameter governing proposal CV:\n").append(this.proposalCV.getPostInfo(prefix + '\t'));
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
