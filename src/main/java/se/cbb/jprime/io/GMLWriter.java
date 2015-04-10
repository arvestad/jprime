package se.cbb.jprime.io;

import org.jgrapht.graph.DefaultEdge;

import se.cbb.jprime.topology.DAG;

/**
 * Creates GML output from graphs, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLWriter {

	/**
	 * Writes a DAG to GML.
	 * @param g the DAG.
	 * @param singleLine true for no line breaks.
	 * @return the GML.
	 */
	public static String write(DAG<? extends DefaultEdge> g, boolean singleLine) {
		throw new UnsupportedOperationException("GML writer not implemented yet!");
	}

	/**
	 * Writes a GML graph to GML.
	 * @param g the graph.
	 * @param singleLine true for no line breaks.
	 * @return the GML.
	 */
	public static String write(GMLGraph g, boolean singleLine) {
		throw new UnsupportedOperationException("GML writer not implemented yet!");
	}

}
