package se.cbb.jprime.prm;

/**
 * Builds a Bayesian network from a "completed" PRM skeleton.
 * 
 * @author Joel Sjöstrand.
 */
public class BNBuilder {

	/* Algorithm:
	 * 
	 * V := empty set.
	 * E := empty set.
	 * G := G(V,E).
	 * for each class c do:
	 *   for each probabilistic attribute a do:
	 *     if a is latent do:
	 *       P := parent probabilistic attributes of a.
	 *       for each entity a_e of a do:
	 *         if V does not contain a_e do:
	 *           V := V union a_e.
	 *         for each p in P do:
	 *           a_p = parent of a_e in p.
	 *           if V does not contain a_p do:
	 *             V := V union a_p.
	 *           E := E union (a_p,a_e).
	 *           
	 * 
	 */
	
}
