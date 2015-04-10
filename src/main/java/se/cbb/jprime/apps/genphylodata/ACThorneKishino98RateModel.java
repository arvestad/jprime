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
 * Implements the Thorne-Kishino 1998 autocorrelated rate model (refer also to the 2001 and 2002 papers).
 * The model has been adjusted so that the child rate rc has E[rc] = rp, for the parent rate rp.
 * In the original paper, there was a slight bias that gave a higher rate the closer to the leaves.
 * Rates refer to edges, in which we have rc ~ ln N(ln(rp)-v*deltat/2, v*deltat), where deltat is the
 * timespan between the midpoints of the parent and child edge.
 * 
 * @author Joel Sjöstrand.
 */
public class ACThorneKishino98RateModel implements RateModel {

	/** Start rate. */
	private double startRate;
	
	/** Autocorrelation factor. */
	private double v;
	
	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param startRate rate for first considered arc.
	 * @param v autocorrelation factor.
	 */
	public ACThorneKishino98RateModel(double startRate, double v, PRNG prng) {
		this.startRate = startRate;
		this.v = v;
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("start rate", "" + this.startRate);
		kv.put("v", "" + this.v);
		return kv;
	}

	@Override
	public String getModelName() {
		return "ThorneKishino98Rates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		int n = t.getNoOfVertices();
		DoubleMap rates = new DoubleMap("Rates", n);
		List<Integer> vx = t.getTopologicalOrdering();
		rates.set(vx.get(0), this.startRate);
		for (int i = 1; i < n; ++i) {
			int x = vx.get(i);
			int xp = t.getParent(x);
			double deltat = (origLengths.get(x) + origLengths.get(xp)) / 2;
			// Correction for v*deltat/2 is to ensure E[r(x)]=r(xp). This was not accounted for in '98 paper.
			double sigma2 = this.v * deltat;
			LogNormalDistribution pd = new LogNormalDistribution(Math.log(rates.get(xp)) - sigma2 / 2, sigma2);
			double r = pd.sampleValue(prng);
			rates.set(x, r);
		}
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return true;
	}

}
