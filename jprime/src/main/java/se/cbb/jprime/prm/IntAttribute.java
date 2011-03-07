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
	
	/** Full name kept for quick access. */
	private final String fullName;
	
	/** True if hidden or unknown. */
	private boolean isLatent;
	
	/** Interval defining valid range. */
	private IntegerInterval interval;
	
	/** Entities. */
	private final ArrayList<Integer> entities;
	
	/** If latent, the probability distribution of each entity, otherwise null. */
	private ArrayList<double[]> entityProbDists;
	
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
		this.entityProbDists = (isLatent ? new ArrayList<double[]>(initialCapacity) : null);
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
		this.setEntity(idx, (Integer) entity); 
	}

	@Override
	public void addEntityAsObject(Object entity) {
		this.addEntity((Integer) entity);
	}
	
	/**
	 * Returns an attribute value.
	 * @param idx the index.
	 * @return the value.
	 */
	public int getEntity(int idx) {
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
		if (this.isLatent) {
			double[] pd = new double[this.interval.getSize()];
			int idx = value - this.interval.getLowerBound();
			pd[idx] = 1.0;
			this.entityProbDists.add(pd);
		}
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

	@Override
	public int getEntityAsNormalisedInt(int idx) {
		return (this.entities.get(idx).intValue() - this.interval.getLowerBound());
	}

	@Override
	public int getIntervalSize() {
		return this.interval.getSize();
	}

	@Override
	public void setEntityAsNormalisedInt(int idx, int value) {
		value += this.interval.getLowerBound();
		this.setEntity(idx, value);
	}

	@Override
	public void addEntityAsNormalisedInt(int value) {
		value += this.interval.getLowerBound();
		this.addEntity(value);
	}

	@Override
	public double[] getEntityProbDistribution(int idx) {
		return this.entityProbDists.get(idx);
	}

	@Override
	public void setEntityProbDistribution(int idx, double[] probDist) {
		this.entityProbDists.set(idx, probDist);
	}
}
