package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for LogDouble.
 * 
 * @author Joel Sjöstrand.
 */
public class TestLogDouble {

	private LogDouble z = new LogDouble(0.0);
	private LogDouble p1 = new LogDouble(0.0001);
	private LogDouble p2 = new LogDouble(0.00123);
	private LogDouble n1 = new LogDouble(-0.00345);
	private LogDouble n2 = new LogDouble(-0.0456);
	private LogDouble e = new LogDouble(Math.E);
	private LogDouble one = new LogDouble(1.0);
	private LogDouble two = new LogDouble(2.0);

	@Test
	public void testConstructors() {
		LogDouble p = new LogDouble(p1);
		assertEquals(-9.21034, p.getLogValue(), 1e-6);
		p = new LogDouble(-100.0, 1);
		assertEquals(-100.0, p.getLogValue(), 1e-6);
	}
	
	@Test
	public void testGetSign() {
		assertEquals(0, z.getSign());
		assertEquals(1, p1.getSign());
		assertEquals(-1, n1.getSign());
	}
	
	@Test
	public void testGetLogValue() {
		assertTrue(Double.isInfinite(z.getLogValue()) && z.getLogValue() < 0);
		assertEquals(-9.21034, p1.getLogValue(), 1e-6);
		assertEquals(-5.669381, n1.getLogValue(), 1e-6);
		assertEquals(1.0, e.getLogValue(), 1e-6);
		assertEquals(0.0, one.getLogValue(), 1e-6);
	}
	
	@Test
	public void testGetValue() {
		assertEquals(0.0, z.getValue(), 1e-6);
		assertEquals(0.0001, p1.getValue(), 1e-6);
		assertEquals(-0.00345, n1.getValue(), 1e-6);
		assertEquals(Math.E, e.getValue(), 1e-6);
		assertEquals(1.0, one.getValue(), 1e-6);
	}
	
	@Test
	public void testAdd() {
		LogDouble p = z.addToNew(p1);
		assertEquals(p1.getValue(), p.getValue(), 1e-6);
		p = p1.addToNew(z);
		assertEquals(p1.getValue(), p.getValue(), 1e-6);
		p = p1.addToNew(p2);
		assertEquals(p1.getValue() + p2.getValue(), p.getValue(), 1e-6);
		p = n1.addToNew(p2);
		assertEquals(n1.getValue() + p2.getValue(), p.getValue(), 1e-6);
		p = n1.addToNew(n2);
		assertEquals(n1.getValue() + n2.getValue(), p.getValue(), 1e-6);
	}
	
	@Test
	public void testSub() {
		LogDouble p = z.subToNew(p1);
		assertEquals(-p1.getValue(), p.getValue(), 1e-6);
		p = p1.subToNew(z);
		assertEquals(p1.getValue(), p.getValue(), 1e-6);
		p = p1.subToNew(p2);
		assertEquals(p1.getValue() - p2.getValue(), p.getValue(), 1e-6);
		p = n1.subToNew(p2);
		assertEquals(n1.getValue() - p2.getValue(), p.getValue(), 1e-6);
		p = n1.subToNew(n2);
		assertEquals(n1.getValue() - n2.getValue(), p.getValue(), 1e-6);
	}
	
	@Test
	public void testMult() {
		LogDouble p = z.multToNew(p1);
		assertEquals(0, p.getValue(), 1e-6);
		p = p1.multToNew(z);
		assertEquals(0, p.getValue(), 1e-6);
		p = p1.multToNew(p2);
		assertEquals(p1.getValue() * p2.getValue(), p.getValue(), 1e-6);
		p = n1.multToNew(p2);
		assertEquals(n1.getValue() * p2.getValue(), p.getValue(), 1e-6);
		p = n1.multToNew(n2);
		assertEquals(n1.getValue() * n2.getValue(), p.getValue(), 1e-6);
	}
	
	@Test
	public void testDiv() {
		LogDouble p = z.divToNew(p1);
		assertEquals(0, p.getValue(), 1e-6);
		p = p1.divToNew(p2);
		assertEquals(p1.getValue() / p2.getValue(), p.getValue(), 1e-6);
		p = n1.divToNew(p2);
		assertEquals(n1.getValue() / p2.getValue(), p.getValue(), 1e-6);
		p = n1.divToNew(n2);
		assertEquals(n1.getValue() / n2.getValue(), p.getValue(), 1e-6);
		p = p1.divToNew(n2);
		assertEquals(p1.getValue() / n2.getValue(), p.getValue(), 1e-6);
	}
	
