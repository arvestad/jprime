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
	 * @return the next available name.
	 */
	public static int labelUnprunableVertices(GuestVertex v, int nextNo) {
		if (v.event == Event.LOSS || v.event == Event.UNSAMPLED_LEAF) {
			v.prunability = Prunability.PRUNABLE;
		} else if (v.event == Event.SAMPLED_LEAF) {
			v.prunability = Prunability.UNPRUNABLE;
			v.setNumber(nextNo);
			v.setName("G" + nextNo++);
		} else {
			// Label kiddos first.
			GuestVertex lc = (GuestVertex) v.getChildren().get(0);
			GuestVertex rc = (GuestVertex) v.getChildren().get(1);
			nextNo = labelUnprunableVertices(lc, nextNo);
			nextNo = labelUnprunableVertices(rc, nextNo);
			if (lc.prunability == Prunability.PRUNABLE && rc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.PRUNABLE;
			} else if (lc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.COLLAPSABLE;
			} else if (rc.prunability == Prunability.PRUNABLE) {
				v.prunability = Prunability.COLLAPSABLE;
			} else {
				v.prunability = Prunability.UNPRUNABLE;
				v.setNumber(nextNo);
				v.setName("G" + nextNo++);
			}
		}
		return nextNo;
	}
	
	/**
	 * Labels the prunable/collapsable vertices of an unpruned tree after unprunable vertices have been labelled.
	 * @param v the subtree root.
	 * @param nextNo next available name.
	 * @param vertexPrefix vertex prefix.
	 * @return the next available name.
	 */
	public static int labelPrunableVertices(GuestVertex v, int nextNo, String vertexPrefix) {
		if (!v.isLeaf()) {
			nextNo = labelPrunableVertices((GuestVertex) v.getChildren().get(0), nextNo, vertexPrefix);
			nextNo = labelPrunableVertices((GuestVertex) v.getChildren().get(1), nextNo, vertexPrefix);
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
			return null;
		}
		
		if (unprunedRoot.event == Event.SAMPLED_LEAF) {
			return new GuestVertex(unprunedRoot);
		}
				
		// Prune kids first.
		GuestVertex lc = prune(unprunedRoot.getLeftChild());
		GuestVertex rc = prune(unprunedRoot.getRightChild());
		
		if (lc == null) {
			// Collapse.
			rc.setBranchLength(NumberManipulation.roundToSignificantFigures(rc.getBranchLength() + unprunedRoot.getBranchLength(), 8));
			return rc;
		}
		if (rc == null) {
			// Collapse.
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
