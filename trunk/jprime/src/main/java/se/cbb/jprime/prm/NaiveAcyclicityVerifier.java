package se.cbb.jprime.prm;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Very naïve (incomplete) verifier of PRM acyclicity.
 * 
 * @author Joel Sjöstrand.
 */
public class NaiveAcyclicityVerifier implements AcyclicityVerifier {

	@Override
	public void isStructureAcyclic(Structure struct) throws DependencyException {
		throw new DependencyException("Structure acyclicity verification not implemented yet!");
	}

	@Override
	public void isStructureAcyclic(Dependency dep, Structure struct)
			throws DependencyException {
		// Already in, then OK to re-add.
		if (struct.hasDependency(dep)) { return; }
		
		// Do BFS search in child-parent direction.
		ProbAttribute child = dep.getChild();
		HashSet<ProbAttribute> tested = new HashSet<ProbAttribute>(8);
		LinkedList<ProbAttribute> q = new LinkedList<ProbAttribute>();
		q.add(dep.getParent());
		while (q.size() > 0) {
			ProbAttribute anc = q.pop();
			if (anc == child) {
				throw new DependencyException("Structure contains cycle.");
			}
			// Retrieve all ancestors of the current ancestor.
			for (Dependency ancDep : struct.getDependencies(anc).getAll()) {
				ProbAttribute ancAnc = ancDep.getParent();
				if (!tested.contains(ancAnc)) {
					q.add(ancAnc);
					tested.add(ancAnc);
				}
			}
		}
		// We're OK if we've reached this!
	}

	@Override
	public void isEntityAcyclic(Structure struct) throws DependencyException {
		throw new DependencyException("PRM realisation acyclicity verification not implemented yet!");
	}

}
