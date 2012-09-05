package se.cbb.jprime.topology;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.Profile;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.template.Sequence;
import org.biojava3.core.util.ConcurrencyTools;
import org.biojava3.phylo.ProgessListenerStub;
import org.biojava3.phylo.TreeConstructionAlgorithm;
import org.biojava3.phylo.TreeConstructor;
import org.biojava3.phylo.TreeType;
import org.junit.Test;

import se.cbb.jprime.seqevo.MultiAlignment;

public class TestBioJavaPhylo {
	
	@Test
	public void testNJ() throws Exception {
		// Create multialignment.
/*		ProteinSequence p1 = new ProteinSequence(
				"MNVILSVMLFSSPSCVNINSFDILIVGAGISGIVLANILANHNKRVLIVEKRDHIGGNCY" +
				"DKVDSKTQLLFHQYGPHIFHTNNQTVINFISPFFELNNYHHRVGLKLKNNLDLTLPFDFQ" +
				"QIYKLMG-KDGRKLVSFFKENFSLNTHLSLAELQLIDNPLAQKLYQFLISNVYKPYSVKM" +
				"WGLPFAMINENVINRVKIVLSEQSSYFPDAIIQGLPKSGYTNSFLKMLANPLIDVQLNCK" +
				"-DNLLVYQ-----DEKLFFNNNLIEKPVVYCGLIDKLFNFCFGHLQYRSLAFSWKRFNQK" +
				"KYQTYPVVNMPLAKSITRSVEYKQLTNQGSFKPQTIVSFETPGSYAINDPRFNEPYYPIN" +
				"NTLNDTLFKKYWKKASKLKNLHLLGRLATYQYIDMDKAILLSIKKAQQLLS----");
		p1.setAccession(new AccessionID("MYGEN1_1_PE138"));
		
		ProteinSequence p2 = new ProteinSequence(
				"------MTLFSFPNYVNWNKFDFIVLGAGISGIVLSHVLAQHGKSVLLLEKRNQLGGNCY" +
				"DKLDETTGLLFHQYGPHIFHTDNQKVMDFIQPFFELNNYQHRVGLQLDNNLDLTLPFDFS" +
				"QMRKLLDTKTASSLINFFQQHFPAEKHLTLMQLQTINFAPVQQLYQFLKIKVYGPYSVKM" +
				"WGMPLEQIDPSVLGRVKISLSENSSYFPTATIQGLPKGGYTKAFTKMVDHPLIDLRLNCP" +
				"-ANLISVN-----NNQLLFANQPITKPVVYCGLIDQLFGYCFGRLQYRSLHFEWKRYAVK" +
				"QHQAYPVMNWPLHPTITRQVEYKQLTQEGLESNQTIVSCETPGAFREGDPRFMEPYYPLN" +
				"DVSNNALFARYLKLANAIPNIHLLGRLALYQYIDMDRAIAQSLAKAEQLLQ----");
		p2.setAccession(new AccessionID("MYPNE1_1_PE278"));
		
		ProteinSequence p3 = new ProteinSequence(
				"----MLKIILSFCY--NLIIMDILIAGAGLSGAVLANKLAKENKKVLIIEKRNHIGGNVF" +
				"DL--KTKGVLVHKYGPHIFHTSNKQVYDYMNQFWKLNNFQNIVAAKIKDEI-VPIPFNYR" +
				"GLDVFFP-EKSEEIKEKLNKKYGFDKRIKILDLIKENDLELQKVADFIYKNVFENYTVKM" +
				"WGLNPKEIDKKVTERVPIISSYNDKYFND-LFEGLPEEGYTNSIKKMLDHPNITVVLETN" +
				"IKDLIKFKISSEGKKAIFFKEKELKVPFVFTGAIDELFDNVDGILDYRSLDIKFTNINQT" +
				"KFQSNAVINYPAHFDITRITEYKHMTLQ-NDVTNTIISREYPGKYDPNSERFNVPFYPLQ" +
				"TEEANKKYLKYFERTKAYSNLFLVGRLAKYKYINMDQAIEEALEIFPQILSYESK");
		p3.setAccession(new AccessionID("MYMOB1_1_PE421"));
		
		ProteinSequence p4 = new ProteinSequence(
				"-----MNTLKALPT--NINSYDYIFIGCGLSTATVCAKLPK-SKRILIIEKRAHIGGNVY" +
				"DH--KKNDILVHQYGPHIFHTNDKEVFDFLNQFTTFNTYKNVVQAKIDDEL-IPLLVNVD" +
				"SIKILFP-NEAEDFINYLKEKFPNQDQVTILELSQIDK--YQHIYQTIYTRIFASYTGKM" +
				"WDKKIEDLDVSVFARVPIYLTKRNTYFTD-TYEGLPSKGYTQMVLNMLDSSNIDIVLNIN" +
				"ITKHLQIK-----DDQIYINDELITKPVINCAPIDEIFGYKYDKLPYRSLNIKFEELNNS" +
				"NLQSTAIVNYPEHPKMTRITEYKNF-----------------------------------" +
				"------------------------------------------------ILK----");
		p4.setAccession(new AccessionID("MYMYC1_1_PE926"));

		ProteinSequence p5 = new ProteinSequence(
				"-----MNTLKALPT--NINSYDYIFIGCGLSTATVCAKLPK-SKRILIIEKRAHIGGNVY" +
				"DH--KKNDILVHQYGPHIFHTNDKEVFDFLNQFTTFNTYKNVVQAKIDDEL-IPLPVNVD" +
				"SIKILFP-NEAEDFINYLKEKFPNQDQVTILELSKIDK--YQHIYQTIYTRIFASYTGKM" +
				"WDKKIEDLDVSVFARVPIYLTKRNTYFTD-TYEGLPSKGYTQMVLNMLDSSNIDIVLNIN" +
				"ITKHLQIK-----DDQIYINDELITKPVINCAPIDEIFGYKYDKLPYRSLNIKFEELNNS" +
				"NLQSTAIVNYPEHPKMTRITEYKNF-----------------------------------" +
				"------------------------------------------------ILK----");
		p5.setAccession(new AccessionID("MYMYC1_1_PE933"));
		
		ProteinSequence p6 = new ProteinSequence(
				"-----MNTLKALPT--NINSYDYIFIGCGLSTATVCAKLPK-SKRILIIEKRAHIGGNVY" +
				"DH--KKNDILVHQYGPHIFHTNDKEVFDFLNQFTTFNTYKNVVQAKIDDEL-IPLPVNVD" +
				"SIKILFP-NEAEDFINYLKEKFPNQDQVTILELSQIDK--YQHIYQTIYTRIFASYTGKM" +
				"WDKKIEDLDVSVFARVPIYLTKRNTYFTD-TYEGLPSKGYTQMVLNMLDSSNIDIVLNIN" +
				"ITKHLQIK-----DDQIYINDELITKPVINCAPIDEIFGYKYDKLPYRSLNIKFEELNNS" +
				"NLQSTAVVNYPEHPKMTRITEYKNFYPEIKNDKNTIISKEFPGAFEQNSKEFSERYYPIP" +
				"NDASRDQYNKYVEESKKISNLYQLGRLAQYRYINMDQAVRGALDFADELIKKFEN");
		p6.setAccession(new AccessionID("MYMYC1_1_PE940"));

		ArrayList<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		lst.add(p1);
		lst.add(p2);
		lst.add(p3);
		lst.add(p4);
		lst.add(p5);
		lst.add(p6);
		Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);
		//System.out.printf("Clustalw:%n%s%n", profile);
        ConcurrencyTools.shutdown();
		MultiAlignment<ProteinSequence, AminoAcidCompound> msa = new MultiAlignment<ProteinSequence, AminoAcidCompound>(false);
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
*/	}
}
