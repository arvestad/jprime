package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

import se.cbb.jprime.math.IntegerInterval;

/**
 * Defines a probabilistic boolean PRM attribute.
 * Treated similarly to {0,1}-valued integer
 * attribute.  The attribute automatically adds itself
 * to its PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanAttribute implements DiscreteAttribute {

	/** Treated analogous to {0,1}-valued integer. */
	public static final IntegerInterval INTERVAL = new IntegerInterval(0, 1);
	
	/** PRM class. */
	private final PRMClass prmClass;
	
	/** Attribute name. */
	private final String name;
	
	/** Full name kept for quick access. */
	private final String fullName;
	
	/** True if hidden or unknown. */
	private boolean isLatent;
	
	/** Entities. */
	private ArrayList<Boolean> entities;
	
	/** If latent, the probability distribution of each entity, otherwise null. */
	private ArrayList<double[]> entityProbDists;
	
	/** Dependency constraints */
	private DependencyConstraints dependencyConstraints;
	
	/**
	 * Constructor.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param isLatent true if hidden or unknown.
	 * @param initialCapacity initial capacity for attribute entities.
	 * @param dependencyConstraints dependency structure constraints.
	 */
	public BooleanAttribute(String name, PRMClass prmClass, boolean isLatent, int initialCapacity,
			DependencyConstraints dependencyConstraints) {
		this.prmClass = prmClass;
		this.name = name;
		this.isLatent = isLatent;
		this.entities = new ArrayList<Boolean>(initialCapacity);
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
		return INTERVAL;
	}

	@Override
	public Object getRandomEntityAsObject(Random random) {
		return new Boolean(random.nextBoolean());
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
		this.setEntity(idx, (Boolean) entity);
	}

	@Override
	public void addEntityAsObject(Object entity) {
		this.addEntity((Boolean) entity);
	}

	/**
	 * Returns an attribute value.
	 * @param idx the index.
	 * @return the value.
	 */
	public boolean getEntity(int idx) {
		return this.entities.get(idx).booleanValue();
	}
	
	/**
	 * Sets an attribute value.
	 * @param idx the index.
	 * @param value the value.
	 */
	public void setEntity(int idx, boolean value) {
		this.entities.set(idx, new Boolean(value));
	}
	
	/**
	 * Adds an attribute value.
	 * @param value the value.
	 */
	public void addEntity(boolean value) {
		this.entities.add(new Boolean(value));
		if (this.isLatent) {
			double[] pd = new double[] {(value ? 0.0 : 1.0), (value ? 1.0 : 0.0)};
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
		return Boolean.class;
	}
	
	@Override
	public int compareTo(ProbAttribute o) {
		return this.fullName.compareTo(o.getFullName());
	}

	@Override
	public int getEntityAsNormalisedInt(int idx) {
		return (this.entities.get(idx).booleanValue() ? 1 : 0);
	}

	@Override
	public int getIntervalSize() {
		return 2;
	}

	@Override
	public void setEntityAsNormalisedInt(int idx, int value) {
		this.entities.set(idx, new Boolean(value == 0 ? false : true));
	}

	@Override
	public double[] getEntityProbDistribution(int idx) {
		return this.entityProbDists.get(idx);
	}

	@Override
	public void setEntityProbDistribution(int idx, double[] probDist) {
		this.entityProbDists.set(idx, probDist);
	}

	@Override
	public void addEntityAsNormalisedInt(int value) {
		this.addEntity(value == 0 ? false : true);
	}
}
