package se.cbb.jprime.apps.genphylodata;

import java.util.ArrayList;

import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Prunability;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.math.NumberManipulation;

/**
 * Pruning helpers.
 * 
 * @author Joel Sjöstrand.
 */
public class PruningHelper {

	/**
	 * Labels the unprunable vertices of an unpruned tree.
	 * @param v the subtree root.
	 * @param nextNo next available name.
	 * @param vertexPrefix vertex prefix.
	 * @return the next available name.
	 */
	public static int labelUnprunableVertices(GuestVertex v, int nextNo, String vertexPrefix) {
		if (v.event == Event.LOSS || v.event == Event.UNSAMPLED_LEAF) {
			v.prunability = Prunability.PRUNABLE;
		} else if (v.event == Event.LEAF) {
			v.prunability = Prunability.UNPRUNABLE;
			v.setNumber(nextNo);
			v.setName(vertexPrefix + nextNo++);
		} else {
			ArrayList<NewickVertex> cs = v.getChildren();
			
			// Special case:
			if (cs.size() == 1) {
				GuestVertex c = (GuestVertex) cs.get(0);
				nextNo = labelUnprunableVertices(c, nextNo, vertexPrefix);
				v.prunability = (c.prunability == Prunability.PRUNABLE) ? Prunability.PRUNABLE : Prunability.COLLAPSABLE;
				return nextNo;
			}
			
			// Label kiddos first.
			GuestVertex lc = (GuestVertex) cs.get(0);
			GuestVertex rc = (GuestVertex) cs.get(1);
			nextNo = labelUnprunableVertices(lc, nextNo, vertexPrefix);
			nextNo = labelUnprunableVertices(rc, nextNo, vertexPrefix);
			
			// Now process vertex based on subtrees' results.
			if (lc.prunability == Prunability.PRUNABLE && rc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.PRUNABLE;
			} else if (lc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.COLLAPSABLE;
			} else if (rc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.COLLAPSABLE;
			} else {
				v.prunability = Prunability.UNPRUNABLE;
				v.setNumber(nextNo);
				v.setName(vertexPrefix + nextNo++);
			}
		}
		return nextNo;
	}
	
	/**
	 * Labels the prunable/collapsable vertices of an unpruned tree after unprunable vertices have been labelled.
	 * This ensures that the vertices of both the pruned and unpruned version of the tree are labelled consistently.
	 * @param v the subtree root.
	 * @param nextNo next available name.
	 * @param vertexPrefix vertex prefix.
	 * @return the next available name.
	 */
	public static int labelPrunableVertices(GuestVertex v, int nextNo, String vertexPrefix) {
		if (!v.isLeaf()) {
			ArrayList<NewickVertex> ch = v.getChildren();
			nextNo = labelPrunableVertices((GuestVertex) ch.get(0), nextNo, vertexPrefix);
			if (ch.size() == 2) {
				nextNo = labelPrunableVertices((GuestVertex) ch.get(1), nextNo, vertexPrefix);
			}
		}
		if (v.getNumber() == -1) {
			v.setNumber(nextNo);
			v.setName(vertexPrefix + nextNo++);
		}
		return nextNo;
	}
	
	
	/**
	 * Creates a pruned deep-copy of an unpruned tree.
	 * @param unprunedRoot the unpruned root.
	 * @return the pruned copy.
	 */
	public static GuestVertex prune(GuestVertex unprunedRoot) {
		if (unprunedRoot.prunability == Prunability.PRUNABLE) {
			// Prune!
			return null;
		}
		
		if (unprunedRoot.event == Event.LEAF) {
			// No pruning.
			return new GuestVertex(unprunedRoot);
		}
		
		// Special case: single unpruned child to start with.
		if (unprunedRoot.getNoOfChildren() == 1) {
			GuestVertex c = prune(unprunedRoot.getLeftChild());
			if (c == null) {
				// Prune!
				return null;
			}
			// Collapse!
			c.setBranchLength(NumberManipulation.roundToSignificantFigures(c.getBranchLength() + unprunedRoot.getBranchLength(), 8));
			return c;
		}
		
		// Prune kids first.
		GuestVertex lc = prune(unprunedRoot.getLeftChild());
		GuestVertex rc = prune(unprunedRoot.getRightChild());
		
		if (lc == null) {
			// Collapse!
			rc.setBranchLength(NumberManipulation.roundToSignificantFigures(rc.getBranchLength() + unprunedRoot.getBranchLength(), 8));
			return rc;
		}
		if (rc == null) {
			// Collapse!
			lc.setBranchLength(NumberManipulation.roundToSignificantFigures(lc.getBranchLength() + unprunedRoot.getBranchLength(), 8));
			return lc;
		}
		
		// No collapsing.
		GuestVertex v = new GuestVertex(unprunedRoot);
		ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(2);
		children.add(lc);
		children.add(rc);
		v.setChildren(children);
		lc.setParent(v);
		rc.setParent(v);
		return v;
	}
}
