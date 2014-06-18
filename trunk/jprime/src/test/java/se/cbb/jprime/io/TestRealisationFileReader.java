package se.cbb.jprime.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import org.junit.Test;

import se.cbb.jprime.io.UnparsedRealisation.Representation;
import se.cbb.jprime.topology.IntArrayMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestRealisationFileReader {

	@Test
	public void test() throws FileNotFoundException, NewickIOException, TopologyException {
	
		URL url = this.getClass().getResource("/phylogenetics/dlrs_realisations.real");
		File f = new File(url.getFile());
		RealisationFileReader reals = new RealisationFileReader(f, 0.25);
		assertEquals(73, reals.getNoOfRealisations());

		UnparsedRealisation real = reals.get(0);
		String srealplus = real.getStringRepresentation(Representation.REALISATION_PLUS);
		assertEquals("(((mm_cfamiliaris2_ENSCAFG00000005165:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(2,0)],mm_hsapiens5_ENSG00000167889:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(4,0)]):0.15[&&PRIME VERTEXTYPE=Speciation DISCPT=(6,0)],mm_mmusculus3_ENSMUSG00000043857:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(3,0)]):0.37916666666666665[&&PRIME VERTEXTYPE=Duplication DISCPT=(8,1)],((((mm_ggallus2_ENSGALG00000001740:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(10,0)],mm_tguttata4_ENSTGUG00000007773:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(11,0)]):0.15[&&PRIME VERTEXTYPE=Speciation DISCPT=(12,0)],vv_acarolinensis3_ENSACAG00000016117:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(9,0)]):0.38[&&PRIME VERTEXTYPE=Speciation DISCPT=(13,0)],mm_mdomestica2_ENSMODG00000001601:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(1,0)]):0.7746666666666666[&&PRIME VERTEXTYPE=Duplication DISCPT=(14,9)],(mm_oanatinus4_ENSOANG00000011572:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(0,0)],(vv_tnigroviridis3_ENSTNIG00000011598:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(15,0)],vv_tnigroviridis3_ENSTNIG00000019529:0.0[&&PRIME VERTEXTYPE=Leaf DISCPT=(15,0)]):0.8333333333333334[&&PRIME VERTEXTYPE=Duplication DISCPT=(15,13)]):1.025[&&PRIME VERTEXTYPE=Duplication DISCPT=(16,1)]):1.125[&&PRIME VERTEXTYPE=Duplication DISCPT=(16,3)]):1.425[&&PRIME VERTEXTYPE=Duplication DISCPT=(16,9)];", srealplus);
		String sreal = real.getStringRepresentation(Representation.REALISATION);
		assertEquals("(((mm_cfamiliaris2_ENSCAFG00000005165[&&PRIME DISCPT=(2,0)],mm_hsapiens5_ENSG00000167889[&&PRIME DISCPT=(4,0)])[&&PRIME DISCPT=(6,0)],mm_mmusculus3_ENSMUSG00000043857[&&PRIME DISCPT=(3,0)])[&&PRIME DISCPT=(8,1)],((((mm_ggallus2_ENSGALG00000001740[&&PRIME DISCPT=(10,0)],mm_tguttata4_ENSTGUG00000007773[&&PRIME DISCPT=(11,0)])[&&PRIME DISCPT=(12,0)],vv_acarolinensis3_ENSACAG00000016117[&&PRIME DISCPT=(9,0)])[&&PRIME DISCPT=(13,0)],mm_mdomestica2_ENSMODG00000001601[&&PRIME DISCPT=(1,0)])[&&PRIME DISCPT=(14,9)],(mm_oanatinus4_ENSOANG00000011572[&&PRIME DISCPT=(0,0)],(vv_tnigroviridis3_ENSTNIG00000011598[&&PRIME DISCPT=(15,0)],vv_tnigroviridis3_ENSTNIG00000019529[&&PRIME DISCPT=(15,0)])[&&PRIME DISCPT=(15,13)])[&&PRIME DISCPT=(16,1)])[&&PRIME DISCPT=(16,3)])[&&PRIME DISCPT=(16,9)];", sreal);
		String srec = real.getStringRepresentation(Representation.RECONCILIATION);
		assertEquals("(((mm_cfamiliaris2_ENSCAFG00000005165[&&PRIME DISCPT=(2,0)],mm_hsapiens5_ENSG00000167889[&&PRIME DISCPT=(4,0)])[&&PRIME DISCPT=(6,0)],mm_mmusculus3_ENSMUSG00000043857[&&PRIME DISCPT=(3,0)])[&&PRIME DISCPT=(8,X)],((((mm_ggallus2_ENSGALG00000001740[&&PRIME DISCPT=(10,0)],mm_tguttata4_ENSTGUG00000007773[&&PRIME DISCPT=(11,0)])[&&PRIME DISCPT=(12,0)],vv_acarolinensis3_ENSACAG00000016117[&&PRIME DISCPT=(9,0)])[&&PRIME DISCPT=(13,0)],mm_mdomestica2_ENSMODG00000001601[&&PRIME DISCPT=(1,0)])[&&PRIME DISCPT=(14,X)],(mm_oanatinus4_ENSOANG00000011572[&&PRIME DISCPT=(0,0)],(vv_tnigroviridis3_ENSTNIG00000011598[&&PRIME DISCPT=(15,0)],vv_tnigroviridis3_ENSTNIG00000019529[&&PRIME DISCPT=(15,0)])[&&PRIME DISCPT=(15,X)])[&&PRIME DISCPT=(16,X)])[&&PRIME DISCPT=(16,X)])[&&PRIME DISCPT=(16,X)];", srec);
		String stopo = real.getStringRepresentation(Representation.TOPOLOGY);
		assertEquals("(((mm_cfamiliaris2_ENSCAFG00000005165,mm_hsapiens5_ENSG00000167889),mm_mmusculus3_ENSMUSG00000043857),((((mm_ggallus2_ENSGALG00000001740,mm_tguttata4_ENSTGUG00000007773),vv_acarolinensis3_ENSACAG00000016117),mm_mdomestica2_ENSMODG00000001601),(mm_oanatinus4_ENSOANG00000011572,(vv_tnigroviridis3_ENSTNIG00000011598,vv_tnigroviridis3_ENSTNIG00000019529))));", stopo);

		RBTree tree = new RBTree(real.tree, "GuestTree");
		NamesMap names = real.tree.getVertexNamesMap(true, "GuestNames");
		IntArrayMap discPts = real.tree.getVertexDiscPtsMap("GuestDiscPts");

		assertTrue(tree != null);
		assertTrue(names != null);
		assertTrue(discPts != null);
		int l1 = names.getVertex("mm_mdomestica2_ENSMODG00000001601");
		int l2 = names.getVertex("mm_oanatinus4_ENSOANG00000011572");
		int lca = tree.getLCA(l1, l2);
		int[] lcaPt = discPts.get(lca);
		assertEquals(16, lcaPt[0]);
		assertEquals(3, lcaPt[1]);
		//System.out.println("success");
	}
}
