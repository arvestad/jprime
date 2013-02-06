package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.math.ExponentialDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * IID exponential rate model.
 * 
 * @author Joel Sjöstrand.
 */
public class IIDExponentialRateModel implements RateModel {

	/** Probability distribution. */
	private ExponentialDistribution pd;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param lambda rate.
	 * @param prng PRNG.
	 */
	public IIDExponentialRateModel(double lambda, PRNG prng) {
		pd = new ExponentialDistribution(lambda);
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(1);
		kv.put("lambda", ""+this.pd.getRate());
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDExponentialRates";
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