	@Test
	public void testNeg() {
		LogDouble p = z.negToNew();
		assertEquals(0, p.getValue(), 1e-6);
		p = p1.negToNew();
		assertEquals(-p1.getValue(), p.getValue(), 1e-6);
		p = n1.negToNew();
		assertEquals(-n1.getValue(), p.getValue(), 1e-6);
	}
	
	@Test
	public void testPower() {
		LogDouble p = z.powToNew(2);
		assertEquals(0, p.getValue(), 1e-6);
		p = z.powToNew(0);
		assertEquals(1.0, p.getValue(), 1e-6);
		p = z.powToNew(-2);
		assertTrue(Double.isInfinite(p.getValue()) && p.getSign() == 1);
		p = p1.powToNew(2.3);
		assertEquals(6.309573e-10, p.getValue(), 1e-6);
		p = p1.powToNew(0.0);
		assertEquals(1.0, p.getValue(), 1e-6);
		p = p1.powToNew(-1.23);
		assertEquals(83176.38, p.getValue(), 1e-2);
		p = n1.powToNew(2);
		assertEquals(n1.getValue() * n1.getValue(), p.getValue(), 1e-6);
		p = n1.powToNew(0);
		assertEquals(1.0, p.getValue(), 1e-6);
		p = n1.powToNew(-2);
		assertEquals(84015.96, p.getValue(), 1e-2);
		p = n1.powToNew(-3);
		assertEquals(-24352453, p.getValue(), 1e-1);
	}
	
	@Test
	public void testExp() {
		LogDouble p = z.expToNew();
		assertEquals(1.0, p.getValue(), 1e-6);
		p = p1.expToNew();
		assertEquals(Math.exp(p1.getValue()), p.getValue(), 1e-6);
		p = two.expToNew();
		assertEquals(Math.exp(2), p.getValue(), 1e-6);
		p = n1.expToNew();
		assertEquals(Math.exp(n1.getValue()), p.getValue(), 1e-6);
	}
	
	@Test
	public void testLog() {
		LogDouble p = z.logToNew();
		assertTrue(Double.isInfinite(p.getValue()) && p.getSign() == -1);
		p = p1.logToNew();
		assertEquals(Math.log(p1.getValue()), p.getValue(), 1e-6);
		p = two.logToNew();
		assertEquals(Math.log(2), p.getValue(), 1e-6);
	}
	
	@Test
	public void testCompareTo() {
		assertEquals(0, z.compareTo(z));
		assertEquals(-1, z.compareTo(p1));
		assertEquals(1, z.compareTo(n1));
		assertEquals(0, p1.compareTo(p1));
		assertEquals(1, p1.compareTo(z));
		assertEquals(-1, p1.compareTo(p2));
		assertEquals(1, p2.compareTo(p1));
		assertEquals(1, p2.compareTo(n1));
		assertEquals(-1, p2.compareTo(two));
		assertEquals(0, n1.compareTo(n1));
		assertEquals(-1, n1.compareTo(z));
		assertEquals(1, n1.compareTo(n2));
		assertEquals(-1, n2.compareTo(n1));
		assertEquals(-1, n2.compareTo(p1));
	}
	
	@Test
	public void testGreaterThan() {
		assertFalse(z.greaterThan(z));
		assertFalse(z.greaterThan(p1));
		assertTrue(z.greaterThan(n1));
		assertFalse(p1.greaterThan(p1));
		assertTrue(p1.greaterThan(z));
		assertFalse(p1.greaterThan(p2));
		assertTrue(p2.greaterThan(p1));
		assertTrue(p2.greaterThan(n1));
		assertFalse(p2.greaterThan(two));
		assertFalse(n1.greaterThan(n1));
		assertFalse(n1.greaterThan(z));
		assertTrue(n1.greaterThan(n2));
		assertFalse(n2.greaterThan(n1));
		assertFalse(n2.greaterThan(p1));
	}
	
