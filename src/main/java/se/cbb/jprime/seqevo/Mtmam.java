package se.cbb.jprime.seqevo;

/**
 * Substitution model definition.
 * @author Gustaf Pihl.
 */
public class Mtmam {

	/**
	 * Returns the model type described by (Cao et al., 1998; Yang, Nielsen, and Hasegawa, 1998).
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createMtmam(int cacheSize) {
		double[] Pi = {
		     0.0692, 0.0184, 0.0400, 0.0186, 0.0065, 0.0238, 0.0236, 0.0557, 0.0277, 0.0905, 0.1675, 0.0221, 0.0561, 0.0611, 0.0536, 0.0725, 0.0870, 0.0293, 0.0340, 0.0428
		};
		
		double[] R = {
		     32, 2, 11, 0, 0, 0, 78, 8, 75, 21, 0, 76, 0, 53, 342, 681, 5, 0, 398, 
		     4, 0, 186, 246, 0, 18, 232, 0, 6, 50, 0, 0, 9, 3, 0, 16, 0, 0, 
		     864, 0, 8, 0, 47, 458, 19, 0, 408, 21, 6, 33, 446, 110, 6, 156, 0, 
		     0, 49, 569, 79, 11, 0, 0, 0, 0, 5, 2, 16, 0, 0, 0, 10, 
		     0, 0, 0, 305, 41, 27, 0, 0, 7, 0, 347, 114, 65, 530, 0, 
		     274, 0, 550, 0, 20, 242, 22, 0, 51, 30, 0, 0, 54, 33, 
		     22, 22, 0, 0, 215, 0, 0, 0, 21, 4, 0, 0, 20, 
		     0, 0, 0, 0, 0, 0, 0, 112, 0, 0, 1, 5, 
		     0, 26, 0, 0, 0, 53, 20, 1, 0, 1525, 0, 
		     232, 6, 378, 57, 5, 0, 360, 0, 16, 2220, 
		     4, 609, 246, 43, 74, 34, 12, 25, 100, 
		     59, 0, 18, 65, 50, 0, 67, 0, 
		     11, 0, 47, 691, 13, 0, 832, 
		     17, 90, 8, 0, 682, 6, 
		     202, 78, 7, 8, 0, 
		     614, 17, 107, 0, 
		     0, 0, 237, 
		     14, 0, 
		     0
		};

		return new SubstitutionMatrixHandler("Mtmam", SequenceType.AMINO_ACID, R, Pi, cacheSize);
	}

}