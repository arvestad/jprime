package se.cbb.jprime.consensus.day;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.cbb.jprime.consensus.day.ClusterTablePSWTree;
import se.cbb.jprime.consensus.day.RobinsonFoulds;
import se.cbb.jprime.consensus.day.TemplatedPSWTree;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeFactory;

/**
 * Unit test case for Robinson-Foulds.
 * 
 * @author Joel Sjöstrand.
 */
public class TestRobinsonFoulds {

	@Test
	public void testRFDistance() throws Exception {
		
		// Tests two bifurcating trees.
		String s1 = "(((root,A),((B,C),(D,E))),(F,G));";
		String s2 = "(((root,F),(A,G)),((B,C),(D,E)));";
		NewickTree n1 = NewickTreeReader.readTree(s1, false);
		NewickTree n2 = NewickTreeReader.readTree(s2, false);
		Pair<RBTree, StringMap> t1 = RBTreeFactory.createTreeAndNames(n1, "t1");
		Pair<RBTree, StringMap> t2 = RBTreeFactory.createTreeAndNames(n2, "t2");
		ClusterTablePSWTree d1 = new ClusterTablePSWTree(t1.first, t1.second, true);
		TemplatedPSWTree d2 = new TemplatedPSWTree(t2.first, t2.second, d1);
		
		// Verify when to treat as unrooted.
		assertEquals(2, RobinsonFoulds.computeAsymmetricDistance(d2));
		assertEquals(4, RobinsonFoulds.computeDistance(t1.first, t1.second, t2.first, t2.second, true));
		
		// Verify distance when treated as rooted.
		assertEquals(6, RobinsonFoulds.computeDistance(t1.first, t1.second, t2.first, t2.second, false));
	}
}
