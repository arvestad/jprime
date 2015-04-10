package se.cbb.jprime.io;

import org.junit.* ;

import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestSampleBooleanArray {

	@Test
	public void test() {
		boolean[] ba1 = new boolean[]{false, true, true, false};
		boolean[] ba2 = null;
		boolean[] ba3 = new boolean[]{};
		assertEquals("[false, true, true, false]", SampleBooleanArray.toString(ba1));
		assertEquals("null", SampleBooleanArray.toString(ba2));
		assertEquals("[]", SampleBooleanArray.toString(ba3));
		boolean[] ba = SampleBooleanArray.toBooleanArray(SampleBooleanArray.toString(ba1));
		assertEquals(false, ba[0]);
		assertEquals(true, ba[1]);
		assertEquals(true, ba[2]);
		assertEquals(false, ba[3]);
		ba = SampleBooleanArray.toBooleanArray(SampleBooleanArray.toString(ba2));
		assertEquals(null, ba);
		ba = SampleBooleanArray.toBooleanArray(SampleBooleanArray.toString(ba3));
		assertEquals(0, ba.length);
	}
	
}
