package se.cbb.jprime.apps.dltrs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.InfoProvider;
import se.cbb.jprime.mcmc.ProperDependent;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;

/**
 * Encapsulates a host tree where the tip of the top time arc and interior vertices
 * have given rise to a slicing of the tree into contemporary "epochs" of arcs (every epoch
 * is bounded above or below of a vertex or the host tree tip, and no vertices appear
 * within an epoch). 
 * Moreover, each epoch has, time-wise, been sliced further into several segments,
 * The top time of the tree is required to be
 * greater than 0 (i.e. there must be a top time arc).
 * <p/>
 * If two (or more) non-leaf vertices have the same time, one of the corresponding borders
 * are moved slightly so that two adjacent but disjoint epochs are created. This implies
 * that, for the sake of consistency, one should strive to access all vertex times, top time,
 * etc. from this class rather than from the underlying tree or the vertices themselves.
 * <p/>
 * The order of arcs in epochs are conserved in this way: comparing epoch number i and epoch
 * i-1 below, the "split arc" at index j in epoch i stems from arcs j and j+1 in
 * epoch i-1. An arc k, k<j, in epoch i has index k in epoch i-1. An arc k, k>j, in
 * epoch i has index k+1 in epoch i-1.
 * 
 * @author Joel Sjöstrand.
 */
public class EpochDiscretiser implements ProperDependent, InfoProvider {
		
	/**
	 * Minimum time span for a epoch. If smaller than this, one of the "time-colliding"
	 * epoch borders is moved slightly. Should work for any number of time-colliding vertices
	 * (leaves not considered, naturally).
	 */
	public static final double MIN_SPLICE_DELTA = 0.0001;
	
	/** The underlying original tree. */
	private RBTree S;
	
	/** Ultrametric times (or similarly) of tree. */
	private TimesMap times;
	
	/** Minimum number of slices per epoch. */
	private int nmin;

	/** Maximum number of slices per epoch. */
	private int nmax;

	/** Approximate (never exceeded) timestep. */
	private double deltat;
	
	/** Number of slices for the arc/epoch leading into the root. -1 if not overridden. */
	private int nroot;
	
	/** Epochs. */
	private Epoch[] epochs;
	
	/** For each epoch, the index of the arc in the epoch leading to split below. */
	private int[] splits;
	
	/** For each vertex, the index of the epoch which the vertex is the lower end of. */
	private IntMap vertexToEpoch;
	
	/** Cache. */
	private Epoch[] epochsCache = null;
	
	/** Cache. */
	private int[] splitsCache = null;
		
