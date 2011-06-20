package se.cbb.jprime.topology;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.io.SampleType;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds a boolean for each vertex of a graph.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected boolean[] values;
	
	/** The child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	protected boolean[] cache = null;

	/** Details the current change. Set by a Proposer. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public BooleanMap(String name, int size, boolean defaultVal) {
		this.name = name;
		this.values = new boolean[size];
		for (int i = 0; i < this.values.length; ++i) {
			values[i] = defaultVal;
		}
		this.dependents = new TreeSet<Dependent>();
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public BooleanMap(String name, boolean[] vals) {
		this.name = name;
		this.values = vals;
		this.dependents = new TreeSet<Dependent>();
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Object getAsObject(int x) {
		return new Boolean(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Boolean) value).booleanValue();
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public boolean get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, boolean val) {
		this.values[x] = val;
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
		this.cache = new boolean[this.values.length];
		System.arraycopy(this.values, 0, this.cache, 0, this.values.length);
	}

	@Override
	public void update(boolean willSample) {
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.values = this.cache;
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public int getNoOfSubParameters() {
		return this.values.length;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}
	
	@Override
	public void setChangeInfo(ChangeInfo info) {
		this.changeInfo = info;
	}

	@Override
	public SampleType getSampleType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSampleHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSampleValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
