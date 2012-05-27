package se.cbb.jprime.seqevo;

/**
 * Substitution model definition.
 * TODO: More object-oriented approach (although low prio...).
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class JC69 {

	/**
	 * Returns the DNA model type described by Jukes & Cantor 1969.
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createJC69(int cacheSize) {
		double[] Pi = new double[4];
		double[] R = new double[6];
		for (int i = 0; i < 4; i++) {
			Pi[i] = 0.25;
		}
		// for (int i = 0; i < 4 * (4 - 1) / 2; i++)
		for (int i = 0; i < 6; i++) {
			R[i] = 1.0;
		}

		return new SubstitutionMatrixHandler("JC69", SequenceType.DNA, R, Pi, cacheSize);
	}
	
}
