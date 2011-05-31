package se.cbb.jprime.topology;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;

/**
 * Extends a guest-host map with functionality concerning the most parsimonious reconciliation
 * when embedding the guest tree in the host tree using duplication and loss events.
 * It is assumed that the guest-to-host leaf mapping does not change, while the
 * guest or host topologies may.
 * 
 * @author Joel Sjöstrand.
 */
public class MPRMap implements Dependent {

	/** Guest-to-host leaf map. */
	private GuestHostMap GSMap;
	
	/** Guest tree topology. */
	private RootedTreeParameter G;
	
	/** Host tree topology. */
	private RootedTreeParameter S;
	
	/** Child-vertex-to-host-vertex MPR map. */
	private int[] sigmaMap;
	
	/** Cache. */
	private int[] sigmaMapCache = null;
	
	/** Child dependents. */
	private TreeSet<Dependent> dependents;
	
	/** Change info. */
	private ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param GSMap guest-to-host leaf map.
	 * @param G guest tree topology.
	 * @param S host tree topology.
	 */
	public MPRMap(GuestHostMap GSMap, RootedTreeParameter G, RootedTreeParameter S) {
		this.GSMap = GSMap;
		this.G = G;
		this.S = S;
		this.sigmaMap = new int[G.getNoOfVertices()];
		this.dependents = new TreeSet<Dependent>();
		G.addChildDependent(this);
		S.addChildDependent(this);
	}
	
	@Override
	public boolean isDependentSink() {
		return this.dependents.isEmpty();
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
		this.sigmaMapCache = new int[this.sigmaMap.length];
		System.arraycopy(this.sigmaMap, 0, this.sigmaMapCache, 0, this.sigmaMap.length);
	}

	@Override
	public void update(boolean willSample) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clearCache(boolean willSample) {
		this.sigmaMapCache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.sigmaMap = this.sigmaMapCache;
		this.sigmaMapCache = null;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}

}
