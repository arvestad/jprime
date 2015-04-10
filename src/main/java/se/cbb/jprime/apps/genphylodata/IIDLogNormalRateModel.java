package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.math.LogNormalDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * IID log-normal rate model.
 * 
 * @author Joel Sjöstrand.
 */
public class IIDLogNormalRateModel implements RateModel {

	/** Probability distribution. */
	private LogNormalDistribution pd;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param mu mu.
	 * @param sigma2 sigma2.
	 * @param prng PRNG.
	 */
	public IIDLogNormalRateModel(double mu, double sigma2, PRNG prng) {
		pd = new LogNormalDistribution(mu, sigma2);
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("mu", ""+this.pd.getUnderlyingMean());
		kv.put("sigma2", ""+this.pd.getUnderlyingVariance());
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDLogNormalRates";
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

	@Override
	public boolean lengthsMustBeUltrametric() {
		return false;
	}

}
