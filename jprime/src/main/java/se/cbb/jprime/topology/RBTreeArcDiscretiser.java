package se.cbb.jprime.topology;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;

/**
 * Discretises an ultrametric host tree by splitting every arc into
 * a number of equidistant slices. This is governed by a user-specified
 * minimum number of slices per edge, <i>nmin</i>, a maximum number, <i>nmax</i>, and an
 * approximate timestep, <i>deltat</i>, (which is never exceeded). The number of slices
 * of an arc with timespan, <i>t</i>, will then be
 * min(max(<i>nmin</i>,ceil(<i>t</i>/<i>deltat</i>)),<i>nmax</i>).
 * <p/>
 * The number of points of the arc leading into the root can be treated separately if
 * desired.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeArcDiscretiser implements Discretiser, Dependent {

	/** Host tree. */
	private RBTree S;
	
	/** Ultrametric times (or similarly) of host tree. */
	private TimesMap times;

	/** Minimum number of slices per arc. */
	private int nmin;

	/** Maximum number of slices per arc. */
	private int nmax;

	/** Approximate (never exceeded) timestep. */
	private double deltat;
	
	/** Number of slices for the arc leading into the root. -1 if not overridden. */
	private int nroot;
	
	/** Number of points for each arc. */
	private int[] noOfPts;
	
	/** Cache. */
	private int[] noOfPtsCache = null;
	
	/** Child dependents. */
	private TreeSet<Dependent> dependents;
	
	/** Change info. */
	private ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param S host tree.
	 * @param times times of host tree.
	 * @param nmin minimum number of slices per arc.
	 * @param nmax maximum number of slices per arc.
	 * @param deltat approximate timestep. Not used if nmin==nmax.
	 * @param nroot overriding exact number of slices for arc leading into root.
	 */
	public RBTreeArcDiscretiser(RBTree S, TimesMap times, int nmin, int nmax, double deltat, int nroot) {
		if (nmin < 1 || nmax < nmin) {
			throw new IllegalArgumentException("Invalid discretisation bounds for RBTreeDiscretiser.");
		}
		if (nmin != nmax && deltat <= 0) {
			throw new IllegalArgumentException("Invalid discretisation timestep for RBTreeDiscretiser.");
		}
		if (nmin == nmax) {
			deltat = Integer.MAX_VALUE;
		}
		this.S = S;
		this.times = times;
		this.nmin = nmin;
		this.nmax = nmax;
		this.deltat = deltat;
		this.nroot = nroot;
		this.noOfPts = new int[S.getNoOfVertices()];
		this.dependents = new TreeSet<Dependent>();
		S.addChildDependent(this);
		times.addChildDependent(this);
		this.update(null);
	}

	/**
	 * Constructor.
	 * @param S host tree.
	 * @param times times of host tree.
	 * @param nmin minimum number of slices per arc.
	 * @param nmax maximum number of slices per arc.
	 * @param deltat approximate timestep. Not used if nmin==nmax.
	 */
	public RBTreeArcDiscretiser(RBTree S, TimesMap times, int nmin, int nmax, double deltat) {
		this(S, times, nmin, nmax, deltat, -1);
	}
	
	@Override
	public boolean isDependentSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public Set<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.noOfPtsCache = new int[this.noOfPts.length];
		System.arraycopy(this.noOfPts, 0, this.noOfPtsCache, 0, this.noOfPts.length);
	}

	@Override
	public void update(boolean willSample) {
		ChangeInfo sInfo = this.S.getChangeInfo();
		ChangeInfo timesInfo = this.times.getChangeInfo();
		if (sInfo != null || timesInfo != null) {
			// Determine affected indices. Whenever we lack info
			// we use null == all elements changed.
			Set<Integer> indices = null;
			if (sInfo == null) {
				indices = timesInfo.getAffectedElements();
			} else if (timesInfo == null) {
				indices = sInfo.getAffectedElements();
			} else {
				Set<Integer> a = sInfo.getAffectedElements();
				Set<Integer> b = timesInfo.getAffectedElements();
				if (a != null && b != null) {
					indices = new TreeSet<Integer>(a);
					indices.addAll(b);
				}
			}
			this.update(indices);
			this.changeInfo = new ChangeInfo(this, null, indices);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.noOfPtsCache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.noOfPts = this.noOfPtsCache;
		this.noOfPtsCache = null;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}

	@Override
	public int getNoOfPts(int x) {
		return this.noOfPts[x];
	}

	/**
	 * Updates number of slices for entire tree.
	 * @param a list of indices; null for the entire tree.
	 */
	private void update(Set<Integer> indices) {
		if (indices == null) {
			for (int x = 0; x < this.S.getNoOfVertices(); ++x) {
				this.noOfPts[x] = Math.min(Math.max(this.nmin,
						(int) Math.ceil(this.times.getArcTime(x) / this.deltat)), this.nmax);
			}
		} else {
			for (int x : indices) {
				this.noOfPts[x] = Math.min(Math.max(this.nmin,
					(int) Math.ceil(this.times.getArcTime(x) / this.deltat)), this.nmax);
			}
		}
		// Override root arc if specified.
		if (this.nroot > 0 && this.times.getArcTime(this.S.getRoot()) > 0) {
			this.noOfPts[this.S.getRoot()] = this.nroot;
		}
	}
}
