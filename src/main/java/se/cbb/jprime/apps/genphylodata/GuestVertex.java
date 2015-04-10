package se.cbb.jprime.apps.genphylodata;

import java.util.LinkedList;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.topology.Epoch;

/**
 * Represents a guest tree vertex during the generative process.
 * 
 * @author Joel Sjöstrand.
 * @author Mehmood Alam Khan
 */
public class GuestVertex extends NewickVertex {
	
	/** Event types. */
	public enum Event {
		SPECIATION,
		LEAF,             // Sampled leaf.
		UNSAMPLED_LEAF,   // Unsampled leaf.
		DUPLICATION,
		LOSS,
		TRANSFER,
		HYBRID_DONATION,
		HYBRID_DONATION_FROM_EXTINCT_DONOR,
		ALLOPLOIDIC_HYBRID_RECEPTION,  // This is only one of the lineages of the polyploidisation.
		AUTOPLOIDIC_HYBRID_RECEPTION,  // Obligate duplication due to polyploidisation.
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
	
	/** Tranfered from arc */
	int transferedFromArc = 0;
	
	/** Tranfered to arc */
	int transferedToArc = 0;
	
	/** Epoch. Not always applicable. */
	Epoch epoch = null;
	
	/** Prunability status. */
	Prunability prunability = Prunability.UNKNOWN;
	
	/**
	 * Host arcs that the arc (where this vertex is head) passes by.
	 * Only applicable for pruned trees.
	 */
	//List<Integer> hostArcs = null;
	
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
	
	public void setTransferedToArc(int x){
		this.transferedToArc= x;
	}
	
	public int getTransferedToArc(){
		return this.transferedToArc;
	}
	
	public void setTransferedFromArc(int x){
		this.transferedFromArc= x;
	}
	
	public int getTransferedFromArc(){
		return this.transferedFromArc;
	}
	
	/**
	 * Helper. Returns the left child. If there is a single child, returns that one.
	 * @return left child.
	 */
	public GuestVertex getLeftChild() {
		if (this.isLeaf()) { return null; }
		return (GuestVertex) this.getChildren().get(0);
	}
	
	/**
	 * Helper. Returns the right child. If there is a single child, null is returned.
	 * @return right child.
	 */
	public GuestVertex getRightChild() {
		if (this.getChildren().size() == 1) { return null; }
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
				double [] disTimes= v.epoch.getTimes();
				int j=0;
				while (true && j < disTimes.length){
					if (disTimes[j] >= v.abstime){
						break;
					}	
					++j;
				}
				String dispt= "DISCPT=(" + v.epoch.getNo() + "," + j +")";
				sb.append(" VERTEXTYPE=Duplication" + " "+ dispt);
				break;
				
			case LOSS:
				sb.append(" VERTEXTYPE=Loss");
				break;
			case TRANSFER:
				sb.append(" VERTEXTYPE=Transfer");
				String fromToArc= "("+v.getTransferedFromArc()+","+ v.getTransferedToArc()+")";
		
				double [] discTimes= v.epoch.getTimes();
				int i=0;
				while (true && i < discTimes.length){
					if (discTimes[i] >= v.abstime){
						break;
					}	
					++i;
				}
				String discpt= "DISCPT=(" + v.epoch.getNo() + "," + i +")";
				
				String speciesEdge= "SPECIES_EDGE=("+ v.getTransferedFromArc() +","+ v.epoch.getNoOfArcs() +")";
				sb.append(" FROMTOLINEAGE="+ fromToArc +" "+ speciesEdge + " "+ discpt);
				break;
			case SPECIATION:
				double [] disctTimes= v.epoch.getTimes();
				int k=0;
				while (true && k < disctTimes.length){
					if (disctTimes[k] >= v.abstime){
						break;
					}	
					++k;
				}
				String disctpt= "DISCPT=(" + v.epoch.getNo() + "," + k +")";
				sb.append(" VERTEXTYPE=Speciation"+ " "+ disctpt);
				break;
			case LEAF:
				sb.append(" VERTEXTYPE=Leaf");
				break;
			case UNSAMPLED_LEAF:
				sb.append(" VERTEXTYPE=UnsampledLeaf");
				break;
			case HYBRID_DONATION:
				sb.append(" VERTEXTYPE=HybridDonation");
				break;
			case HYBRID_DONATION_FROM_EXTINCT_DONOR:
				sb.append(" VERTEXTYPE=HybridDonationFromExtinctDonor");
				break;
			case ALLOPLOIDIC_HYBRID_RECEPTION:
				sb.append(" VERTEXTYPE=AlloploidicHybridReception");
				break;
			case AUTOPLOIDIC_HYBRID_RECEPTION:
				sb.append(" VERTEXTYPE=AutoploidicHybridReception");
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
