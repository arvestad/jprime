package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.math.NormalDistribution;
import se.cbb.jprime.math.RealInterval;

/**
 * Represents a normal proposal distribution, with the possibility of bounding the
 * domain to [A,B] and similarly. It perturbs real-valued state parameters; either singletons
 * or arrays. In the latter case, the user can control how many sub-parameters should be affected.
 * <p/>
 * For each perturbed sub-parameter, the proposal distribution has its mode at the current value v,
 * and a standard deviation proportional to v*t where t is a user-defined tuning parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class TruncatedNormalProposer implements Proposer {

	/** N(1,1) distribution used for making all computations. */
	private static final NormalDistribution N = new NormalDistribution(1.0, 1.0);
	
	/** Perturbed parameter. */
	private RealParameter param;
	
	/** Domain of parameter and proposal distribution. */
	private RealInterval interval;
	
	/**
	 * Tuning parameter. The standard deviation of the normal
	 * distribution (prior to truncation) is equal to v*t.
	 */
	private TuningParameter tuner;

	/** Weight. */
	private ProposerWeight weight;

	/** Statistics. */
	private ProposerStatistics stats;
	
	/**
	 * Constructor. Creates a proposal distribution bounded to a certain domain.
	 * @param param state parameter perturbed by this proposer.
	 * @param interval domain of state parameter and proposal distribution.
	 * @param tuner tuning parameter governing audacity of state changes.
	 * @param weight proposer weight.
	 * @param stats proposer statistics.
	 */
	public TruncatedNormalProposer(RealParameter param, RealInterval interval, TuningParameter tuner,
			ProposerWeight weight, ProposerStatistics stats) {
		if (tuner.getMinValue() < 0) {
			throw new IllegalArgumentException("Tuning parameter must not be negative.");
		}
		this.param = param;
		this.interval = interval;
		this.tuner = tuner;
		this.weight = weight;
		this.stats = stats;
	}
	
	/**
	 * Constructor. Creates an unbounded proposal distribution, i.e. defined over (-inf,+inf).
	 * @param param state parameter perturbed by this proposer.
	 * @param tuner tuning parameter governing audacity of state changes.
	 * @param weight proposer weight.
	 * @param stats proposer statistics.
	 */
	public TruncatedNormalProposer(RealParameter param, TuningParameter tuner,
			ProposerWeight weight, ProposerStatistics stats) {
		this(param, new RealInterval(), tuner, weight, stats);
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

	@Override
	public ProposerStatistics getStatistics() {
		return this.stats;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		ArrayList<TuningParameter> l = new ArrayList<TuningParameter>(1);
		l.add(this.tuner);
		return l;
	}

	@Override
	public Proposal propose() {
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

}
