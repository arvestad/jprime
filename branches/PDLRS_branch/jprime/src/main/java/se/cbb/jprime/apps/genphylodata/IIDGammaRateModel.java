package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * IID gamma rate model.
 * 
 * @author Joel Sjöstrand.
 */
public class IIDGammaRateModel implements RateModel {

	/** Probability distribution. */
	private GammaDistribution pd;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param k k.
	 * @param theta theta.
	 * @param prng PRNG.
	 */
	public IIDGammaRateModel(double k, double theta, PRNG prng) {
		pd = new GammaDistribution(k, theta);
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		kv.put("k", ""+this.pd.getShape());
		kv.put("theta", ""+this.pd.getScale());
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDGammaRates";
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
