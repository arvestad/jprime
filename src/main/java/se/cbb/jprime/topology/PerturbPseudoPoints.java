package se.cbb.jprime.topology;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.RealParameter;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;


/**
 * Proposer which perturbs the topology of a bifurcating rooted tree.
 * In order to increase the probability of accepting the new topology,
 * corresponding lengths and/or times may also be changed according
 * to simple heuristics.
 * <p/>
 * Currently, the tree is perturbed using NNI, SPR (also superset of NNI), and
 * rerooting. By default, these are selected using default probabilities, e.g., [0.5,0,3,0,2], but
 * this may be substituted using <code>setOperationWeights(...)</code>.
 * 
 * @author Owais Mahmudi.
 * 
 */
public class PerturbPseudoPoints implements Proposer {

	/** Topology. */
	protected RBTree T;
	
	/** Perturbed parameter. */
	private RealParameter param;
	
	/** Pseudogenization switches. */
	protected DoubleMap pgSwitches;
	
	/** Substitution model labels for all edges of T. */
	protected IntMap edgeModels;
	
	/** Gene-Pseudogene Map. */
	protected LinkedHashMap<String, Integer> gpgMap;
	
	/** Names of gene tree vertices for T. */
	protected NamesMap gNames;
	
	/** Statistics. */
	protected ProposerStatistics statistics = null;
	
	/** Pseudo-random number generator. */
	protected PRNG prng;
	
	/** Active flag. */
	protected boolean isActive;
	
	/** Last operation type. */
	protected String lastOperationType;
	
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 */
	public PerturbPseudoPoints(RBTree T, DoubleMap pgSwitches, IntMap edgeModels, NamesMap gNames, LinkedHashMap<String, Integer> gpgMap, PRNG prng) {
		System.out.println("Perturbing Pg Points..");
		this.T = T;
		this.pgSwitches = pgSwitches;
		this.edgeModels = edgeModels;
		this.gNames = gNames;
		this.gpgMap = gpgMap;
		this.prng = prng;
		this.isActive = true;
		this.lastOperationType = null;
		this.param = pgSwitches;
		
	}

	@Override
	public ProposerStatistics getStatistics() {
		return this.statistics;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		return null;
	}
	
