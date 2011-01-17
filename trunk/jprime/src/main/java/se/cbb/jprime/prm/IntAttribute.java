package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

import se.cbb.jprime.math.IntegerInterval;

/**
 * Defines a probabilistic integer PRM attribute, either bounded or unbounded.
 * The attribute automatically adds itself to its PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class IntAttribute implements DiscreteAttribute {
	
	/** PRM class. */
	private final PRMClass prmClass;
	
	/** Attribute name. */
	private final String name;
	
	/** True if hidden or unknown. */
	private final boolean isLatent;
	
	/** Interval defining valid range. */
	private IntegerInterval interval;
	
	/** Entities. */
	private final ArrayList<Integer> entities;
	
	/** Full name kept for quick access. */
	private final String fullName;
	
	/** Dependency constraints */
	private DependencyConstraints dependencyConstraints;
	
	/**
	 * Constructor for bounded or unbounded integer range.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param isLatent true if hidden or unknown.
	 * @param initialCapacity initial capacity for attribute entities.
	 * @param dependencyConstraints dependency structure constraints.
	 * @param interval the valid range of values.
	 */
	public IntAttribute(String name, PRMClass prmClass, boolean isLatent, int initialCapacity,
			DependencyConstraints dependencyConstraints, IntegerInterval interval) {
		this.name = name;
		this.prmClass = prmClass;
		this.isLatent = isLatent;
		this.interval = interval;
		this.entities = new ArrayList<Integer>(initialCapacity);
		this.fullName = this.prmClass.getName() + '.' + this.name;
		this.dependencyConstraints = dependencyConstraints;
		this.prmClass.addProbAttribute(this);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return this.fullName;
	}
	
	@Override
	public DataType getDataType() {
		return ProbAttribute.DataType.DISCRETE;
	}

	@Override
	public DependencyConstraints getDependencyConstraints() {
		return this.dependencyConstraints;
	}

	@Override
	public IntegerInterval getInterval() {
		return this.interval;
	}

	@Override
	public Object getRandomEntityAsObject(Random random) {
		return new Integer(this.interval.getRandom(random));
	}

	@Override
	public PRMClass getPRMClass() {
		return this.prmClass;
	}

	@Override
	public Object getEntityAsObject(int idx) {
		return entities.get(idx);
	}

	@Override
	public void setEntityAsObject(int idx, Object entity) {
		Integer i = (Integer) entity;
		if (!this.interval.isWithin(i.intValue())) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.set(idx, i);
	}

	@Override
	public void addEntityAsObject(Object entity) {
		Integer i = (Integer) entity;
		if (!this.interval.isWithin(i.intValue())) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.add(i);
	}
	
	/**
	 * Returns an attribute value.
	 * @param idx the index.
	 * @return the value.
	 */
	public int getEntity(int idx) {
		return this.entities.get(idx).intValue();
	}
	
	@Override
	public int getEntityAsInt(int idx) {
		return this.entities.get(idx).intValue();
	}
	
	/**
	 * Sets an attribute value.
	 * @param idx the index.
	 * @param value the value.
	 */
	public void setEntity(int idx, int value) {
		if (!this.interval.isWithin(value)) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.set(idx, new Integer(value));
	}
	
	/**
	 * Adds an attribute value.
	 * @param value the value.
	 */
	public void addEntity(int value) {
		if (!this.interval.isWithin(value)) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.add(new Integer(value));
	}
	
	@Override
	public int getNoOfEntities() {
		return this.entities.size();
	}

	@Override
	public boolean isLatent() {
		return this.isLatent;
	}

	@Override
	public Class<?> getComponentType() {
		return Integer.class;
	}

	@Override
	public int compareTo(ProbAttribute o) {
		return this.fullName.compareTo(o.getFullName());
	}
}