	@Test
	public void testGreaterThanOrEquals() {
		assertTrue(z.greaterThanOrEquals(z));
		assertFalse(z.greaterThanOrEquals(p1));
		assertTrue(z.greaterThanOrEquals(n1));
		assertTrue(p1.greaterThanOrEquals(p1));
		assertTrue(p1.greaterThanOrEquals(z));
		assertFalse(p1.greaterThanOrEquals(p2));
		assertTrue(p2.greaterThanOrEquals(p1));
		assertTrue(p2.greaterThanOrEquals(n1));
		assertFalse(p2.greaterThanOrEquals(two));
		assertTrue(n1.greaterThanOrEquals(n1));
		assertFalse(n1.greaterThanOrEquals(z));
		assertTrue(n1.greaterThanOrEquals(n2));
		assertFalse(n2.greaterThanOrEquals(n1));
		assertFalse(n2.greaterThanOrEquals(p1));
	}
	
	@Test
	public void testLessThan() {
		assertFalse(z.lessThan(z));
		assertFalse(z.lessThan(z));
		assertTrue(z.lessThan(p1));
		assertFalse(z.lessThan(n1));
		assertFalse(p1.lessThan(p1));
		assertFalse(p1.lessThan(z));
		assertTrue(p1.lessThan(p2));
		assertFalse(p2.lessThan(p1));
		assertFalse(p2.lessThan(n1));
		assertTrue(p2.lessThan(two));
		assertFalse(n1.lessThan(n1));
		assertTrue(n1.lessThan(z));
		assertFalse(n1.lessThan(n2));
		assertTrue(n2.lessThan(n1));
		assertTrue(n2.lessThan(p1));
	}
	
	@Test
	public void testLessThanOrEquals() {
		assertTrue(z.lessThanOrEquals(z));
		assertTrue(z.lessThanOrEquals(p1));
		assertFalse(z.lessThanOrEquals(n1));
		assertTrue(p1.lessThanOrEquals(p1));
		assertFalse(p1.lessThanOrEquals(z));
		assertTrue(p1.lessThanOrEquals(p2));
		assertFalse(p2.lessThanOrEquals(p1));
		assertFalse(p2.lessThanOrEquals(n1));
		assertTrue(p2.lessThanOrEquals(two));
		assertTrue(n1.lessThanOrEquals(n1));
		assertTrue(n1.lessThanOrEquals(z));
		assertFalse(n1.lessThanOrEquals(n2));
		assertTrue(n2.lessThanOrEquals(n1));
		assertTrue(n2.lessThanOrEquals(p1));
	}

	@Test
	public void testEquals() {
		assertTrue(z.equals(z));
		assertFalse(z.equals(p1));
		assertFalse(z.equals(n1));
		assertTrue(p1.equals(p1));
		assertFalse(p1.equals(z));
		assertFalse(p1.equals(p2));
		assertFalse(p2.equals(p1));
		assertFalse(p2.equals(n1));
		assertFalse(p2.equals(two));
		assertTrue(n1.equals(n1));
		assertFalse(n1.equals(z));
		assertFalse(n1.equals(n2));
		assertFalse(n2.equals(n1));
		assertFalse(n2.equals(p1));
		LogDouble p = new LogDouble(p1);
		assertEquals(p1, p);
	}
	
	@Test
	public void testToStringAndParse() {
		String sz = z.toString();
		String sp1 = p1.toString();
		String sn1 = n1.toString();
		String stwo = two.toString();
		//System.out.println(sz);
		//System.out.println(sp1);
		//System.out.println(sn1);
		//System.out.println(one);
		LogDouble cz = LogDouble.parseLogDouble(sz);
		LogDouble cp1 = LogDouble.parseLogDouble(sp1);
		LogDouble cn1 = LogDouble.parseLogDouble(sn1);
		LogDouble ctwo = LogDouble.parseLogDouble(stwo);
		assertEquals(z.getValue(), cz.getValue(), 1e-6);
		assertEquals(p1.getValue(), cp1.getValue(), 1e-6);
		assertEquals(n1.getValue(), cn1.getValue(), 1e-6);
		assertEquals(two.getValue(), ctwo.getValue(), 1e-6);
	}
	
}