	/**
	 * Constructor. The user specifies a discretisation sub-division of each epoch.
	 * @param S host tree.
	 * @param times times of host tree.
	 * @param nmin minimum number of slices per epoch.
	 * @param nmax maximum number of slices per epoch.
	 * @param deltat approximate timestep. Not used if nmin==nmax.
	 * @param nroot overriding exact number of slices for arc/epoch leading into root. Set to 0
	 * for normal discretisation.
	 */
	public EpochDiscretiser(RBTree S, TimesMap times, int nmin, int nmax, double deltat, int nroot) {
		if (nmin <= 1 || nmax < nmin) {
			// We must have least two points for other classes to work safely...
			throw new IllegalArgumentException("Invalid discretisation bounds for EpochDiscretiser.");
		}
		if (nmin != nmax && deltat <= 0) {
			throw new IllegalArgumentException("Invalid discretisation timestep for EpochDiscretiser.");
		}
		if (nmin == nmax) {
			deltat = Double.MAX_VALUE;
		}
		this.S = S;
		this.times = times;
		this.nmin = nmin;
		this.nmax = nmax;
		this.deltat = deltat;
		this.nroot = nroot;
		vertexToEpoch = new IntMap("VertexAboveMap", S.getNoOfVertices());
		update();
	}
	
		
	/**
	 * Updates the discretisation based on the underlying host tree.
	 */
	public void update() {
		epochs = new Epoch[(S.getNoOfVertices()+1)/2];
		splits = new int[epochs.length];
		
		// Lowermost epoch contains all leaf arcs. Use these as starting point.
		LinkedList<Integer> q = new LinkedList<Integer>();
		addLeavesLeftToRight(q, S.getRoot());
		for (int x : q) {
			vertexToEpoch.set(x, 0);  // Epoch index of all leaves.
		}
		
		int xLo = q.peekFirst();                   // Lower vertex of epoch.
		double tLo = times.getVertexTime(xLo);     // Lower time of epoch.
		double tUp;                                // Upper time of epoch.
			
		// Find epochs.
		int epochNo = 0;
		splits[0] = -1;          // Undefined, since leaf epoch.
		while (q.size() > 1) {
			int xUpIdx = -1;
			tUp = Double.MAX_VALUE;
			
			// Find upper boundary of current epoch.
			int j = 0;
			for (int x : q) {
				int xpar = S.getParent(x);
				if (times.getVertexTime(xpar) < tUp) {
					xUpIdx = j;
					tUp = times.getVertexTime(xpar);
				}
				++j;
			}
			
			// Make sure there is at least a certain timespan for each epoch.
			if (tUp + MIN_SPLICE_DELTA < tLo) {
				tUp = tLo + MIN_SPLICE_DELTA;
			}
			assert(tLo < tUp);
			
			// Create epoch, etc.
			int noOfIvs = Math.min(Math.max(this.nmin, (int) Math.ceil((tUp - tLo) / this.deltat - 1e-6)), this.nmax);
			epochs[epochNo] = new Epoch(q, tLo, tUp, noOfIvs);
			splits[epochNo + 1] = xUpIdx;
			vertexToEpoch.set(xLo, epochNo);
			
			// Update arcs for next epoch. IMPORTANT: Note "order conservation".
			int par = this.S.getParent(q.remove(xUpIdx));   // Remove left child arc.
			q.add(xUpIdx, par);           // Insert parent arc where child arcs were originally placed.
			q.remove(xUpIdx + 1);         // Remove right child arc.
			xLo = par;
			tLo = tUp;
			epochNo++;
		}
		
		// Only the root should now remain.
		assert(q.size() == 1 && xLo == q.peekFirst() && xLo == S.getRoot());
		
		// Add epoch for top time arc.
		tUp = times.getVertexTime(S.getRoot()) + times.getArcTime(S.getRoot());
		if (tUp + MIN_SPLICE_DELTA < tLo) {
			tUp = tLo + MIN_SPLICE_DELTA;
		}
		assert(tLo < tUp);
		int noOfIvs = this.nroot > 0 ? this.nroot :
				Math.min(Math.max(this.nmin, (int) Math.ceil((tUp - tLo) / this.deltat - 1e-6)), this.nmax);
		epochs[epochNo] = new Epoch(q, tLo, tUp, noOfIvs);
		vertexToEpoch.set(xLo, epochNo);      // Actually undefined, since top time arc.
	}
	
	/**
	 * Returns the epoch at a specified index. Index 0 corresponds to epoch at leaves.
	 * @param epochNo the epoch identifier.
	 * @return the epoch discretisation.
	 */
	public Epoch getEpoch(int epochNo) {
		return epochs[epochNo];
	}
		
	/**
	 * Returns the discretised root-to-leaf-time. Note: Don't use the tree's own value
	 * directly, since it may not be exactly the same as the discretised value.
	 * @return the discretised root-to-leaf time.
	 */
	public double getRootToLeafTime() {
		return epochs[epochs.length-1].getLowerTime();
	}
	
	/**
	 * Returns the discretised tip-to-leaf-time. Note: Don't use the tree's own value
	 * directly, since it may not be exactly the same as the discretised value.
	 * @return the discretised tip-of-stem-to-leaf time.
	 */
	public double getTipToLeafTime() {
		return epochs[epochs.length-1].getUpperTime();
	}
	
	/**
	 * Returns the discretised time of a vertex. Note: Don't use the vertex' own time
	 * directly, since it may not be exactly the same as the discretised value.
	 * @param x the vertex.
	 * @return the discretised time of the vertex.
	 */
	public double getTime(int x) {
		return epochs[vertexToEpoch.get(x)].getLowerTime();
	}
	
	/**
	 * Returns a discretised time.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the discretised time.
	 */
	public double getTime(int epochNo, int idx) {
		return epochs[epochNo].getTime(idx);
	}
	
	/**
	 * Returns a discretised timespan. Do not confuse with getEpochTimestep().
	 * @param epochNo the epoch identifier.
	 * @return the discretised timespan.
	 */
	public double getEpochTimespan(int epochNo) {
		return epochs[epochNo].getTimespan();
	}
		
	/**
	 * Returns the epoch identifier above a specified vertex.
	 * @param x the lower end of the epoch.
	 * @return the epoch number.
	 */
	public int getEpochAbove(int x) {
		return vertexToEpoch.get(x);
	}

	/**
	 * Returns the epoch identifier below a specified vertex.
	 * Undefined for leaves.
	 * @param x the upper end of the epoch.
	 * @return the epoch number.
	 */
	public int getEpochBelow(int x) {
		return (vertexToEpoch.get(x) - 1);
	}
		
