package se.cbb.jprime.apps.genphylodata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RootedTree;
import se.cbb.jprime.topology.TimesMap;

/**
 * Implements the host-specific rates of Rasmussen-Kellis 2011.
 * Every arc in the host tree is associated with a gamma distribution.
 * The rate of a guest tree arc is derived from the distributions of the
 * host tree arcs it passes over. Also, a simple scale factor is applied
 * subsequent to the probabilistic relaxation.
 * <p>
 * IMPORTANT NOTE: Currently, this class assumes there are no transfers!
 * 
 * @author Joel Sjöstrand.
 */
public class IIDRasmussenKellis11RateModel implements RateModel {

	/** Host tree. */
	private RBTree hostTree;

	/** Guest tree. */
	private RBTree guestTree;
	
	/** "Gene family"-specific scale factor. */
	private double scaleFact;
	
	/** Sigma map. */
	private MPRMap sigma;
	
	/** Guest tree times. */
	private TimesMap guestTimes;
	
	/** Host tree times. */
	private TimesMap hostTimes;
	
	/** Gamma distribution parameters for each host tree arc. */
	private DoubleArrayMap hostRateParams;

	/** PRNG. */
	private PRNG prng;
	
	/**
	 * Constructor.
	 * @param hostTree host tree.
	 * @param guestTree guest tree.
	 * @param gs sigma map.
	 * @param scaleFact scale factor.
	 * @param prng PRNG.
	 */
	public IIDRasmussenKellis11RateModel(PrIMENewickTree hostTree, PrIMENewickTree guestTree, GuestHostMap gs, double scaleFact, PRNG prng) {
		try {
			this.hostTree = new RBTree(hostTree, "HostTree");
			this.guestTree = new RBTree(guestTree, "GuestTree");
			this.sigma = new MPRMap(gs, this.guestTree, guestTree.getVertexNamesMap(false, "GuestTreeNames"),
					this.hostTree, hostTree.getVertexNamesMap(false, "HostTreeNames"));
			this.guestTimes = guestTree.getTimesMap("GuestTimes");
			this.hostTimes = hostTree.getTimesMap("HostTimes");
			this.hostRateParams = hostTree.getVertexParamsMap("Params");
			this.scaleFact = scaleFact;
			int hroot = this.hostTree.getRoot();
			if (Double.isNaN(hostTimes.getArcTime(hroot))) {
				hostTimes.getArcTimes()[hroot] = 0.0;
			}
			if (hostRateParams.get(hroot) == null) {
				hostRateParams.set(hroot, new double[] {1000, 0.001});
			}
			if (Double.isNaN(guestTimes.getArcTime(this.guestTree.getRoot()))) {
				guestTimes.getArcTimes()[this.guestTree.getRoot()] = 0.0;
			}
			this.prng = prng;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid input to rate model: ", e);
		}
		int x = this.guestTree.getRoot();
		int y = this.hostTree.getRoot();
		if (Math.abs(guestTimes.getVertexTime(x) + guestTimes.getArcTime(x) - hostTimes.getVertexTime(y) - hostTimes.getArcTime(y)) > 1e-4) {
			throw new IllegalArgumentException("Invalid guest and host trees: Time scales do not agree.");
		}
	}
	
	@Override
	public Map<String, String> getModelParameters() {
		HashMap<String, String> kv =  new HashMap<String, String>(2);
		return kv;
	}

	@Override
	public String getModelName() {
		return "IIDRasmussenKellis11Rates";
	}

	@Override
	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
		DoubleMap rates = new DoubleMap("Rates", t.getNoOfVertices());
		ArrayList<Double> ws = new ArrayList<Double>(8);
		ArrayList<Double> rs = new ArrayList<Double>(8);
		GammaDistribution pd;
		for (int x = 0; x < t.getNoOfVertices(); ++x) {
			// Find the upper and lower host branches of the guest branch.
			int lo = findEnclosingArc(x);
			int up = (this.guestTree.isRoot(x) ? this.hostTree.getRoot() : findEnclosingArc(this.guestTree.getParent(x)));
			// Get rates over all spanned segments, and weights.
			ws.clear();
			rs.clear();
			double l = guestTimes.getArcTime(x);
			int h = lo;
			while (true) {
				pd = new GammaDistribution(this.hostRateParams.get(h, 0), this.hostRateParams.get(h, 1));
				ws.add(hostTimes.getArcTime(h) / l);
				rs.add(pd.sampleValue(prng));
				if (h == up) { break; }
				h = hostTree.getParent(h);
			}
			// Correct endpoint weights.
			if (ws.size() == 1) {
				ws.set(0, 1.0);
			} else {
				double dt = guestTimes.getVertexTime(x) - hostTimes.getVertexTime(lo);
				ws.set(0, Math.max(0.0, ws.get(0) - dt / l));
				if (!guestTree.isRoot(x)) {
					dt = guestTimes.getVertexTime(guestTree.getParent(x)) - hostTimes.getVertexTime(up);
					ws.set(ws.size()-1, Math.max(0.0, dt / l));
				}
			}
			
			// Compute overall rate.
			double r = 0.0;
			for (int i = 0; i < ws.size(); ++i) {
				r += ws.get(i) * rs.get(i);
			}
			r *= this.scaleFact;   // Scale factor.
			rates.set(x, r);
		}
		return rates;
	}

	@Override
	public boolean lengthsMustBeUltrametric() {
		return true;
	}
	
	/**
	 * Finds the host arc enclosing a guest vertex.
	 * @param x the guest vertex.
	 * @return the host arc.
	 */
	private int findEnclosingArc(int x) {
		int lo = this.sigma.getSigma(x);
		double xt = this.guestTimes.getVertexTime(x);
		while (true) {
			if (lo == this.hostTree.getRoot()) {
				return lo;
			}
			int lop = this.hostTree.getParent(lo);
			double lopt = this.hostTimes.getVertexTime(lop);
			if (lopt >= xt) {
				return lo;
			}
			lo = lop;
		}
	}

}
