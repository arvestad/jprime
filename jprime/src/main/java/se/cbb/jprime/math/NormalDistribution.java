package se.cbb.jprime.math;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.PerturbationInfo;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Represents a 1-D normal distribution, "Gaussian", N(m,v).
 * <p/>
 * It is possible to let the distribution rely on two floating point state parameters and
 * act as a <code>Dependent</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class NormalDistribution implements Continuous1DPD, Dependent {
	
	/**
	 * When the distribution depends on state parameters, defines
	 * what they represent.
	 */
	public enum StateParameterSetup {
		/** Mean and variance. */                  MEAN_AND_VAR,
		/** Mean and standard deviation. */        MEAN_AND_STDEV,
		/** Mean and coefficient of variation. */  MEAN_AND_CV
	}
	
	/** First state parameter. Null if not used. */
	private StateParameter p1;
	
	/** Second state parameter. Null if not used. */
	private StateParameter p2;
	
	/** State parameter representation. Null if not used. */
	private StateParameterSetup setup;
	
	/** Child dependents. */
	private ArrayList<Dependent> dependents;
	
	/**
	 * Constructor.
	 * @param mean distribution mean.
	 * @param var distribution variance.
	 */
	public NormalDistribution(double mean, double var) {
		// TODO: Implement.
		this.p1 = null;
		this.p2 = null;
		this.setup = null;
		this.dependents = new ArrayList<Dependent>();
	}
	
	public NormalDistribution(StateParameter p1, StateParameter p2, StateParameterSetup setup) {
		this.p1 = p1;
		this.p2 = p2;
		this.setup = setup;
		// TODO: Implement.
	}
	
	@Override
	public String getName() {
		return "Normal distribution";
	}

	@Override
	public int getNoOfParameters() {
		return 2;
	}

	@Override
	public int getNoOfDimensions() {
		return 1;
	}

	@Override
	public double getPDF(double x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCDF(double x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getProbability(double a, double b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMean() throws MathException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMedian() throws MathException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStandardDeviation() throws MathException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getVariance() throws MathException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCV() throws MathException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval();
	}

	@Override
	public boolean isSource() {
		return (this.p1 == null);
	}

	@Override
	public boolean isSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public List<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public List<Dependent> getParentDependents() {
		if (p1 == null) {
			return null;
		}
		ArrayList<Dependent> l = new ArrayList<Dependent>(2);
		l.add(this.p1);
		l.add(this.p2);
		return l;
	}

	@Override
	public void cache(boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearCache(boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreCache(boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PerturbationInfo getPerturbationInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPerturbationInfo(PerturbationInfo info) {
		// TODO Auto-generated method stub
		
	}

}
