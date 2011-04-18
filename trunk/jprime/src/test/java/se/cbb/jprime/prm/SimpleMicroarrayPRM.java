package se.cbb.jprime.prm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import org.junit.* ;
import org.uncommons.maths.random.MersenneTwisterRNG;
import se.cbb.jprime.math.Probability;
import se.cbb.jprime.prm.ProbAttribute.DependencyConstraints;
import se.cbb.jprime.prm.Relation.Type;

import static org.junit.Assert.*;

/**
 * Simple microarray PRM example. Notation more or less complies with the one in "Learning Probabilistic
 * Relational Models", Friedman et al., IJCAI-99, 1999.
 * 
 * Outline (sorry, no proper TeX):
 * 
 * - Given a structure S, skeleton sigma and a completion I of all observed attributes, perform EM by:
 *   0) Create random parameter assignment Theta.
 *   1) Compute soft assignment of latent attribute(s) using Belief Propagation, or similarly.
 *      In our case, there is only one latent attribute, conditioned to be a source. Thus, simple
 *      exact inference is tractable and easier.
 *   2) Learn parameters Theta_est according to E[P(X.A=v | Pa(X.A)=u) | I] =
 *      (C_{X.A}[v,u] + alpha_{X.A}[v,u]) / sum_v'(C_{X.A}[v',u] + alpha_{X.A}[v',u]), for all child
 *      attributes X.A, given its parent(s) Pa(X.A). C_{X.A} refers to child-parent counts and
 *      alpha_{X.A} to user-defined Dirichlet pseudo-counts.
 *   3) Estimate log L(Theta | I,sigma,S) = log P(I | sigma,S,Theta) = l(Theta | I,sigma,S) =
 *      sum_{X_i}( sum_{A in X_i}( sum_{x in O^sigma(X_i)}(log P(I_{x.a} | I_{Pa(x.a)})) ) ).
 *   4) Iterate 1-3 until convergence.
 *   
 *   Of course, starting multiple EM runs will be necessary in order to search for the global
 *   maximum.
 *  
 * - For structure search (not implemented), consider:
 *   - Obtaining top soft assignment and parameters of current structure S according to above.
 *   - Doing hard assignment of top ranked soft assignment for all latent attribute entities.
 *   - Evaluating S according to P(S | I,sigma) prop. to P(I | S,sigma) =
 *     prod_i( prod{A in A(X_i)}( prod{u in V(Pa(X_i,A))}( DM({C_{X_i,A}[v,u]},{alpha_{X_i,A}[v,u]}) ) ) ),
 *     where DM({C[v]},{alpha[v]}) = Gamma(sum_v(alpha[v])) / Gamma(sum_v(alpha[v] + C[v])) *
 *     prod_v(Gamma(alpha[v] + C[v]) / Gamma(alpha[v])).
 *   - Possible making use of BIC like scoring to avoid too many edges.
 * 
 * 
 * @author Joel Sjöstrand.
 */
public class SimpleMicroarrayPRM {

	@Test
	public void main() throws FileNotFoundException {
		// Test skeleton.
		assertEquals(1000, this.skeleton.getPRMClass("Gene").getNoOfEntities());
		assertEquals(80, this.skeleton.getPRMClass("Array").getNoOfEntities());
		assertEquals(1000 * 80, this.skeleton.getPRMClass("Measurement").getNoOfEntities());
		
		this.run();
	}

	private MersenneTwisterRNG rng;
	
	private Skeleton skeleton;
	
	private int noOfClusters;

	private String suffix;
	
	public SimpleMicroarrayPRM() throws FileNotFoundException {
		this.rng = new MersenneTwisterRNG();
		System.out.println(this.rng.getSeed().length);
		this.skeleton = new Skeleton("SimpleMicroarraySkeleton");
		
		// For alternating between setups.
		if (false) {
			this.noOfClusters = 3;
			this.suffix = "_new.out";
		} else {
			this.noOfClusters = 12;
			this.suffix = ".out";
		}
		
		// Fill skeleton.
		this.readGeneFile();
		this.readArrayFiles();
		this.readMeasurementFiles();
	}
	
