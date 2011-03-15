package se.cbb.jprime.math;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

/**
 * Extension of a Mersenne twister pseudo-random number generator.
 * Enables adding functionality like specialised serialisation, 
 * statistics, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class PRNG extends MersenneTwisterRNG {

	/** Eclipse-generated serial version UID. */
	private static final long serialVersionUID = 310669248550266600L;

	/**
	 * Constructor. Uses default seeding strategy.
	 */
	public PRNG() {
		super();
	}
	
	/**
	 * Constructor. Uses the specified seed data.
	 * @param seed the seed data.
	 */
	public PRNG(byte[] seed) {
		super(seed);
	}
	
	/**
	 * Constructor. Uses a seed using the specified seed
	 * generator.
	 * @param seedGenerator the seed generator.
	 * @throws SeedException.
	 */
	public PRNG(SeedGenerator seedGenerator) throws SeedException {
		super(seedGenerator);
	}
}
