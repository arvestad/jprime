package se.cbb.jprime.math;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

/**
 * Extension of a Mersenne twister pseudo-random number generator.
 * In the future, may be used for adding functionality like specialised serialisation, 
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
	 * Constructor. Uses the specified seed data by converting the
	 * int to a byte array.
	 * @param seed the seed data.
	 */
	public PRNG(int seed) {
		super(new byte[] {(byte)(seed >>> 24), (byte)(seed >>> 16), (byte)(seed >>> 8), (byte)seed});
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
