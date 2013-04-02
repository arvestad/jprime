package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.topology.DAG;
import se.cbb.jprime.topology.DiscretisedArc;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.EnumMap;
import se.cbb.jprime.topology.HybridVertexType;
import se.cbb.jprime.topology.NamesMap;

/**
 * Reads a GML file containing a hybrid graph w. times.
 * Might be replaced with some suitable factory pattern in the future.
 * 
 * @author Joel Sjöstrand.
 */
public class HybridGraphCreator {

	/**
	 * Reads a hybrid network from file.
	 * @param f the file.
	 * @param nmin min number of discretisation points per arc.
	 * @param nmax max number of discretisation points per arc.
	 * @param deltat approximate (never exceeded) discretisation timestep.
	 * @param nroot overriding number of discretisation points for the root stem arc.
	 * @return the DAG.
	 * @throws GMLIOException.
	 * @throws IOException.
	 */
	public static Triple<DAG<DiscretisedArc>, NamesMap, EnumMap<HybridVertexType>>
	createHybridGraph(File f, int nmin, int nmax, double deltat, int nroot) throws GMLIOException, IOException {
		GMLGraph g = GMLFactory.getGraphs(GMLReader.readGML(f)).get(0);
		return createHybridGraph(g, nmin, nmax, deltat, nroot);
	}
	
	/**
	 * Creates and validates a hybrid graph with times.
	 * @param rawG raw graph.
	 * @param nmin min number of discretisation points per arc.
	 * @param nmax max number of discretisation points per arc.
	 * @param deltat approximate (never exceeded) discretisation timestep.
	 * @param nroot overriding number of discretisation points for the root stem arc.
	 * @return the DAG.
	 */
	public static Triple<DAG<DiscretisedArc>, NamesMap, EnumMap<HybridVertexType>>
	createHybridGraph(GMLGraph rawG, int nmin, int nmax, double deltat, int nroot) {
		// Read name.
		if (!rawG.getDirected()) {
			throw new IllegalArgumentException("Invalid hybrid graph: graph is not directed.");
		}
		String name = rawG.getName();
		if (name == null || name.trim().equals("")) {
			name = "HybridGraph";
		}
		
		// Various data structures.
		List<GMLNode> vs = rawG.getNodes();
		List<GMLEdge> as = rawG.getEdges();
		int n = vs.size();
		HashSet<Integer> uniq = new HashSet<Integer>(n);
		NamesMap names = new NamesMap("Names", new String[n]);
		DoubleMap vertexTimes = new DoubleMap("AbsTimes", n);
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
				vertexTimes.set(x, ((Integer) t).intValue());
			} else if (t.getClass() == Double.class) {
				vertexTimes.set(x, ((Double) t).doubleValue());
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
			double tx = vertexTimes.get(x);
			double ty = vertexTimes.get(y);
			double at = tx - ty;
			DiscretisedArc e = new DiscretisedArc(tx, ty, nmin, nmax, deltat);
			topo.addEdge(x, y, e);
			topo.setEdgeWeight(e, at);
		}
		
		// Validate topology.
		ConnectivityInspector<Integer, DiscretisedArc> insp = new ConnectivityInspector<Integer, DiscretisedArc>(topo);
		if (insp.connectedSets().size() != 1) {
			throw new IllegalArgumentException("Invalid hybrid graph: Graph is not connected.");
		}
		EnumMap<HybridVertexType> types = HybridVertexType.getVertexTypes(topo, n);
		DAG<DiscretisedArc> dag = new DAG<DiscretisedArc>("HybridGraph", topo);
		for (int x : dag.getSinks()) {
			String ln = names.get(x);
			if (ln == null || ln.equals("")) {
				throw new IllegalArgumentException("Invalid hybrid graph: Missing name for leaf vertex " + x + ".");
			}
		}
		
		// Rediscretise stem.
		DiscretisedArc stem = dag.getOutgoingArcs(dag.getSource()).iterator().next();
		stem.updateDiscretisation(nroot);
		
		// We're done.
		return new Triple<DAG<DiscretisedArc>, NamesMap, EnumMap<HybridVertexType>>(dag, names, types);
	}
}