	/**
	 * Asserts that all vertices are present and unique.
	 * @return true if all presents
	 */
	protected boolean verticesAreUnique() {
		String nw = this.T.toString().replace(")", ",").replace("(", "").replace(";", "");
		String[] vertices = nw.split(",");
		if (vertices.length != this.T.getNoOfVertices()) {
			return false;
		}
		HashSet<String> visited = new HashSet<String>(vertices.length);
		for (String x : vertices) {
			if (!visited.add(x)) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Asserts that all pseudogenization points are "sane", i.e. assumes that a pseudogene cannot convert to a gene.
	 * @return true if all descendants of pseudogenization switches are genes
	 */
	protected boolean assignmentMakeSense() {
		
		// Make sure the assignments makes sense, i.e. pgSwitches, and edgeModels
		
		/*
		String nw = this.T.toString().replace(")", ",").replace("(", "").replace(";", "");
		String[] vertices = nw.split(",");
		if (vertices.length != this.T.getNoOfVertices()) {
			return false;
		}
		HashSet<String> visited = new HashSet<String>(vertices.length);
		for (String x : vertices) {
			if (!visited.add(x)) {
				return false;
			}
		}*/
		return true;
	}	
	
	
	
	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		
		
		// First determine move to make.
		double move = 0.0;
		double barrier = 0.1;
		boolean pgPointMoved = false;
		int direction = 0; // direction = 0 if downward, 1 if upward, and -1 if stays on the same edge
		int[] indices = new int[this.pgSwitches.getSize()];
		
		for(int i =0; i < this.pgSwitches.getSize(); i++)
			indices[i]=i;
		
		// Cache everything.
		//this.T.cache();
//		if (this.pgSwitches != null) {
//			this.pgSwitches.cache(null);
//		}
//		if (this.edgeModels != null) {
//			this.edgeModels.cache(null);
//		}
		
//		for(int ver=0; ver<this.T.getNoOfVertices(); ver++)
//			System.out.println(Integer.toString(ver) + " : " + this.gNames.get(ver) + (this.T.isLeaf(ver)? (this.gpgMap.get(this.gNames.get(ver))==1? " Pseudogene":" Gene") : " Internal Vertex") );
//		System.out.println();
		
//		System.out.println(this.gNames.toString());
		
		while(pgPointMoved == false)
		{
			move = this.prng.nextDouble() - 0.5;
			// Perturb until a move is made!
			int k = getNoOfSwitches();
			int pgSwitch = this.prng.nextInt(k);
			int pgpoint=-1;
			
			for(int i=0; i<this.pgSwitches.getSize(); i++)
			{
				if(pgSwitches.get(i) < 1)
				{
					pgpoint++;
					if(pgpoint == pgSwitch)
					{
						double newPoint = this.pgSwitches.get(i) + move;
						
						// Move the pg-move up or down if it lies in the interval of (barrier, 1, -barrier) or (barrier, 0, -barrier)
						if(newPoint > 1-barrier && newPoint < 1)
							newPoint-=barrier;
						else if(newPoint > 1 && newPoint < 1+barrier)
							newPoint+=barrier;
						else if(newPoint > 0 && newPoint < barrier)
							newPoint+=barrier;
						else if(newPoint < 0 && newPoint > 0-barrier)
							newPoint-=barrier;
						
						if(newPoint > 1)
						{
							if(!this.T.isRoot(i))
								// Check if the parent edge can have pseudogenization switch
								if(canBePseudogenized(this.T.getParent(i)))
									// Check if the sibling have PG-point, then move both up-edge, else do nothing
									if(this.pgSwitches.get( this.T.getSibling(i) ) < 1)
									{
										this.pgSwitches.set(i, 1);
										this.pgSwitches.set(this.T.getSibling(i), 1);
										this.pgSwitches.set(this.T.getParent(i), newPoint-1);
										
										this.edgeModels.set(i, 0);
										this.edgeModels.set(this.T.getSibling(i),0);
										this.edgeModels.set(this.T.getParent(i), 2);
										
										direction = 1;
										pgPointMoved=true;
										System.out.println("Point moved upward " + i );
									}
						} else if(newPoint < 0)
						{
							// Move the PG-point to the sibling edges
							if(!this.T.isLeaf(i))
							{
								this.pgSwitches.set(i, 1);
								this.pgSwitches.set(this.T.getLeftChild(i), 1.0+newPoint);
								this.pgSwitches.set(this.T.getRightChild(i), 1.0+newPoint);
								
								this.edgeModels.set(i, 1);
								this.edgeModels.set(this.T.getLeftChild(i), 2);
								this.edgeModels.set(this.T.getRightChild(i), 2);
								
								direction = 0;
								pgPointMoved=true;
								System.out.println("Point moved downward " + i );
							}	
						}else
						{
							// Move the point
							this.pgSwitches.set(i, newPoint);
							direction = -1;
							pgPointMoved=true;
						}
					}
				}
			}
		}
		
		// Note changes. Just say that all sub-parameters have changed.
		//ArrayList<StateParameter> affected = new ArrayList<StateParameter>(3);
		//changeInfos.put(this.pgSwitches, new ChangeInfo(this.pgSwitches));
		//affected.add(this.pgSwitches);
		//int no = this.pgSwitches.getNoOfSubParameters();
		//int no = 0;
		
		//TODO: Adjust the forward-backward probabilities according to the direction of edge?
		double forwardDensity = 1.0;
		double backwardDensity = 1.0;
		if(direction == 1) // upward in the tree
			forwardDensity= 0.5;
		// Right now, we consider forward-backward probabilities as equal.
		
		changeInfos.put(this.param, new ChangeInfo(this.param, "Perturbed by PG Proposer", indices));
		//System.out.println(this.pgSwitches.toString());
//		System.out.println();
		return new MetropolisHastingsProposal(this, new LogDouble(forwardDensity), new LogDouble(backwardDensity), this.param, indices.length);
	}

	private boolean canBePseudogenized(int pgSwitch) {
		// TODO Auto-generated method stub
		boolean legalassignment = true;
		// Return false if the descendants have a gene
		List<Integer> descendants = this.T.getDescendantLeaves(pgSwitch, true);
		
		for(Integer leaf:descendants)
		{
			Integer g = this.gpgMap.get(this.gNames.get(leaf));
				if(g != 1)
					legalassignment = false;
		}
		return legalassignment;
	}

	private int getNoOfSwitches() {
		// TODO Auto-generated method stub
		int k=0;
		for(int i=0; i<this.pgSwitches.getSize(); i++)
			if(this.pgSwitches.get(i) < 1)
				k++;
		return k;
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
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("PSEUDOGENIZATION PROPOSER\n");
		sb.append(prefix).append("Perturbed pgSwitches parameter: ").append(this.pgSwitches == null ? "None" : this.pgSwitches.getName()).append('\n');
		sb.append(prefix).append("Is active: ").append(this.isActive).append("\n");
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("PSEUDOGENIZATION PROPOSER\n");
		sb.append(prefix).append("Perturbed pgSwitches parameter: ").append(this.pgSwitches == null ? "None" : this.pgSwitches.getName()).append('\n');
		
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}




	@Override
	public void clearCache() {
		if (this.statistics != null) {
			this.statistics.increment(true, this.lastOperationType);
		}
		this.T.clearCache();
		if (this.pgSwitches != null) {
			this.pgSwitches.clearCache();
		}
		if (this.edgeModels != null) {
			this.edgeModels.clearCache();
		}
	}

	@Override
	public void restoreCache() {
		if (this.statistics != null) {
			this.statistics.increment(false, this.lastOperationType);
		}
		//this.T.restoreCache();
		if (this.pgSwitches != null) {
			this.pgSwitches.restoreCache();
		}
		if (this.edgeModels != null) {
			this.edgeModels.restoreCache();
		}
	}

	@Override
//	public Set<StateParameter> getParameters() {
//		HashSet<StateParameter> ps = new HashSet<StateParameter>(7);
//		ps.add(this.pgSwitches);
//		return ps;
//	}
	public ArrayList<StateParameter> getParameters() {
		ArrayList<StateParameter> ps = new ArrayList<StateParameter>(7);
	ps.add(this.pgSwitches);
	return ps;
}

	@Override
	public int getNoOfParameters() {
		return 1;
	}

	@Override
	public int getNoOfSubParameters() {
		return this.pgSwitches.getSize();
	}
	
	@Override
	public void setStatistics(ProposerStatistics stats) {
		this.statistics = stats;
	}
	
	@Override
	public String toString() {
		return "PerturbPseudoPoints perturbing [" + 
			(this.pgSwitches != null ? this.pgSwitches.getName() : "") +
			"]";
	}


}
