package se.cbb.jprime.seqevo;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.io.FastaReaderHelper;
import org.junit.Test;

import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.NeighbourJoiningTreeGenerator;
import se.cbb.jprime.topology.RBTree;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestSubstitutionModel {

	@Test
	public void test() throws Exception {
		// Read FASTA file.
		URL url = this.getClass().getResource("/phylogenetics/56.pep.align");
		LinkedHashMap<String, ProteinSequence> seqs = FastaReaderHelper.readFastaProteinSequence(new File(url.getFile()));
		MSAData D = new MSAData(SequenceType.AMINO_ACID, seqs);
		
		// Site rates and matrix.
		GammaSiteRateHandler siteRates = new GammaSiteRateHandler(new DoubleParameter("k", 3.0), 4);
		SubstitutionMatrixHandler Q = JTT.createJTT(100);
		
		// (Unrooted) tree from NJ on sequence identity.
		MultiAlignment<ProteinSequence, AminoAcidCompound> msa = new MultiAlignment<ProteinSequence, AminoAcidCompound>(false);
		for (ProteinSequence seq : seqs.values()) {
			msa.addAlignedSequence(seq);
			//String name = seq.getOriginalHeader();
			//System.out.println(name);
		}
		NewickTree rawT = NeighbourJoiningTreeGenerator.createNewickTree(msa);
		RBTree T = new RBTree(rawT, "T");
		NamesMap names = rawT.getVertexNamesMap(true, "Names");
		DoubleMap bls = new DoubleMap("Names", T.getNoOfVertices(), 0.1);
		
		// Model.
		SubstitutionModel sm = new SubstitutionModel("JTT", D, siteRates, Q, T, names, bls, true);
		LogDouble L = sm.getLikelihood();
		assertTrue(L.greaterThan(0.0));
		assertTrue(L.lessThan(1.0));
	}
}

