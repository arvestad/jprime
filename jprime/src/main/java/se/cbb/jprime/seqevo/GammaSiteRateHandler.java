package se.cbb.jprime.seqevo;

import java.util.Map;

import se.cbb.jprime.math.Gamma;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.ProperDependent;

/**
 * Handles sequence evolution rate variation across sites using a discretisation
 * approach on top of a gamma distribution.
 * <p/>
 * The gamma distribution is constrained to have mean 1.0 (meaning
 * it can be parameterised using only the shape parameter k=alpha),
 * and is represented by n discrete
 * site rate categories, where n is a user-defined number. E.g., for n=4,
 * each site rate category represents a quarter of the distribution's probability
 * mass, [0,0.25), [0.25,0.50), [0.50,0.75), [0.75,1.00], and the rate of a
 * category is the mean of the rates over its interval (not mean probability density, mind you).
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class GammaSiteRateHandler implements ProperDependent {

	/** Gamma distribution shape parameter. */
	private DoubleParameter k;
	
	/** Number of categories. */
	private int nCat;
	
	/** Rates. */
	private double[] rates;
	
	/** Cache. */
	private double[] cacheRates = null;
	
	/**
	 * Constructor.
	 * @param k the gamma distribution shape parameter.
	 * @param noOfCats the number of categories.
	 */
	public GammaSiteRateHandler(DoubleParameter k, int noOfCats) {
		if (noOfCats < 1) {
			throw new IllegalArgumentException("Invalid number of discrete gamma site rate categories: " + noOfCats + ".");
		}
		this.k = k;
		this.nCat = noOfCats;
		this.rates = new double[noOfCats];
		this.update();
	}
	
	/**
	 * Returns the number of discrete site rate categories.
	 * @return the number of categories.
	 */
	public int getNoOfCategories() {
		return this.nCat;
	}
	
	/**
	 * Returns the rate of a category, indexed from 0.
	 * @param cat the category.
	 * @return the rate.
	 */
	public double getRate(int cat) {
		return this.rates[cat];
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.k };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		if (changeInfos.get(this.k) != null) {
			this.cacheRates = new double[this.rates.length];
			System.arraycopy(this.rates, 0, this.cacheRates, 0, this.rates.length);
			this.update();
			changeInfos.put(this, new ChangeInfo(this, "Gamma site rate categories updated."));
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cacheRates = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.rates = this.cacheRates;
		this.cacheRates = null;
	}
	
	/**
	 * Updates the rates.
	 */
	private void update() {
		double k = this.k.getValue();
		double theta = 1.0 / k;  // Mean should always be 1.
		assert k > 0;
		assert theta > 0;
		this.rates = Gamma.getDiscreteGammaCategories(this.nCat, k, theta);
	}
}
