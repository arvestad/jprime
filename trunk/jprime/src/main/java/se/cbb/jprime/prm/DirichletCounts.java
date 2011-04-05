package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * For a discrete child PRM attribute and a set of discrete parent attributes, computes and stores counts
 * of all entity value
 * configurations (vp1,...,vpk,vc) for parent values vpi and child value vc.
 * Furthermore, assuming parameter independence, holds a Dirichlet hyperparameter representing
 * the prior belief bestowed upon (any) configuration (i.e. the hyperparameter is invariant).
 * It is possible to let the number of parents be 0, in which case only child entities are counted.
 * <p/>
 * The counts are maintained on a hash-basis internally, meaning that memory complexity is
 * O(k) where k is the number of actual value configurations, not possible configurations.
 * <p/>
 * Interestingly, the counts are held as doubles, so that also soft completions for latent
 * attributes may be counted in a "weighted" manner.
 * 
 * @author Joel Sjöstrand.
 */
public class DirichletCounts {
	
	/**
	 * Inner class representing a value configuration and its count.
	 * Two instances are equal if they have the equivalent configurations.
	 */
	private class ConfigCount {
		int[] config;
		double count;
		
		/**
		 * Creates a new configuration-count pair.
		 * @param config the configuration.
		 * @param count the count.
		 */
		ConfigCount(int[] config, double count) {
			this.config = config;
			this.count = count;
		}
		
		/**
		 * Partly copies an existing count.
		 * @param cc object to copy.
		 * @param k copies configuration values (0,...,k-1).
		 * @param val value to be set at index k.
		 * @param weight sets the new count to copied count * weight.
		 */
		public ConfigCount(ConfigCount cc, int k, int val, double weight) {
			this.config = new int[cc.config.length];
			System.arraycopy(cc.config, 0, this.config, 0, k);
			this.config[k] = val;
			this.count = cc.count * weight;
		}
		
		/**
		 * Copies an existing ConfigCount, but leaves its last configuration value out.
		 * @param cc object to copy, apart from last element.
		 */
		public ConfigCount(ConfigCount cc) {
			this.config = new int[cc.config.length - 1];
			System.arraycopy(cc.config, 0, this.config, 0, this.config.length);
			this.count = cc.count;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(this.config);
		}

		@Override
		public boolean equals(Object obj) {
			return Arrays.equals(this.config, ((ConfigCount) obj).config);
		}
	}
	
	/** Child. */
	private final DiscreteAttribute child;
	
	/** Dependencies for child and its parents (perhaps none). */
	private Dependency[] dependencies;
	
	/** Size of total value range |v(p1)|*...*|v(pk)| for parents pi. */
	private int parentCardinality;
	
	/** Size of value range |v(c)| for child c. */
	private int childCardinality;
	
	/** Invariant Dirichlet parameter used in all parent-child value configurations. */
	private double dirichletParam;
	
	/** Counts hashed by (vp1,...,vpk,vc) for parent values vpi and child value vc. */
	private HashMap<ConfigCount, ConfigCount> counts;
	
	/** Counts summed over child values, hashed by (vp1,...,vpk) for parent values vpi. */
	private HashMap<ConfigCount, ConfigCount> summedCounts;
	
	/**
	 * Constructor.
	 * @param dependencies the dependencies.
	 * @param dirichletParam the Dirichlet hyperparameter, invariant for all possible
	 *        configurations.
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
		this.counts = new HashMap<ConfigCount, ConfigCount>(n / 8);
		this.summedCounts = (k == 0 ? null : new HashMap<ConfigCount, ConfigCount>(n / 8));
		
		this.update();
	}
	
	/**
	 * Recounts all parent-child value configurations.
	 */
	public void update() {
		this.counts.clear();
		int k = this.dependencies.length;
		int n = this.child.getNoOfEntities();
		ArrayList<ConfigCount> ccs = new ArrayList<ConfigCount>();
		
		if (this.dependencies.length == 0) {
			// Count child entities only.
			for (int i = 0; i < n; ++i) {
				ccs.clear();
				createConfigurations(ccs, 0, i);
				for (ConfigCount cc : ccs) {
					ConfigCount ccExist = this.counts.get(cc);
					if (ccExist == null) {
						this.counts.put(cc, cc);
					} else {
						ccExist.count += cc.count;
					}
				}
			}
		} else {
			// Count parent-child entity configurations.
			this.summedCounts.clear();
			for (int i = 0; i < n; ++i) {
				ccs.clear();
				for (int j = 0; j <= k; ++j) {
					createConfigurations(ccs, j, i);
				}
				for (ConfigCount cc : ccs) {
					ConfigCount ccExist = this.counts.get(cc);
					if (ccExist == null) {
						this.counts.put(cc, cc);
					} else {
						ccExist.count += cc.count;
					}
				}
				// Now increment summed counts.
				// Based on the parent-child configuration, revert to a parent-only set.
				for (ConfigCount cc : ccs) {
					ConfigCount ccParOnly = new ConfigCount(cc);
					ConfigCount ccExist = this.summedCounts.get(ccParOnly);
					if (ccExist == null) {
						this.summedCounts.put(ccParOnly, ccParOnly);
					} else {
						ccExist.count += ccParOnly.count;
					}
				}
			}
		}
	}
	
