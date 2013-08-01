package se.cbb.jprime.math;

import java.math.BigInteger;
import java.util.Arrays;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

import se.cbb.jprime.mcmc.InfoProvider;

/**
 * Extension of a Mersenne twister pseudo-random number generator.
 * In the future, may be used for adding functionality like specialised serialisation, 
 * statistics, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class PRNG extends MersenneTwisterRNG implements InfoProvider {

	/** Eclipse-generated serial version UID. */
	private static final long serialVersionUID = 310669248550266600L;
	
	/**
	 * Makes sure the byte array seed is 16-bytes.
	 * @param seed the seed.
	 * @return a 16-byte version, possibly truncated.
	 */
	public static byte[] get16bytes(byte[] seed) {
		byte[] sixteen = new byte[16];
		int from = Math.max(0, seed.length - 16);
		int to = Math.max(0, 16 - seed.length);
		int length = Math.min(16, seed.length);
		System.arraycopy(seed, from, sixteen, to, length);
		return sixteen;
	}
	
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
		super(get16bytes(seed));
	}

	/**
	 * Constructor. Uses the specified seed data by converting the
	 * int to a byte array.
	 * @param seed the seed data.
	 */
	public PRNG(BigInteger seed) {
		super(get16bytes(seed.toByteArray()));
	}
	
	/**
	 * Constructor. Uses the specified seed data by converting the
	 * int to a byte array.
	 * @param seed the seed data.
	 */
	public PRNG(int seed) {
		super(get16bytes((new BigInteger(seed + "")).toByteArray()));
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

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(prefix).append("MERSENNE-TWISTER PRNG\n");
		sb.append(prefix).append("Seed as integer: ").append(new BigInteger(this.getSeed())).append('\n');
		sb.append(prefix).append("Seed as byte-array: ").append(Arrays.toString(this.getSeed())).append('\n');
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return (prefix + "MERSENNE-TWISTER PRNG\n");
	}
	
	/**
	 * Returns the seed as a big integer.
	 * @return the seed.
	 */
	public BigInteger getSeedAsBigInteger() {
		return new BigInteger(super.getSeed());
	}
	
//	@Override
//	public double nextDouble() {
//		double d = super.nextDouble();
//		System.out.println("nextDouble(): " + d);
//		return d;
//	}
//	
//	@Override
//	public int nextInt() {
//		int i = super.nextInt();
//		System.out.println("nextInt(): " + i);
//		return i;
//	}
//	
//	@Override
//	public int nextInt(int n) {
//		int i = super.nextInt(n);
//		System.out.println("nextInt(n): " + i);
//		return i;
//	}
//	
//	@Override
//	public boolean nextBoolean() {
//		boolean b = super.nextBoolean();
//		System.out.println("nextBoolean(): " + b);
//		return b;
//	}
//	
//	@Override
//	public double nextGaussian() {
//		double b = super.nextGaussian();
//		System.out.println("nextGaussian(): " + b);
//		return b;
//	}
//	
//	@Override
//	public float nextFloat() {
//		float b = super.nextFloat();
//		System.out.println("nextFloat(): " + b);
//		return b;
//	}
//	
//	@Override
//	public long nextLong() {
//		long b = super.nextLong();
//		System.out.println("nextLong(): " + b);
//		return b;
//	}
//	
//	@Override
//	public void nextBytes(byte[] bytes) {
//		super.nextBytes(bytes);
//		System.out.println("nextBytes(bytes): ");
//	}
}
