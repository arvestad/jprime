package se.cbb.jprime.seqevo;

/**
 * Substitution model definition.
 * TODO: More object-oriented approach (although low prio...).
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class UniformAA {

	/**
	 * Returns the amino acid model type corresponding to that
	 * described by Jukes & Cantor 1969 for DNA.
	 * @param cacheSize matrix cache size. Probably not useful with more than twice the number
	 * of arcs in tree...?
	 * @return the model type.
	 */
	public static SubstitutionMatrixHandler createUniformAA(int cacheSize) {
		double[] Pi = new double[20];
		double[] R = new double[190];
		for (int i = 0; i < 20; i++) {
			Pi[i] = 0.05;
		}
		for (int i = 0; i < 190; i++) {
			R[i] = 1.0;
		}
		return new SubstitutionMatrixHandler("UniformAA", SequenceType.AMINO_ACID, R, Pi, cacheSize);
	}
	
}
