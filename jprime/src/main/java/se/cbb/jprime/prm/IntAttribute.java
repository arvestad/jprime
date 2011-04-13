package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Random;

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
	private int noOfVals;
	
	/** Entities. */
	private final ArrayList<Integer> entities;
	
	/** If latent, the probability distribution of each entity, otherwise null. */
	private ArrayList<double[]> entityProbDists;
	
	/** Dependency constraints */
	private DependencyConstraints dependencyConstraints;
	
	/** Makes soft completion comply with hard assignment when adding values. Mostly for debugging. */
	private boolean sharpSoftCompletion = false;
	
	/**
	 * Constructor for bounded or unbounded integer range.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param isLatent true if hidden or unknown.
	 * @param initialCapacity initial capacity for attribute entities.
	 * @param dependencyConstraints dependency structure constraints.
	 * @param k the number of valid values, i.e., yields the range 0,...,k-1.
	 */
	public IntAttribute(String name, PRMClass prmClass, boolean isLatent, int initialCapacity,
			DependencyConstraints dependencyConstraints, int k) {
		this.name = name;
		this.prmClass = prmClass;
		this.isLatent = isLatent;
		this.noOfVals = k;
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
	public int getNoOfValues() {
		return this.noOfVals;
	}

	@Override
	public Object getRandomEntityAsObject(Random random) {
		return new Integer(random.nextInt(this.noOfVals));
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
		this.entities.set(idx, new Integer(value));
	}
	
	/**
	 * Adds an attribute value. If latent, assigns a soft completion as well.
	 * @param value the value.
	 */
	public void addEntity(int value) {
		if (value < 0 || value >= this.noOfVals) {
			throw new IllegalArgumentException("Value out-of-range.");
		}
		this.entities.add(new Integer(value));
		if (this.isLatent) {
			this.entityProbDists.add(new double[this.noOfVals]);
			this.createSoftCompletion(this.entities.size() - 1);
		}
	}
	
	/**
	 * Creates a mock soft completion.
	 * @param i the entity.
	 */
	private void createSoftCompletion(int i) {
		double[] pd = this.entityProbDists.get(i);
		int idx = this.entities.get(i);
		if (this.sharpSoftCompletion) {
			for (int j = 0; j < pd.length; ++j) {
				pd[j] = 0.0;
			}
			pd[idx] = 1.0;
		} else {
			// As soft completion, assign twice the weight to the value corresponding to the hard assignment.
			double p = 1.0 / (pd.length + 1);
			for (int j = 0; j < pd.length; ++j) {
				pd[j] = p;
			}
			pd[idx] = 2 * p;
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
	public int getEntityAsInt(int idx) {
		return this.entities.get(idx).intValue();
	}

	@Override
	public void setEntityAsInt(int idx, int value) {
		this.setEntity(idx, value);
	}

	@Override
	public void addEntityAsInt(int value) {
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

	@Override
	public void clearEntityProbDistribution(int idx) {
		double pd[] = this.entityProbDists.get(idx);
		for (int i = 0; i < pd.length; ++i) {
			pd[i] = Double.NaN;
		}
	}

	@Override
	public void normaliseEntityProbDistribution(int idx) {
		double sum = 0.0;
		double pd[] = this.entityProbDists.get(idx);
		for (int i = 0; i < pd.length; ++i) {
			if (Double.isNaN(pd[i])) {
				pd[i] = 0.0;
			} else {
				sum += pd[i];
			}
		}
		if (sum == 0.0) {
			for (int i = 0; i < pd.length; ++i) {
				pd[i] = 1.0 / pd.length;
			}
		} else {
			for (int i = 0; i < pd.length; ++i) {
				pd[i] /= sum;
			}
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
		int topIdx = -1;
		double topProb = -1;
		double[] pd = this.entityProbDists.get(idx);
		for (int i = 0; i < pd.length; ++i) {
			if (pd[i] > topProb) {
				topProb = pd[i];
				topIdx = i;
			}
		}
		//System.out.print(topProb + "\t");
		return topIdx;
	}

	@Override
	public void perturbEntityProbDistribution(int idx) {
		// Switch place of best and second best soft completion values.
		int topIdx = -1;
		int oldTopIdx = -1;
		double topProb = -1;
		double[] pd = this.entityProbDists.get(idx);
		for (int i = 0; i < pd.length; ++i) {
			if (pd[i] > topProb) {
				oldTopIdx = topIdx;
				topProb = pd[i];
				topIdx = i;
			}
		}
		if (topIdx == 0) {
			topProb = -1;
			for (int i = 1; i < pd.length; ++i) {
				if (pd[i] > topProb) {
					topProb = pd[i];
					oldTopIdx = i;
				}
			}
		}
		if (pd[topIdx] < 1.5 * pd[oldTopIdx]) {
			topProb = pd[topIdx];
			pd[topIdx] = pd[oldTopIdx];
			pd[oldTopIdx] = topProb;
		}
	}

	@Override
	public void assignRandomValues(Random rng) {
		for (int i = 0; i < this.entities.size(); ++i) {
			this.entities.set(i, rng.nextInt(this.noOfVals));
			this.createSoftCompletion(i);
		}
	}
}
