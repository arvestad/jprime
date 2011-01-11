package se.cbb.jprime.prm;

/**
 * Represents a parent-child dependency relationship between
 * two probabilistic PRM attributes. The two attributes may
 * belong to the same class, or belong to different classes connected
 * through a 'slot chain' (a chain of PRM relations).
 * 
 * @author Joel Sjöstrand.
 */
public class Dependency {

	/** Parent. */
	private ProbabilisticAttribute parent;
	
	/** Child. */
	private ProbabilisticAttribute child;
	
	
	
}
