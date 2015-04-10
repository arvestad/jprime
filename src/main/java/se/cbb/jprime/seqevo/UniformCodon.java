package se.cbb.jprime.seqevo;

/**
 * Substitution model definition.
 * TODO: More object-oriented approach (although low prio...).
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class UniformCodon {

	/**
	 * Returns the codon model type corresponding to that
	 * described by Jukes & Cantor 1969 for DNA.
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createUniformCodon(int cacheSize) {
		double[] Pi = new double[61];
		double[] R = new double[1830];
		for (int i = 0; i < 61; ++i) {
			Pi[i] = 1.0 / 61;
		}
		//   for (int i = 0; i < 4 * (4 - 1) / 2; ++i)
		for (int i = 0; i < 1830; ++i) {
			R[i] = 1.0;
		}
		return new SubstitutionMatrixHandler("UniformCodon", SequenceType.CODON, R, Pi, cacheSize);
	}
}
