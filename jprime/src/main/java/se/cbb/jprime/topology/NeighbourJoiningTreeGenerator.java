package se.cbb.jprime.topology;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.template.Profile;
import org.biojava3.core.sequence.AccessionID;
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

	/** For generating temporary safe names. */
	private static final DecimalFormat SAFE_NAME_NUMBER = new DecimalFormat("000000");
	
	/**
	 * Relabels accession IDs in a safe (=short) format.
	 * @param <S>
	 * @param <C>
	 * @param msa MSA.
	 * @return new-to-original ID map.
	 */
	private static <S extends AbstractSequence<C>, C extends Compound>
	LinkedHashMap<String,String> setSafeAccessions(MultiAlignment<S, C> msa) {
		LinkedHashMap<String,String> map = new LinkedHashMap<String, String>(msa.getSize());
		int i = 0;
		for (S seq : msa.getAlignedSequences()) {
			String origID = seq.getAccession().getID();
			String newID = "NJID" + SAFE_NAME_NUMBER.format(i++);
			seq.setAccession(new AccessionID(newID));
			map.put(newID, origID);
		}
		return map;
	}
	
	/**
	 * Restores accession IDs.
	 * @param <S>
	 * @param <C>
	 * @param idMap IDs.
	 * @param msa MSA.
	 */
	private static <S extends AbstractSequence<C>, C extends Compound>
	void restoreAccessions(LinkedHashMap<String,String> idMap, MultiAlignment<S, C> msa) {
		for (S seq : msa.getAlignedSequences()) {
			String newID = seq.getAccession().getID();
			String origID = idMap.get(newID);
			seq.setAccession(new AccessionID(origID));
		}
	}
	
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
	public static <S extends AbstractSequence<C>, C extends Compound>
	String createNewickTreeString(List<S> unalignedSeqs) throws Exception {	
		
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
	public static <S extends AbstractSequence<C>, C extends Compound>
	String createNewickTreeString(MultiAlignment<S, C> alignedSeqs) throws Exception {
		// Rename with safe names.
		LinkedHashMap<String, String> namesMap = setSafeAccessions(alignedSeqs);
		TreeConstructor<S, C> treeConstructor =
			new TreeConstructor<S, C>(alignedSeqs, TreeType.NJ, TreeConstructionAlgorithm.PID, new ProgessListenerStub());
	    treeConstructor.process();
	    String nw = treeConstructor.getNewickString(true, true);
	    restoreAccessions(namesMap, alignedSeqs);
	    for (Entry<String, String> name : namesMap.entrySet()) {
	    	nw = nw.replaceAll(name.getKey(), name.getValue());
	    }
	    return nw;
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
	public static <S extends AbstractSequence<C>, C extends Compound>
	NewickTree createNewickTree(List<S> unalignedSeqs) throws Exception {
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
	public static <S extends AbstractSequence<C>, C extends Compound>
	NewickTree createNewickTree(MultiAlignment<S, C> alignedSeqs) throws Exception {
		// HACK: Go detour via Newick string.
		String nw = createNewickTreeString(alignedSeqs);
		return NewickTreeReader.readTree(nw, false);
	}
	
}
