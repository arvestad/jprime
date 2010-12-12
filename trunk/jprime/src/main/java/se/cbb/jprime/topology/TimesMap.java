package se.cbb.jprime.topology;

/**
 * Specialisation of a DoubleMap. Holds times for each vertex/arc of an ultrametric tree or graph.
 * Provides access to both absolute times of vertices and time span of arcs.
 * 
 * @author Joel Sjöstrand.
 */
public class TimesMap extends DoubleMap {
	
	/**
	 * Constructor. One may input either vertex times (absolute times) or
	 * arc times. It is assumed that the times are in fact ultrametric.
	 * @param name the name of the map.
	 * @param times the times.
	 * @param areAbsolute true if the times refer to vertex times, false if they refer
	 *        to arc times.
	 * @param topTime a time predating the root. If not null, this will override
	 *        any such value already in the array.
	 */
	public TimesMap(String name, double[] times, boolean areAbsolute, Double topTime) {
		super(name, times.length);
		//TODO: Implement.
	}

}
