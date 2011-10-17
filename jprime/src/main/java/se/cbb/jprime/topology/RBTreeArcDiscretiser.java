package se.cbb.jprime.topology;

import java.util.HashSet;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.ProperDependent;

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
 * <p/>
 * Also, it provides access to the times of the arcs and vertices, as well as the mid-points
 * of the equidistant slices.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeArcDiscretiser implements Discretiser, ProperDependent {

	/** Tree. */
	private RBTree S;
	
	/** Ultrametric times (or similarly) of tree. */
	private TimesMap times;

	/** Minimum number of slices per arc. */
	private int nmin;

	/** Maximum number of slices per arc. */
	private int nmax;

	/** Approximate (never exceeded) timestep. */
	private double deltat;
	
	/** Number of slices for the arc leading into the root. -1 if not overridden. */
	private int nroot;
	
	/** Times of discretisation slice mid-points for each arc. */
	private double[][] discTimes;
	
	/** Currently affected vertices. */
	private int[] vertexCache = null;
	
	/** Cached times for affected vertices. */
	private double[][] discTimesCache = null;
	
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
		this.discTimes = new double[S.getNoOfVertices()][];
		this.discTimesCache = null;
		this.update();
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
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}

	@Override
	public int getNoOfPts(int x) {
		return this.discTimes[x].length;
	}

	/**
	 * Updates discretisation times.
	 */
	private void update() {
		// Acquire arcs to update.
		int[] vertices;
		if (this.vertexCache == null) {
			int no = this.S.getNoOfVertices();
			this.discTimes = new double[no][];
			vertices = new int[no];
			for (int i = 0; i < no; ++i) { vertices[i] = i; }
		} else {
			vertices = this.vertexCache;
		}
		
		// Update arc midpoint times.
		for (int x : this.vertexCache) {
			double vt = this.times.getVertexTime(x);
			double at = this.times.getArcTime(x);
			int no = Math.min(Math.max(this.nmin, (int) Math.ceil(this.times.getArcTime(x) / this.deltat)), this.nmax);
			this.discTimes[x] = new double[no];
			double timestep = at / no;
			for (int xx = 0; xx < no; ++xx) {
				this.discTimes[x][xx] = vt + timestep * (0.5 + xx);
			}
		}
			
		// Override root arc if specified. Never mind if it hasn't changed.
		int root = this.S.getRoot();
		double vt = this.times.getVertexTime(root);
		double at = this.times.getArcTime(root);
		if (this.nroot > 0 && !Double.isNaN(at) && at > 1e-8) {
			this.discTimes[root] = new double[this.nroot];
			double timestep = at / this.nroot;
			for (int xx = 0; xx < this.nroot; ++xx) {
				this.discTimes[root][xx] = vt + timestep * (0.5 + xx);
			}
		}
	}
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.S, this.times };
	}

	@Override
	public void cacheAndUpdateAndSetChangeInfo(boolean willSample) {
		
		// Determine affected vertices.
		ChangeInfo sInfo = this.S.getChangeInfo();
		ChangeInfo timesInfo = this.times.getChangeInfo();
		this.vertexCache = null;
		if (sInfo == null) {
			this.vertexCache = timesInfo.getAffectedElements();
		} else if (timesInfo == null) {
			this.vertexCache = sInfo.getAffectedElements();
		} else {
			int[] a = sInfo.getAffectedElements();
			int[] b = timesInfo.getAffectedElements();
			if (a != null && b != null) {
				HashSet<Integer> union = new HashSet<Integer>(a.length + b.length);
				for (int x : a) { union.add(x); }
				for (int x : b) { union.add(x); }
				this.vertexCache = new int[union.size()];
				int i = 0;
				for (int x : union) { this.vertexCache[i++] = x; }
			}
		}
		if (this.vertexCache == null) {
			// All elements may need update.
			this.vertexCache = new int[this.discTimes.length];
			for (int i = 0; i < this.vertexCache.length; ++i) { this.vertexCache[i] = i; }
		}
		
		// Cache affected times.
		this.discTimesCache = new double[this.discTimes.length][];
		for (int x : this.vertexCache) {
			int l = this.discTimes[x].length;
			this.discTimesCache[x] = new double[l];
			System.arraycopy(this.discTimes[x], 0, this.discTimesCache[x], 0, l);
		}
		
		// Update.
		this.update();
		
		// Set change info.
		this.changeInfo = new ChangeInfo(this, null, this.vertexCache);
	}

	@Override
	public void clearCacheAndClearChangeInfo(boolean willSample) {
		this.vertexCache = null;
		this.discTimesCache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCacheAndClearChangeInfo(boolean willSample) {
		for (int x : this.vertexCache) {
				this.discTimes[x] = this.discTimesCache[x];
		}
		this.vertexCache = null;
		this.discTimesCache = null;
		this.changeInfo = null;
	}
	
	/**
	 * Returns the absolute time (vertex time) of a vertex.
	 * @param x the vertex.
	 * @return the vertex time.
	 */
	public double getVertexTime(int x) {
		return this.times.getVertexTime(x);
	}
	
	/**
	 * Returns the relative time (arc time) of an arc, indexed by the arc's head x, i.e. time(p(x))-time(x).
	 * If x is the root, the "top time" is returned.
	 * @param x the head.
	 * @return the arc time.
	 */
	public double getArcTime(int x) {
		return this.times.getArcTime(x);
	}
	
	/**
	 * Returns the midpoint time for a discretisation interval of an arc.
	 * @param x the head vertex of the arc.
	 * @param xx the index of the discretisation interval (0,...,k in the
	 * direction from head to tail, i.e., leaves to root).
	 * @return the midpoint time.
	 */
	public double getMidpointTime(int x, int xx) {
		return this.discTimes[x][xx];
	}
}
