package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleDirectedGraph;

import se.cbb.jprime.io.GMLEdge;
import se.cbb.jprime.io.GMLGraph;
import se.cbb.jprime.io.GMLNode;

/**
 * Specialisation of a DAG to a hybridisation graph.
 * The graph is ultrametric, and every arc has discretisation points.
 *  
 * @author Joel Sjöstrand.
 */
public class HybridGraph extends DAG<DiscretisedArc> {
	
	/** Available vertex types. */
	public enum VertexType {
		STEM_TIP,
		SPECIATION,
		LEAF,
		HYBRID_DONOR,
		EXTINCT_HYBRID_DONOR,
		ALLOPOLYPLOIDIC_HYBRID,
		AUTOPOLYPLOIDIC_HYBRID;
	}
	
	/** Vertex (or, more often, leaf) names. */
	private NamesMap vertexNames;
	
	/** Vertex types. */
	private EnumMap<VertexType> vertexTypes;
	
	/** The one and only source. */
	private int source;
	
	/** Topological ordering. */
	private List<Integer> ordering;

	/** Vertex times. Must comply with arc times. */
	private DoubleMap vertexTimes;
		
	/**
	 * Constructor. The input graph must have vertex times (identical for donors and hybridisations) and correct
	 * in- and outdegrees.
	 * @param rawG raw GML graph.
	 * @param nmin min number of discretisation points per arc.
	 * @param nmax max number of discretisation points per arc.
	 * @param deltat approximate (never exceeded) discretisation timestep.
	 * @param nroot overriding number of discretisation points for the root stem arc.
	 */
	public HybridGraph(GMLGraph rawG, int nmin, int nmax, double deltat, int nroot) {
		super("HybridGraph");
		this.topo = new SimpleDirectedGraph<Integer, DiscretisedArc>(DiscretisedArc.class);
		
		// Read name.
		if (!rawG.getDirected()) {
			throw new IllegalArgumentException("Invalid hybrid graph: graph is not directed.");
		}
		this.name = rawG.getName();
		if (this.name == null || this.name.trim().equals("")) {
			this.name = "HybridGraph";
		}
		
		// Various data structures.
		List<GMLNode> vs = rawG.getNodes();
		List<GMLEdge> as = rawG.getEdges();
		int n = vs.size();
		HashSet<Integer> uniq = new HashSet<Integer>(n);
		this.vertexNames = new NamesMap("VertexNames", new String[n]);
		this.vertexTimes = new DoubleMap("VertexTimes", n);
		
		// Read the vertices.
		for (GMLNode v : vs) {
			Integer x = v.getId();
			if (x == null) {
				throw new IllegalArgumentException("Invalid hybrid graph: Vertex lacks ID.");
			}
			if (!uniq.add(x)) {
				throw new IllegalArgumentException("Invalid hybrid graph: Duplicate vertex with ID " + x + ".");
			}
			this.vertexNames.set(x, v.getName());
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
			this.topo.addVertex(x);
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
			// For sorting safety, we set the 0 arc time of hybrid arcs to DiscretisedArc.EPS/2.
			if (Math.abs(tx - ty) <= DiscretisedArc.EPS) {
				ty = tx - DiscretisedArc.EPS / 2.0;
				vertexTimes.set(y, ty);
			}
			DiscretisedArc e = new DiscretisedArc(tx, ty, nmin, nmax, deltat);
			topo.addEdge(x, y, e);
		}
		
		// Update some simple help structures.
		this.update();
		
		// Validate topology.
		ConnectivityInspector<Integer, DiscretisedArc> insp = new ConnectivityInspector<Integer, DiscretisedArc>(this.topo);
		if (insp.connectedSets().size() != 1) {
			throw new IllegalArgumentException("Invalid hybrid graph: Graph is not connected.");
		}
		this.vertexTypes = new EnumMap<VertexType>("VertexTypes", n);
		for (int x = 0; x < n; ++x) {
			this.vertexTypes.set(x, getVertexTyp(x, topo.incomingEdgesOf(x), topo.outgoingEdgesOf(x)));
		}
		for (int x : this.sinks) {
			String ln = this.vertexNames.get(x);
			if (ln == null || ln.equals("")) {
				throw new IllegalArgumentException("Invalid hybrid graph: Missing name for leaf vertex " + x + ".");
			}
		}
		
		// Rediscretise stem.
		DiscretisedArc stem = this.getOutgoingArcs(this.getSource()).iterator().next();
		stem.updateDiscretisation(nroot);
	}
	
