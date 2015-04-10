package se.cbb.jprime.io;

import org.jgrapht.graph.DefaultEdge;

import se.cbb.jprime.topology.DAG;

/**
 * Sample type for GML graphs.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleGMLGraph implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "GMLGraph";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a DAG to a GML string.
	 * @param g the graph.
	 * @return the string.
	 * @throws GMLIOException.
	 */
	public static String toString(DAG<? extends DefaultEdge> g, boolean singleLine) throws GMLIOException {
		return GMLWriter.write(g, true);
	}

	/**
	 * Converts a GML graph to a GML string.
	 * @param g the graph.
	 * @return the string.
	 * @throws GMLIOException.
	 */
	public static String toString(GMLGraph g, boolean singleLine) throws GMLIOException {
		return GMLWriter.write(g, true);
	}
	
	/**
	 * Reads a GML graph sample.
	 * @param s the string.
	 * @return the graph.
	 * @throws GMLIOException.
	 */
	public static GMLGraph toGMLGraph(String s) throws GMLIOException {
		return new GMLGraph(GMLReader.readGML(s));
	}
}
