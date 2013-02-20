package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.cbb.jprime.math.NormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Implements the Lepage-Bryant-Phillipe-Lartillot 2007 autocorrelated CIR rate model.
 * Rates evolve across the branches according to a CIR process. We numerically integrate
 * total branch rates by simulating a discretised process over each branch.
 * 
 * @author Joel Sjöstrand.
 */
public class ACLepageBryantPhillipeLartillot07RateModel implements RateModel {

	/** No. of discretisation steps used on each branch. */
	public static final int NO_OF_STEPS = 200;
	
	/** Start rate. */
	private double startRate;
	
	/** Mean. */
	private double mu;
	
	/** Speed of adjustment. */
	private double theta;
	
	/** Volatility */
	private double sigma;
	
	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param startRate start rate.
	 * @param mu mean value.
	 * @param theta speed of adjustment.
	 * @param sigma volatility.
	 * @param prng PRNG.
	 */
	public ACLepageBryantPhillipeLartillot07RateModel(double startRate, double mu, double theta, double sigma, PRNG prng) {
		if (2 * theta * mu < sigma * sigma) {
			throw new IllegalArgumentException("Invalid parameters for CIR process: 0 rate may be achieved.");
		}
		this.startRate = startRate;
		this.mu = mu;
		this.theta = theta;
		this.sigma = sigma;
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("start rate", "" + this.startRate);
		kv.put("mu", "" + this.mu);
		kv.put("theta", "" + this.theta);
		kv.put("sigma", "" + this.sigma);
		return kv;
	}

	@Override
	public String getModelName() {
		return "LepageBryantPhillipeLartillot07Rates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		int sz = t.getNoOfVertices();
		DoubleMap rates = new DoubleMap("Rates", sz);        // Overall rate across branches.
		DoubleMap vrates = new DoubleMap("VertexRates", sz); // Instantaneous rate at vertices.
		List<Integer> vx = t.getTopologicalOrdering();
		
		int x = vx.get(0);           // Lower vertex.
		double ir = this.startRate;  // Instantaneous rate.
		double r = 0.0;              // Rate of branch.
		double dt = origLengths.get(x) / NO_OF_STEPS;  // dt
		NormalDistribution n;        // N(0,dt).
		
		// Stem edge.
		if (dt == 0.0) {
			vrates.set(x, this.startRate);
			rates.set(x, this.startRate);
		} else {
			n = new NormalDistribution(0, dt);
			for (int i = 0; i < NO_OF_STEPS; ++i) {
				ir = ir + this.theta * (this.mu - ir) * dt + this.sigma * Math.sqrt(ir) * n.sampleValue(this.prng);
				r += ir;
			}
			vrates.set(x, ir);
			r = r / NO_OF_STEPS;
			rates.set(x, r);
		}
		
		// Interior vertices.
		for (int j = 1; j < sz; ++j) {
			x = vx.get(j);
			int xp = t.getParent(x);
			ir = vrates.get(xp);
			r = 0.0;
			dt = origLengths.get(x) / NO_OF_STEPS;
			n = new NormalDistribution(0, dt);
			for (int i = 0; i < NO_OF_STEPS; ++i) {
				ir = ir + this.theta * (this.mu - ir) * dt + this.sigma * Math.sqrt(ir) * n.sampleValue(this.prng);
				r += ir;
			}
			vrates.set(x, ir);
			r = r / NO_OF_STEPS;
			rates.set(x, r);
		}
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return false;
	}

}