	@Override
	public void update() {
		// Note that times, vertex types, etc are assumed to be up-to-date already!
		super.update();
		this.source = sources.iterator().next();
		
		// Compute topological ordering that complies with vertex times!
		this.ordering = new ArrayList<Integer>(this.noOfVertices);
		PriorityQueue<Integer> q = new PriorityQueue<Integer>(this.noOfVertices, new Comparator<Integer>() {
			public int compare(Integer v, Integer w) {
				double tv = vertexTimes.get(v);
				double tw = vertexTimes.get(w);
				if (tv > tw) { return -1; }
				if (tv < tw) { return 1; }
				return 0;
			}
		});
		HashSet<Integer> visited = new HashSet<Integer>(this.noOfVertices);
		q.add(this.source);
		while (!q.isEmpty()) {
			int x = q.poll();
			this.ordering.add(x);
			for (int c : this.getChildren(x)) {
				if (!visited.contains(c)) {
					q.add(c);
					visited.add(c);
				}
			}
		}
	}
	
	/**
	 * Returns a topologically sorted list of all vertices, i.e., a list such that x will
	 * appear before y whenever there is an arc (x,y). Moreover, the topological ordering also
	 * ensures that time(v) >= time(w) should v appear before w. Donor vertices of hybridisations
	 * appear before the receiver vertex.
	 * @return the ordering.
	 */
	@Override
	public List<Integer> getTopologicalOrdering() {
		// Stored as an optimisation.
		return this.ordering;
	}
	
	/**
	 * Since the hybridisation graph is required to have a single stem arc, its tip is the only source of the DAG.
	 * @return the stem tip.
	 */
	public int getSource() {
		return this.source;
	}
	
	/**
	 * Returns the vertex type.
	 * @param x vertex.
	 * @param in ingoing arcs.
	 * @param out outgoing arcs.
	 * @return type.
	 */
	private static VertexType getVertexTyp(int x, Set<DiscretisedArc> in, Set<DiscretisedArc> out) {
		int indegree = (in == null ? 0 : in.size());
		int outdegree = (out == null ? 0 : out.size());
		
		if (indegree == 0) {
			// STEM_TIP.
			if (outdegree != 1) {
				throw new IllegalArgumentException("Invalid stem tip vertex " + x + ": must have exactly one child (or did you forget the incoming edge?).");
			}
			return VertexType.STEM_TIP;
		}
		
		if (indegree == 1) {
			DiscretisedArc par = in.iterator().next();
			if (outdegree == 0) {
				// LEAF.
				if (par.getArcTime() <= DiscretisedArc.EPS) {
					throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc ending in " + x + ".");
				}
				return VertexType.LEAF;
			}
			
			if (outdegree == 1) {
				DiscretisedArc ch = out.iterator().next();
				double tp = par.getArcTime();
				double tc = ch.getArcTime();
				if (Math.abs(tp) <= DiscretisedArc.EPS) {
					// AUTOPOLYPLOIDIC_HYBRID.
					return VertexType.AUTOPOLYPLOIDIC_HYBRID;
				}
				if (Math.abs(tp) > DiscretisedArc.EPS && Math.abs(tc) <= DiscretisedArc.EPS) {
					// EXTINCT_HYBRID_DONOR.
					return VertexType.EXTINCT_HYBRID_DONOR;
				}
				throw new IllegalArgumentException("Invalid arc times for vertex " + x + ". One of the surrounding arcs must have time span 0.");
			}
			
			if (par.getArcTime() <= DiscretisedArc.EPS) {
				throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc ending in " + x + ".");
			}
			Iterator<DiscretisedArc> it = out.iterator();
			DiscretisedArc c1 = it.next();
			DiscretisedArc c2 = it.next();
			double t1 = c1.getArcTime();
			double t2 = c2.getArcTime();
			if (t1 > DiscretisedArc.EPS && t2 > DiscretisedArc.EPS) {
				// SPECIATION.
				return VertexType.SPECIATION;
			}
			if ((t1 > DiscretisedArc.EPS && Math.abs(t2) <= DiscretisedArc.EPS) || (t2 > DiscretisedArc.EPS && Math.abs(t1) <= DiscretisedArc.EPS)) {
				// HYBRID_DONOR.
				return VertexType.HYBRID_DONOR;
			}
			throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree or times of outgoing arcs.");
		}
		
		if (indegree == 2 && outdegree == 1) {
			Iterator<DiscretisedArc> it = in.iterator();
			DiscretisedArc p1 = it.next();
			DiscretisedArc p2 = it.next();
			double t1 = p1.getArcTime();
			double t2 = p2.getArcTime();
			if (Math.abs(t1) <= DiscretisedArc.EPS && Math.abs(t2) <= DiscretisedArc.EPS) {
				// ALLOPOLYPLOIDIC_HYBRID.
				return VertexType.ALLOPOLYPLOIDIC_HYBRID;
			}
			throw new IllegalArgumentException("Invalid outgoing arc times for vertex " + x + ": Must be greater than 0.");
		}
		throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree of in- or outgoing arcs.");
	}
	
