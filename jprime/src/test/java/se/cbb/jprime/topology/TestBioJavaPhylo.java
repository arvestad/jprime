package se.cbb.jprime.topology;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.Profile;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.MultipleSequenceAlignment;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;
import org.biojava3.core.util.ConcurrencyTools;
import org.biojava3.phylo.ProgessListenerStub;
import org.biojava3.phylo.TreeConstructionAlgorithm;
import org.biojava3.phylo.TreeConstructor;
import org.biojava3.phylo.TreeType;
import org.junit.Test;


public class TestBioJavaPhylo {
	
	/**
	 * There seems to be indexing inconsistencies in BioJava (starting from 0 vs. starting from 1).
	 */
	class MSA<S extends Sequence<C>, C extends Compound> extends MultipleSequenceAlignment<S, C> {
		@Override
		public S getAlignedSequence(int listIndex) {
			return super.getAlignedSequence(listIndex + 1);
		}
	}
	
	@Test
	public void testNJ() throws Exception {
		// Create multialignment.
		ProteinSequence p1 = new ProteinSequence("MDAHFLKSASDLKDCPQDNIPEICFMGRSNVGKSSLINAFFKKKLAKTSATPGRTQLLNYFEYKDKRFVDLPGYGFAKINKNKKDFITNLLT" +
				"QFLNFRSNLVGVVLIVDSGVVTVQDQEVVKIILQTGLNFLIVANKFDKLNQSERYFSLKNIANFLKVNFDKCVFASTKTHHNLALVHKKIFELFVEDER");
		p1.setAccession(new AccessionID("MYGEN1_1_PE346"));
		ProteinSequence p2 = new ProteinSequence("MEARFLKSASDLESCPQDSVKEVCFMGRSNVGKSSLINAFFQKKLAKTSATPGRTQLLNFFEYNRKRFVDLPGYGFAKLSKVQKEAITNLLT" +
				"QFLNFRQNLTGVVLVIDSGVVTVQDQEVVKTILQTGLNFLVIANKFDKLNQSERFHTQNKLAHFLKVNPNKCLFVSAKTGYNLQVMHKQIFELFKADGQAI");
		p2.setAccession(new AccessionID("MYPNE1_1_PE479"));
		ProteinSequence p3 = new ProteinSequence("MVKCKLFFWLISWFLKVKNGEKVWKFLKSCPENCYSEPQFAFIGRSNVGKSTLINALANKKIAKTSTKPGRTQLLNFYKNESEKLFVDLPGY" +
				"GYAAVSKTKKHQIDRIIAGYFQKDQPISAVFLILDARVGFTNLDYIMIEYIIQQGFKLHILANKIDKTNQSTRAILLNQCKKLKLNCLLISAKNKNNLSKLQELLE");
		p3.setAccession(new AccessionID("MYCH2_1_PE443"));
		ProteinSequence p4 = new ProteinSequence("MTSMNRFIKSATSLKDCINDSKKEVCLIGRSNVGKSTIINGLANAKIAQTSKTPGRTVTMNFYEISNQRIVDLPGYGYARIKKSQKEEISLF" +
				"LSDYLNHRKNLVGIFLILDLGVITDQDIEIVRLLTTLDVEYYIVFNKIDKYPKSAYINNKEKILDALKVNEDRILLISAKNKQNLNNLMLKIIDVIS");
		p4.setAccession(new AccessionID("MYGAL1_1_PE569"));
		ArrayList<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		lst.add(p1);
		lst.add(p2);
		lst.add(p3);
		lst.add(p4);
		Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);
		//System.out.printf("Clustalw:%n%s%n", profile);
        ConcurrencyTools.shutdown();
		MSA<ProteinSequence, AminoAcidCompound> msa = new MSA<ProteinSequence, AminoAcidCompound>();
		List<AlignedSequence<ProteinSequence,AminoAcidCompound>> alSeq = profile.getAlignedSequences();
		for (Sequence<AminoAcidCompound> seq : alSeq) {
			ProteinSequence pSeq = new ProteinSequence(seq.getSequenceAsString());
			pSeq.setAccession(seq.getAccession());
			msa.addAlignedSequence(pSeq);
		}
        //System.out.println(msa);
		
		// Create NJ tree.
		TreeConstructor<ProteinSequence, AminoAcidCompound> treeConstructor =
			new TreeConstructor<ProteinSequence, AminoAcidCompound>(msa, TreeType.NJ, TreeConstructionAlgorithm.PID, new ProgessListenerStub());
	    treeConstructor.process();
	    String newick = treeConstructor.getNewickString(true, true);
	    //System.out.println(newick);
	    assertTrue(newick.startsWith("("));
	    assertTrue(newick.endsWith(");"));
	}
}
