package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

/**
 * Defines a probabilistic boolean PRM attribute.
 * Treated similarly to {0,1}-valued integer
 * attribute.  The attribute automatically adds itself
 * to its PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanAttribute implements DiscreteAttribute {
	
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
	
	/** Makes soft completion comply with hard assignment when adding values. Mostly for debugging. */
	private boolean sharpSoftCompletion = false;
	
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
	public int getNoOfValues() {
		return 2;
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
	 * Sets an attribute value. If latent, does not affect the
	 * corresponding soft assignment.
	 * @param idx the index.
	 * @param value the value.
	 */
	public void setEntity(int idx, boolean value) {
		this.entities.set(idx, new Boolean(value));
	}
	
	/**
	 * Adds an attribute value. If latent, also adds a
	 * corresponding soft completion.
	 * @param value the value.
	 */
	public void addEntity(boolean value) {
		this.entities.add(new Boolean(value));
		if (this.isLatent) {
			this.entityProbDists.add(new double[2]);
			this.createSoftCompletion(this.entities.size() - 1);
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
	public int getEntityAsInt(int idx) {
		return (this.entities.get(idx).booleanValue() ? 1 : 0);
	}

	@Override
	public void setEntityAsInt(int idx, int value) {
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
	public void addEntityAsInt(int value) {
		this.addEntity(value == 0 ? false : true);
	}
	
	@Override
	public void clearEntityProbDistribution(int idx) {
		double pd[] = this.entityProbDists.get(idx);
		pd[0] = Double.NaN;
		pd[1] = Double.NaN;
	}

	@Override
	public void normaliseEntityProbDistribution(int idx) {
		double pd[] = this.entityProbDists.get(idx);
		if (Double.isNaN(pd[0])) { pd[0] = 0.0; }
		if (Double.isNaN(pd[1])) { pd[1] = 0.0; }
		double sum = pd[0] + pd[1];
		if (sum == 0.0) {
			pd[0] = 0.5;
			pd[1] = 0.5;
		} else {
			pd[0] /= sum;
			pd[1] /= sum;
		}
	}
	
	@Override
	public String toString() {
		return this.getFullName();
	}
	
	@Override
	public void useSharpSoftCompletion() {
		this.sharpSoftCompletion = true;
	}
	
	@Override
	public int getMostProbEntityAsInt(int idx) {
		double[] pd = this.entityProbDists.get(idx);
		return (pd[1] > pd[0] ? 1 : 0);
	}

	@Override
	public void perturbEntityProbDistribution(int idx) {
		double[] pd = this.entityProbDists.get(idx);
		if (Math.max(pd[0], pd[1]) < 1.5 * Math.min(pd[0], pd[1])) {
			double tmp = pd[0];
			pd[0] = pd[1];
			pd[1] = tmp;
		}
	}

	@Override
	public void assignRandomValues(Random rng) {
		for (int i = 0; i < this.entities.size(); ++i) {
			this.entities.set(i, rng.nextBoolean());
			this.createSoftCompletion(i);
		}
		
	}

	/**
	 * Updates the soft completion in accordance with the hard assignment.
	 * Only applicable on latent attributes.
	 * @param i the entity.
	 */
	private void createSoftCompletion(int i) {
		double[] pd = this.entityProbDists.get(i);
		boolean b = this.entities.get(i);
		if (this.sharpSoftCompletion) {
			pd[0] = (b ? 0.0 : 1.0);
			pd[1] = (b ? 1.0 : 0.0);
		} else { 
			pd[0] = (b ? 0.33 : 0.67);
			pd[1] = (b ? 0.67 : 0.33);
		}
	}
}
