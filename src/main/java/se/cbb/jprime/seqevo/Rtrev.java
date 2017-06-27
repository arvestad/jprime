package se.cbb.jprime.seqevo;

/**
 * Substitution model definition.
 * @author Gustaf Pihl.
 */
public class Rtrev {

	/**
	 * Returns the model type described by (Dimmic et al., 2002).
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createRtrev(int cacheSize) {
		double[] Pi = {
		     0.0646, 0.0453, 0.0376, 0.0422, 0.0114, 0.0606, 0.0607, 0.0639, 0.0273, 0.0679, 0.1018, 0.0751, 0.015, 0.0287, 0.0681, 0.0488, 0.0622, 0.0251, 0.0318, 0.0619
		};
		
		double[] R = {
		     34, 51, 10, 439, 32, 81, 135, 30, 1, 45, 38, 235, 1, 97, 460, 258, 5, 55, 197, 
		     35, 30, 92, 221, 10, 41, 90, 24, 18, 593, 57, 7, 24, 102, 64, 13, 47, 29, 
		     384, 128, 236, 79, 94, 320, 35, 15, 123, 1, 49, 33, 294, 148, 16, 28, 21, 
		     1, 78, 542, 61, 91, 1, 5, 20, 1, 1, 55, 136, 55, 1, 1, 6, 
		     70, 1, 48, 124, 104, 110, 16, 156, 70, 1, 75, 117, 55, 131, 295, 
		     372, 18, 387, 33, 54, 309, 158, 1, 68, 225, 146, 10, 45, 36, 
		     70, 34, 1, 21, 141, 1, 1, 52, 95, 82, 17, 1, 35, 
		     68, 1, 3, 30, 37, 7, 17, 152, 7, 23, 21, 3, 
		     34, 51, 76, 116, 141, 44, 183, 49, 48, 307, 1, 
		     385, 34, 375, 64, 10, 4, 72, 39, 26, 1048, 
		     23, 581, 179, 22, 24, 25, 47, 64, 112, 
		     134, 14, 43, 77, 110, 6, 1, 19, 
		     247, 1, 1, 131, 111, 74, 236, 
		     11, 20, 69, 182, 1017, 92, 
		     134, 62, 9, 14, 25, 
		     671, 14, 31, 39, 
		     1, 34, 196, 
		     176, 26, 
		     59
		};

		return new SubstitutionMatrixHandler("Rtrev", SequenceType.AMINO_ACID, R, Pi, cacheSize);
	}

}