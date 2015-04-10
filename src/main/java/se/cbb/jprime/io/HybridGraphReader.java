package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import se.cbb.jprime.topology.HybridGraph;

/**
 * Reads a GML file containing a hybrid graph w. times.
 * Might be replaced with some suitable factory pattern in the future.
 * 
 * @author Joel Sjöstrand.
 */
public class HybridGraphReader {

	/**
	 * Reads a hybrid graph from file.
	 * @param f the file.
	 * @param nmin min number of discretisation points per arc.
	 * @param nmax max number of discretisation points per arc.
	 * @param deltat approximate (never exceeded) discretisation timestep.
	 * @param nroot overriding number of discretisation points for the root stem arc.
	 * @return the graph.
	 * @throws GMLIOException.
	 * @throws IOException.
	 */
	public static HybridGraph readHybridGraph(File f, int nmin, int nmax, double deltat, int nroot) throws GMLIOException, IOException {
		GMLGraph g = GMLFactory.getGraphs(GMLReader.readGML(f)).get(0);
		return new HybridGraph(g, nmin, nmax, deltat, nroot);
	}
}