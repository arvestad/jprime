package se.cbb.jprime.io;

import org.junit.* ;

import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestSampleDoubleArray {

	@Test
	public void test() {
		double[] ba1 = new double[]{4.4, 5.5, 1.0E-13, -2.0E-34, 7.99E23, -5.0E56};
		double[] ba2 = null;
		double[] ba3 = new double[]{};
		assertEquals("[4.4, 5.5, 1.0E-13, -2.0E-34, 7.99E23, -5.0E56]", SampleDoubleArray.toString(ba1));
		assertEquals("null", SampleDoubleArray.toString(ba2));
		assertEquals("[]", SampleDoubleArray.toString(ba3));
		double[] ba = SampleDoubleArray.toDoubleArray(SampleDoubleArray.toString(ba1));
		assertEquals(4.4, ba[0], 1e-6);
		assertEquals(-2.0E-34, ba[3], 1e-6);
		ba = SampleDoubleArray.toDoubleArray(SampleDoubleArray.toString(ba2));
		assertEquals(null, ba);
		ba = SampleDoubleArray.toDoubleArray(SampleDoubleArray.toString(ba3));
		assertEquals(0, ba.length);
	}
	
}
