package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.cbb.jprime.math.ExponentialDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Implements the Aris-Brosou-Yang 2002 autocorrelated rate model.
 * Rates refer to edges, in which we have r_child ~ Exp(lambda), lambda=1/r_parent.
 * 
 * @author Joel Sjöstrand.
 */
public class ACArisBrosouYang02RateModel implements RateModel {

	/** Start rate. */
	private double startRate;
	
	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param startRate rate for first considered arc.
	 */
	public ACArisBrosouYang02RateModel(double startRate, PRNG prng) {
		this.startRate = startRate;
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(1);
		kv.put("start rate", "" + this.startRate);
		return kv;
	}

	@Override
	public String getModelName() {
		return "ArisBrosouYang02Rates";
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
			double lambda = 1.0 / rates.get(xp);
			ExponentialDistribution pd = new ExponentialDistribution(lambda);
			double r = pd.sampleValue(prng);
			//System.out.println(r);
			rates.set(x, r);
		}
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return false;
	}

}
