package se.cbb.jprime.seqevo;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.io.FastaReaderHelper;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;
import org.junit.Test;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestSequenceData {

	@Test
	public void test() throws Exception {
		// Read FASTA file.
		URL url = this.getClass().getResource("/phylogenetics/56.pep.align");
		LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs = FastaReaderHelper.readFastaProteinSequence(new File(url.getFile()));
		MSAData D = new MSAData(SequenceType.AMINO_ACID, seqs);
		int sz = D.getNoOfSequences();
		assertEquals(9, sz);
		int i;
		char c; 
		assertEquals(0, D.getSequenceIndex("dwil_GLEAN_17706"));
		i = D.getIntState(0, 0);
		assertEquals(23, i);
		c = D.getCharState(0, 0);
		assertEquals('-', c);
		i = D.getIntState(0, 80);
		assertEquals(10, i);
		c = D.getCharState(0, 80);
		assertEquals('l', c);
		assertEquals(8, D.getSequenceIndex("CG9675-PA"));
		LinkedHashMap<String, int[]> patterns = D.getPatterns();
		assertEquals(80, patterns.get("lvllvmlll")[0]);
		assertEquals(2, patterns.get("---h-----")[1]);
		assertTrue(patterns.size() < D.getNoOfPositions());
	}
}

