package se.cbb.jprime.consensus.day;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.cbb.jprime.consensus.day.ClusterTablePSWTree;
import se.cbb.jprime.consensus.day.RobinsonFoulds;
import se.cbb.jprime.consensus.day.TemplatedPSWTree;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.topology.NamesMap;
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
		RBTree t1 = RBTreeFactory.createTree(n1, "t1");
		NamesMap names1 = n1.getVertexNamesMap(true);
		RBTree t2 = RBTreeFactory.createTree(n2, "t2");
		NamesMap names2 = n2.getVertexNamesMap(true);
		ClusterTablePSWTree d1 = new ClusterTablePSWTree(t1, names1, true);
		TemplatedPSWTree d2 = new TemplatedPSWTree(t2, names2, d1);
		
		// Verify when to treat as unrooted.
		assertEquals(2, RobinsonFoulds.computeAsymmetricDistance(d2));
		assertEquals(4, RobinsonFoulds.computeDistance(t1, names1, t2, names2, true));
		
		// Verify distance when treated as rooted.
		assertEquals(6, RobinsonFoulds.computeDistance(t1, names1, t2, names2, false));
	}
}
