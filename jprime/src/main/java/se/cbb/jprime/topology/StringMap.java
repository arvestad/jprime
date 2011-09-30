package se.cbb.jprime.topology;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.io.SampleStringArray;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds a string for each vertex of a graph. No generics for the sake of speed.
 * 
 * @author Joel Sjöstrand.
 */
public class StringMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected String[] values;
	
	/** The child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	protected String[] cache = null;

	/** Details the current change. Set by a Proposer. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor. Initialises all map values to null.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public StringMap(String name, int size) {
		this.name = name;
		this.values = new String[size];
		this.dependents = new TreeSet<Dependent>();
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public StringMap(String name, int size, String defaultVal) {
		this.name = name;
		this.values = new String[size];
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
	public StringMap(String name, String[] vals) {
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
		return this.values[x];
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = value.toString();
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public String get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, String val) {
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
		this.cache = new String[this.values.length];
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
	public Class<?> getSampleType() {
		return SampleStringArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		return SampleStringArray.toString(this.values);
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
	public int getSize() {
		return this.values.length;
	}
}
