package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

import se.cbb.jprime.math.RealInterval;

/**
 * Defines a probabilistic double PRM attribute, either bounded or unbounded.
 * The attribute automatically adds itself to its PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleAttribute implements ContinuousAttribute {
	
	/** PRM class. */
	private final PRMClass prmClass;
	
	/** Attribute name. */
	private final String name;
	
	/** Interval defining valid range. */
	private final RealInterval interval;
	
	/** Entities. */
	private final ArrayList<Double> entities;
	
	/** Full name kept for quick access. */
	private final String fullName;
	
	/** Dependency constraints. */
	private DependencyConstraints dependencyConstraints;
	
	/**
	 * Constructor for bounded or unbounded double range.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param initialCapacity initial capacity for attribute entities.
	 * @param dependencyConstraints dependency structure constraints.
	 * @param interval the valid range of values.
	 */
	public DoubleAttribute(String name, PRMClass prmClass, int initialCapacity,
			DependencyConstraints dependencyConstraints, RealInterval interval) {
		this.name = name;
		this.prmClass = prmClass;
		this.interval = interval;
		this.entities = new ArrayList<Double>(initialCapacity);
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
		return ProbabilisticAttribute.DataType.CONTINUOUS;
	}

	@Override
	public DependencyConstraints getDependencyConstraints() {
		return this.dependencyConstraints;
	}

	@Override
	public RealInterval getInterval() {
		return this.interval;
	}

	@Override
	public Object getRandomEntityAsObject(Random random) {
		return new Double(this.interval.getRandom(random));
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
		Double d = (Double) entity;
		if (!this.interval.isWithin(d.doubleValue())) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.set(idx, d);
	}

	@Override
	public void addEntityAsObject(Object entity) {
		Double d = (Double) entity;
		if (!this.interval.isWithin(d.doubleValue())) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.add(d);
	}
	
	/**
	 * Returns an attribute value.
	 * @param idx the index.
	 * @return the value.
	 */
	public double getEntity(int idx) {
		return this.entities.get(idx).doubleValue();
	}
	
	/**
	 * Sets an attribute value.
	 * @param idx the index.
	 * @param value the value.
	 */
	public void setEntity(int idx, double value) {
		if (!this.interval.isWithin(value)) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.set(idx, new Double(value));
	}
	
	/**
	 * Adds an attribute value.
	 * @param value the value.
	 */
	public void addEntity(double value) {
		if (!this.interval.isWithin(value)) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.add(new Double(value));
	}
	
	@Override
	public int getNoOfEntities() {
		return this.entities.size();
	}
}