	/**
	 * Reads the gene file. Sets the hidden variable to 0 by default.
	 * @throws FileNotFoundException.
	 */
	public void readGeneFile() throws FileNotFoundException {
		// Skeleton part.
		PRMClass genes = new PRMClass("Gene");
		FixedAttribute id = new FixedAttribute("ID", genes, 1024);
		BooleanAttribute a1 = new BooleanAttribute("A1", genes, false, 1024, DependencyConstraints.NONE);
		BooleanAttribute a2 = new BooleanAttribute("A2", genes, false, 1024, DependencyConstraints.NONE);
		BooleanAttribute a3 = new BooleanAttribute("A3", genes, false, 1024, DependencyConstraints.NONE);
		IntAttribute cluster = new IntAttribute("Cluster", genes, true, 1024, DependencyConstraints.PARENT_ONLY, this.noOfClusters);
		this.skeleton.addPRMClass(genes);
		
		// Read values. Assign random values to latent variable.
		System.out.println(this.getClass().getResource("."));
		File f = new File(this.getClass().getResource("/microarray/synthetic/genesAttributes" + this.suffix).getFile());
		Scanner sc = new Scanner(f);
		File f2 = new File(this.getClass().getResource("/microarray/synthetic/gene_clusters.true" + this.suffix).getFile());
		Scanner sc2 = new Scanner(f2);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split(",");
			id.addEntity(parts[0]);
			a1.addEntity(parts[1].contains("A1"));
			a2.addEntity(parts[1].contains("A2"));
			a3.addEntity(parts[1].contains("A3"));
			cluster.addEntityAsInt(Integer.parseInt(sc2.nextLine().trim()));
		}
		sc.close();
	}
	
	/**
	 * Reads the microarray file.
	 * @throws FileNotFoundException.
	 */
	private void readArrayFiles() throws FileNotFoundException {
		// Skeleton part
		PRMClass arrays = new PRMClass("Array");
		FixedAttribute id = new FixedAttribute("ID", arrays, 128);
		IntAttribute cluster = new IntAttribute("Cluster", arrays, false, 128, DependencyConstraints.PARENT_ONLY, 4);
		this.skeleton.addPRMClass(arrays);
		
		// Read values.
		File f = new File(this.getClass().getResource("/microarray/synthetic/ArrayCluster" + this.suffix).getFile());
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split("[\t ]+");
			id.addEntity(parts[0]);
			cluster.addEntity(Integer.parseInt(parts[1]) - 1);
		}
		sc.close();
	}

	/**
	 * Reads the expression level files.
	 * @throws FileNotFoundException.
	 */
	private void readMeasurementFiles() throws FileNotFoundException {
		// Skeleton part.
		PRMClass measurements = new PRMClass("Measurement");
		FixedAttribute id = new FixedAttribute("ID", measurements, 131072);
		FixedAttribute gID = new FixedAttribute("GeneID", measurements, 131072);
		FixedAttribute aID = new FixedAttribute("ArrayID", measurements, 131072);
		IntAttribute level = new IntAttribute("Level", measurements, false, 131072, DependencyConstraints.NONE, 3);
		PRMClass genes = this.skeleton.getPRMClass("Gene");
		PRMClass arrays = this.skeleton.getPRMClass("Array");
		// Relations add themselves to their classes...
		new Relation(gID, genes.getFixedAttribute("ID"), Type.MANY_TO_ONE, true);
		new Relation(aID, arrays.getFixedAttribute("ID"), Type.MANY_TO_ONE, true);
		this.skeleton.addPRMClass(measurements);
		
		// Read values.
		File f;
		Scanner sc;
		String suff = this.suffix.equals("_new.out") ? "new_" : "";
		// One file per array (80 in total).
		for (int i = 0; i < 80; ++i) {
			f = new File(this.getClass().getResource("/microarray/synthetic/exp_array_" + suff + i + ".out").getFile());
			sc = new Scanner(f);
			String[] lvls = sc.nextLine().trim().split(",");
			// One expression level per gene (1000 in total).
			for (int j = 0; j < 1000; ++j) {
				id.addEntity("G" + j + "-" + "A" + i);
				gID.addEntity("G" + j);
				aID.addEntity("A" + i);
				level.addEntity(Integer.parseInt(lvls[j]) + 1);
			}
			sc.close();
		}
	}
	
	/** TBD. */
	public Structure getTrueStructure() {
		// True structure.
		Structure s = new Structure(this.skeleton);
		ProbAttribute gc = this.skeleton.getPRMClass("Gene").getProbAttribute("Cluster");
		ProbAttribute a1 = this.skeleton.getPRMClass("Gene").getProbAttribute("A1");
		ProbAttribute a2 = this.skeleton.getPRMClass("Gene").getProbAttribute("A2");
		ProbAttribute a3 = this.skeleton.getPRMClass("Gene").getProbAttribute("A3");
		ProbAttribute ac = this.skeleton.getPRMClass("Array").getProbAttribute("Cluster");
		ProbAttribute lvl = this.skeleton.getPRMClass("Measurement").getProbAttribute("Level");
		Relation m2g = this.skeleton.getPRMClass("Measurement").getRelation("Measurement.GeneID-Gene.ID");
		Relation m2a = this.skeleton.getPRMClass("Measurement").getRelation("Measurement.ArrayID-Array.ID");
		ArrayList<Relation> sc = new ArrayList<Relation>(1);
		sc.add(m2g);
		s.putDependency(new Dependency(lvl, sc, gc, true));
		s.putDependency(new Dependency(lvl, sc, a1, true));
		s.putDependency(new Dependency(lvl, sc, a2, true));
		s.putDependency(new Dependency(lvl, sc, a3, true));
		sc = new ArrayList<Relation>(1);
		sc.add(m2a);
		s.putDependency(new Dependency(lvl, sc, ac, true));
		return s;
	}
		
	
	private void run() {
//		for (int i = 0; i < 5000; ++i) {
//			Structure s = RandomStructureGenerator.createStrictRandomStructure(this.rng, this.skeleton, 5, 5, 2, 200);
//			if (!this.structures.contains(s)) {
//				this.structures.add(s);
//				//System.out.println(s);
//			}
//		}
//		for (Structure s : this.structures) {
//			System.out.println(s);
//		}
//		System.out.println(this.structures.size());
		
		DependenciesCache<DirichletCounts> counts = new DependenciesCache<DirichletCounts>();
		ArrayList<Dependencies> toAdd = new ArrayList<Dependencies>();
		ArrayList<Dependencies> toUpdate = new ArrayList<Dependencies>();
		
		IntAttribute gc = (IntAttribute) this.skeleton.getPRMClass("Gene").getProbAttribute("Cluster");
		int n = gc.getNoOfEntities();
		
		// Start with true structure.
		Structure struct = this.getTrueStructure();
		
		// Classification correlation matrix.
		int[][] corr = new int[n][n];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				corr[i][j] = 0;
			}
		}
		
		// Run many EM runs on the same structure.
		double topLoglhood = Double.NEGATIVE_INFINITY;
		for (int emi = 0; emi < 1; ++emi) {
			
			// Create random assignments.
			gc.assignRandomValues(this.rng);
			for (Dependencies deps : struct.getDependencies()) {
				counts.put(deps, new DirichletCounts(deps, this.suggestDirichletParam(deps)));
			}
			
			// Perform EM algorithm.
			double loglhood = -1000000000;
			int nonImpr = 0;
			int iter = 0;
			while (nonImpr < 10 && iter < 500) {
				
				// ----- E-STEP -----
				// Make soft completion given current parameters.
				this.inferGeneClusters(struct, counts);
	//			if (iter % 20 == 0 && iter <= 100) {
	//				for (int i = 0; i < gc.getNoOfEntities() / 10; ++i) {
	//					int idx = (int) (Math.random() * gc.getNoOfEntities());
	//					gc.perturbEntityProbDistribution(idx);
	//				}
	//			}
				
				// ----- M-STEP -----
				// Update parameters.
				counts.getNonCached(struct, toAdd, toUpdate);
				for (Dependencies deps : toAdd) {
					counts.put(deps, new DirichletCounts(deps, this.suggestDirichletParam(deps)));
				}
				for (Dependencies deps : toUpdate) {
					counts.get(deps).update();
				}
				
				// Compute the log-likelihood.
				Probability p = new Probability(1.0);
				for (Dependencies deps : struct.getDependencies()) {
					p.mult(counts.get(deps).getLikelihood());
				}
				
				// Count the number of consecutive non-improvements.
				if (p.getLogValue() < loglhood || (p.getLogValue() - loglhood) < 1e-8) {
					++nonImpr;
				} else {
					nonImpr = 0;
				}
				loglhood = p.getLogValue();
				if (loglhood > topLoglhood) { topLoglhood = loglhood; }
				System.out.println(iter + ":\t" + loglhood);
				++iter;
			}
			
			System.out.println("Top log-likelihood: " + topLoglhood);
			
			// Update classification matrix.
			this.inferGeneClusters(struct, counts);
			for (int i = 0; i < n; ++i) {
				int a = gc.getMostProbEntityAsInt(i);
				System.out.println(a);
				for (int j = 0; j < n; ++j) {
					int b = gc.getMostProbEntityAsInt(j);
					if (i != j && a == b)
						++corr[i][j];
				}
			}
		}
		
		// Use e.g. k-means to do final cluster partitioning of matrix.
		try {
			FileWriter fstream = new FileWriter("/tmp/corr.matrix");
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < n; ++i) {
				for (int j = 0; j < n; ++j) {
					out.write("" + corr[i][j] + "\t");
				}
				out.write('\n');
			}
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}	
	}
	
	/**
	 * Makes a soft completion exact inference of gene cluster, assuming it is the only latent attribute and
	 * that is is a source in the induced BN. Used instead of implementing a complete belief
	 * propagation or similarly.
	 * @param struct the structure.
	 * @param counts the counts (and thus conditional probabilities of the dependencies).
	 */
	private void inferGeneClusters(Structure struct, DependenciesCache<DirichletCounts> counts) {
		// Obtain all dependencies of which gene cluster is a parent.
		DiscreteAttribute gc = (DiscreteAttribute) struct.getSkeleton().getPRMClass("Gene").getProbAttribute("Cluster");
		Collection<Dependencies> gcDeps = struct.getInverseDependencies(gc);
		ArrayList<DirichletCounts> gcCounts = new ArrayList<DirichletCounts>(gcDeps.size());
		for (Dependencies deps : gcDeps) {
			gcCounts.add(counts.get(deps));
		}
		
		// Also add itself, for the prior!
		gcCounts.add(counts.get(struct.getDependencies(gc)));
		
		// Clear the soft completions.
		for (int i = 0; i < gc.getNoOfEntities(); ++i) {
			gc.clearEntityProbDistribution(i);
		}
		
		// For each child attribute.
		for (Dependencies deps : gcDeps) {
			// Retrieve child 'ch' and the dependency 'gc->ch', etc.
			ProbAttribute ch = deps.getChild();
			DirichletCounts dc = counts.get(deps);
			Dependency gcDep = null;
			for (Dependency dep : deps.getAll()) {
				if (dep.getParent() == gc) {
					gcDep = dep;  // Gene cluster can occur only once in our case.
					break;
				}
			}
			
			// For each child entity.
			for (int i = 0; i < ch.getNoOfEntities(); ++i) {
				
				// Get gene cluster entity and its soft completion.
				int gcIdx = gcDep.getSingleParentEntity(i);
				double[] sc = gc.getEntityProbDistribution(gcIdx);
				
				// For each possible value of the gene cluster entity,
				// make a temporary hard assignment, then update soft completion.
				for (int j = 0; j < this.noOfClusters; ++j) {
					gc.setEntityAsInt(gcIdx, j);
					double p = dc.getExpectedConditionalProb(i);
					sc[j] = (Double.isNaN(sc[j]) ? p : sc[j] * p);  // NaN if uninitialised.
				}
				// For well-conditioning, keep mean(sc[j]) to 1.
				// Should be harmless (right?) since it corresponds to multiplying
				// end-result with #<relevant rows> intermediary factors.
				gc.normaliseEntityProbDistribution(gcIdx);
			}
		}
	}
	
	/**
	 * 
	 * @param deps
	 * @return
	 */
	private double suggestDirichletParam(Dependencies deps) {
		double pseudoCnt = 0.0;
		try {
			int noOfSamples = deps.getChild().getNoOfEntities();
			int noOfPosVals = Math.max(1, deps.getParentCardinality()) * deps.getChildCardinality();
			pseudoCnt = ((double) noOfSamples / noOfPosVals) / 10;
		} catch (Exception ex) {}
		return pseudoCnt;
	}
}
