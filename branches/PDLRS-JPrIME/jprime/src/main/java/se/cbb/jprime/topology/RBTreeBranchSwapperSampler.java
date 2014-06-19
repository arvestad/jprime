package se.cbb.jprime.topology;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;

/**
 * Represents a branch-swapper which is constrained to a certain
 * number of trees (typically obtained as an output from a previous
 * MCMC run).
 * <p/>
 * Topologies with or without branch lengths are currently supported.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeBranchSwapperSampler implements Proposer {
	
	/** Trees to sample from. */
	private NewickRBTreeSamples treeSamples;
	
	/** Topology. */
	protected RBTree T;
	
	/** Lengths. Null if not used. */
	protected DoubleMap lengths;
	
	/** The number of available instances of the current topology. */
	protected int count;
	
	/** Cached number of counts. */
	protected int countCache = -1;
	
	/** If true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence. */
	protected boolean equalTopoChance;
	
	/** List with topology indices among one samples. */
	protected int[] sampleIndices;
	
	/** Statistics. */
	protected ProposerStatistics statistics = null;
	
	/** Pseudo-random number generator. */
	protected PRNG prng;
	
	/** Active flag. */
	protected boolean isActive;
	
    /** Pseudogenization switches across the gene tree T. */
    protected DoubleMap pgSwitches;
    
    /** Pseudogenization models across the gene tree T. */
    protected IntMap edgeModels; 
	
	/** Gene-Pseudogene Map. */
	protected LinkedHashMap<String, Integer> gpgMap;

	/** Gene Names to be used to check legality after selecting a new gene tree **/
	protected NamesMap geneNames;
	
	/** Maximum limit for changing the gene tree using the pseudogenization points **/
	public static final int MAX_LIMIT = 10;	

	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance) {
		this(T, null, prng, treeSamples, equalTopoChance);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, DoubleMap lengths, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance) {
		this.T = T;
		this.count = 1;     // Dummy, as low as possible.
		this.lengths = lengths;
		this.treeSamples = treeSamples;
		this.equalTopoChance = equalTopoChance;
		this.prng = prng;
		this.isActive = true;
		
		// Fill a list with indices to choose from.
		if (equalTopoChance) {
			this.sampleIndices = new int[treeSamples.getNoOfTrees()];
			for (int i = 0; i < this.sampleIndices.length; ++i) {
				this.sampleIndices[i] = i;
			}
		} else {
			this.sampleIndices = new int[treeSamples.getTotalTreeCount()];
			int i = 0;
			for (int j = 0; j < treeSamples.getNoOfTrees(); ++j) {
				for (int k = 0; k < this.treeSamples.getTreeCount(j); ++k) {
					this.sampleIndices[i++] = j;
				}
			}
		}
	}
	
	/**
	 * Constructor for pseudogenized model.
	 * @param T tree topology to perturb.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance, DoubleMap pgSwitchs, IntMap edgeModel, LinkedHashMap<String, Integer> pgMap, NamesMap gNames) {
		this(T, null, prng, treeSamples, equalTopoChance, pgSwitchs, edgeModel, pgMap, gNames);
	}
	
	/**
	 * Constructor for pseudogenized model.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, DoubleMap lengths, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance, DoubleMap pgSwitchs, IntMap edgeModel, LinkedHashMap<String, Integer> pgMap, NamesMap gNames) {
		this.T = T;
		this.count = 1;     // Dummy, as low as possible.
		this.lengths = lengths;
		this.treeSamples = treeSamples;
		this.equalTopoChance = equalTopoChance;
		this.prng = prng;
		this.isActive = true;
		this.pgSwitches = pgSwitchs;
		this.edgeModels = edgeModel;
		this.gpgMap = pgMap; 
		this.geneNames = gNames;
		
		// Fill a list with indices to choose from.
		if (equalTopoChance) {
			this.sampleIndices = new int[treeSamples.getNoOfTrees()];
			for (int i = 0; i < this.sampleIndices.length; ++i) {
				this.sampleIndices[i] = i;
			}
		} else {
			this.sampleIndices = new int[treeSamples.getTotalTreeCount()];
			int i = 0;
			for (int j = 0; j < treeSamples.getNoOfTrees(); ++j) {
				for (int k = 0; k < this.treeSamples.getTreeCount(j); ++k) {
					this.sampleIndices[i++] = j;
				}
			}
		}
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("BRANCH-SWAPPER-SAMPLER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		sb.append(prefix).append("Is active: ").append(this.isActive).append("\n");
		sb.append(prefix).append("No. of unique trees: ").append(this.treeSamples.getNoOfTrees()).append('\n');
		sb.append(prefix).append("Total no. of tree instances: ").append(this.treeSamples.getTotalTreeCount()).append('\n');
		sb.append(prefix).append("Coverage of most prevalent tree: ").append(this.treeSamples.getTreeCount(0) / (double) this.treeSamples.getTotalTreeCount()).append('\n');
		sb.append(prefix).append("Coverage of least prevalent tree: ").append(this.treeSamples.getTreeCount(this.treeSamples.getNoOfTrees() - 1) / (double) this.treeSamples.getTotalTreeCount()).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("BRANCH-SWAPPER-SAMPLER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public Set<StateParameter> getParameters() {
		HashSet<StateParameter> ps = new HashSet<StateParameter>();
		ps.add(this.T);
		if (this.lengths != null) { ps.add(this.lengths); }
		return ps;
	}

	@Override
	public int getNoOfParameters() {
		int cnt = 1;
		if (this.lengths != null) { cnt++; }
		return cnt;
	}

	@Override
	public int getNoOfSubParameters() {
		int cnt = this.T.getNoOfSubParameters();
		if (this.lengths != null) { cnt += this.lengths.getNoOfSubParameters(); }
		return cnt;
	}

	@Override
	public void setStatistics(ProposerStatistics stats) {
		this.statistics = stats;
	}

	@Override
	public ProposerStatistics getStatistics() {
		return this.statistics;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return this.isActive;
	}

	@Override
	public void setEnabled(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
//		boolean flag = isLegalSwitches(pgSwitches, gpgMap);
//		if(flag==false)
//			System.out.println(flag);
		
		// Cache everything.
		this.T.cache();
		this.pgSwitches.cache(null);
		this.edgeModels.cache(null);
		this.countCache = this.count;
		if (this.lengths != null) {
			this.lengths.cache(null);
		}
		
		// Sample a tree, and check if its legal according to pseudogenized model.
		RBTree geneTree = new RBTree(this.T);
		RBTree sampledTree;
		int i =0;
		int idx;
		do
		{	
			i++;
			idx = this.sampleIndices[this.prng.nextInt(this.sampleIndices.length)];
			sampledTree = this.treeSamples.getTree(idx);
		}
		while((!isALegalConfiguration(sampledTree.getRoot(), sampledTree)) && i < MAX_LIMIT);
		
		if (i < MAX_LIMIT)	
		{						// if a valid perturbed gene tree is found, change the topology 
			this.T.setTopology(sampledTree);
			this.count = this.treeSamples.getTreeCount(idx);
			// Converts all switches below switches to plain pseudogenized edge (does not allow a gene edge below a switch)
			makePseudogenizationConsistant(this.T.getRoot(), this.T);
		}else
			this.count = 0; 					// makes the forward probability zero, thus no forward transition
		if (this.lengths != null) {
			// Sample a lengths.
			List<DoubleMap> lengthses = this.treeSamples.getTreeBranchLengths(idx);
			DoubleMap sampledLengths = lengthses.get(this.prng.nextInt(lengthses.size()));
			for (int x = 0; x < sampledLengths.getSize(); ++x) {
				this.lengths.set(x, sampledLengths.get(x));
			}
		}
		
		// Note changes.
		ArrayList<StateParameter> affected = new ArrayList<StateParameter>(2);
		changeInfos.put(this.T, new ChangeInfo(this.T));
		int no = this.T.getNoOfSubParameters();
		affected.add(this.T);
		if (this.lengths != null) {
			changeInfos.put(this.lengths, new ChangeInfo(this.lengths));
			affected.add(this.lengths);
			no += this.lengths.getNoOfSubParameters();
		}
		
		// Bias forward-backwards density according to topology prevalence, disregarding branch lengths.
		LogDouble forward, backward;
		if (this.equalTopoChance) {
			forward = new LogDouble(1.0 / this.treeSamples.getNoOfTrees());
			backward = new LogDouble(1.0 / this.treeSamples.getNoOfTrees());
		} else {
			forward = new LogDouble(this.count / (double) this.treeSamples.getTotalTreeCount());
			backward = new LogDouble(this.countCache / (double) this.treeSamples.getTotalTreeCount());
		}
		
//		boolean flag2 = isLegalSwitches(pgSwitches, gpgMap);
//		if(flag2==false)
//			System.out.println(flag2);
		return new MetropolisHastingsProposal(this, forward, backward, affected, no);
	}

	
	public boolean isALegalConfiguration(int vertex, RBTree gtree) {	
//
//		if (!gtree.isLeaf(vertex))
//		{
//			if(this.edgeModels.get(vertex)==2 || this.edgeModels.get(vertex)==0)						// Case of pseudogenization ..
//			{
//				// Check if all the descendants are pseudogenes
//				List<Integer> descendants = gtree.getDescendantLeaves(vertex, true); 
//				for(Integer leaf:descendants)
//				{
//					Integer g = this.gpgMap.get(this.geneNames.get(leaf));
//						if(g != 1)
//							return false;
//				}
//				
//				// No descendant edge should be 1
//				List<Integer> alldescendantsvertices = gtree.getDescendants(vertex, true); 
//				for(Integer v:alldescendantsvertices)
//				{
//					if(this.edgeModels.get(v) == 1)
//						return false;
//				}
//			}
//			else	// Case of gene edge.. 
//			{
//				return (isALegalConfiguration(gtree.getLeftChild(vertex), gtree) && isALegalConfiguration(gtree.getRightChild(vertex), gtree));
//			}
//		}else
//		{
//			Integer g = this.gpgMap.get(this.geneNames.get(vertex));
//			if (g == 1) 												// Pseudogene case
//			{
//				if(this.edgeModels.get(vertex)!=1 )
//					return true;
//				else return false;
//			}
//			else 														// Gene case
//			{
//				if(this.edgeModels.get(vertex)==1)
//					return true;
//				else return false;
//			}
//		}
	
		
		int falseSample=0;
		List<Integer> leaves = gtree.getLeaves();
		for(Integer l: leaves)
		{
			if(gpgMap.get(geneNames.get(l.intValue()))==1)
			{
				int numberofswitches=0;
				int v=l.intValue();
				while(!gtree.isRoot(v))
				{
					if(pgSwitches.get(v)!=1)
						numberofswitches++;
					v=gtree.getParent(v);
				}
				if(gtree.isRoot(v) && pgSwitches.get(v)!=1)
					numberofswitches++;
				if(numberofswitches ==0 )
					falseSample=1;	
				if(numberofswitches>1)
					falseSample=2;
			}
		}
		if( falseSample != 0)
			return false;
		else
			return true;
		
	}			
	
	
	public void makePseudogenizationConsistant(int vertex, RBTree gtree)
	{
		boolean flag = isLegalSwitches(pgSwitches, gpgMap);
		if(flag==false)
			System.out.println(flag);
		if(!gtree.isLeaf(vertex))
		{
			if(this.edgeModels.get(vertex)==0)
			{
				if ( this.edgeModels.get(gtree.getParent(vertex)) == 1)  // parent is gene, child pseudogene with no switch! (introducing switch on child lineage)
				{
					this.edgeModels.set(vertex, 2);
					this.pgSwitches.set(vertex, 0.5);
				}
			}
			if(this.edgeModels.get(vertex)==2)
			{
				RemoveHalfPseudogenizedEdges(vertex, gtree);
			}else if(this.edgeModels.get(vertex)==1)
			{
				makePseudogenizationConsistant(gtree.getLeftChild(vertex), gtree);
				makePseudogenizationConsistant(gtree.getRightChild(vertex), gtree);
			}
		}else
		{
			if(this.edgeModels.get(vertex)==0)
			{
				if ( this.edgeModels.get(gtree.getParent(vertex)) == 1)  // parent is gene, child pseudogene with no switch! (introducing switch on child lineage)
				{
					this.edgeModels.set(vertex, 2);
					this.pgSwitches.set(vertex, 0.5);
				}
			}
		}
		boolean flag2 = isLegalSwitches(pgSwitches, gpgMap);
		if(flag2==false)
			System.out.println(flag2);
//		else
//			System.out.println("Pseudogenization consistent");
	}
	
	
	public void RemoveHalfPseudogenizedEdges(int vertex, RBTree gtree)
	{
		List<Integer> alldescendantsvertices = gtree.getDescendants(vertex, true);
		for(Integer v:alldescendantsvertices)
		{
			if(this.edgeModels.get(v) == 2)
			{
				this.edgeModels.set(v, 0);
				this.pgSwitches.set(v, 1);
			}
			else
			{
				try{
				PrintWriter faulty = new PrintWriter(new BufferedWriter(new FileWriter("error_in_RemoveHalfPseudogenizedEdges.txt", true)));
				faulty.write("Error in RemoveHalfPseudogenizedEdges");
				faulty.write("edgeModels are : " + edgeModels);
				faulty.write("pgSwitches are : " + pgSwitches);
				faulty.close();
				}catch(IOException e)
				{e.printStackTrace();}
			}
		}
	}
	
	@Override
	public void clearCache() {
		if (this.statistics != null) {
			this.statistics.increment(true);
		}
		this.T.clearCache();
		if (this.lengths != null) {
			this.lengths.clearCache();
		}
		this.countCache = -1;
	}

	@Override
	public void restoreCache() {
		if (this.statistics != null) {
			this.statistics.increment(false);
		}
		this.T.restoreCache();
		if (this.lengths != null) {
			this.lengths.restoreCache();
		}
		this.count = this.countCache;
		this.countCache = -1;
	}
	
	/**
	 * Checks if the switches are a legal pseudogenization
	 * @param r
	 * @return true or false
	 */
	public boolean isLegalSwitches(DoubleMap pgSwitches, LinkedHashMap<String, Integer> gpgMap)
	{
		
		int falseSample=0;
		List<Integer> leaves = T.getLeaves();
		for(Integer l: leaves)
		{
			if(gpgMap.get(geneNames.get(l.intValue()))==1)
			{
				int numberofswitches=0;
				int v=l.intValue();
				while(!T.isRoot(v))
				{
					if(pgSwitches.get(v)!=1)
						numberofswitches++;
					v=T.getParent(v);
				}
				if(T.isRoot(v) && pgSwitches.get(v)!=1)
					numberofswitches++;
				if(numberofswitches ==0 )
					falseSample=1;	
				if(numberofswitches>1)
					falseSample=2;
			}
		}		
		if(falseSample == 0)
			return true;
		else
			return false;
	}
}
