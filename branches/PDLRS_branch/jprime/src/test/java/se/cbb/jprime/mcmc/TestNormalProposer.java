package se.cbb.jprime.mcmc;

import java.math.BigInteger;
import java.util.HashMap;

import org.junit.*;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.math.RealInterval;

import static org.junit.Assert.*;

public class TestNormalProposer {

	@Test
	public void testSingleUnboundedParameter() {
		PRNG prng = new PRNG(new BigInteger("49193538192926113129390326306797599946"));
		DoubleParameter p = new DoubleParameter("Param", 2.34);
		double t1 = 0.6;
		double t2 = 0.5;
		double tcv = 0.8895613;
		NormalProposer prop = new NormalProposer(p, new ConstantTuningParameter(tcv), prng);
		int within = 0;
		int n = 1000;
		for (int i = 0; i < n; ++i) {
			double oldVal = p.getValue();
			HashMap<Dependent, ChangeInfo> map = new HashMap<Dependent, ChangeInfo>(1);
			@SuppressWarnings("unused")
			MetropolisHastingsProposal psal = (MetropolisHastingsProposal) prop.cacheAndPerturb(map);
			prop.clearCache();
			double newVal = p.getValue();
			double a = Math.min((1-t1)*oldVal, (1+t1)*oldVal);
			double b = Math.max((1-t1)*oldVal, (1+t1)*oldVal);
			if (newVal >= a && newVal < b) {
				++within;
			}
			//System.out.println(newVal + ", forward: " + psal.getForwardDensity().getValue() + ", backward: " + psal.getBackwardDensity().getValue());
			if (newVal < 1e-20) {
				p.setValue(2.34);
			}
		}
		double ratio = within / ((double) n);
		//System.out.println("" + within + " / " + n + " = " + ratio);
		assertEquals(t2, ratio, 0.05);
	}
	
	
	@Test
	public void testSingleBoundedParameter() {
		PRNG prng = new PRNG(new BigInteger("49193538192926113129390326306797599946"));
		DoubleParameter p = new DoubleParameter("Param", 2.34);
		RealInterval iv = new RealInterval(0, 10, true, true);
		double t1 = 0.3;
		double t2 = 0.5;
		double tcv = 0.4447807;
		NormalProposer prop = new NormalProposer(p, iv, new ConstantTuningParameter(tcv), prng);
		int within = 0;
		int n = 500;
		for (int i = 0; i < n; ++i) {
			double oldVal = p.getValue();
			HashMap<Dependent, ChangeInfo> map = new HashMap<Dependent, ChangeInfo>(1);
			@SuppressWarnings("unused")
			MetropolisHastingsProposal psal = (MetropolisHastingsProposal) prop.cacheAndPerturb(map);
			prop.clearCache();
			double newVal = p.getValue();
			double a = Math.min((1-t1)*oldVal, (1+t1)*oldVal);
			double b = Math.max((1-t1)*oldVal, (1+t1)*oldVal);
			if (newVal >= a && newVal < b) {
				++within;
			}
			//System.out.println(newVal + ", forward: " + psal.getForwardDensity().getValue() + ", backward: " + psal.getBackwardDensity().getValue());
			if (newVal < 1e-20) {
				p.setValue(2.34);
			}
		}
		double ratio = within / ((double) n);
		//System.out.println("" + within + " / " + n + " = " + ratio);
		assertEquals(t2, ratio, 0.05);
	}
	
}
