package se.cbb.jprime.prm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.cbb.jprime.misc.IntPair;

/**
 * 
 * 
 * @author Joel Sjöstrand.
 */
public class DirichletCounts {
	
	/** Inner class representing a single count. */
	private class Count {
		int val;
		Count(int val) { this.val = val; }
	}
	
	/** Child. */
	private final DiscreteAttribute child;
	
	/** Dependencies for child and its parents (perhaps none). */
	private final Dependency[] dependencies;
	
	/** Size of value range |v(p1)|*...*|v(pk)| for parents pi. */
	private final int parentCardinality;
	
	/** Size of value range |v(c)| for child c. */
	private final int childCardinality;
	
	/** Invariant Dirichlet parameter used in all parent-child value configurations. */
	private final double dirichletParam;
	
	/** Counts hashed by (vp1,...,vpk,vc) for parent values vpi and child value vc. */
	private final HashMap<int[], Count> counts;
	
	/** Counts summed over child values, hashed by (vp1,...,vpk) for parent values vpi. */
	private final HashMap<int[], Count> summedCounts;
	
	/**
	 * 
	 * @param dependencies
	 * @param dirichletParam
	 */
	public DirichletCounts(Dependencies dependencies, double dirichletParam) {
		if (!dependencies.isDiscrete()) {
			throw new IllegalArgumentException("Cannot create Dirichlet counts when there are non-discrete dependencies.");
		}
		this.child = (DiscreteAttribute) dependencies.getChild();
		Set<Dependency> deps = dependencies.getAll();
		int k = deps.size();
		this.dependencies = new Dependency[k];
		Iterator<Dependency> it = deps.iterator();
		int pc = 1;
		for (int i = 0; i < k; ++i) {
			this.dependencies[i] = it.next();
			pc *= ((DiscreteAttribute) this.dependencies[i].getParent()).getIntervalSize();
		}
		this.parentCardinality = (k == 0 ? 0 : pc);
		this.childCardinality = this.child.getIntervalSize();
		this.dirichletParam = dirichletParam;
		int n = dependencies.getChild().getNoOfEntities();
		this.counts = new HashMap<int[], Count>(n / 8);
		this.summedCounts = (k == 0 ? null : new HashMap<int[], Count>(n / 8));
		
		this.update();
	}
	
	/**
	 * Recounts all parent-child value configurations.
	 */
	public void update() {
		this.counts.clear();
		int k = this.dependencies.length;
		int n = this.child.getNoOfEntities();
		
		if (k == 0) {
			// Count child entities only.
			for (int i = 0; i < n; ++i) {
				int[] cVal = new int[1];
				cVal[0] = this.child.getEntityAsInt(i);
				Count count = this.counts.get(cVal);
				if (count == null) {
					this.counts.put(cVal, new Count(1));
				} else {
					count.val++;
				}
			}
		} else {
			// Count parent-child entity configurations.
			this.summedCounts.clear();
			for (int i = 0; i < n; ++i) {
				int[] pcVals = new int[k + 1];
				int[] pVals = new int[k];
				for (int j = 0; j < k; ++j) {
					DiscreteAttribute par = (DiscreteAttribute) this.dependencies[j].getParent();
					pcVals[j] = (this.dependencies[j].hasIndex() ?
							par.getEntityAsInt(this.dependencies[j].getSingleParentEntityIndexed(i)) :
							par.getEntityAsInt(this.dependencies[j].getSingleParentEntity(i)));
				}
				pcVals[k] = this.child.getEntityAsInt(i);
				System.arraycopy(pcVals, 0, pVals, 0, k);
				Count count = this.counts.get(pcVals);
				if (count == null) {
					this.counts.put(pcVals, new Count(1));
					count = this.summedCounts.get(pVals);
					if (count == null) {
						this.summedCounts.put(pVals, new Count(1));
					} else {
						count.val++;
					}
				} else {
					count.val++;
					this.summedCounts.get(pVals).val++;
				}
			}
		}
	}
	
	/**
	 * Returns E[P(vc | vp1,...,vpk) | I] where vc is the child value,
	 * vpi the parent values, and I the complete current entity assignment.
	 * <p/>
	 * If there are no parents, returns the prior (uniform) probability
	 * of the child c, i.e. 1.0/|v(c)|.
	 * @param pcVals the attribute values converted to integers in this
	 *        order: (vp1,...,vpk,vc).
	 * @return the expected conditional probability of the.
	 */
	public double getExpectedConditionalProb(int[] pcVals) {
		int k = this.dependencies.length;
		if (k == 0) {
			return (1.0 / this.childCardinality);
		}
		int[] pVals = new int[k];
		System.arraycopy(pcVals, 0, pVals, 0, k);
		Count count = this.summedCounts.get(pcVals);
		if (count == null) {
			return (1.0 / this.childCardinality);
		}
		int s = count.val;
		count = this.counts.get(pVals);
		int c = (count == null ? 0 : count.val);
		return ((c + this.dirichletParam) / (s + this.dirichletParam * this.childCardinality));
	}
}
