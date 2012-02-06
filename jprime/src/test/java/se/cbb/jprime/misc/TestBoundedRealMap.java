package se.cbb.jprime.misc;

import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;

import se.cbb.jprime.math.PRNG;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestBoundedRealMap {

	
	@Test
	public void testSingle() {
		BoundedRealMap<String> map = new BoundedRealMap<String>(10, true);
		map.put(345.4543, "single");
		map.get(345.4543);
		assertEquals("single", map.get(345.4543));
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testOverfulfilled() {
		BoundedRealMap<String> map = new BoundedRealMap<String>(8, true);
		PRNG prng = new PRNG(new BigInteger("-112422468083609399096200301406773480499"));
		double d = 0.0001234567;
		double df = d;
		for (int i = 0; i < 7; ++i) {
			map.put(df, "" + prng.nextGaussian());
			//System.out.println(map.getValuesChronologically(true));
			df *= 13.0;
		}
		for (int i = 7; i < 12; ++i) {
			map.put(df, "" + prng.nextGaussian());
			//System.out.println(map.getValuesChronologically(true));
			df *= 13.0;
			String s = map.get(d);
			//System.out.print(map.getValuesChronologically(true) + "\t");
			String s2 = map.get(d * 13.0);
			//System.out.println(map.getValuesChronologically(true));
		}
		assertEquals(8, map.size());
		
	}
	
}
