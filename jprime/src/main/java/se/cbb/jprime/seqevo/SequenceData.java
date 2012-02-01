package se.cbb.jprime.seqevo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.ejml.data.DenseMatrix64F;

/**
 * This handles a sequence data matrix (multiple sequence alignment)
 * of a specified sequence type (see <code>SequenceType</code>).
 * This matrix has rows corresponding to e.g. genes and 
 * columns corresponding to aligned positions of these genes. 
 * The class provides methods for accessing the data and 
 * associated attributes, and also for converting the data into a hash
 * where a pattern is the key and the number of occurrences of this
 * pattern is the value. User defined partitions of data is prepared for,
 * but are currently not used, see below TODO.
 * <p/>
 * TODO: Bens: Add support for disjoint partitions of sequence data. Joel: Perhaps
 * that should be handled at a higher level? Seems reasonable that an
 * instance of this class represents one partition.
 *
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class SequenceData {

	/** Underlying sequence type. */
	private SequenceType seqType;

	/** Sequence alignment matrix. */
	private LinkedHashMap<String, String> data;

	/** Length of sequences. */
	private int noOfPositions;
	
	/**
	 * Constructor.
	 * @param seqType sequence type.
	 */
	public SequenceData(SequenceType seqType) {
		this.seqType = seqType;
		this.data = new LinkedHashMap<String, String>(64);
		this.noOfPositions = -1;
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
	public int getNumberOfPositions() {
		return this.noOfPositions;
	}

	/**
	 * Returns the number of sequences.
	 * @return no. of sequences.
	 */
	public int getNumberOfSequences() {
		return this.data.size();
	}

	/**
	 * Returns the size of the longest name.
	 * @return the length.
	 */
	public int getNameMaxSize() {
		int maxlen = 0;
		for (Entry<String, String> seq : this.data.entrySet()) {
			int l = seq.getKey().length();
			if (l > maxlen) {
				maxlen = l;
			}
		}
		return maxlen;
	}

	/**
	 * Returns the integer index of a specific position of a specific sequence.
	 * @param name the sequence identifier.
	 * @param pos the position in the sequence.
	 * @return the integer index of that character.
	 */
	public int get(String name, int pos) {
		assert this.data.keySet().contains(name);
		return this.seqType.get(this.data.get(name).charAt(pos));
	}

	/**
	 * Returns the likelihood of a character at a specific index of a specific sequence.
	 * @param name the sequence identifier.
	 * @param pos the position in the sequence
	 * @return the likelihood.
	 */
	public DenseMatrix64F getLeafLikelihood(String name, int pos) {
		assert this.data.keySet().contains(name);
		return this.seqType.getLeafLikelihood(this.data.get(name).charAt(pos));
	}


	/**
	 * Returns a specific sequence.
	 * @param name the sequence identifier.
	 * @return
	 */
	public String get(String name) {
		return this.data.get(name);
	}


//	/**
//	 * Sorts data into a table with character patterns present in the 
//	 * given partition, ordered according to name order, and their 
//	 * associated number of occurrences in the interval. If no interval
//	 * is given, all data are sorted.
//	 * Should probably be completely replaced by <code>getSortedData()</code>, 
//	 * but might be interesting when outputting statistics of data.
//	 * @return the sorted data.
//	 */
//	public LinkedHashMap<String, List<Integer>> sortData() {
//		String s = "all";
//		return this.sortData(s);
//	}
//
//	
//	public LinkedHashMap<String, List<Integer>> sortData(String partition) {
//		//This does not handle partitions yet.
//		
//		// Sort the data
//		LinkedHashMap<String, List<Integer>> sorted = new LinkedHashMap<String, List<Integer>>();
//		int nchar = data.begin()->second.size();
//		for (int j = 0; j < nchar; j++) {
//			ostringstream oss;
//			for (map<String, String>::const_iterator i = data.begin(); i != data.end(); i++) {
//				oss << (*i).second[j];
//			}
//			// TODO: Verify that SequenceType handles ambiguities properly. / bens
//			sorted[oss.str()].push_back(j);
//		}
//		return sorted; 
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	public List<Pair<Integer, Integer> > getSortedData() {
//		// SubstitutionModel actually never uses map<std::string, vector<unsigned>> as a map. Thus, 
//		// it is better to return a vector<std::pair<unsigned, unsigned> > instead.
//		String s = "all";
//		return this.getSortedData(s);
//	}

	
	/**
	 * Returns a compact representation of the unique column patterns of the data.
	 * Each unique pattern (key) is represented by [first position, count] (values).
	 * TODO: This method does not handle partitions yet.
	 * @return patterns as keys, along with first position and count as values.
	 */
	public LinkedHashMap<String, int[]> getUniqueColumnPatterns() {    
		
		// Map where patterns (unique columns) are keys and
		// [first position, count] of the patterns are values.
		LinkedHashMap<String, int[]> patterns = new LinkedHashMap<String, int[]>(this.noOfPositions);

		int height = this.data.size();
		for (int j = 0; j < this.noOfPositions; j++) {
			// Read the current column's pattern.
			StringBuilder sb = new StringBuilder(height);
			for (Entry<String, String> seq : this.data.entrySet()) {
				sb.append(seq.getValue().charAt(j));
			}
			String pattern = sb.toString();
			
			// Retrieve the position and count of the pattern.
			int[] idxCount = patterns.get(pattern);
			if (idxCount == null) {
				patterns.put(pattern, new int[] {j, 1});
			} else {
				idxCount[1] += 1;
			}
		}
		return patterns;
	}


	/**
	 * Changes the sequence type.
	 * @param newtype new type.
	 */
	public void changeSequenceType(SequenceType newtype) {
		this.seqType = newtype;
	}


	/**
	 * Adds data. All data is converted to lower case internally.
	 * <p/>
	 * TODO: Bens: A short test conducted seemed to indicate that 
	 * <code>SequenceData</code> distinguishes between capital and small letters in data - this
	 * is not good and need to be fixed!! Joel: This method now converts input data to
	 * lower case. Perhaps that solved it...?
	 * @param name the sequence identifier.
	 * @param sequence the sequence.
	 */
	public void addData(String name, String sequence) {
		sequence = sequence.toLowerCase();
		int sz = -1;
		// Special handling of codons.
		if (this.seqType == SequenceType.CODON) {
			StringBuilder c = new StringBuilder(sequence.length() / 3);
			for (int i = 0; i + 2 < sequence.length(); i += 3) {
				c.append(this.seqType.int2char(this.seqType.codonStr2int(sequence.substring(i, 3))));
			}
			this.data.put(name, c.toString());
			sz = c.length();
			if (sz * 3 != sequence.length()) {
				throw new IllegalArgumentException("Sequence " + name + " does not contain an even reading frame: length is not a multiple of 3.");
			}
		} else {
			this.data.put(name, sequence);
			sz = sequence.length();
		}
		if (this.noOfPositions > 0 && this.noOfPositions != sz) {
			throw new IllegalArgumentException("Invalid sequence data: sequences have varying lengths.");
		} else {
			this.noOfPositions = sz;
		}
	}


	@Override
	public String toString() {
		return this.getInfoString();
	}

	/**
	 * Returns an info string of this object.
	 * @return
	 */
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		if (this.data.size() == 0) {
			sb.append("<No data>\n");
		} else {
			sb
			.append(this.seqType.toString()).append("; ")
			.append("No. of sequences: ").append(this.data.size()).append("; ")
			.append("No. of positions: ").append(this.noOfPositions)
			.append('\n');
		}
		return sb.toString();
	}


	/**
	 * Returns the data as a string. See also <code>getDataAsFasta()</code>.
	 * @return the data.
	 */
	public String getData() {
		StringBuilder sb = new StringBuilder(this.data.size() * (128 + this.noOfPositions));
		for (Map.Entry<String, String> keyval : this.data.entrySet()) {
			sb.append(keyval.getKey()).append('\t');
			if (this.seqType == SequenceType.CODON) {
				String seq = keyval.getValue();
				for (int j = 0;  j < seq.length(); ++j) {
					sb.append(this.seqType.codonInt2str(this.seqType.char2int(seq.charAt(j))));
				}
			} else {
				sb.append(keyval.getValue());
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
		StringBuilder sb = new StringBuilder(this.data.size() * (128 + this.noOfPositions));
		for (Map.Entry<String, String> keyval : this.data.entrySet()) {
			sb.append('>').append(keyval.getKey()).append('\n');
			if (this.seqType == SequenceType.CODON) {
				String seq = keyval.getValue();
				for (int j = 0;  j < seq.length(); ++j) {
					sb.append(this.seqType.codonInt2str(this.seqType.char2int(seq.charAt(j))));
				}
			} else {
				sb.append(keyval.getValue());
			}
			sb.append('\n');
		}
		sb.append('\n');
		return sb.toString();
	}


	/**
	 * Returns a list of all sequence names.
	 * @return the names.
	 */
	public List<String> getAllSequenceNames() {
		// Safest to use entrySet-order here, to ensure consistency over class.
		ArrayList<String> names = new ArrayList<String>(this.data.size());
		for (Entry<String, String> seq : this.data.entrySet()) {
			names.add(seq.getKey());
		}
		return names;
	}


}
