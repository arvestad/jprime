package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.cbb.jprime.math.LogNormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Implements the Rannala-Yang 2007 autocorrelated rate model. This model is similar to
 * Kishino-Thorne's, but with some small adjustments.
 * The rate of an edge is approximated by the rate of its midpoint.
 * 
 * @author Joel Sjöstrand.
 */
public class ACRannalaYang07RateModel implements RateModel {

	/** Start rate. */
	private double startRate;
	
	/** Autocorrelation factor. */
	private double sigma2;
	
	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param startRate rate for first considered arc.
	 * @param sigma2 autocorrelation factor.
	 */
	public ACRannalaYang07RateModel(double startRate, double sigma2, PRNG prng) {
		this.startRate = startRate;
		this.sigma2 = sigma2;
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("start rate", "" + this.startRate);
		kv.put("sigma2", "" + this.sigma2);
		return kv;
	}

	@Override
	public String getModelName() {
		return "RannalaYang07Rates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		int n = t.getNoOfVertices();
		DoubleMap vrates = new DoubleMap("VertexRates", n);    // Vertex rates.
		DoubleMap mprates = new DoubleMap("Rates", n);         // Midpoint (and thus edge) rates.
		List<Integer> vx = t.getTopologicalOrdering();
		
		int root = vx.get(0);
		double dt = origLengths.get(root);
		LogNormalDistribution pd;
		if (dt == 0.0) {
			mprates.set(root, this.startRate);
			vrates.set(root, this.startRate);
		} else {
			pd = new LogNormalDistribution(Math.log(this.startRate) - dt * sigma2 / 2, dt * sigma2);
			double r = pd.sampleValue(this.prng);
			mprates.set(root, r);
			pd = new LogNormalDistribution(Math.log(r) - dt * sigma2 / 2, dt * sigma2);
			r = pd.sampleValue(this.prng);
			vrates.set(root, r);
		}
		for (int i = 1; i < n; ++i) {
			int x = vx.get(i);
			int xp = t.getParent(x);
			dt = origLengths.get(x) / 2;
			pd = new LogNormalDistribution(Math.log(vrates.get(xp)) - dt * sigma2 / 2, dt * sigma2);
			double r = pd.sampleValue(prng);
			mprates.set(x, r);
			pd = new LogNormalDistribution(Math.log(r) - dt * sigma2 / 2, dt * sigma2);
			r = pd.sampleValue(this.prng);
			vrates.set(x, r);
		}
		return mprates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return true;
	}

}
