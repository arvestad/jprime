package se.cbb.jprime.io;

import java.util.HashSet;
import java.util.List;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

import se.cbb.jprime.topology.DAG;
import se.cbb.jprime.topology.DiscretisedArc;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;

/**
 * Reads a GML file containing a hybrid tree w. times.
 * Might be replaced with some suitable factory pattern in the future.
 * 
 * @author Joel Sjöstrand.
 */
public class HybridGraphCreator {

	/**
	 * Constructor.
	 * @param rawG raw graph.
	 * @param nmin min number of discretisation points per arc.
	 * @param nmax max number of discretisation points per arc.
	 * @param deltat approximate (never exceeded) discretisation timestep.
	 * @param nroot overriding number of discretisation points for the root stem arc.
	 * @return the DAG.
	 */
	public static DAG<DiscretisedArc> createHybridGraph(GMLGraph rawG, int nmin, int nmax, double deltat, int nroot) {
		// Read name.
		if (!rawG.getDirected()) {
			throw new IllegalArgumentException("Invalid hybrid graph: graph is not directed.");
		}
		String name = rawG.getName().trim();
		if (name == null || name.equals("")) {
			name = rawG.getLabel().trim();
		}
		if (name == null || name.equals("")) {
			name = "HybridGraph";
		}
		
		// Various data structures.
		List<GMLNode> vs = rawG.getNodes();
		List<GMLEdge> as = rawG.getEdges();
		int n = vs.size();
		HashSet<Integer> uniq = new HashSet<Integer>(n);
		NamesMap names = new NamesMap("Names", new String[n]);
		DoubleMap absTimes = new DoubleMap("AbsTimes", n);
		SimpleDirectedGraph<Integer, DiscretisedArc> topo = new SimpleDirectedGraph<Integer, DiscretisedArc>(DiscretisedArc.class);
		
		// Read the vertices.
		for (GMLNode v : vs) {
			Integer x = v.getId();
			if (x == null) {
				throw new IllegalArgumentException("Invalid hybrid graph: Vertex lacks ID.");
			}
			if (!uniq.add(x)) {
				throw new IllegalArgumentException("Invalid hybrid graph: Duplicate vertex with ID " + x + ".");
			}
			names.set(x, v.getName());
			Object t = v.getAttributeValue("time");
			if (t == null) {
				throw new IllegalArgumentException("Invalid hybrid graph: No absolute time specified for vertex with ID " + x + ".");
			}
			if (t.getClass() == Integer.class) {
				absTimes.set(x, ((Integer) t).intValue());
			} else if (t.getClass() == Double.class) {
				absTimes.set(x, ((Double) t).doubleValue());
			} else {
				throw new IllegalArgumentException("Invalid hybrid graph: Time specified for vertex with ID " + x + " is not a number.");
			}
			topo.addVertex(x);
		}
		
		// Arcs.
		for (GMLEdge a : as) {
			int x = a.getSource();
			int y = a.getTarget();
			if (x < 0 || y < 0 || x >= n || y >= n) {
				throw new IllegalArgumentException("Invalid hybrid graph: Invalid arc source or target ID (" + x + "," + y + ").");
			}
			DiscretisedArc e = new DiscretisedArc(x, y, nmin, nmax, deltat);
			topo.addEdge(x, y, e);
			double tx = absTimes.get(x);
			double ty = absTimes.get(y);
			double at = tx - ty;
			topo.setEdgeWeight(e, at);
		}
		
		// Validate topology.
		ConnectivityInspector<Integer, DiscretisedArc> insp = new ConnectivityInspector<Integer, DiscretisedArc>(topo);
		if (insp.connectedSets().size() != 1) {
			throw new IllegalArgumentException("Invalid hybrid graph: Graph is not connected.");
		}
		
		return null;
	}
}
