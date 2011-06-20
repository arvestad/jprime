package se.cbb.jprime.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.* ;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestNewickTreeWriter {

	@Test
	public void writeRootedTree() throws IOException, NewickIOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		assertEquals("((AYWBP:0.031[&&PRIME ID=0],ONYEL:0.031[&&PRIME ID=1]):0.969[&&PRIME ID=2],((MEFLO:0.27[&&PRIME ID=3],(MYCCT:0.03[&&PRIME ID=4]," +
				"MYMYC:0.03[&&PRIME ID=5])100:0.24[&&PRIME ID=6])100:0.58[&&PRIME ID=7],(((MYGAL:0.461[&&PRIME ID=15],(MYGEN:0.15[&&PRIME ID=17]," +
				"MYPNE:0.15[&&PRIME ID=16])100:0.311[&&PRIME ID=18])100:0.139[&&PRIME ID=19],(MYPEN:0.51[&&PRIME ID=20]," +
				"URPAR:0.51[&&PRIME ID=21])54:0.09[&&PRIME ID=22])100:0.18[&&PRIME ID=23],(MYCH2:0.53[&&PRIME ID=8],(MYMOB:0.46[&&PRIME ID=9]," +
				"(MYPUL:0.38[&&PRIME ID=10],MYCS5:0.38[&&PRIME ID=11])99:0.08[&&PRIME ID=12])77:0.07[&&PRIME ID=13])100:" +
				"0.25[&&PRIME ID=14])77:0.07[&&PRIME ID=24]):0.15[&&PRIME ID=25])100:1.0[&&PRIME ID=26][&&PRIME NAME=molli];", NewickTreeWriter.write(tree));
		RTree T = new RTree(tree, "Molli");
		NamesMap names = tree.getVertexNamesMap(true);
		DoubleMap bls = tree.getBranchLengthsMap();
		assertEquals("((AYWBP,ONYEL),((MEFLO,(MYCCT,MYMYC)),(((MYGAL,(MYGEN,MYPNE)),(MYPEN,URPAR)),(MYCH2,(MYMOB,(MYPUL,MYCS5))))));", NewickTreeWriter.write(T, names, false));
		assertEquals("((AYWBP:0.031,ONYEL:0.031):0.969,((MEFLO:0.27,(MYCCT:0.03,MYMYC:0.03):0.24):0.58,((MYCH2:0.53,((MYCS5:0.38,MYPUL:0.38):0.08," +
				"MYMOB:0.46):0.07):0.25,((MYGAL:0.461,(MYGEN:0.15,MYPNE:0.15):0.311):0.139,(MYPEN:0.51,URPAR:0.51):0.09):0.18):0.07):0.15):1.0;", NewickTreeWriter.write(T, names, bls, true));
	}
	
}
