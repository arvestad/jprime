package se.cbb.jprime.apps.genphylodata;

import java.util.LinkedList;

import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.topology.Epoch;

/**
 * Represents a guest tree vertex during the generative process.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestVertex extends NewickVertex {
	
	/** Event types. */
	public enum Event {
		SPECIATION,
		LEAF,  // Sampled leaf.
		UNSAMPLED_LEAF,
		DUPLICATION,
		LOSS,
		TRANSFER
	}
	
	/** Prunability status. */
	public enum Prunability {
		UNPRUNABLE,
		PRUNABLE,
		COLLAPSABLE,		
		UNKNOWN
	}
	
	/** Type of event of vertex. */
	Event event;
	
	/** Absolute time. */
	double abstime;
	
	/** Host vertex/arc. */
	int sigma;
	
	/** Epoch. */
	Epoch epoch;
	
	/** Prunability status. */
	Prunability prunability = Prunability.UNKNOWN;
	
	/**
	 * Constructor.
	 * @param event type of event of this vertex.
	 * @param sigma enclosing host arc/vertex ID.
	 * @param epoch enclosing epoch.
	 * @param abstime absolute time of the this vertex.
	 * @param branchtime arc time of this vertex.
	 */
	public GuestVertex(Event event, int sigma, Epoch epoch, double abstime, double branchtime) {
		super(-1, "", branchtime, "");
		this.event = event;
		this.sigma = sigma;
		this.epoch = epoch;
		this.abstime = abstime;
	}
	
	/**
	 * Shallow copy constructor. References to parent or children are not included.
	 * @param orig the original.
	 */
	public GuestVertex(GuestVertex orig) {
		super(orig);
		this.event = orig.event;
		this.abstime = orig.abstime;
		this.sigma = orig.sigma;
		this.epoch = orig.epoch;
		this.prunability = orig.prunability;
	}
	
	/**
	 * Helper.
	 * @return left child.
	 */
	public GuestVertex getLeftChild() {
		return (GuestVertex) this.getChildren().get(0);
	}
	
	/**
	 * Helper.
	 * @return right child.
	 */
	public GuestVertex getRightChild() {
		return (GuestVertex) this.getChildren().get(1);
	}
	
	/**
	 * Helper.
	 * @param root root.
	 */
	public static void setMeta(GuestVertex root) {
		LinkedList<NewickVertex> vertices = new LinkedList<NewickVertex>();
		vertices.add(root);
		while (!vertices.isEmpty()) {
			GuestVertex v = (GuestVertex) vertices.pop();
			StringBuilder sb = new StringBuilder(1024);
			sb.append("[&&PRIME");
			sb.append(" ID=").append(v.getNumber());
			switch (v.event) {
			case DUPLICATION:
				sb.append(" VERTEXTYPE=Duplication");
				break;
			case LOSS:
				sb.append(" VERTEXTYPE=Loss");
				break;
			case TRANSFER:
				sb.append(" VERTEXTYPE=Transfer");
				break;
			case SPECIATION:
				sb.append(" VERTEXTYPE=Speciation");
				break;
			case LEAF:
				sb.append(" VERTEXTYPE=Leaf");
				break;
			case UNSAMPLED_LEAF:
				sb.append(" VERTEXTYPE=UnsampledLeaf");
				break;
			default:
				throw new UnsupportedOperationException("Invalid vertex event type.");	
			}
			sb.append("]");
			v.setMeta(sb.toString());
			if (!v.isLeaf()) {
				vertices.addAll(v.getChildren());
			}
		}
	}
}
