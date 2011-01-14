package se.cbb.jprime.prm;

/**
 * Interface for verifiers of acyclicity on a PRM dependency structure or
 * induced Bayesian network.
 * <p/>
 * There are two acyclicity conditions of different strengths:
 * <ol>
 * <li>The structure itself is acyclic, i.e. the "template graph" is acyclic.
 *     This is the stricter of the two.</li>
 * <li>The structure itself is not acyclic, but the complete Bayesian network induced
 *     by the PRM entities are acyclic. This is weaker (and significantly tougher to verify),
 *     but may be desirable when modelling more complex PRMs. </li>
 * </ol>
 * For more details, see section 4.1, <i>Learning Probabilistic Relational
 * Models, Friedman et al., 1999</i>.
 * 
 * @author Joel Sjöstrand.
 */
public interface AcyclicityVerifier {

	/**
	 * Verifies that a PRM structure is in itself acyclic, i.e., the stronger
	 * of the two conditions (see also <code>isEntityAcyclic()</code>).
	 * @param struct the structure.
	 * @throws DependencyException if structure invalid.
	 */
	public void isStructureAcyclic(Structure struct) throws DependencyException;
	
	/**
	 * Verifies that the structure resulting from adding a dependency is acyclic,
	 * preconditioned on the structure being acyclic before the addition.
	 * This corresponds to the stronger condition, see <code>isStructureAcyclic()</code>.
	 * @param dep the dependency to be added. 
	 * @param struct the existing structure. Assumed to be correct.
	 * @throws DependencyException if dependency cannot be added.
	 */
	public void isStructureAcyclic(Dependency dep, Structure struct) throws DependencyException;
	
	
	/**
	 * Verifies that a PRM realisation (Bayesian network induced by skeleton,
	 * structure and entities), are acyclic. This is the weaker, of the two
	 * conditions (see also <code>isStructureAcyclic()</code>).
	 * @param struct the structure, providing access also to entities, etc.
	 * @throws DependencyException if PRM realisation is invalid.
	 */
	public void isEntityAcyclic(Structure struct) throws DependencyException;
}
