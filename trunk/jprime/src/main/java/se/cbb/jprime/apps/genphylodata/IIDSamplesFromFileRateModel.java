package se.cbb.jprime.apps.genphylodata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * IID rate model uniformly drawn from samples of file.
 * 
 * @author Joel Sjöstrand.
 */
public class IIDSamplesFromFileRateModel implements RateModel {

	/** File. */
	private File file;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param file file.
	 * @param prng PRNG.
	 */
	public IIDSamplesFromFileRateModel(File f, PRNG prng) {
		this.file = f;
		this.prng = prng;
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		try {
			kv.put("filename", ""+this.file.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDSamplesFromFileRates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		DoubleMap rates = new DoubleMap("Rates", t.getNoOfVertices());
		// Read file.
		ArrayList<Double> samples = new ArrayList<Double>(8192);
		try {
			Scanner sc = new Scanner(this.file);
			while (sc.hasNextLine()) {
				samples.add(Double.parseDouble(sc.nextLine()));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int x = 0; x < t.getNoOfVertices(); ++x) {
			int s = prng.nextInt(samples.size());
			rates.set(x, samples.get(s));
		}
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return false;
	}

}
