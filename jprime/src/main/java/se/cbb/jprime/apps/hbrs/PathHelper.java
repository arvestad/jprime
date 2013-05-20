package se.cbb.jprime.apps.hbrs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import se.cbb.jprime.topology.HybridGraph;

/**
 * Keeps track of paths in the host tree.
 * 
 * @author Joel Sjöstrand.
 */
public class PathHelper {

	/** DAG. */
	HybridGraph dag;
	
	/** Temporally ordered descendants. */
	ArrayList<ArrayList<Integer>> descendants;
	
	/** Paths indexed by end-vertex. */
	ArrayList<ArrayList<Path>> pathsByHead;
	
	/** Paths indexed by start-vertex. */
	ArrayList<ArrayList<Path>> pathsByTail;
	
	/** Paths indexed by both vertex ends. */
	ArrayList<ArrayList<Path>> pathsByHeadAndTail;
	
	/**
	 * Constructor.
	 * @param dag DAG.
	 */
	public PathHelper(HybridGraph dag) {
		this.dag = dag;
		int n = dag.getNoOfVertices();
		this.descendants = new ArrayList<ArrayList<Integer>>(n);
		this.pathsByHead = new ArrayList<ArrayList<Path>>(n);
		this.pathsByTail = new ArrayList<ArrayList<Path>>(n);
		this.pathsByHeadAndTail = new ArrayList<ArrayList<Path>>(n * n);
		
		// Fill arrays.
		for (int x = 0; x < n; ++x) {
			this.descendants.add(new ArrayList<Integer>());
			this.pathsByHead.add(new ArrayList<Path>());
			this.pathsByTail.add(new ArrayList<Path>());
			for (int y = 0; y < n; ++y) {
				this.pathsByHeadAndTail.add(new ArrayList<Path>());
			}
		}

		this.addDescendants();
		this.addPathsByHead();
		
		// Fill remaining structures.
		for (int x = 0; x < n; ++x) {
			ArrayList<Path> ps = this.pathsByHead.get(x);
			for (Path p : ps)  {
				int head = p.getHead();
				int tail = p.getTail();
				pathsByTail.get(tail).add(p);
				pathsByHeadAndTail.get(head * n + tail).add(p);
			}
		}
	}

	/**
	 * Adds all descendants.
	 */
	private void addDescendants() {
		List<Integer> ordering = dag.getTopologicalOrdering();
		int n = ordering.size();
		for (int i = ordering.size() - 1; i >= 0; i--) {
			int x = ordering.get(i);
			HashSet<Integer> desc = new HashSet<Integer>(n);
			for (int y : this.dag.getChildren(x)) {
				desc.addAll(this.descendants.get(y));
			}
			desc.add(x);
			this.descendants.get(x).addAll(desc);
			Collections.sort(this.descendants.get(x), new Comparator<Integer>() {
				public int compare(Integer x1, Integer x2) {
					double t1 = dag.getVertexTime(x1);
					double t2 = dag.getVertexTime(x2);
					if (t1 < t2) { return -1; }
					if (t1 > t2) { return 1; }
					return 0;
				}
			});
		}
	}
	
	/**
	 * Adds all paths ending in x, then proceeds below subtree rooted at x.
	 */
	private void addPathsByHead() {
		List<Integer> ordering = dag.getTopologicalOrdering();
		for (int x : ordering) {
			ArrayList<Path> ps = pathsByHead.get(x);
						
			// Single-vertex path.
			ps.add(new Path(x));
				
			// Paths ending in this vertex.
			for (int y : dag.getParents(x)) {
				ArrayList<Path> yps = pathsByHead.get(y);
				for (Path yp : yps) {
					ps.add(new Path(yp, x));
				}
			}
		}
	}

	/**
	 * Returns all paths for a specified vertex.
	 * @param head the head of the path.
	 * @return the paths.
	 */
	public List<Path> getPathsByHead(int head) {
		return this.pathsByHead.get(head);
	}
	
	/**
	 * Returns all paths for a specified vertex.
	 * @param tail the tail of the path.
	 * @return the paths.
	 */
	public List<Path> getPathsByTail(int tail) {
		return this.pathsByHead.get(tail);
	}

	/**
	 * Returns all paths between two specified verteices.
	 * @param head the head of the path.
	 * @param tail the tail of the path.
	 * @return the paths.
	 */
	public List<Path> getPathsByHeadAndTail(int head, int tail) {
		int n = this.dag.getNoOfVertices();
		return this.pathsByHead.get(n *  head + tail);
	}

	/**
	 * Returns all descendants temporally sorted from leaves to the tip.
	 * @param x the vertex.
	 * @return the descendants of (and including) x.
	 */
	public ArrayList<Integer> getDescendants(int x) {
		return this.descendants.get(x);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32768);
		List<Integer> ordering = dag.getTopologicalOrdering();
		for (int x : ordering) {
			for (Path p : pathsByHead.get(x)) {
				sb.append(p.toString()).append('\n');
			}
		}
		return sb.toString();
	}
	
	
}
