package se.cbb.jprime.io;

import org.junit.* ;

import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestSampleIntArray {

	@Test
	public void test() {
		int[] ba1 = new int[]{-30, -0, -4444134, 234};
		int[] ba2 = null;
		int[] ba3 = new int[]{};
		assertEquals("[-30, 0, -4444134, 234]", SampleIntArray.toString(ba1));
		assertEquals("null", SampleIntArray.toString(ba2));
		assertEquals("[]", SampleIntArray.toString(ba3));
		int[] ba = SampleIntArray.toIntArray(SampleIntArray.toString(ba1));
		assertEquals(-30, ba[0]);
		assertEquals(0, ba[1]);
		assertEquals(-4444134, ba[2]);
		assertEquals(234, ba[3]);
		ba = SampleIntArray.toIntArray(SampleIntArray.toString(ba2));
		assertEquals(null, ba);
		ba = SampleIntArray.toIntArray(SampleIntArray.toString(ba3));
		assertEquals(0, ba.length);
	}
	
}
