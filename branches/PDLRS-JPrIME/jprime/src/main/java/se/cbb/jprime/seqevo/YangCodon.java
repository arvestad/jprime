package se.cbb.jprime.seqevo;

import java.util.List;

/**
 * Substitution model from Journal of Structural and Functional Genomics 3: 201–212, 2003.
 * TODO: 
 * 
 * @author Owais Mahmudi
 */
public class YangCodon {

	/**
	 * Returns the codon model type corresponding to that
	 * described by Jukes & Cantor 1969 for DNA.
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * 
	 * 				0 = T, 1 = C, 2 = A, 3 = G (implemented as 1 = T, 2 = C, 3 = A, 4 = G)
	 * 		
	 * 		1	000		5	010		09	020		13	030
	 * 		2	001		6	011		10	021		14	.
	 * 		3	002		7	012		11	022		15	.
	 * 		4	003		8	013		12	023		16	.
	 * 
	 * 		17	100		21	110		25	120		29	130
	 * 		18	101		22	... 	26	... 	30	.
	 * 		19	102		23	.		27	.		31	.
	 * 		20	...		24	.		28	.		32	.
	 * 	
	 * 		33	...		37	.
	 * 
	 * 		{ 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21}
	 * 		{ F, L, S, Y, *, C, W, P, H, Q, R, I, M, T, N, K, V, A, D, E, G}
	 * 		Amino acid map for above codon matrix
	 * 		{ 1, 1, 2, 2, 	3, 3, 3, 3,	 4, 4, 5, 5,  6, 6, 5, 7,  		2, 2, 2, 2,  8, 8, 8, 8,  9, 9, 10, 10,  11, 11, 11, 11,  		12, 12, 12, 13,  14, 14, 14, 14,  15, 15, 16, 16,  3, 3, 11, 11, 		17, 17, 17, 17,  18, 18, 18, 18,  19, 19, 20, 20,  21, 21, 21, 21  }
	 * 
	 * 	
	 * 		Pi(j)		if i and j differ by a synonymous transversion
	 * 		K*Pi(j)		if i and j differ by a synonymous transition
	 * 		w*Pi(j)		if i and j differ by a nonsynonymous transversion
	 * 		K*w*Pi(j)	if i and j differ by a nonsynonymous transition
	 * 
	 * 			1 2 3 4 5
	 * 		0	. . . . .
	 * 		1	  . . . .
	 * 		2	    . . .
	 * 		3		  . .
	 * 		4			.
	 * 		
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createYangCodon(Double kappa, Double omega, int cacheSize, boolean allowStopCodons, List<Integer> nucleotideFrequencies) {
		
		
		final int CODONSIZE = SequenceType.CODON.getAlphabetSize();
		int[] aminoacidmap = { 1, 1, 2, 2, 	3, 3, 3, 3,	 4, 4, 5, 5,  6, 6, 5, 7,  		2, 2, 2, 2,  8, 8, 8, 8,  9, 9, 10, 10,  11, 11, 11, 11,  		12, 12, 12, 13,  14, 14, 14, 14,  15, 15, 16, 16,  3, 3, 11, 11, 		17, 17, 17, 17,  18, 18, 18, 18,  19, 19, 20, 20,  21, 21, 21, 21  };
		double[] Pi = new double[CODONSIZE];
		double[] Ro = new double[(CODONSIZE*CODONSIZE-CODONSIZE)/2];
		double sum=0;

		// Uniform equilibrium frequencies 
		for ( int i = 0; i < CODONSIZE; ++i) {
			Pi[i] = 1.0 / CODONSIZE;
		}
		
		int totalfreq =0; for(int i=0; i<nucleotideFrequencies.size(); i++) totalfreq += nucleotideFrequencies.get(i);
		double[] nuclFreq = new double[nucleotideFrequencies.size()]; 
		for(int i=0; i<nucleotideFrequencies.size(); i++) nuclFreq[i] = nucleotideFrequencies.get(i)/(double)totalfreq;
		
		for (int i = 0; i < CODONSIZE; ++i) {
			String codon = SequenceType.CODON.codonInt2str(i);
			double codonFrequency = nuclFreq[SequenceType.DNA.char2int(codon.charAt(0))] * nuclFreq[SequenceType.DNA.char2int(codon.charAt(1))] * nuclFreq[SequenceType.DNA.char2int(codon.charAt(2))];
			Pi[i]=codonFrequency;
			sum=sum+Pi[i];
//			Pi[i]=1.0 / CODONSIZE;
		}
		
		for ( int i = 0; i < CODONSIZE; ++i) {
			Pi[i] = Pi[i] / sum;
		}

		
		// Fill the top triangle above diagonal with values based on kappa and omega provided
		int j=0, tmp=j;
		int count = 0;
		for(int i=0;i<CODONSIZE;i++)
		{	j=tmp+1;
			tmp=j;
			for(;j<CODONSIZE;j++)
			{
//				(i,j) covers top triangle of the matrix above the diagonal of the matrix
//				System.out.print(" ["+i+","+j+"] ");
				Ro[count]= computeTransitionExchangeability(omega, kappa, SequenceType.CODON.codonInt2str(i), SequenceType.CODON.codonInt2str(j), aminoacidmap, Pi, allowStopCodons );
				count++;
			}
		}
		
		
//		Prints the coded matrix for codons	
//		int codon=0;
//		for (int i =0; i<4; i++)
//		{
//			
//			for (int k=0; k<4; k++)
//			{
//				for(int l = 0; l<4; l++)
//				{	
//					codon++;
////					System.out.println(codon+ " ");
//					int cod =  decodeIndex(codon);
//					char codo = 'l';
//					switch ((int) Math.floor(cod/100)){
//					case 1:
//						codo = 'T';
//						break;
//					case 2:
//						codo = 'C';
//						break;
//					case 3:
//						codo = 'A';
//						break;
//					case 4:
//						codo = 'G';
//						break;						
//					}
//					System.out.print("\""+codo);
//					cod = cod%100;
//					switch ((int) Math.floor(cod/10)){
//					case 1:
//						codo = 'T';
//						break;
//					case 2:
//						codo = 'C';
//						break;
//					case 3:
//						codo = 'A';
//						break;
//					case 4:
//						codo = 'G';
//						break;						
//					}
//					System.out.print(codo);
//					cod = cod%10;
//					switch (cod){
//					case 1:
//						codo = 'T';
//						break;
//					case 2:
//						codo = 'C';
//						break;
//					case 3:
//						codo = 'A';
//						break;
//					case 4:
//						codo = 'G';
//						break;						
//					}
//					System.out.print(codo+"\",  ");
//				}
//				System.out.println();
//			}
//			System.out.println();
//		}
		
//		Prints the coded matrix for codons	
//		int codon=0;
//		for (int i =0; i<4; i++)
//		{
//			
//			for (int k=0; k<4; k++)
//			{
//				for(int l = 0; l<4; l++)
//				{	
//					codon++;
//					System.out.print(codon + " " + decodeIndex(codon) + "\t"); 
//				}
//				System.out.println();
//			}
//			System.out.println();
//		}
		
		
//		Codon-Index Table
//		
//		1 111		2 112		3 113		4 114	
//		5 121		6 122		7 123		8 124	
//		9 131		10 132		11 133		12 134	
//		13 141		14 142		15 143		16 144	
//
//		17 211		18 212		19 213		20 214	
//		21 221		22 222		23 223		24 224	
//		25 231		26 232		27 233		28 234	
//		29 241		30 242		31 243		32 244	
//
//		33 311		34 312		35 313		36 314	
//		37 321		38 322		39 323		40 324	
//		41 331		42 332		43 333		44 334	
//		45 341		46 342		47 343		48 344	
//
//		49 411		50 412		51 413		52 414	
//		53 421		54 422		55 423		56 424	
//		57 431		58 432		59 433		60 434	
//		61 441		62 442		63 443		64 444	
 
//		
//		"TTT",  "TTC",  "TTA",  "TTG",  
//		"TCT",  "TCC",  "TCA",  "TCG",  
//		"TAT",  "TAC",  "TAA",  "TAG",  
//		"TGT",  "TGC",  "TGA",  "TGG",  
//
//		"CTT",  "CTC",  "CTA",  "CTG",  
//		"CCT",  "CCC",  "CCA",  "CCG",  
//		"CAT",  "CAC",  "CAA",  "CAG",  
//		"CGT",  "CGC",  "CGA",  "CGG",  
//
//		"ATT",  "ATC",  "ATA",  "ATG",  
//		"ACT",  "ACC",  "ACA",  "ACG",  
//		"AAT",  "AAC",  "AAA",  "AAG",  
//		"AGT",  "AGC",  "AGA",  "AGG",  
//
//		"GTT",  "GTC",  "GTA",  "GTG",  
//		"GCT",  "GCC",  "GCA",  "GCG",  
//		"GAT",  "GAC",  "GAA",  "GAG",  
//		"GGT",  "GGC",  "GGA",  "GGG",  
		SubstitutionMatrixHandler q = new SubstitutionMatrixHandler("YangCodon", SequenceType.CODON, Ro, Pi, cacheSize);
		if(q.getInvalidParameters())
			return null;
		else
			return q; 
	}
	
	
	private static double computeTransitionExchangeability(Double omega,
			Double kappa, String codon1, String codon2, int[] aminoacidmap, double[] Pi, boolean allowStopCodons) {
		boolean transition=false, nonsynonymous = false;
		
		//  1. find if the difference is one nucleic acid? 
		//		a. if i and j differ by a synonymous transversion
		// 		b. if i and j differ by a synonymous transition
		//		c. if i and j differ by a nonsynonymous transversion
		//		d. if i and j differ by a nonsynonymous transition
		// synonymous means the same value in aminoacidmap, non-synonymous means different values
		// transition is between A(3),G(4) and C(2),T(1), transversion are all others
		// stop codons are checked first
		
		if(!allowStopCodons)
			if(codon2.equals("133") || codon2.equals("134") || codon2.equals("143"))
				return 0;
		
		int differntChars = differences(SequenceType.CODON.codonStr2int(codon1), SequenceType.CODON.codonStr2int(codon2));
		
	
		if (differntChars == 1)
		{
			transition = isTransition(codon1, codon2);
			
			if(aminoacidmap[SequenceType.CODON.codonStr2int(codon1)/*encodeCodon(Integer.parseInt(codon1))-1*/] != aminoacidmap[SequenceType.CODON.codonStr2int(codon2)/*encodeCodon(Integer.parseInt(codon2))-1*/])
				nonsynonymous = true;
			else
				nonsynonymous = false;
			
			if(nonsynonymous == true && transition == false)
			return  omega;
			
			else if(nonsynonymous == true && transition == true)
			return kappa * omega;
			
			else if(nonsynonymous == false && transition == false)
			return 1;
			
			else if(nonsynonymous == false && transition == true)
			return kappa;
			
			return -1;
		}else
		return 0;
	}

	public static boolean isTransition(String str_a, String str_b)
	{
		final int CODONLENTGH=3;
    	int i=0;
    	for(; i<CODONLENTGH; i++){
    		if(str_a.charAt(i) != str_b.charAt(i))
    			break;
    	}
        	
        assert(i<CODONLENTGH);
        
        if( (str_a.charAt(i)=='A' && str_b.charAt(i)=='G') || (str_a.charAt(i)=='G' && str_b.charAt(i)=='A')
                || (str_a.charAt(i)=='C' && str_b.charAt(i)=='T') || (str_a.charAt(i)=='T' && str_b.charAt(i)=='C') )
            return true;
        else
            return false;	
	}        
	
	public static int differences(int codon_a, int codon_b)
	{
		final int CODONLENTGH=3;
		String str_a = SequenceType.CODON.codonInt2str(codon_a);
		String str_b = SequenceType.CODON.codonInt2str(codon_b);
		
        int difference=0, idx=0;
        for(int i=0; i<CODONLENTGH; i++)
        {       
            if(str_a.charAt(i)!=str_b.charAt(i)){
                difference++;
            }
        }		
		return difference;
		
	}

	public static int decodeIndex(int index)
	{	
		int codon = -1;
		
		int r = (int) Math.floor(index/16);
		if(index%16==0) return ((r-1)*100 + 33 +111);
		int rem = index - r*16;
		int c = (int) Math.floor(rem/4);
		if(rem%4 == 0) c--;
		int remainder = index%4;
		int corr = (remainder == 0)? 4:remainder;
		return (r*100 +c*10 +corr-1 +111);	
	}
	
	public static int encodeCodon(int codon)
	{
		codon = codon -111;
		int a = (int) Math.floor(codon/100);
		int rem = codon % 100;
		int b = (int) Math.floor(rem/10);
		int remb = rem % 10;
		return (a*16 + b*4 + remb +1 );
	}
}
