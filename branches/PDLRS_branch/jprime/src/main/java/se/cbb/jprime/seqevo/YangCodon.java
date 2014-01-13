package se.cbb.jprime.seqevo;

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
	 * 
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createYangCodon(Double kappa, Double omega, int cacheSize, boolean allowStopCodons) {
		final int CODONSIZE = 64;
		int[] aminoacidmap = { 1, 1, 2, 2, 	3, 3, 3, 3,	 4, 4, 5, 5,  6, 6, 5, 7,  		2, 2, 2, 2,  8, 8, 8, 8,  9, 9, 10, 10,  11, 11, 11, 11,  		12, 12, 12, 13,  14, 14, 14, 14,  15, 15, 16, 16,  3, 3, 11, 11, 		17, 17, 17, 17,  18, 18, 18, 18,  19, 19, 20, 20,  21, 21, 21, 21  };
		double[] Pi = new double[CODONSIZE];
		double[] Ro = new double[(CODONSIZE*CODONSIZE-CODONSIZE)/2];

		// Uniform equilibrium frequencies 
		for (int i = 0; i < CODONSIZE; ++i) {
			Pi[i] = 1.0 / CODONSIZE;
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
				Ro[count]= computeTransitionExchangeability(omega, kappa, String.valueOf(decodeIndex(i+1)), String.valueOf(decodeIndex(j+1)), aminoacidmap, Pi, allowStopCodons );
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
		
		
		return new SubstitutionMatrixHandler("YangCodon", SequenceType.CODON, Ro, Pi, cacheSize);
	}
	
	
	private static double computeTransitionExchangeability(Double omega,
			Double kappa, String codon1, String codon2, int[] aminoacidmap, double[] Pi, boolean allowStopCodons) {
		final int CODONS = 3;
		boolean transition=false, synonymous = false;
		
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
		
		int differntChars = 0, position = 0;
		
		for(int i=0; i<CODONS; i++)
		{
			if(codon1.charAt(i) != codon2.charAt(i))
			{
				differntChars++;
				position = i;
			}
		}
		
	
		if (differntChars == 1)
		{
			if( ((codon1.charAt(position)) == '1' && (codon2.charAt(position)) == '2' ) ||
					((codon1.charAt(position)) == '2' && (codon2.charAt(position)) == '1' ) ||
					((codon1.charAt(position)) == '3' && (codon2.charAt(position)) == '4' ) ||
					((codon1.charAt(position)) == '4' && (codon2.charAt(position)) == '3' ))
				transition=true;
			else
				transition=false;
			
			if(aminoacidmap[encodeCodon(Integer.parseInt(codon1))-1] == aminoacidmap[encodeCodon(Integer.parseInt(codon2))-1])
				synonymous = true;
			else
				synonymous = false;
			
			if(synonymous == true && transition == false)
			return  Pi[encodeCodon(Integer.parseInt(codon2))-1];
			
			else if(synonymous == true && transition == true)
			return kappa * Pi[encodeCodon(Integer.parseInt(codon2))-1];
			
			else if(synonymous == false && transition == false)
			return omega*Pi[encodeCodon(Integer.parseInt(codon2))-1];
			
			else if(synonymous == false && transition == true)
			return kappa*omega*Pi[encodeCodon(Integer.parseInt(codon2))-1];
			
			return -1;
		}else
		return 0;
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
