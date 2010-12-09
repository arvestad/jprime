package se.cbb.jprime.topology;

/**
 * Specialisation of a DoubleMap. Holds times for each vertex/arc of an ultrametric tree or graph.
 * Provides access to both absolute times of vertices and time span of arcs.
 * 
 * @author Joel Sjöstrand.
 */
public class TimesMap extends DoubleMap {

	/**
	 * Constructor.
	 * TODO: Implement.
	 * @param name
	 * @param vals
	 */
	public TimesMap(String name, double[] vertexTimes, double[] arcTimes) {
		super(name, vertexTimes);
		// TODO Auto-generated constructor stub
	}

}
