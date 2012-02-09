package se.cbb.jprime.seqevo;

import java.util.LinkedHashMap;

import org.biojava3.core.sequence.MultipleSequenceAlignment;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

/**
 * Simple wrapper for BioJava's <code>MultipleSequenceAlignment</code> which tries to remedy
 * the fact that there seems to be indexing inconsistencies in the latter
 * (starting from 0 vs. starting from 1).
 * 
 * @author Joel Sjöstrand.
 *
 * @param <S> Sequence type.
 * @param <C> Compound type.
 */
public class MultiAlignment<S extends Sequence<C>, C extends Compound> extends MultipleSequenceAlignment<S, C> {
	
	/** True to index sequences from 1 (a.k.a "bioIndex"); false to index from 0. */
	protected boolean indexFromOne;

	/**
	 * Constructor.
	 * @param seqs sequences. Assumed to be aligned already.
	 * @param indexFromOne true to index sequences from 1 (a.k.a "bioIndex"); false to index from 0.
	 */
	@SuppressWarnings("unchecked")
	public MultiAlignment(LinkedHashMap<String, ? extends Sequence<? extends Compound>> seqs, boolean indexFromOne) {
		super();
		this.indexFromOne = indexFromOne;
		for (Sequence<? extends Compound> seq : seqs.values()) {
			this.addAlignedSequence((S) seq);
		}
	}
	
	/**
	 * Constructor.
	 * @param indexFromOne true to index sequences from 1 (a.k.a "bioIndex"); false to index from 0.
	 */
	public MultiAlignment(boolean indexFromOne) {
		super();
		this.indexFromOne = indexFromOne;
	}
	
	@Override
	public S getAlignedSequence(int listIndex) {
		return (this.indexFromOne ? super.getAlignedSequence(listIndex) : super.getAlignedSequence(listIndex + 1));
	}
	
	/**
	 * Sets the indexing type.
	 * @param indexFromOne true to index sequences from 1 (a.k.a "bioIndex"); false to index from 0
	 */
	public void setIndexing(boolean indexFromOne) {
		this.indexFromOne = indexFromOne;
	}
	
	/**
	 * Returns the indexing type, i.e. true if indexing sequences from 1 (a.k.a "bioIndex"),
	 * false if indexing from 0
	 * @return the indexing type.
	 */
	public boolean getIndexing() {
		return this.indexFromOne;
	}
}
