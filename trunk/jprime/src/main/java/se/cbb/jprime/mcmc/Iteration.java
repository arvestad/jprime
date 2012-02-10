package se.cbb.jprime.mcmc;

import java.util.LinkedList;

import se.cbb.jprime.io.SampleInt;
import se.cbb.jprime.io.Sampleable;

/**
 * Holds the current and total number of iterations k for e.g. an MCMC chain.
 * Iterations typically range between 0 and k, where 0 refers to a starting iteration (which
 * is not counted, but may very well be sampled).
 * Other objects may subscribe as listeners to increments of this object.
 * 
 * @author Joel Sjöstrand.
 */
public class Iteration implements Sampleable, InfoProvider {

	/** The total number of iterations, start iteration 0 excluded. */
	private int totalNoOfIterations;
	
	/** The current iteration. */
	private int currentIteration;
	
	/** Subscribers to changes to this object. */
	private LinkedList<IterationListener> listeners;
	
	/** Flag determining whether listeners should be notified or not. */
	private boolean notifyListeners;
	
	/**
	 * Constructor. Sets the current iteration to 0 by default.
	 * @param totalNoOfIterations the total number of iterations, start iteration 0 excluded.
	 */
	public Iteration(int totalNoOfIterations) {
		this(totalNoOfIterations, 0);
	}
	
	/**
	 * Constructor.
	 * @param totalNoOfIterations the total number of iterations, start iteration 0 excluded.
	 * @param currentIteration the initial iteration. Must be >= 0.
	 */
	public Iteration(int totalNoOfIterations, int initialIteration) {
		if (initialIteration < 0 || initialIteration > totalNoOfIterations) {
			throw new IllegalArgumentException("Initial iteration out of range.");
		}
		this.totalNoOfIterations = totalNoOfIterations;
		this.currentIteration = initialIteration;
		this.listeners = new LinkedList<IterationListener>();
		this.notifyListeners = true;
	}
	
	/**
	 * Adds a subscriber to changes of this object.
	 * @param listener the subscriber.
	 */
	public void addIterationListener(IterationListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Returns whether listeners are notified of changes to this object or not.
	 * @return true if notified; false if not notified.
	 */
	public boolean getNotificationStatus() {
		return this.notifyListeners;
	}
	
	/**
	 * Sets whether listeners should be notified of changes to this object or not.
	 * @param notifyListeners true if to be notified; false if not to be notified.
	 * @return the previous state (before setting the new value).
	 */
	public boolean setNotificationStatus(boolean notifyListeners) {
		boolean old = this.notifyListeners;
		this.notifyListeners = notifyListeners;
		return old;
	}
	
	/**
	 * Returns the total number of iterations, start iteration 0 excluded.
	 * @return the number of iterations.
	 */
	public int getTotalNoOfIterations() {
		return this.totalNoOfIterations;
	}
	
	/**
	 * Returns the current iteration.
	 * @return the iteration.
	 */
	public int getIteration() {
		return this.currentIteration;
	}
	
	/**
	 * Increments the current iteration. If this exceeds
	 * the total number of iterations, nothing happens and false is returned.
	 * Listeners are notified synchronously before a call is returned.
	 * @return true if total number not reached before incrementing; false if total number reached
	 *         before incrementing.
	 */
	public boolean increment() {
		if (this.currentIteration >= this.totalNoOfIterations) {
			return false;
		}
		this.currentIteration++;
		if (this.notifyListeners) {
			for (IterationListener i : this.listeners) {
				i.incrementPerformed(this.currentIteration, this.totalNoOfIterations);
			}
		}
		return true;
	}	
	
	/**
	 * Returns true if the iterator has not yet reached its end.
	 * @return true if it can be incremented, otherwise false.
	 */
	public boolean canIncrement() {
		return (this.currentIteration < this.totalNoOfIterations);
	}

	@Override
	public Class<?> getSampleType() {
		return SampleInt.class;
	}

	@Override
	public String getSampleHeader() {
		return "Iteration";
	}

	@Override
	public String getSampleValue() {
		return SampleInt.toString(this.currentIteration);
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("ITERATION\n");
		sb.append(prefix).append("Range: [").append(this.currentIteration).append(", ").append(this.totalNoOfIterations).append("]\n");
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return (prefix  + "ITERATION\n");
	}
	
}

