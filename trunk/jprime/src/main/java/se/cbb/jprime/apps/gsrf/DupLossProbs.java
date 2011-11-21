package se.cbb.jprime.apps.gsrf;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.topology.RBTree;

/**
 * Point-wise duplication and loss probabilities for a
 * discretised host tree.
 * 
 * @author Joel Sjöstrand.
 */
public class DupLossProbs implements Dependent {

	/** Host tree. */
	protected RBTree s;
	
	/** Discretised times. */
	//protected 
	
	/** Duplication (birth) rate. */
	protected DoubleParameter lambda;
	
	/** Loss (death) rate. */
	protected DoubleParameter mu;
	
	public DupLossProbs(RBTree s, /** XXX sDiscTimes,*/ DoubleParameter lambda, DoubleParameter mu) {
		s.addChildDependent(this);
		//s.DiscTimes.addChildDependent(this);
		lambda.addChildDependent(this);
		mu.addChildDependent(this);
	}
	
	@Override
	public boolean isDependentSink() {
		return false;
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public Set<Dependent> getChildDependents() {
		return this.dependents;
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
	public ChangeInfo getChangeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
