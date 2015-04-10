package se.cbb.jprime.topology;

import org.jgrapht.graph.DefaultEdge;

/**
 * Adds discretisation points to a <code>DefaultArc</code> of the JGraphT package.
 * An arc is split into
 * a number of equidistant slices. This is governed by a user-specified
 * minimum number of slices per edge, <i>nmin</i>, a maximum number, <i>nmax</i>, and an
 * approximate timestep, <i>deltat</i>, (which is never exceeded). The number of slices
 * of an arc with timespan, <i>t</i>, will then be
 * min(max(<i>nmin</i>,ceil(<i>t</i>/<i>deltat</i>)),<i>nmax</i>).
 * 
 * @author Joel Sjöstrand.
 */
public class DiscretisedArc extends DefaultEdge {

	/** Tolerance for arc time discrepancy. */
	public static final double EPS = 1e-10;
	
	/** Eclipse-generated UID. */
	private static final long serialVersionUID = 2304554207421769088L;
	
	/**
	 * Discretisation times thus:
	 * <ul>
	 * <li>Index 0: head (target) of arc.</li>
	 * <li>Index 1,..,k: discretisation slice midpoints from head to tail.</li>
	 * <li>Index k+1: tail (source) of arc.</li>
	 * </ul>
	 */
	private double[] times;
	
	/**
	 * Constructor. An arc with zero length will by default have no discretisation intervals.
	 * @param sourcet source vertex (tail) absolute time.
	 * @param targett target vertex (head) absolute time.
	 * @param nmin minimum number of discretisation intervals.
	 * @param nmax maximum number of discretisation intervals.
	 * @param deltat max allowed timespan of discretisation interval.
	 */
	public DiscretisedArc(double sourcet, double targett, int nmin, int nmax, double deltat) {
		super();
		double at = sourcet - targett;
		if (at < 0) {
			throw new IllegalArgumentException("Invalid arc time: Timespan is less than 0. Time of source must be greater than or equal to time of target.");
		}
		if (Math.abs(at) <= EPS) {
			// No discretisation interval.
			this.times = new double[] { sourcet, sourcet };
		} else {
			int no = Math.min(Math.max(nmin, (int) Math.ceil(at / deltat)), nmax);
			this.times = new double[no + 2];   // Endpoints also included.
			double timestep = at / no;
			this.times[0] = targett;		// Head of arc at first index.
			for (int xx = 1; xx <= no; ++xx) {
				this.times[xx] = targett + timestep * (xx - 0.5);
			}
			this.times[no+1] = sourcet;	// Tail of arc at last index.
		}
	}
	
	/**
	 * @return the arc timespan.
	 */
	public double getArcTime() {
		// This must equal (times[last]-times[0])!
		return (this.times[this.times.length-1] - this.times[0]);
	}

	/**
	 * Returns the number of discretisation slices.
	 * @return the number of discretisation slices.
	 */
	public int getNoOfSlices() {
		return (this.times.length - 2);
	}
	
	/**
	 * Returns the absolute time (vertex time) of the source (tail).
	 * @return the vertex time.
	 */
	public double getSourceTime() {
		return this.times[this.times.length - 1];
	}
	
	/**
	 * Returns the absolute time (vertex time) of the target (head).
	 * @return the vertex time.
	 */
	public double getTargetTime() {
		return this.times[0];
	}
	
	/**
	 * Returns the discretisation times thus:
	 * <ul>
	 * <li>Index 0: head of arc.</li>
	 * <li>Index 1,..,k: discretisation slice midpoints from head to tail.</li>
	 * <li>Index k+1: tail of arc.</li>
	 * </ul>
	 * @return the discretisation times.
	 */
	public double[] getDiscretisationTimes() {
		return this.times;
	}
	
	/**
	 * Returns the discretisation interval time span of an arc slice.
	 * @return the discretisation interval time span.
	 */
	public double getSliceTime() {
		return (this.getArcTime() / this.getNoOfSlices());
	}
	
	/**
	 * Updates the discretisation with a predefined number of slices.
	 * An arc with time span 0 will by default get 0 slices.
	 * @param noOfSlices the number of slices.
	 */
	public void updateDiscretisation(int noOfSlices) {
		double targett = this.times[0];
		double sourcet = this.times[this.times.length - 1];
		this.updateTimesAndDiscretisation(sourcet, targett, noOfSlices);
	}
	
	/**
	 * Updates the times of the arc and discretisation with a predefined number of slices.
	 * An arc with time span 0 will by default get 0 slices.
	 * All corresponding vertex times are assumed to have been (or be) updated
	 * in agreement with this change.
	 * @param sourcet source vertex time (greater than target's).
	 * @param targett target vertex time (less than source's).
	 * @param noOfSlices the number of slices.
	 */
	public void updateTimesAndDiscretisation(double sourcet, double targett, int noOfSlices) {
		double at = sourcet - targett;
		if (Math.abs(at) <= EPS) {
			this.times = new double[] { sourcet, sourcet };
		} else {
			this.times = new double[noOfSlices + 2];   // Endpoints also included.
			double timestep = at / noOfSlices;
			this.times[0] = targett;		// Head of arc at first index.
			for (int xx = 1; xx <= noOfSlices; ++xx) {
				this.times[xx] = targett + timestep * (xx - 0.5);
			}
			this.times[noOfSlices+1] = sourcet;	// Tail of arc at last index.
		}
	}
}
