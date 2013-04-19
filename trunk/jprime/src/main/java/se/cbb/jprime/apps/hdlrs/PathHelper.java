package se.cbb.jprime.apps.hdlrs;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	/** All paths indexed by end-vertex. */
	HashMap<Integer, ArrayList<Path>> paths;
	
	/**
	 * Constructor.
	 * @param dag DAG.
	 */
	public PathHelper(HybridGraph dag) {
		this.dag = dag;
		this.paths = new HashMap<Integer, ArrayList<Path>>(dag.getNoOfVertices() * dag.getNoOfVertices());
		this.addPaths();
	}

	/**
	 * Adds all paths ending in x, then proceeds below subtree rooted at x.
	 */
	private void addPaths() {
		List<Integer> ordering = dag.getTopologicalOrdering();
		for (int x : ordering) {
			ArrayList<Path> ps = paths.get(x);
			if (ps == null) {
				ps = new ArrayList<Path>();
				paths.put(x, ps);
			}
			
			// Single-vertex path.
			ps.add(new Path(x));
				
			// Paths ending in this vertex.
			for (int y : dag.getParents(x)) {
				ArrayList<Path> yps = paths.get(y);
				for (Path yp : yps) {
					ps.add(new Path(yp, x));
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32768);
		List<Integer> ordering = dag.getTopologicalOrdering();
		for (int x : ordering) {
			for (Path p : paths.get(x)) {
				sb.append(p.toString()).append('\n');
			}
		}
		return sb.toString();
	}
	
	
}
