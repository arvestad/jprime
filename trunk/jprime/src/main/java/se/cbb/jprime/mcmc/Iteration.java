package se.cbb.jprime.mcmc;

import java.util.LinkedList;

/**
 * Holds the current and total number of iterations k for e.g. an MCMC chain.
 * Iterations typically range between 0 and k-1.
 * Other objects may subscribe as listeners to increments of this object.
 * 
 * @author Joel Sjöstrand.
 */
public class Iteration {

	/** The total number of iterations. */
	private int totalNoOfIterations;
	
	/** The current iteration. */
	private int currentIteration;
	
	/** Subscribers to changes to this object. */
	private LinkedList<IterationListener> listeners;
	
	/** Flag determining whether listeners should be notified or not. */
	private boolean notifyListeners;
	
	/**
	 * Constructor. Sets the current iteration to 0 by default.
	 * @param totalNoOfIterations the total number of iterations.
	 */
	public Iteration(int totalNoOfIterations) {
		this(totalNoOfIterations, 0);
	}
	
	/**
	 * Constructor.
	 * @param noOfIterations the total number of iterations.
	 * @param currentIteration the initial iteration. Must be >=0.
	 */
	public Iteration(int totalNoOfIterations, int initialIteration) {
		if (initialIteration < 0 || initialIteration >= totalNoOfIterations) {
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
	 * Returns the total number of iterations.
	 * @return the number of iterations.
	 */
	public int getTotalNoOfIterations() {
		return this.totalNoOfIterations;
	}
	
	/**
	 * Returns the current iteration.
	 * @return the iteration.
	 */
	public int getCurrentIteration() {
		return this.currentIteration;
	}
	
	/**
	 * Increments the current iteration. If this would reach or exceed
	 * the total number of iterations, no increment is performed and
	 * false is returned. Listeners are notified synchronously before
	 * a call is returned.
	 * @return true if incremented and listeners potentially notified; false if max number reached.
	 */
	public boolean increment() {
		if (this.currentIteration >= this.totalNoOfIterations - 1) {
			return false;
		}
		this.currentIteration++;
		if (this.notifyListeners) {
			for (IterationListener i : this.listeners) {
				i.wasIncremented(this);
			}
		}
		return true;
	}	
	
}