	/**
	 * Returns the total number of epochs.
	 * @return the number of epochs. 
	 */
	public int getNoOfEpochs() {
		return epochs.length;
	}

	/**
	 * Returns the number of arcs of a specified epoch.
	 * @param epochNo the epoch identifier.
	 * @return the number of arcs.
	 */
	public int getNoOfArcs(int epochNo) {
		return epochs[epochNo].getNoOfArcs();
	}
		
	/**
	 * Returns the timestep of a certain epoch. Do not confuse with getEpochTimespan().
	 * @param epochNo the epoch identifier.
	 * @return the timestep.
	 */
	public double getEpochTimestep(int epochNo) {
		return epochs[epochNo].getTimestep();
	}
		
	/**
	 * Returns the smallest timestep among the epochs.
	 * @return the smallest timestep.
	 */
	public double getMinTimestep() {
		double rec = Double.MAX_VALUE;
		for (Epoch ep : this.epochs) {
			if (ep.getTimestep() < rec) {
				rec = ep.getTimestep();
			}
		}
		return rec;
	}

		
	/**
	 * Returns the number of discretised times of the entire tree.
	 * @param unique if false, counts epoch-epoch boundary times
	 *        twice (the time is part of both adjacent epochs).
	 * @return the number of discretised times.
	 */
	public int getTotalNoOfTimes(boolean unique) {
		int sum = 0;
		for (Epoch ep : this.epochs) {
			sum += ep.getTimes().length;
		}
		if (unique) { sum -= (epochs.length - 1); } 
		return sum;
	}
		
	/**
	 * Returns the total number of points in the
	 * entire tree, all endpoints included (meaning that time-coinciding ones
	 * are both counted).
	 * @return overall number of points.
	 */
	public int getTotalNoOfPoints() {
		int sum = 0;
		for (Epoch ep : this.epochs) {
			sum += ep.getNoOfPoints();
		}
		return sum;
	}
		
	/**
	 * With respect to an epoch's arc vector, returns
	 * the index of the arc splitting at the epoch's
	 * lower border, i.e., the index of the arc that has out-degree
	 * 2.
	 * @param epochNo epoch identifier.
	 * @return index of splitting arc (not a vertex index, mind you).
	 */
	public int getSplitIndex(int epochNo) {
		return splits[epochNo];
	}
		
	/**
	 * Returns the discretised time identifier of
	 * the very tip of the top time arc.
	 * @return the top time "tip" as [epoch number, index in epoch].
	 */
	public int[] getEpochPtAtTop() {
		return new int[] { epochs.length-1, epochs[epochs.length-1].getNoOfTimes()-1 };
	}
		
	/**
	 * Returns the discretised time identifier
	 * below another. Undefined for input
	 * corresponding to leaf time.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtBelow(int epochNo, int idx) {
		return (idx == 0 ?
				new int[] { epochNo-1, epochs[epochNo-1].getNoOfTimes()-1 } :
				new int[] { epochNo, idx-1 });
	}
		
	/**
	 * Returns the discretised time identifier below another,
	 * with the condition that their time values do not coincide,
	 * i.e. if the input parameter is lowermost of an epoch, the
	 * returned value is the penultimate time of epoch below.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtBelowStrict(int epochNo, int idx) {
		return (idx == 0 ?
				new int[] { epochNo-1, epochs[epochNo-1].getNoOfTimes()-2} :
				new int[] { epochNo, idx-1});
	}
		
	/**
	 * Returns the discretised time identifier above another.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtAbove(int epochNo, int idx) {
		return (idx + 1 >= epochs[epochNo].getNoOfTimes() ?
				new int[] { epochNo + 1, 0} : new int[] {epochNo, idx + 1});
	}
		
	/**
	 * Returns the discretised time identifier above another,
	 * with the condition that their time values do not coincide,
	 * i.e. if the input refers to last time on an epoch, the
	 * returned value is second time of epoch above.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtAboveStrict(int epochNo, int idx) {
		return (idx + 1 >= epochs[epochNo].getNoOfTimes() ?
				new int[] {epochNo + 1, 1} : new int[] {epochNo, idx + 1});
	}
		
	/**
	 * Returns the discretised time identifier above another,
	 * with the condition that if the new value would be the last
	 * of the epoch, a move yet another notch up is made.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return the time identifier as [epoch number, index in epoch], albeit not last of epoch.
	 */
	public int[] getEpochPtAboveNotLast(int epochNo, int idx) {
		return (idx + 2 >= epochs[epochNo].getNoOfTimes() ?
				new int[] {epochNo + 1, 0} : new int[] {epochNo, idx + 1});
	}
		
