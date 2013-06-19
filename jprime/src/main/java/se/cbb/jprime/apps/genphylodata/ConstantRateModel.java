package se.cbb.jprime.apps.genphylodata;

import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Constant rate model, i.e., a strict molecular clock.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantRateModel implements RateModel {

	/** Rate. */
	private double rate;
	
	/**
	 * Constructor.
	 * @param lambda rate.
	 * @param prng PRNG.
	 */
	public ConstantRateModel(double rate) {
		this.rate = rate;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(1);
		kv.put("rate", ""+this.rate);
		return kv;
	}

	@Override
	public String getModelName() {
		return "ConstantRates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		int n = t.getNoOfVertices();
		DoubleMap rates = new DoubleMap("Rates", n, this.rate);
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return false;
	}

}
