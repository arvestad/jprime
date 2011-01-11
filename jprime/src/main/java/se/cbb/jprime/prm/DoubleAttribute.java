package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

import se.cbb.jprime.math.RealInterval;

/**
 * Defines an integer attribute, either bounded or unbounded.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleAttribute implements ContinuousAttribute {
	
	/** PRM class. */
	private PRMClass prmClass;
	
	/** Attribute name. */
	private String name;
	
	/** Entities. */
	private ArrayList<Double> entities;
	
	/** Dependency constraints */
	private DependencyConstraints dependencyConstraints;
	
	/** Interval defining valid range. */
	RealInterval interval;
	
	/**
	 * Constructor for bounded or unbounded double range.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param initialCapacity initial capacity for attribute entities.
	 * @param dependencyConstraints dependency structure constraints.
	 * @param interval the valid range of values.
	 */
	public DoubleAttribute(PRMClass prmClass, String name, int initialCapacity,
			DependencyConstraints dependencyConstraints, RealInterval interval) {
		this.prmClass = prmClass;
		this.name = name;
		this.entities = new ArrayList<Double>(initialCapacity);
		this.dependencyConstraints = dependencyConstraints;
		this.interval = interval;
		this.prmClass.addProbAttribute(this);
	}
	
	@Override
	public String getName() {
		return name;
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
		this.entities.set(idx, (Double) entity);
	}

	@Override
	public void addEntityAsObject(Object entity) {
		this.entities.add((Double) entity);
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
		this.entities.set(idx, new Double(value));
	}
	
	/**
	 * Adds an attribute value.
	 * @param value the value.
	 */
	public void addEntity(double value) {
		this.entities.add(new Double(value));
	}
}
