package se.cbb.jprime.seqevo;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;
import org.ejml.data.DenseMatrix64F;

/**
 * Handles multiple sequence alignment (MSA) data
 * of a specified sequence type (see <code>SequenceType</code>).
 * The data is kept in a matrix with rows corresponding to e.g. genes and 
 * columns corresponding to aligned positions of these genes. 
 * <p/>
 * The class provides methods for accessing the data and 
 * associated attributes, and also for accessing a hash
 * where a column pattern is the key and the number of occurrences of this
 * pattern is the value.
 * <p/>
 * User defined partitions of data (e.g. independent loci) are currently
 * not supported, see below.
 * TODO: Bens: Add support for disjoint partitions of sequence data. Joel: Perhaps
 * that should be handled at a higher level? Seems reasonable that an
 * instance of this class represents one partition (loci).
 *
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class MSAData {

	/** Underlying sequence type. */
	private SequenceType seqType;

	/** Sequences as characters. */
	private String[] dataAsStrings;
	
	/**
	 * Sequence alignment matrix with states coded as ints. [i][j] for position j in sequence i.
	 * Ambiguity characters may be represented by states beyond the alphabet size.
	 */
	private int[][] data;

	/** Name-to-index mapping for sequences. */
	private LinkedHashMap<String, Integer> nameToKey;
	
	/** Length of sequences. */
	private int noOfPositions = -1;
	
	/**
	 * Map where patterns (unique columns) are keys and
	 * [first position, count] of the patterns are values.
	 */
	private LinkedHashMap<String, int[]> patterns;
	
	/**
	 * Private constructor.
	 * @param seqType sequence type.
	 * @param sz number of sequences.
	 */
	private MSAData(SequenceType seqType, int sz) {
		if (sz <= 0) {
			throw new IllegalArgumentException("Cannot create sequence data without any sequences.");
		}
		this.seqType = seqType;
		this.nameToKey = new LinkedHashMap<String, Integer>(sz);
		this.dataAsStrings = new String[sz];
		this.data = new int[sz][];
	}
	
	/**
	 * Constructor.
	 * @param seqType sequence type.
	 * @param sequences sequences.
	 */
	public MSAData(SequenceType seqType, LinkedHashMap<String, ? extends Sequence<? extends Compound>> sequences) {
		this(seqType, sequences.size());
		int i = 0;
		for (Entry<String, ? extends Sequence<? extends Compound>> seq : sequences.entrySet()) {
			String name = seq.getKey();
			this.addData(name, seq.getValue().getSequenceAsString(), i);
			i++;
		}
		this.updatePatterns();
	}

	/**
	 * Adds a sequence. All data is converted to lower case internally.
	 * Call updatePatterns() after adding all sequences.
	 * <p/>
	 * TODO: Bens: A short test conducted seemed to indicate that 
	 * <code>SequenceData</code> distinguishes between capital and small letters in data - this
	 * is not good and need to be fixed!! Joel: This method now converts input data to
	 * lower case. Perhaps that solved it...?
	 * @param name the sequence identifier.
	 * @param sequence the sequence.
	 * @param seqIdx the integer key of the sequence.
	 */
	private void addData(String name, String sequence, int seqIdx) {
		this.nameToKey.put(name, seqIdx);
		// Lower case internally.
		sequence = sequence.toLowerCase();
		int sz = -1;
		// Special handling of codons.
		if (this.seqType == SequenceType.CODON) {
			StringBuilder c = new StringBuilder(sequence.length() / 3);
			for (int i = 0; i + 2 < sequence.length(); i += 3) {
				String codon = sequence.substring(i, 3);
				c.append(this.seqType.int2char(this.seqType.codonStr2int(codon)));
			}
			this.dataAsStrings[seqIdx] = c.toString();
			sz = c.length();
			if (sz * 3 != sequence.length()) {
				throw new IllegalArgumentException("Sequence " + name + " does not contain an even reading frame: length is not a multiple of 3.");
			}
		} else {
			this.dataAsStrings[seqIdx] = sequence;
			sz = sequence.length();
		}
		
		// Update number of positions.
		if (this.noOfPositions > 0 && this.noOfPositions != sz) {
			throw new IllegalArgumentException("Invalid sequence data: sequences have varying lengths.");
		} else {
			this.noOfPositions = sz;
		}
		
		// Create integer state representation.
		this.data[seqIdx] = new int[sz];
		int i = 0;
		for (char c : this.dataAsStrings[seqIdx].toCharArray()) {
			this.data[seqIdx][i++] = this.seqType.get(c);
		}
	}
	
	/**
	 * Updates the pattern hash.
	 */
	private void updatePatterns() {    
		this.patterns = new LinkedHashMap<String, int[]>(this.noOfPositions);
		int height = this.data.length;
		for (int j = 0; j < this.noOfPositions; j++) {
			// Read the current column's pattern.
			char[] col = new char[height];
			for (int i = 0; i < height; ++i) {
				col[i] = this.dataAsStrings[i].charAt(j);
			}
			String pattern = new String(col);
			
			// Retrieve the position and count of the pattern.
			int[] idxCount = this.patterns.get(pattern);
			if (idxCount == null) {
				this.patterns.put(pattern, new int[] {j, 1});
			} else {
				idxCount[1] += 1;
			}
		}
	}
	
	/**
	 * Returns the sequence type.
	 * @return sequence type.
	 */
	public SequenceType getSequenceType() {
		return this.seqType;
	}

	/**
	 * Returns the number of positions (columns) in the data.
	 * A codon triplet counts as 1 position.
	 * @return no. of positions.
	 */
	public int getNoOfPositions() {
		return this.noOfPositions;
	}

	/**
	 * Returns the number of sequences.
	 * @return no. of sequences.
	 */
	public int getNoOfSequences() {
		return this.data.length;
	}

	/**
	 * Returns the size of the longest name.
	 * @return the length.
	 */
	public int getNameMaxSize() {
		int maxlen = 0;
		for (Entry<String, Integer> seq : this.nameToKey.entrySet()) {
			int l = seq.getKey().length();
			if (l > maxlen) {
				maxlen = l;
			}
		}
		return maxlen;
	}

	/**
	 * Returns the index of a sequence. NOTE: This is NOT necessarily
	 * the same as the vertex number of a tree leaf corresponding to the sequence.
	 * @param name sequence identifier.
	 * @return the index of the sequence.
	 */
	public int getSequenceIndex(String name) {
		return this.nameToKey.get(name);
	}
	
	/**
	 * Returns the integer state of a specific position of a specific sequence.
	 * @param seqIdx the sequence index. This is NOT necessarily
	 * the same as the vertex number of a tree leaf corresponding to the sequence.
	 * @param pos the position in the sequence.
	 * @return the integer index of that character.
	 */
	public int getIntState(int seqIdx, int pos) {
		return this.data[seqIdx][pos];
	}
	
	/**
	 * Returns the integer state of a specific position of a specific sequence.
	 * @param name the sequence identifier.
	 * @param pos the position in the sequence.
	 * @return the integer index of that character.
	 */
	public int getIntState(String name, int pos) {
		assert this.nameToKey.keySet().contains(name);
		return this.data[this.nameToKey.get(name)][pos];
	}

	/**
	 * Returns the character state of a specific position of a specific sequence.
	 * @param seqIdx the sequence index. This is NOT necessarily
	 * the same as the vertex number of a tree leaf corresponding to the sequence.
	 * @param pos the position in the sequence.
	 * @return the character.
	 */
	public char getCharState(int seqIdx, int pos) {
		return this.dataAsStrings[seqIdx].charAt(pos);
	}
	
	/**
	 * Returns the char state of a specific position of a specific sequence.
	 * @param name the sequence identifier.
	 * @param pos the position in the sequence.
	 * @return the character.
	 */
	public char getCharState(String name, int pos) {
		assert this.nameToKey.keySet().contains(name);
		return this.dataAsStrings[this.nameToKey.get(name)].charAt(pos);
	}
	
	/**
	 * Returns the likelihood of a character at a specific index of a specific sequence.
	 * @param seqIdx the sequence index. This is NOT necessarily
	 * the same as the vertex number of a tree leaf corresponding to the sequence.
	 * @param pos the position in the sequence
	 * @return the likelihood.
	 */
	public DenseMatrix64F getLeafLikelihood(int seqIdx, int pos) {
		return this.seqType.getLeafLikelihood(this.data[seqIdx][pos]);
	}
	
	/**
	 * Returns the likelihood of a character at a specific index of a specific sequence.
	 * @param name the sequence identifier.
	 * @param pos the position in the sequence
	 * @return the likelihood.
	 */
	public DenseMatrix64F getLeafLikelihood(String name, int pos) {
		assert this.nameToKey.keySet().contains(name);
		return this.seqType.getLeafLikelihood(this.data[this.nameToKey.get(name)][pos]);
	}

	/**
	 * Returns a specific sequence. Codons are coded using internal symbol representation.
	 * @param name the sequence identifier.
	 * @return sequence.
	 */
	public String getSequence(String name) {
		return this.dataAsStrings[this.nameToKey.get(name)];
	}

	/**
	 * Returns a compact representation of the unique column patterns of the data.
	 * The pattern is the key, and [first position, count] are the values.
	 * @return patterns as keys, first position and count as values.
	 */
	public LinkedHashMap<String, int[]> getPatterns() {
		return this.patterns;
	}

	/**
	 * Changes the sequence type.
	 * @param newtype new type.
	 */
	public void changeSequenceType(SequenceType newtype) {
		this.seqType = newtype;
	}


	@Override
	public String toString() {
		return this.getInfoString();
	}

	/**
	 * Returns an info string of this object.
	 * @return the info.
	 */
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		sb
		.append(this.seqType.toString()).append("; ")
		.append("No. of sequences: ").append(this.nameToKey.size()).append("; ")
		.append("No. of positions: ").append(this.noOfPositions)
		.append('\n');
		return sb.toString();
	}


	/**
	 * Returns the data as a string. See also <code>getDataAsFasta()</code>.
	 * @return the data.
	 */
	public String getData() {
		StringBuilder sb = new StringBuilder(this.data.length * (128 + this.noOfPositions));
		for (Entry<String, Integer> keyval : this.nameToKey.entrySet()) {
			sb.append(keyval.getKey()).append('\t');
			if (this.seqType == SequenceType.CODON) {
				for (char c : this.dataAsStrings[keyval.getValue()].toCharArray()) {
					sb.append(this.seqType.codonInt2str(this.seqType.char2int(c)));
				}
			} else {
				sb.append(this.dataAsStrings[keyval.getValue()]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}


	/**
	 * Returns the data on FASTA format.
	 * @return the data.
	 */
	public String getDataAsFasta() {
		StringBuilder sb = new StringBuilder(this.nameToKey.size() * (128 + this.noOfPositions));
		for (Entry<String, Integer> keyval : this.nameToKey.entrySet()) {
			sb.append('>').append(keyval.getKey()).append('\n');
			if (this.seqType == SequenceType.CODON) {
				for (char c : this.dataAsStrings[keyval.getValue()].toCharArray()) {
					sb.append(this.seqType.codonInt2str(this.seqType.char2int(c)));
				}
			} else {
				sb.append(this.dataAsStrings[keyval.getValue()]);
			}
			sb.append('\n');
		}
		sb.append('\n');
		return sb.toString();
	}


	/**
	 * Returns all sequence names.
	 * @return the names.
	 */
	public Set<String> getAllSequenceNames() {
		return this.nameToKey.keySet();
	}


}
