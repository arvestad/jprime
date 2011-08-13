package se.cbb.jprime.topology;

import java.util.List;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.template.Profile;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.template.AbstractSequence;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;
import org.biojava3.core.util.ConcurrencyTools;
import org.biojava3.phylo.ProgessListenerStub;
import org.biojava3.phylo.TreeConstructionAlgorithm;
import org.biojava3.phylo.TreeConstructor;
import org.biojava3.phylo.TreeType;
import se.cbb.jprime.seqevo.MultiAlignment;

/**
 * Tree generator which computes a bifurcating Neighbour Joining (NJ) tree
 * based on sequence identity (not more fancy than that!).
 * <p/>
 * Note: Heavy code reusage not using generics; just couldn't cope with sorting out the type mess...
 * 
 * @author Joel Sjöstrand.
 */
public class NeighbourJoiningTreeGenerator {

	/**
	 * Creates a NJ Newick tree (with some sort of lengths) using a list of unaligned sequences.
	 * Based on sequence identity only.
	 * Sequences will be aligned with CLUSTALW first.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return a Newick tree with lengths.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> String createNewickTree(List<S> unalignedSeqs) throws Exception {
		Profile<S, C> profile = Alignments.getMultipleSequenceAlignment(unalignedSeqs);
		ConcurrencyTools.shutdown();
		
		// Copy-paste...
		if (unalignedSeqs.get(0) instanceof DNASequence) {
			MultiAlignment<DNASequence, NucleotideCompound> msa = new MultiAlignment<DNASequence, NucleotideCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				DNASequence pSeq = new DNASequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTree(msa);
		} else if (unalignedSeqs.get(0) instanceof RNASequence) {
			MultiAlignment<RNASequence, NucleotideCompound> msa = new MultiAlignment<RNASequence, NucleotideCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				RNASequence pSeq = new RNASequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTree(msa);
		} else if (unalignedSeqs.get(0) instanceof ProteinSequence) {
			MultiAlignment<ProteinSequence, AminoAcidCompound> msa = new MultiAlignment<ProteinSequence, AminoAcidCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				ProteinSequence pSeq = new ProteinSequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTree(msa);
		}
		throw new IllegalArgumentException("Unknown sequence type in when trying to compute NJ tree.");
	}
	
	/**
	 * Creates a NJ Newick tree (with some sort of lengths) using a list corresponding to an existing multialignment.
	 * Based on sequence identity only.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return a Newick tree with lengths.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> String createNewickTree(MultiAlignment<S, C> alignedSeqs) throws Exception {
		TreeConstructor<S, C> treeConstructor =
			new TreeConstructor<S, C>(alignedSeqs, TreeType.NJ, TreeConstructionAlgorithm.PID, new ProgessListenerStub());
	    treeConstructor.process();
	    return treeConstructor.getNewickString(true, true);
	}
}
