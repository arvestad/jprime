package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.TreeSet;

import org.junit.* ;

import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;
import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestPrIMENewickTreeReader {

	@Test
	public void readSingleTreeFromFile() throws IOException, NewickIOException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		assertEquals(14, tree.getNoOfLeaves());
		assertTrue(tree.hasProperty(MetaProperty.BRANCH_LENGTHS));
		String[] names = tree.getVertexNames(true);
		assertEquals("AYWBP", names[0]);
		assertEquals("URPAR", names[21]);
		NamesMap namesMap = tree.getVertexNamesMap(true, "T.names");
		assertEquals("AYWBP", namesMap.get(0));
		assertEquals(0, namesMap.getVertex("AYWBP"));
		assertEquals(21, namesMap.getVertex("URPAR"));
		TreeSet<String> namesSorted = namesMap.getNamesSorted(true);
		assertEquals("AYWBP", namesSorted.first());
		assertEquals("URPAR", namesSorted.last());
		assertEquals(14, namesSorted.size());
		namesMap = tree.getVertexNamesMap(false, "T.names");
		assertNotSame(14, namesMap.getNamesSorted(false).size());
		assertEquals(14, namesMap.getNamesSorted(true).size());
	}
	
	@Test
	public void readDiscretizedTree() throws IOException, NewickIOException, TopologyException {
		String nwt = "(((vv_acarolinensis3:0.0[&&PRIME ID=9 NT=0.0 DISCTIMES=(0.0, 0.012666666666666666, 0.038, 0.06333333333333332, 0.08866666666666667, 0.11399999999999999, 0.13933333333333334, 0.16466666666666666, 0.19, 0.21533333333333332, 0.24066666666666667, 0.266, 0.29133333333333333, 0.31666666666666665, 0.34199999999999997, 0.36733333333333335, 0.38)],(mm_ggallus2:0.0[&&PRIME ID=10 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)],mm_tguttata4:0.0[&&PRIME ID=11 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)]):0.15[&&PRIME ID=12 NT=0.15 DISCTIMES=(0.15, 0.15958333333333333, 0.17875, 0.19791666666666666, 0.21708333333333335, 0.23625000000000002, 0.2554166666666667, 0.27458333333333335, 0.29375, 0.3129166666666667, 0.33208333333333334, 0.35125, 0.37041666666666667, 0.38)]):0.38[&&PRIME ID=13 NT=0.38 DISCTIMES=(0.38, 0.39, 0.41000000000000003, 0.43, 0.45, 0.47, 0.48)],(((mm_cfamiliaris2:0.0[&&PRIME ID=2 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)],(mm_hsapiens5:0.0[&&PRIME ID=4 NT=0.0 DISCTIMES=(0.0, 0.009285714285714286, 0.027857142857142858, 0.04642857142857143, 0.065, 0.08357142857142857, 0.10214285714285715, 0.12071428571428572, 0.13)],mm_mmusculus3:0.0[&&PRIME ID=3 NT=0.0 DISCTIMES=(0.0, 0.009285714285714286, 0.027857142857142858, 0.04642857142857143, 0.065, 0.08357142857142857, 0.10214285714285715, 0.12071428571428572, 0.13)]):0.13[&&PRIME ID=5 NT=0.13 DISCTIMES=(0.13, 0.1325, 0.1375, 0.14250000000000002, 0.14750000000000002, 0.15)]):0.15[&&PRIME ID=6 NT=0.15 DISCTIMES=(0.15, 0.16, 0.18, 0.2, 0.22, 0.24, 0.26, 0.28, 0.3, 0.31)],mm_mdomestica2:0.0[&&PRIME ID=1 NT=0.0 DISCTIMES=(0.0, 0.010333333333333333, 0.031, 0.051666666666666666, 0.07233333333333333, 0.093, 0.11366666666666667, 0.13433333333333333, 0.155, 0.17566666666666667, 0.19633333333333333, 0.217, 0.23766666666666666, 0.2583333333333333, 0.279, 0.29966666666666664, 0.31)]):0.31[&&PRIME ID=7 NT=0.31 DISCTIMES=(0.31, 0.3175, 0.3325, 0.3475, 0.3625, 0.37)],mm_oanatinus4:0.0[&&PRIME ID=0 NT=0.0 DISCTIMES=(0.0, 0.012333333333333333, 0.037, 0.06166666666666667, 0.08633333333333333, 0.111, 0.13566666666666666, 0.16033333333333333, 0.185, 0.20966666666666667, 0.23433333333333334, 0.259, 0.2836666666666667, 0.30833333333333335, 0.333, 0.3576666666666667, 0.37)]):0.37[&&PRIME ID=8 NT=0.37 DISCTIMES=(0.37, 0.37916666666666665, 0.3975, 0.41583333333333333, 0.43416666666666665, 0.4525, 0.4708333333333333, 0.48)]):0.48[&&PRIME ID=14 NT=0.48 DISCTIMES=(0.48, 0.4973333333333333, 0.532, 0.5666666666666667, 0.6013333333333333, 0.636, 0.6706666666666666, 0.7053333333333334, 0.74, 0.7746666666666666, 0.8093333333333332, 0.844, 0.8786666666666667, 0.9133333333333333, 0.948, 0.9826666666666666, 1.0)],vv_tnigroviridis3:0.0[&&PRIME ID=15 NT=0.0 DISCTIMES=(0.0, 0.03333333333333333, 0.1, 0.16666666666666666, 0.23333333333333334, 0.3, 0.36666666666666664, 0.43333333333333335, 0.5, 0.5666666666666667, 0.6333333333333333, 0.7, 0.7666666666666666, 0.8333333333333334, 0.9, 0.9666666666666667, 1.0)]):0.5[&&PRIME ID=16 NT=1.0 DISCTIMES=(1.0, 1.025, 1.075, 1.125, 1.175, 1.225, 1.275, 1.325, 1.375, 1.425, 1.475, 1.5)][&&PRIME NAME=HostTree DISCTYPE=RBTreeArcDiscretiser NMIN=4 NMAX=15 DELTAT=0.02 NROOT=10];";
		PrIMENewickTree rawt = PrIMENewickTreeReader.readTree(nwt, false, true);
		assertTrue(rawt.hasProperty(MetaProperty.BRANCH_LENGTHS));
		assertTrue(rawt.hasProperty(MetaProperty.VERTEX_NUMBERS));
		assertTrue(rawt.hasProperty(MetaProperty.VERTEX_TIMES));
		assertTrue(rawt.hasProperty(MetaProperty.VERTEX_DISC_TIMES));
		assertTrue(rawt.hasProperty(MetaProperty.TREE_NAME));
		assertTrue(rawt.hasProperty(MetaProperty.TREE_N_MIN));
		assertTrue(rawt.hasProperty(MetaProperty.TREE_N_MAX));
		assertTrue(rawt.hasProperty(MetaProperty.TREE_N_ROOT));
		assertTrue(rawt.hasProperty(MetaProperty.TREE_DELTA_T));
		RBTree t = new RBTree(rawt, rawt.getTreeName());
		NamesMap names = rawt.getVertexNamesMap(true, "Names");
		TimesMap times = rawt.getTimesMap("Times");
		int nmin = rawt.getTreeNMin();
		int nmax = rawt.getTreeNMax();
		int nroot = rawt.getTreeNRoot();
		double deltat = rawt.getTreeDeltaT();
		RBTreeArcDiscretiser disct = new RBTreeArcDiscretiser(t, names, times, nmin, nmax, deltat, nroot);
		//System.out.println(disct);
	}
	
}