	/**
	 * Fills in an extra value to a set of configurations at a specific entity index.
	 * This method exists so as to be able to cope with soft completions.
	 * @param ccs the partially filled set of configuration-counts.
	 * @param idx the currently processed parent index (or child  for the last iteration).
	 * @param i the entity being processed.
	 */
	private void createConfigurations(ArrayList<ConfigCount> ccs, int idx, int i) {
		// Use child if index exceeds parent size.
		DiscreteAttribute attr;
		if (idx == this.dependencies.length) {
			attr = (DiscreteAttribute) this.child;
		} else {
			attr = (DiscreteAttribute) this.dependencies[idx].getParent();
			i = (this.dependencies[idx].hasIndex() ? this.dependencies[idx].getSingleParentEntityIndexed(i) : 
				this.dependencies[idx].getSingleParentEntity(i));
		}		
		
		if (attr.isLatent()) {
			double[] pd = attr.getEntityProbDistribution(i);
			if (idx == 0) {
				// Create new configurations with corresponding soft completion weights.
				for (int val = 0; val < pd.length; ++val) {
					int[] config = new int[this.dependencies.length + 1];
					config[0] = val;
					ccs.add(new ConfigCount(config, pd[val]));
				}
			} else {
				// Multiply existing configurations with number of soft completions.
				int origSz = ccs.size();
				for (int v = 1; v < pd.length; ++v) {
					double w = pd[v];
					for (int j = 0; j < origSz; ++j) {
						ccs.add(new ConfigCount(ccs.get(j), idx, v, w));
					}
				}
				for (ConfigCount cc : ccs.subList(0, origSz)) {
					cc.config[idx] = 0;
					cc.count *= pd[0];
				}
			}
		} else {
			// Read single value.
			int val = attr.getEntityAsNormalisedInt(i);
			if (idx == 0) {
				int[] config = new int[this.dependencies.length + 1];
				config[0] = val;
				ccs.add(new ConfigCount(config, 1.0));
			} else {
				for (ConfigCount cc : ccs) {
					cc.config[idx] = val;
				}
			}
		}
	}
	
	/**
	 * Returns E[P(vc | vp1,...,vpk) | I] where vc is the child value,
	 * vpi the parent values, and I the complete current entity assignment.
	 * If there are no parents, returns the prior (uniform) probability
	 * of the child c, i.e. 1.0/|v(c)|.
	 * <p/>
	 * No bounds checking.
	 * @param pcVals the attribute values converted to integers in this
	 *        order: (vp1,...,vpk,vc). Conversion must comply with
	 *        the attributes' method <code>getEntityAsNormalisedInt()</code>.
	 * @return the expected conditional probability of the child value given the parent values.
	 */
	public double getExpectedConditionalProb(int[] pcVals) {
		int k = this.dependencies.length;
		if (k == 0) {
			return (1.0 / this.childCardinality);
		}
		ConfigCount pc = new ConfigCount(pcVals, 0);
		ConfigCount p = new ConfigCount(pc);
		ConfigCount cc = this.summedCounts.get(pc);
		if (cc == null) {
			return (1.0 / this.childCardinality);
		}
		double s = cc.count;
		cc = this.counts.get(p);
		double c = (cc == null ? 0.0 : cc.count);
		return ((c + this.dirichletParam) / (s + this.dirichletParam * this.childCardinality));
	}
	
	/**
	 * Returns the number of valid child values.
	 * @return the number of valid child values.
	 */
	public int getChildCardinality() {
		return this.childCardinality;
	}
	
	/**
	 * Returns the number of valid parent value configurations (vp1,...,vpk).
	 * @return the number of valid parent value configurations.
	 */
	public int getParentCardinality() {
		return this.parentCardinality;
	}
	
	/**
	 * Returns the count of a certain configuration (vp1,...,vpk,vc).
	 * The count may be a non-integer float if one
	 * of the pertinent attributes is latent with a soft completion.
	 * No bounds checking.
	 * @param pcVals the configuration in the order outlined above.
	 *        The conversion to integers must comply the attributes' method
	 *        <code>getEntityAsNormalisedInt()</code>.
	 * @return the count, possibly 0.
	 */
	public double getCount(int[] pcVals) {
		ConfigCount pc = new ConfigCount(pcVals, 0.0);
		ConfigCount cc = this.counts.get(pc);
		return (cc == null ? 0.0 : cc.count);
	}
	
	/**
	 * Returns the count of a certain parent configuration (vp1,...,vpk) summed
	 * over all child values. The count may be a non-integer float if one
	 * of the pertinent attributes is latent with a soft completion.
	 * No bounds checking.
	 * @param pVals the configuration in the order outlined above.
	 *        The conversion to integers must comply the attributes' method
	 *        <code>getEntityAsNormalisedInt()</code>.
	 * @return the count, possibly 0.
	 */
	public double getSummedCount(int[] pVals) {
		ConfigCount p = new ConfigCount(pVals, 0);
		ConfigCount cc = this.summedCounts.get(p);
		return (cc == null ? 0.0 : cc.count);
	}
}
