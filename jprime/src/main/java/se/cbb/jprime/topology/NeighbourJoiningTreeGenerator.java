package se.cbb.jprime.topology;

import java.util.List;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.template.Profile;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.MultipleSequenceAlignment;
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

import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.seqevo.MultiAlignment;

/**
 * Tree generator which computes an (arbitrarily rooted) bifurcating Neighbour Joining (NJ) tree
 * based on sequence identity (not more fancy sequence evolution than that!).
 * <p/>
 * Note: Heavy code reusage not using generics; just couldn't cope with sorting out the type mess...
 * TODO: This class should be improved when BioJava is more stable.
 * 
 * @author Joel Sjöstrand.
 */
public class NeighbourJoiningTreeGenerator {

	/** Stupid limit beyond which it seems external NJ routine cuts off names. */
	public static final int MAX_ACCESSION_LENGTH = 14;
	
	/**
	 * Creates a NJ Newick tree on string format (with some sort of lengths) using a list of unaligned sequences.
	 * Based on sequence identity only.
	 * Sequences will be aligned with CLUSTALW first.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred Newick tree with lengths.
	 * @throws Exception.
	 */
	public static <S extends Sequence<C>, C extends Compound> String createNewickTreeString(List<S> unalignedSeqs) throws Exception {	
		for (S seq : unalignedSeqs) {
			if (seq.getAccession().getID().length() > MAX_ACCESSION_LENGTH) {
				throw new IllegalArgumentException("Cannot create NJ tree from sequences: Stupid NJ routine won't accept accession IDs longer than ~" + MAX_ACCESSION_LENGTH + " characters.");
			}
		}
		
		Profile<S, C> profile = Alignments.getMultipleSequenceAlignment(unalignedSeqs);
		
		// TODO: Should this really be here? /Joel.
		ConcurrencyTools.shutdown();
		
		// Copy-paste...
		if (unalignedSeqs.get(0) instanceof DNASequence) {
			MultiAlignment<DNASequence, NucleotideCompound> msa = new MultiAlignment<DNASequence, NucleotideCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				DNASequence pSeq = new DNASequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTreeString(msa);
		} else if (unalignedSeqs.get(0) instanceof RNASequence) {
			MultiAlignment<RNASequence, NucleotideCompound> msa = new MultiAlignment<RNASequence, NucleotideCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				RNASequence pSeq = new RNASequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTreeString(msa);
		} else if (unalignedSeqs.get(0) instanceof ProteinSequence) {
			MultiAlignment<ProteinSequence, AminoAcidCompound> msa = new MultiAlignment<ProteinSequence, AminoAcidCompound>(false);
			for (Sequence<C> seq : profile.getAlignedSequences()) {
				ProteinSequence pSeq = new ProteinSequence(seq.getSequenceAsString());
				pSeq.setAccession(seq.getAccession());
				msa.addAlignedSequence(pSeq);
			}
			return createNewickTreeString(msa);
		}
		throw new IllegalArgumentException("Unknown sequence type in when trying to compute NJ tree.");
	}
	
	/**
	 * Creates a NJ Newick tree on string format (with some sort of lengths) using a list corresponding to an existing multialignment.
	 * Based on sequence identity only.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred Newick tree with lengths.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> String createNewickTreeString(MultiAlignment<S, C> alignedSeqs) throws Exception {
		for (S seq : alignedSeqs.getAlignedSequences()) {
			if (seq.getAccession().getID().length() > MAX_ACCESSION_LENGTH) {
				throw new IllegalArgumentException("Cannot create NJ tree from sequences: Stupid NJ routine won't accept accession IDs longer than ~" + MAX_ACCESSION_LENGTH + " characters.");
			}
		}
		TreeConstructor<S, C> treeConstructor =
			new TreeConstructor<S, C>(alignedSeqs, TreeType.NJ, TreeConstructionAlgorithm.PID, new ProgessListenerStub());
	    treeConstructor.process();
	    return treeConstructor.getNewickString(true, true);
	}
	
	/**
	 * Creates a NJ Newick tree on string format (with some sort of lengths) using a list corresponding to an existing multialignment.
	 * Based on sequence identity only. If you run into problems using this, try method where input is a <code>MultiAlignment</code>
	 * rather than a <code>MultipleSequenceAlignment</code>.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred Newick tree with lengths.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> String createNewickTreeString(MultipleSequenceAlignment<S, C> alignedSeqs) throws Exception {
		for (S seq : alignedSeqs.getAlignedSequences()) {
			if (seq.getAccession().getID().length() > MAX_ACCESSION_LENGTH) {
				throw new IllegalArgumentException("Cannot create NJ tree from sequences: Stupid NJ routine won't accept accession IDs longer than ~" + MAX_ACCESSION_LENGTH + " characters.");
			}
		}
		TreeConstructor<S, C> treeConstructor =
			new TreeConstructor<S, C>(alignedSeqs, TreeType.NJ, TreeConstructionAlgorithm.PID, new ProgessListenerStub());
	    treeConstructor.process();
	    return treeConstructor.getNewickString(true, true);
	}
	
	/**
	 * Creates a NJ tree using a list of unaligned sequences.
	 * Based on sequence identity only.
	 * Sequences will be aligned with CLUSTALW first.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred tree.
	 * @throws Exception.
	 */
	public static <S extends Sequence<C>, C extends Compound> NewickTree createNewickTree(List<S> unalignedSeqs) throws Exception {
		// HACK: Go detour via Newick string format.
		String nw = createNewickTreeString(unalignedSeqs);
		return NewickTreeReader.readTree(nw, false);
	}
	
	/**
	 * Creates a NJ tree using an existing multialignment.
	 * Based on sequence identity only.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred tree.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> NewickTree
	createNewickTree(MultiAlignment<S, C> alignedSeqs) throws Exception {
		// HACK: Go detour via Newick string.
		String nw = createNewickTreeString(alignedSeqs);
		return NewickTreeReader.readTree(nw, false);
	}
	
	/**
	 * Creates a NJ tree using an existing multialignment.
	 * Based on sequence identity only. If you run into problems using this,
	 * try method where input is a <code>MultiAlignment</code>
	 * rather than a <code>MultipleSequenceAlignment</code>.
	 * @param <S> sequence type.
	 * @param <C> compound type.
	 * @param unalignedSeqs unaligned sequences.
	 * @return an inferred tree.
	 * @throws Exception.
	 */
	public static <S extends AbstractSequence<C>, C extends Compound> NewickTree
	createNewickTree(MultipleSequenceAlignment<S, C> alignedSeqs) throws Exception {
		// HACK: Go detour via Newick string.
		String nw = createNewickTreeString(alignedSeqs);
		return NewickTreeReader.readTree(nw, false);
	}
}
