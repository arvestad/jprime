package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import se.cbb.jprime.io.GMLIOException;
import se.cbb.jprime.io.SampleGMLGraph;

/**
 * Implementation of an acyclic digraph (DAG). As is common for most JPrIME
 * topologies, vertices are referenced by their IDs, which must be labelled
 * uniquely as 0,...,|V(G)|-1, and
 * empty references are represented by DAG.NULL==-1. The edge class
 * is generic and may contain weights, times, and so forth as desired.
 * Loops are, by definition, prohibited, as are duplicate arcs. We allow multiple
 * components at the time being.
 * <p/>
 * Internally, the implementation makes use of a <code>SimpleDirectedGraph</code>
 * from the JGraphT package.
 * 
 * @author Joel Sjöstrand.
 */
public class DAG<E extends DefaultEdge> implements AcyclicDigraphParameter {

	/** Used to indicate null references. */
	public static final int NULL = AcyclicDigraph.NULL;
	
	/** Name. */
	protected String name;
	
	/** Underlying topology. */
	protected SimpleDirectedGraph<Integer, E> topo;

	/** Sources. Must be updated  when topology changes. */
	protected HashSet<Integer> sources;

	/** Sinks. Must be changed when topology changes. */
	protected HashSet<Integer> sinks;
	
	/** No. of vertices. Must be changed when topology changes. */
	protected int noOfVertices;
	
	/**
	 * Constructor for the time being. Might be replaced with suitable factory pattern in the future.
	 */
	public DAG(String name, SimpleDirectedGraph<Integer, E> topo) {
		this.name = name;
		this.topo = topo;
		this.update();
	}
	
	/**
	 * Constructor.
	 * @param name name.
	 */
	protected DAG(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getNoOfVertices() {
		return this.noOfVertices;
	}

	@Override
	public int getNoOfComponents() {
		return (new ConnectivityInspector<Integer, E>(this.topo)).connectedSets().size();
	}

	@Override
	public boolean isStronglyConnected() {
		return (new ConnectivityInspector<Integer, E>(this.topo)).isGraphConnected();
	}

	@Override
	public List<Set<Integer>> getComponents() {
		return (new ConnectivityInspector<Integer, E>(this.topo)).connectedSets();
	}

	@Override
	public Set<Integer> getSources() {
		return this.sources;
	}

	@Override
	public int getNoOfSources() {
		return this.sources.size();
	}

	@Override
	public Set<Integer> getSinks() {
		return this.sinks;
	}

	@Override
	public int getNoOfSinks() {
		return this.sinks.size();
	}

	@Override
	public boolean hasArc(int x, int y) {
		return (this.topo.getEdge(x, y) != null);
	}

	@Override
	public boolean hasPath(int x, int y) {
		List<E> p = DijkstraShortestPath.findPathBetween(this.topo, x, y);
		return (p == null ? false : true);
	}

	@Override
	public Set<Integer> getDirectSuccessors(int x) {
		Set<E> es = this.topo.outgoingEdgesOf(x);
		HashSet<Integer> vs = new HashSet<Integer>(es.size());
		for (E e : es) {
			vs.add(this.topo.getEdgeTarget(e));
		}
		return vs;
	}

	@Override
	public int getNoOfDirectSuccessors(int x) {
		return this.topo.outDegreeOf(x);
	}

	@Override
	public Set<Integer> getSuccessors(int x) {
		HashSet<Integer> succ = new HashSet<Integer>();
		BreadthFirstIterator<Integer, E> bfsit = new BreadthFirstIterator<Integer, E>(this.topo, x);
		while (bfsit.hasNext()) {
			succ.add(bfsit.next());
		}
		return succ;
	}

	@Override
	public int getNoOfSuccessors(int x) {
		BreadthFirstIterator<Integer, E> bfsit = new BreadthFirstIterator<Integer, E>(this.topo, x);
		int i = 0;
		while (bfsit.hasNext()) {
			i++;
			bfsit.next();
		}
		return i;
	}

	@Override
	public Set<Integer> getSuccessorSinks(int x) {
		HashSet<Integer> succ = new HashSet<Integer>();
		BreadthFirstIterator<Integer, E> bfsit = new BreadthFirstIterator<Integer, E>(this.topo, x);
		while (bfsit.hasNext()) {
			int y = bfsit.next();
			if (this.topo.outDegreeOf(y) == 0) {
				succ.add(y);
			}
		}
		return succ;
	}

	@Override
	public int getNoOfSuccessorSinks(int x) {
		int i = 0;
		BreadthFirstIterator<Integer, E> bfsit = new BreadthFirstIterator<Integer, E>(this.topo, x);
		while (bfsit.hasNext()) {
			int y = bfsit.next();
			if (this.topo.outDegreeOf(y) == 0) {
				i++;
			}
		}
		return i;
	}

	@Override
	public boolean isSource(int x) {
		return this.sources.contains(x);
	}

	@Override
	public boolean isSink(int x) {
		return this.sinks.contains(x);
	}

	@Override
	public List<Integer> getTopologicalOrdering() {
		ArrayList<Integer> order = new ArrayList<Integer>(this.noOfVertices);
		TopologicalOrderIterator<Integer, E> topoit = new TopologicalOrderIterator<Integer, E>(this.topo);
		while (topoit.hasNext()) {
			order.add(topoit.next());
		}
		return order;
	}

	@Override
	public List<Integer> getTopologicalOrdering(int source) {
		throw new UnsupportedOperationException("Topological ordering of sub-graph not implemented yet.");
	}

	@Override
	public int getNoOfSubParameters() {
		return 1;
	}

	@Override
	public Class<?> getSampleType() {
		return SampleGMLGraph.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		try {
			return SampleGMLGraph.toString(this, false);
		} catch (GMLIOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the underlying topology. This can be used with JGraphT's
	 * extensive set of graph algorithms.
	 * @return the topology.
	 */
	public SimpleDirectedGraph<Integer, E> getUnderlyingTopology() {
		return this.topo;
	}
	
	/**
	 * Updates the internal representation of the graph. Must be invoked after
	 * topology changes. The DAG property is assumed to be preserved.
	 */
	public void update() {
		Set<Integer> vs = this.topo.vertexSet();
		this.noOfVertices = vs.size();
		if (this.sinks == null) {
			this.sinks = new HashSet<Integer>(this.noOfVertices);
		} else {
			this.sinks.clear();
		}
		if (this.sources == null) {
			this.sources = new HashSet<Integer>(this.noOfVertices);
		} else {
			this.sources.clear();
		}
		for (int x : vs) {
			if (this.topo.inDegreeOf(x) == 0) {
				this.sources.add(x);
			}
			if (this.topo.outDegreeOf(x) == 0) {
				this.sinks.add(x);
			}
		}
	}
	
	/**
	 * Returns the incoming arcs of a vertex.
	 * @param x the vertex.
	 * @return the arcs.
	 */
	public Set<E> getIncomingArcs(int x) {
		return this.topo.incomingEdgesOf(x);
	}
	
	/**
	 * Returns the outgoing arcs of a vertex.
	 * @param x the vertex.
	 * @return the arcs.
	 */
	public Set<E> getOutgoingArcs(int x) {
		return this.topo.outgoingEdgesOf(x);
	}
}
