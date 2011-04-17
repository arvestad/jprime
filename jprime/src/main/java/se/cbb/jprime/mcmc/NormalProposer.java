package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.math.NormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;

/**
 * Represents a normal proposal distribution. That is, given a current state parameter value m,
 * it draws a new value Y ~ N(m,v). It is also possible to limit the domain to [A,B], (A,inf) and
 * so forth.
 * <p/>
 * The proposer can perturb both singleton parameters and arrays. In the latter case, the user can control
 * how many sub-parameters should be affected.
 * By default, this is set to one sub-parameter only, but it may be changed by invoking
 * <code>setSubParameterWeights(...)</code>.
 * <p/>
 * The proposer relies on two user-defined tuning parameters, t1 and t2, which define the variance v of
 * the proposal distribution: v is chosen so that Pr[(1-t1)*m < Y < (1+t1)*m] = t2. For instance, with
 * t1 = 0.5 and t2 = 0.6, the proposed value Y will in 60% of the cases be at most 50% greater or smaller
 * than the previous value m. If truncated, v is chosen as if not truncated.
 * 
 * @author Joel Sjöstrand.
 */
public class NormalProposer implements Proposer {

	/** Distribution used for making all computations. */
	private final NormalDistribution N = new NormalDistribution(0.0, 1.0);
	
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

	/** Weight. */
	private ProposerWeight weight;

	/** Statistics. */
	private ProposerStatistics stats;
	
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
	 * @param weight proposer weight.
	 * @param stats proposer statistics.
	 * @param prng pseudo-random number generator.
	 */
	public NormalProposer(RealParameter param, RealInterval interval, TuningParameter t1,
			TuningParameter t2, ProposerWeight weight, ProposerStatistics stats, PRNG prng) {
		if (t1.getMinValue() <= 0) {
			throw new IllegalArgumentException("First tuning parameter for normal proposer must be > 0.");
		}
		if (t2.getMinValue() <= 0 || t2.getMaxValue() >= 1.0) {
			throw new IllegalArgumentException("Second tuning parameter for normal proposer must be in (0,1).");
		}
		this.param = param;
		this.interval = interval;
		this.t1 = t1;
		this.t2 = t2;
		this.weight = weight;
		this.stats = stats;
		this.prng = prng;
		this.cumSubParamWeights = new double[] { 1.0 };
		this.isEnabled = true;
	}
	
	/**
	 * Constructor. Creates an unbounded proposal distribution, i.e. defined over (-inf,+inf).
	 * @param param state parameter perturbed by this proposer.
	 * @param t1 tuning parameter governing factor from old state.
	 * @param t2 tuning parameter governing probability of staying within +-(1+t1)*old state.
	 * @param weight proposer weight.
	 * @param stats proposer statistics.
	 * @param prng random number generator.
	 */
	public NormalProposer(RealParameter param, TuningParameter t1, TuningParameter t2,
			ProposerWeight weight, ProposerStatistics stats, PRNG prng) {
		this(param, new RealInterval(), t1, t2, weight, stats, prng);
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

	@Override
	public ProposerWeight getProposerWeight() {
		return this.weight;
	}

	@Override
	public double getWeight() {
		return this.weight.getWeight();
	}

	/**
	 * For a parameter containing multiple sub-parameters, it is possible to allow for multiple
	 * sub-parameters to be changed each proposal. This achieved by specifying a set of weights
	 * for the probabilities, e.g. [0.5,0.3,0.1] for perturbing 1 sub-parameter 50% of the time,
	 * 2 30% of the time, and 3 10% of the time. The sub-parameters are chosen uniformly.
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
	public Proposal propose() {
		
		int k = this.getNoOfSubParameters();
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
				indices[i] = l.get(this.prng.nextInt(l.size()));				
			}
		}
		
		for (int idx : indices) {
			double oldMean = this.param.getValue(idx);
			
		}
		
		
		//double oldMean = 
//		Real
//		  StdMCMCModel::perturbTruncatedNormal(Real value, Real (*varFunc)(Real, Real, Real, Real),
//						       Real min, Real max, Probability& propRatio, Real hyper) const
//		  {
//		    static NormalDensity tnd(1.0, 1.0);
	//
//		    assert(value > min && value < max);
	//
//		    // Retrieve current variance by invoking function pointer.
//		    Real old = value;
//		    Real var = (*varFunc)(old, min, max, hyper);
	//
//		    // Use current distribution. Get area when truncated tails excluded.
//		    tnd.setParameters(old, var);
//		    Probability nonTails = tnd.cdf(max) - tnd.cdf(min);
	//
//		    unsigned cntr = 0;
//		    do
//		      {
//			value = tnd.sampleValue(R.genrand_real3());
//			++cntr;
	//
//			// Abort after 100 tries.
//			if (cntr > 100)
//			  {
//			    propRatio = 0.0;
//			    return old;
//			  }
//		      }
//		    while (value <= min || value >= max);
	//
//		    // Must compensate for removed tails to get true PDF.
//		    propRatio = 1.0 / (tnd(value) / nonTails);
	//
//		    // Get new distribution.
//		    var = (*varFunc)(value, min, max, hyper);
//		    tnd.setParameters(value, var);
//		    nonTails = tnd.cdf(max) - tnd.cdf(min);
	//
//		    // Must compensate for removed tails to get true PDF.
//		    propRatio *= (tnd(old) / nonTails);
	//
//		    return value;
//		  }
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnabled;
	}

	@Override
	public void setEnabled(boolean isActive) {
		this.isEnabled = isActive;
	}

}
