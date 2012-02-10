package se.cbb.jprime.topology;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.InfoProvider;
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
public class RBTreeArcDiscretiser implements ProperDependent, InfoProvider {

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
	
	/**
	 * For each arc, times of discretisation times thus:
	 * <ul>
	 * <li>Index 0: head of arc.</li>
	 * <li>Index 1,..,k: discretisation slice midpoints from head to arc.</li>
	 * <li>Index k+1: tail of arc (or "tip", if stem arc).</li>
	 * </ul>
	 */
	private double[][] discTimes;
	
	/** Currently affected vertices. */
	private int[] vertexCache = null;
	
	/** Cached times for affected vertices. */
	private double[][] discTimesCache = null;
	
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
		if (nmin <= 1 || nmax < nmin) {
			// We must have least two points for other classes to work safely...
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

	/**
	 * Returns the number of discretisation slices of an arc.
	 * @param x the head vertex of the arc.
	 * @return the number of discretisation slices.
	 */
	public int getNoOfSlices(int x) {
		return (this.discTimes[x].length - 2);
	}
	
	/**
	 * Returns the number of discretisation slices of a path from a
	 * vertex to tip of the tree.
	 * @param x the vertex.
	 * @return the number of discretisation slices.
	 */
	public int getNoOfSlicesForRootPath(int x) {
		int cnt = 0;
		while (x != RBTree.NULL) {
			cnt += (this.discTimes[x].length - 2);
			x = this.S.getParent(x);
		}
		return cnt;
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
		for (int x : vertices) {
			double vt = this.times.getVertexTime(x);
			double at = this.times.getArcTime(x);
			int no = Math.min(Math.max(this.nmin, (int) Math.ceil(this.times.getArcTime(x) / this.deltat)), this.nmax);
			this.discTimes[x] = new double[no + 2];		// Endpoints also included.
			double timestep = at / no;
			this.discTimes[x][0] = vt;		// Head of arc at first index.
			for (int xx = 1; xx <= no; ++xx) {
				this.discTimes[x][xx] = vt + timestep * (xx - 0.5);
			}
			this.discTimes[x][no+1] = vt + at;	// Tail of arc at last index.
		}
			
		// Override root arc if specified. Never mind if it hasn't changed.
		int root = this.S.getRoot();
		double vt = this.times.getVertexTime(root);
		double at = this.times.getArcTime(root);
		if (this.nroot > 0 && !Double.isNaN(at) && at > 1e-8) {
			this.discTimes[root] = new double[this.nroot + 2];		// Endpoints also included.
			double timestep = at / this.nroot;
			this.discTimes[root][0] = vt;		// Head of arc at first index.
			for (int xx = 1; xx <= this.nroot; ++xx) {
				this.discTimes[root][xx] = vt + timestep * (xx - 0.5);
			}
			this.discTimes[root][nroot+1] = vt + at;	// Tail of arc at last index.
		}
	}
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.S, this.times };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		
		// Determine affected vertices.
		ChangeInfo sInfo = changeInfos.get(this.S);
		ChangeInfo timesInfo = changeInfos.get(this.times);
		
		if (sInfo == null && timesInfo == null) {
			changeInfos.put(this, null);
			return;
		}
				
		this.vertexCache = null;
		if (sInfo == null) {
			this.vertexCache = timesInfo.getAffectedElements();
		} else if (timesInfo == null) {
			this.vertexCache = sInfo.getAffectedElements();
		} else {
			this.vertexCache = ChangeInfo.getUnion(new int[][]{sInfo.getAffectedElements(), timesInfo.getAffectedElements()});
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
		changeInfos.put(this, new ChangeInfo(this, null, this.vertexCache));
	}

	@Override
	public void clearCache(boolean willSample) {
		this.vertexCache = null;
		this.discTimesCache = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		for (int x : this.vertexCache) {
			this.discTimes[x] = this.discTimesCache[x];
		}
		this.vertexCache = null;
		this.discTimesCache = null;
	}
	
	/**
	 * For convenience, returns the root of the host tree.
	 * @return the root vertex of the host tree.
	 */
	public int getRoot() {
		return this.S.getRoot();
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
	 * Returns the discretisation times for an arc, thus:
	 * <ul>
	 * <li>Index 0: head of arc.</li>
	 * <li>Index 1,..,k: discretisation slice midpoints from head to arc.</li>
	 * <li>Index k+1: tail of arc (or "tip", if stem arc).</li>
	 * </ul>
	 * @param x the head vertex of the arc.
	 * @return the discretisation times.
	 */
	public double[] getDiscretisationTimes(int x) {
		return this.discTimes[x];
	}
	
	/**
	 * Returns the discretisation time thus:
	 * <ul>
	 * <li>Index 0: head of arc.</li>
	 * <li>Index 1,..,k: discretisation slice midpoints from head to arc.</li>
	 * <li>Index k+1: tail of arc (or "tip", if stem arc).</li>
	 * </ul>
	 * @param x the head vertex of the arc.
	 * @param i the index of the point within the arc.
	 * @return the discretisation times
	 */
	public double getDiscretisationTime(int x, int i) {
		return this.discTimes[x][i];
	}
	
	/**
	 * Returns the discretisation interval time span of an arc slice.
	 * @param x the head vertex of the arc.
	 * @return the discretisation interval time span.
	 */
	public double getSliceTime(int x) {
		return (this.times.getArcTime(x) / this.getNoOfSlices(x));
	}

	/**
	 * Returns the maximum number of slices along any tip-to-leaf path in the tree.
	 * @return the maximum number of slices.
	 */
	public int getMaxSliceHeight() {
		int[] mx = new int[this.S.getNoOfVertices()];
		List<Integer> vertices = this.S.getTopologicalOrdering();
		for (int i = vertices.size() - 1; i >= 0; --i) {
			int x = vertices.get(i);
			if (this.S.isLeaf(x)) {
				mx[x] = this.discTimes[x].length - 2;
			} else {
				mx[x] = Math.max(mx[this.S.getLeftChild(x)], mx[this.S.getRightChild(x)]) + this.discTimes[x].length - 2;
			}
		}
		return mx[this.S.getRoot()];
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.S.getNoOfVertices() * 1024);
		
		return sb.toString();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(16536);
		sb.append(prefix).append("RBTREE ARC-DISCRETISER\n");
		//sb.append("Times in pre-order of RBTreeDiscretizer on tree parameter ").append(this.S.getName()).append(" and times parameter ").append(this.times.getName()).append(":\n");
		sb.append(prefix).append("Discretisation:\n");
		prefix += '\t';
		sb.append(prefix).append("Arc:\tNo. of slices:\tTimes:\n");
		for (int x : this.S.getTopologicalOrdering()) {
			sb.append(prefix).append(x).append('\t').append(this.getNoOfSlices(x)).append('\t').append(Arrays.toString(this.discTimes[x])).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("RBTREE ARC-DISCRETISER\n");
		return sb.toString();
	}
	
}