	/**
	 * Returns true if the discretised time identifier is the
	 * last of its epoch.
	 * @param epochNo the epoch identifier.
	 * @param idx the index within the epoch.
	 * @return true if last time of epoch.
	 */
	public boolean isLastEpochPt(int epochNo, int idx) {
		return (idx + 1 == epochs[epochNo].getNoOfTimes());
	}
		
	
	/**
	 * Recursive helper. Adds leaves of tree rooted at x from
	 * left to right.
	 * @param q list to add leaves to.
	 * @param x root of subtree.
	 */
	private void addLeavesLeftToRight(LinkedList<Integer> q, int x) {
		if (this.S.isLeaf(x)) {
			q.add(x);
		} else {
			this.addLeavesLeftToRight(q, S.getLeftChild(x));
			this.addLeavesLeftToRight(q, S.getRightChild(x));
		}
	}


	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(16536);
		sb.append(prefix).append("EPOCH DISCRETISER\n");
		sb.append(prefix).append("Min no. of slices: ").append(this.nmin).append('\n');
		sb.append(prefix).append("Max no. of slices: ").append(this.nmax).append('\n');
		sb.append(prefix).append("Approx. timestep: ").append(this.deltat).append('\n');
		if (this.nroot != -1) {
			sb.append(prefix).append("Stem no. of slices: ").append(this.nroot).append('\n');
		}
		sb.append(prefix).append("Discretised stem timespan: ").append(this.getEpochTimespan(this.epochs.length-1)).append('\n');
		sb.append(prefix).append("Discretised root-to-leaf timespan: ").append(this.getRootToLeafTime()).append('\n');
		sb.append(prefix).append("No. of epochs: ").append(this.getNoOfEpochs()).append('\n');
		sb.append(prefix).append("Total no. of times: ").append(this.getTotalNoOfTimes(false)).append('\n');
		sb.append(prefix).append("Total no. of unique times: ").append(this.getTotalNoOfTimes(true)).append('\n');
		sb.append(prefix).append("Total no. disc. points: ").append(this.getTotalNoOfPoints()).append('\n');
		sb.append(prefix).append("Discretisation:\n");
		prefix += '\t';
		sb.append(prefix).append("Epoch:\tNo. of pts:\tTimestep:\tTimespan:\tArcs:\tSplit index:\n");
		for (int i = getNoOfEpochs()-1; i >= 0; --i) {
			Epoch ep = this.epochs[i];
			sb.append(prefix).append(i).append('\t');
			sb.append(prefix).append(ep.getNoOfArcs() + '*' + ep.getNoOfTimes() + '=' + ep.getNoOfPoints()).append('\t');
			sb.append(prefix).append(ep.getTimestep()).append('\t');
			sb.append(prefix).append(ep.getLowerTime() + "--" + ep.getUpperTime()).append('\t');
			sb.append(prefix).append(Arrays.toString(ep.getArcs())).append('\t');
			sb.append(prefix).append(this.splits[i]).append('\n');
		}
		sb.append(prefix).append("Vertex:\tDiscretisation time:\tEpoch above:\tEpoch below:\n");
		for (int x : this.S.getTopologicalOrdering()) {
			sb.append(prefix).append(x).append('\t');
			sb.append(prefix).append(this.getTime(x)).append('\t');
			sb.append(prefix).append(this.getEpochAbove(x)).append('\t');
			sb.append(prefix).append(this.getEpochBelow(x)).append('\n');
		}
		return sb.toString();
	}



	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(prefix).append("EPOCH DISCRETISER\n");
		return sb.toString();
	}



	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.S, this.times };
	}



	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		this.epochsCache = this.epochs;
		this.splitsCache = this.splits;
		this.vertexToEpoch.cache(null);
		
		// We always update the lot.
		this.update();
		
		// Set change info.
		changeInfos.put(this, new ChangeInfo(this, "Full EpochDiscretiser update."));
	}



	@Override
	public void clearCache(boolean willSample) {
		this.epochsCache = null;
		this.splitsCache = null;
		this.vertexToEpoch.clearCache();
	}



	@Override
	public void restoreCache(boolean willSample) {
		this.epochs = this.epochsCache;
		this.splits = this.splitsCache;
		this.vertexToEpoch.restoreCache();
	}
		
	@Override
	public String toString() {
		return this.getPreInfo("");
	}
}
