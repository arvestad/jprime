package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.UniformDistribution;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * IID uniform rate model.
 * 
 * @author Joel Sjöstrand.
 */
public class IIDUniformRateModel implements RateModel {

	/** Probability distribution. */
	private UniformDistribution pd;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param a lower boundary.
	 * @param b upper boundary.
	 * @param prng PRNG.
	 */
	public IIDUniformRateModel(double a, double b, PRNG prng) {
		pd = new UniformDistribution(a, b, false, false);
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("a", ""+this.pd.getDomainInterval().getLowerBound());
		kv.put("b", ""+this.pd.getDomainInterval().getUpperBound());
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDUniformRates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		int n = t.getNoOfVertices();
		DoubleMap rates = new DoubleMap("Rates", n);
		for (int x = 0; x < n; ++x) {
			rates.set(x, pd.sampleValue(this.prng));
		}
		return rates;
	}

}