	/**
	 * Returns the vertex name.
	 * @param x the vertex.
	 * @return the name.
	 */
	public String getVertexName(int x) {
		return this.vertexNames.get(x);
	}
	
	public NamesMap getVertexNames() {
		return this.vertexNames;
	}
	
	/**
	 * Returns the vertex type.
	 * @param x the vertex.
	 * @return the type.
	 */
	public VertexType getVertexType(int x) {
		return this.vertexTypes.get(x);
	}
	
	/**
	 * Returns the time of a vertex.
	 * @param x the vertex.
	 * @return the time.
	 */
	public double getVertexTime(int x) {
		return this.vertexTimes.get(x);
	}
	
	/**
	 * Overrides the time of the stem arc.
	 * @param timespan timespan.
	 * @param nroot no. of discretisation points.
	 */
	public void overrideStemArc(double timespan, int nroot) {
		// Rediscretise stem.
		DiscretisedArc stem = this.getOutgoingArcs(this.getSource()).iterator().next();
		double targett = stem.getTargetTime();
		stem.updateTimesAndDiscretisation(targett + timespan, targett, nroot);
	}
	
	/**
	 * Returns the stem arc.
	 * @return the stem arc.
	 */
	public DiscretisedArc getStemArc() {
		return this.getOutgoingArcs(this.getSource()).iterator().next();
	}
	
	/**
	 * Alias for <code>getDirectSuccessors(x)</code> that returns a convenience list instead.
	 * @param x source vertex.
	 * @return target vertices of x.
	 */
	public int[] getChildren(int x) {
		Set<DiscretisedArc> ch = this.topo.outgoingEdgesOf(x);
		if (ch == null || ch.isEmpty()) {
			return new int[] {};
		}
		Iterator<DiscretisedArc> it = ch.iterator();
		if (ch.size() == 1) {
			return new int[] { this.topo.getEdgeTarget(it.next()) };
		}
		return new int[] { this.topo.getEdgeTarget(it.next()), this.topo.getEdgeTarget(it.next()) };
	}
	
	/**
	 * Convenience method that returns the first child of an arc.
	 * No "leaf check".
	 * @param x source vertex.
	 * @return one target vertex of x, no guarantee on which if there are multiple children.
	 */
	public int getChild(int x) {
		return super.getDirectSuccessors(x).iterator().next();
	}
	
	/**
	 * Returns a convenience list of parent vertices.
	 * @param x target vertex.
	 * @return source vertices of x.
	 */
	public int[] getParents(int x) {
		Set<DiscretisedArc> pars = this.topo.incomingEdgesOf(x);
		if (pars == null || pars.isEmpty()) {
			return new int[] {};
		}
		Iterator<DiscretisedArc> it = pars.iterator();
		if (pars.size() == 1) {
			return new int[] { this.topo.getEdgeSource(it.next()) };
		}
		return new int[] { this.topo.getEdgeSource(it.next()), this.topo.getEdgeSource(it.next()) };
	}

	/**
	 * Alias to isSink().
	 * @param x the vertex.
	 * @return true if leaf; false if interior vertex.
	 */
	public boolean isLeaf(int x) {
		return super.isSink(x);
	}

	/**
	 * Returns the leaves.
	 * @return the leaves.
	 */
	public List<Integer> getLeaves() {
		ArrayList<Integer> leaves = new ArrayList<Integer>(super.getSinks());
		return leaves;
	}
}
