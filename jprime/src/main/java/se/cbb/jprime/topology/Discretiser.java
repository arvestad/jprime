package se.cbb.jprime.topology;

/**
 * Simple base interface for "discretizers"; objects which govern the
 * discretization of e.g. a topology.
 * 
 * @author Joel Sjöstrand.
 */
public interface Discretiser {

	/**
	 * Returns the number of discretization points for some entity x,
	 * e.g. an arc in a DAG.
	 * @param x the entity.
	 * @return the number of points.
	 */
	public int getNoOfPts(int x);
}
