package se.cbb.jprime.seqevo;

import java.util.Arrays;
import org.ejml.data.DenseMatrix64F;

/**
 * The various sequence types. These are used for substitution models,
 * and stems from C++ PrIME, although BioJava has similar stuff. In general,
 * each state has both a character representation and an integer representation
 * (the latter being the character's index in the alphabet).
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public enum SequenceType {

	/**
	 * Used for DNA sequences. The alphabet has 4 states:
	 * a=0, c=1, g=2, t=3.
	 * The alternative alphabet has 13 states and represents
	 * different combination of the 4 basic nucleotides.
	 * E.g. m is a or c with 50% on each.
	 * m(ac)=01, r(ag)=02, w(at)=03, s(cg)=12, y(ct)=13, k(gt)=23, 
	 * v(acg)=012, h(act)=013, d(agt)=023, b(cgt)=123, 
	 * x(acgt)=n(acgt)=-(acgt)=0123.
	 * See http://www.hgu.mrc.ac.uk/Softdata/Misc/ambcode.htm for more info.
	 */
	DNA        ("DNA", "acgt", "mrwsykvhdbxn-.", getDNALeafLike()),
	
	/**
	 * Used for AA sequences. 
	 * The alphabet has 20 states that represents the different amino acids:
	 * a=1, r=2, n=3, d=4, c=5, q=6, e=7, g=8, h=9, i=10, l=11, k=12, 
	 * m=13, f=14, p=15, s=16, t=17, w=18, y=19, v=20.
	 * The alternative alphabet has 4 states and represents
	 * different combination of the 20 basic AAs,
	 * b=21, z=22, x=23, -=24
	 * b=dn z=eq x=acdefghiklmnpqrstvwy.
	 * See http://www.hgu.mrc.ac.uk/Softdata/Misc/ambcode.htm for more info.
	 */
	AMINO_ACID ("Amino acid", "arndcqeghilkmfpstwyv", "bzx-.", getAminoAcidLeafLike()),
	
	/**
	 * Used for codon sequences. 
	 * The alphabet has 61 states, all three letter combination of
	 * the nucleotides except for the most common stop codons (4^3-3=61).
	 * Instead of using three letter symbols we invent a char representation.
	 */
	CODON      ("Codon", "abcdefghijklmnopqrstuvwxyz_.,1234567890!#�%&/()=?+@�${[]}+?|<", "*", getCodonLeafLike());
	
	/** A string describing the type. */
	private String type;

	/** All states in the alphabet represented as chars. */
	private String alphabet;

	/** The alternative alphabet. */
	private String ambiguityAlphabet;

	/** The probabilities for alphabet states for any given state. */
	private DenseMatrix64F[] leafLike;

	/** The probability of observing an alphabet character in a seq. */
	private double alphProb;
	
	/** The probability of observing an ambiguity symbol in a seq. */
	private double ambiguityProb;
	
	/**
	 * If you have a string description of a sequence type, the 
	 * following method returns a statically allocated object of
	 * that type.
	 */
	public static SequenceType getSequenceType(String name) {
		name = name.trim().toUpperCase();
		if (name.equals("DNA")) {
			return SequenceType.DNA;
		}
		if (name.equals("AMINO ACID") || name.equals("AA") || name.equals("PROTEIN") || name.equals("AMINOACID") || name.equals("AMINO_ACID")) {
			return SequenceType.AMINO_ACID;
		}
		if (name.equals("CODON")) {
			return SequenceType.CODON;
		}
		throw new IllegalArgumentException("Invalid sequence type identifier: " + name);
	}
	
	/**
	 * Enum constructor.
	 * @param type identifier.
	 * @param alphabet alphabet.
	 * @param ambiguityAlphabet alternative alphabet.
	 */
	private SequenceType(String type, String alphabet, String ambiguityAlphabet, DenseMatrix64F[] leafLike) {
		this.type = type;
		this.alphabet = alphabet;
		this.ambiguityAlphabet = ambiguityAlphabet;
		this.leafLike = leafLike;
		this.alphProb = (1.0 - 0.001) / this.getAlphabetSize();
		this.ambiguityProb = 0.001;
	}
	
	/**
	 * Returns the type, e.g. "DNA" or "Amino acid".
	 * @return the type.
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Returns the integer equivalent of a character.
	 * @param state the character.
	 * @return the corresponding integer.
	 */
	public int get(char state) {
		return this.char2int(state);
	}
	
	/** 
	 * The number of states in the alphabet. E.g., for DNA this is 4.
	 * @return the length.
	 */
	public int getAlphabetSize() {
		return this.alphabet.length();
	}
	
	/**
	 * Returns the integer value for a specific state in the sequence 
	 * alphabet.
	 * Alternative (ambiguity) alphabet uses values > alphabet size, i.e.,
	 * as if appended to the original alphabet.
	 * @param c a state from the alphabet.
	 * @return the integer code.
	 */
	public int char2int(char c) {
		c = Character.toLowerCase(c);
		int ret = this.alphabet.indexOf(c);
		if (ret != -1) {
			return ret;
		}
		ret = this.ambiguityAlphabet.indexOf(c);
		if (ret != -1) {
			return (ret + this.alphabet.length());
		}
		throw new IllegalArgumentException("Sequence character " + c + " is not a valid character.");
	}
	
	/**
	 * Returns the specific state in the sequence alphabet from its integer index.
	 * Alternative (ambiguity) alphabet use values > alphabet size, i.e.,
	 * as if appended after the original alphabet.
	 * @param i the integer code.
	 * @return a state (character) from the alphabet.
	 */
	public char int2char(int i) {
		if (i >= this.alphabet.length()) {
			return this.ambiguityAlphabet.charAt(i - this.alphabet.length());
		} else {
			return this.alphabet.charAt(i);
		}
	}
	
	
	/**
	 * Converts a string with states in text format to a vector of state values.
	 * @param s char values.
	 * @return int states.
	 */
	public int[] stringTranslate(String s) {
		int[] vec = new int[s.length()];
		for (int i = 0; i < s.length(); i++) {
			vec[i] = this.char2int(s.charAt(i));
		}
		return vec;
	}
	
	/**
	 * Returns a vector of likelihoods for a leaf with a character. 
	 * The size of the vector equals the alphabet size and the 
	 * likelihood is the probability that the leaf is in a specific
	 * state.
	 * @param state character.
	 * @return likelihood.
	 */
	public DenseMatrix64F getLeafLikelihood(char state) {
		return this.leafLike[this.char2int(state)];
	}
	
	/**
	 * Returns a vector of likelihoods for a leaf with a character. 
	 * The size of the vector equals the alphabet size and the 
	 * likelihood is the probability that the leaf is in a specific
	 * state.
	 * @param i character index. May be an ambiguity state as well.
	 * @return likelihood.
	 */
	public DenseMatrix64F getLeafLikelihood(int i) {
		return this.leafLike[i];
	}
	
	/** 
	 * Returns (roughly) how likely it is that the given sequence came 
	 * from the present alphabet? The alternative alphabet is
	 * also considered.
	 * @param s the sequence.
	 * @return a measure of how likely the string is of the current type.
	 */
	public double getTypeLikelihood(String s) {
		double p = 1.0;
		for (int i = 0; i < s.length(); i++) {
			char c = Character.toLowerCase(s.charAt(i));
			if (alphabet.indexOf(c) < alphabet.length()) {
				p *= alphProb;
			} else if (ambiguityAlphabet.indexOf(c) < ambiguityAlphabet.length()) {
				p *= ambiguityProb;
			} else {
				return 0.0;
			}
		}
		return p;
	}
	
	/**
	 * Checks all the states in the given list if they are 
	 * valid within the alphabet. Alternative alphabet is not considered.
	 * @param v states (characters).
	 * @param true if valid; false if invalid.
	 */
	boolean isValid(int[] v) {
		for (int s : v) {
			if (s >= alphabet.length()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The alphabet has 4 states.
	 * The alternative alphabet has 13 states and represents
	 * different combination of the 4 basic nucleotides.
	 * See http://www.hgu.mrc.ac.uk/Softdata/Misc/ambcode.htm for more info.
	 */
	private static DenseMatrix64F[] getDNALeafLike() {
		DenseMatrix64F[] leafLike = new DenseMatrix64F[18];
		double[] l = 
		{
				// acgt
				1.0,0,0,0, 
				0,1.0,0,0,
				0,0,1.0,0,
				0,0,0,1.0,

				// mrwsyk
				0.5,0.5,0,0,
				0.5,0,0.5,0,
				0.5,0,0,0.5, 
				0,0.5,0.5,0,
				0,0.5,0,0.5,
				0,0,0.5,0.5,

				// vhdb
				1.0/3,1.0/3,1.0/3,0,
				1.0/3,1.0/3,0,1.0/3,
				1.0/3,0,1.0/3,1.0/3,  
				0,1.0/3,1.0/3,1.0/3,

				// xn-.
				0.25,0.25,0.25,0.25,
				0.25,0.25,0.25,0.25,
				0.25,0.25,0.25,0.25,
				0.25,0.25,0.25,0.25
		};
		for (int i = 0; i < 18; i++) {
			leafLike[i] = new DenseMatrix64F(4, 1, true, Arrays.copyOfRange(l, i*4, (i+1)*4));
		}
		return leafLike;
	}
	
	private static DenseMatrix64F[] getAminoAcidLeafLike() {
		DenseMatrix64F[] leafLike = new DenseMatrix64F[25];
		double[] l = 
		{
				// arndcqeghilkmfpstwyv
				1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  
				0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  
				0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  
				0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  
				0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,0,  
				0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,0,  
				0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,0, 
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,0,  
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0, 

				// b and z
				0,0,0.5,0.5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0.5,0.5,0,0,0,0,0,0,0,0,0,0,0,0,

				// x
				0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,

				// -
				0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,

				// .
				0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05
		};

		for (int i = 0; i < 25; i++) {
			leafLike[i] = new DenseMatrix64F(20, 1, true, Arrays.copyOfRange(l, i*20, (i+1)*20));
		}
		return leafLike;
	}

	
	private static DenseMatrix64F[] getCodonLeafLike() {
		// Instead of using three letter symbols we invent a char representation.
		DenseMatrix64F[] leafLike = new DenseMatrix64F[62];
		for (int i = 0; i < 61; i++) {
			double[] v = new double[61];
			v[i] = 1.0;
			DenseMatrix64F a = new DenseMatrix64F(61, 1, true, v);
			leafLike[i] = a;
		}

		double[] v = new double[61];
		for (int j = 0; j < 61; j++) {
			v[j] = 1.0 / 61.0;
		}
		DenseMatrix64F a = new DenseMatrix64F(61, 1, true, v);
		leafLike[61] = a;
		return leafLike;
	}
	
	/**
	 * Returns the integer value of a codon triplet.
	 * Codon needs to override default conversion 
	 * due to the 3 letter coding.
	 * @param codon_str triplet, case insensitive.
	 * @return int state.
	 */
	public int codonStr2int(String codon_str) {
		if (this != SequenceType.CODON) {
			throw new UnsupportedOperationException("Method is only supported for codons.");
		} else if (codon_str.length() != 3) {
			throw new IllegalArgumentException("Invalid codon: " + codon_str);
		}
	
		// TODO: Replace with <triplet,int> hash table.
		final String codons[] = {
				"AAA", "AAC", "AAG", "AAT",
				"ACA", "ACC", "ACG", "ACT",
				"AGA", "AGC", "AGG", "AGT",
				"ATA", "ATC", "ATG", "ATT",
				"CAA", "CAC", "CAG", "CAT",
				"CCA", "CCC", "CCG", "CCT",
				"CGA", "CGC", "CGG", "CGT",
				"CTA", "CTC", "CTG", "CTT",
				"GAA", "GAC", "GAG", "GAT",
				"GCA", "GCC", "GCG", "GCT",
				"GGA", "GGC", "GGG", "GGT",
				"GTA", "GTC", "GTG", "GTT",
				/*TAA*/ "TAC",/*TAG*/ "TAT",
				"TCA", "TCC", "TCG", "TCT",
				/*TGA*/ "TGC", "TGG", "TGT",
				"TTA", "TTC", "TTG", "TTT"
		};
	
		codon_str = codon_str.toUpperCase();
		for (int i = 0; i < 61; i++) {
			if (codon_str == codons[i]) {
				return i;
			}
		}
		return this.alphabet.length() + 1;	// Ambiguity is place 61.
	}
	
	/**
	 * Returns the codon triplet from an integer value.
	 * Codon needs to override default conversion 
	 * due to the 3 letter coding.
	 * @param codon state.
	 * @return triplet in upper case.
	 */
	public String codonInt2str(int codon) {
		if (codon > 61) {
			throw new IllegalArgumentException("Invalid codon state: " + codon);
		}
		final String codons[] = {		// Stop codons are commented out!
				"AAA", "AAC", "AAG", "AAT",
				"ACA", "ACC", "ACG", "ACT",
				"AGA", "AGC", "AGG", "AGT",
				"ATA", "ATC", "ATG", "ATT",
				"CAA", "CAC", "CAG", "CAT",
				"CCA", "CCC", "CCG", "CCT",
				"CGA", "CGC", "CGG", "CGT",
				"CTA", "CTC", "CTG", "CTT",
				"GAA", "GAC", "GAG", "GAT",
				"GCA", "GCC", "GCG", "GCT",
				"GGA", "GGC", "GGG", "GGT",
				"GTA", "GTC", "GTG", "GTT",
				/*TAA*/ "TAC",/*TAG*/ "TAT",
				"TCA", "TCC", "TCG", "TCT",
				/*TGA*/ "TGC", "TGG", "TGT",
				"TTA", "TTC", "TTG", "TTT"
		};
		if (codon < 61) {
			return codons[codon];
		} 
		return "NNN";   // Ambiguity is place 61.
	}
	
}