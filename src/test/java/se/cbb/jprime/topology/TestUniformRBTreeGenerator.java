package se.cbb.jprime.topology;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestUniformRBTreeGenerator {
	
	@Test
	public void test() {
		List<String> n = Arrays.asList("a", "b", "c", "d", "e");
		PRNG prng = new PRNG();
		Pair<RBTree, NamesMap> TNames = UniformRBTreeGenerator.createUniformTree("TestTree", n, prng);
		RBTree T = TNames.first;
		NamesMap names = TNames.second;
		assertEquals(5, names.getNames(false).size());
		assertEquals(4, names.getVertex("e"));
		assertEquals(5, T.getNoOfLeaves());
		assertEquals(8, T.getRoot());
		assertTrue(T.getParent(0) >= 5);
	}
	
}
