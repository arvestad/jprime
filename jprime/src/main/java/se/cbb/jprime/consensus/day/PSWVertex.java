package se.cbb.jprime.consensus.day;

import java.util.LinkedList;

import se.cbb.jprime.misc.IntTriple;

/**
 * Multifurcating vertex of a PSW tree. Holds some of the information
 * used by Day's algorithms, etc.:
 * <ul>
 * <li>If a leaf, integer label (as numbered in a DFS traversal unless created
 * with an existing tree as a template). Interior vertices have number -1.</li>
 * <li>If a leaf, the name of the vertex.
 * <li>The smallest integer label of the leaves in the subtree rooted at the vertex.</li>
 * <li>The largest integer label of the leaves in the subtree rooted at the vertex.</li>
 * <li>Weight, i.e. the number of proper descendants of the vertex.</li>
 * <li>The number of leaves of the subtree rooted at the vertex.</li>
 * <li>For reference, the original number of the vertex in the tree from
 * which the PSW tree was created.</li>
 * </ul>
 * The min, max, and weight above constitute the "post-order sequence with weights",
 * PSW, info.
 * 
 * @author Joel Sjöstrand.
 */
public class PSWVertex {

	/** Integer label. -1 if not a leaf. */
	private int number = -1;
	
	/** Name. Null if not a leaf. */
	private String name = null;
	
	/**
	 * Min and max labels of leaves in subtree below, followed by
	 * the number of proper descendants.
	 */
	private IntTriple minMaxWeight = null;
	
	/** The number of leaves of the subtree. */
	private int noOfLeaves;
	
	/** Original number in the input tree. */
	private int originalNumber;
	
	/** Parent. Null for root. */
	private PSWVertex parent = null;
	
	/** Children. */
	private LinkedList<PSWVertex> children = new LinkedList<PSWVertex>();
	
	/**
	 * Constructor.
	 * @param originalNumber the original number of this vertex.
	 */
	public PSWVertex(int originalNumber) {
		this.originalNumber = originalNumber;
	}
	
	/**
	 * Constructor for leaves.
	 * @param name the name of this leaf.
	 * @param originalNumber the original number of this leaf.
	 */
	public PSWVertex(String name, int originalNumber) {
		this.name = name;
		this.originalNumber = originalNumber;
	}
	
	/**
	 * Copy constructor which recursively deep copies the tree rooted at another vertex.
	 * @param copy the root of the tree to copy.
	 */
	public PSWVertex(PSWVertex copy) {
		this.number = copy.number;
		this.name = copy.name;
		this.minMaxWeight = (copy.minMaxWeight == null ? null :
			new IntTriple(copy.minMaxWeight.first, copy.minMaxWeight.second, copy.minMaxWeight.third));
		this.noOfLeaves = copy.noOfLeaves;
		this.originalNumber = copy.noOfLeaves;
		this.parent = null; // Set later.
		this.children = new LinkedList<PSWVertex>();
		for (PSWVertex c : copy.children) {
			PSWVertex d = new PSWVertex(c);
			d.setParent(this);
			this.children.add(d);
		}
	}
	
	/**
	 * Returns the integer label of this vertex.
	 * @return the integer label, -1 if an interior vertex.
	 */
	public int getNumber() {
		return this.number;
	}
	
	/**
	 * Sets the integer label of this vertex.
	 * Leaves are numbered from 0 to |L(T)|-1 while interior vertices
	 * should be -1.
	 * @param number the number to be set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	
	/**
	 * Returns the name of this vertex.
	 * @return the name if a leaf; null if an interior vertex.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the min and max numbers of the leaves in the subtree rooted
	 * at the vertex, along with the weight.
	 * @return min and max.
	 */
	public IntTriple getMinMaxWeight() {
		return this.minMaxWeight;
	}
	
	/**
	 * Returns the smallest number of the leaves in the subtree rooted
	 * at the vertex.
	 * @return min.
	 */
	public int getMin() {
		return this.minMaxWeight.first;
	}
	
	/**
	 * Returns the largest number of the leaves in the subtree rooted
	 * at the vertex.
	 * @return max.
	 */
	public int getMax() {
		return this.minMaxWeight.second;
	}
	
	/**
	 * Returns the number of proper descendants of this vertex, e.g. 0 for a leaf.
	 * @return the number of proper descendants.
	 */
	public int getWeight() {
		return this.minMaxWeight.third;
	}
	
	/**
	 * Returns the number of leaves of the subtree rooted at this vertex, e.g. 1 for
	 * a leaf.
	 * @return the number of leaves.
	 */
	public int getNoOfLeaves() {
		return this.noOfLeaves;
	}
	
	/**
	 * Returns true if the min and max label span
	 * of this vertex is compatible with the number of leaves, i.e.
	 * the subtree contains all the leaves min,min+1,...,max.
	 * @return
	 */
	public boolean hasContiguousLeafSpan() {
		return (this.minMaxWeight.second-this.minMaxWeight.first+1 == this.noOfLeaves);
	}
	
	/**
	 * For reference, returns the original number the vertex corresponding
	 * to this had in the original input tree. Do not confuse this with
	 * the regular number, which is the one used in Day algorithms.
	 * @return the original number.
	 */
	public int getOriginalNumber() {
		return this.originalNumber;
	}
	
	/**
	 * Returns the parent of this vertex.
	 * @return the parent, null if the root.
	 */
	public PSWVertex getParent() {
		return this.parent;
	}
	
	/**
	 * Sets the parent of this vertex.
	 * @param parent the parent, null if the root.
	 */
	public void setParent(PSWVertex parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the children of this vertex.
	 * @return the children, an empty list (not null) if lacking children.
	 */
	public LinkedList<PSWVertex> getChildren() {
		return this.children;
	}
	
	/**
	 * Returns the number of children of this vertex.
	 * @return the number of children.
	 */
	public int getNoOfChildren() {
		return this.children.size();
	}
	
	/**
	 * Returns true if this vertex has no parent.
	 * @return true if lacking a parent.
	 */
	public boolean isRoot() {
		return (this.parent == null);
	}
	
	/**
	 * Returns true if this vertex has no children.
	 * @return true if lacking children.
	 */
	public boolean isLeaf() {
		return this.children.isEmpty();
	}

	/**
	 * Prepends a vertex to the list of children.
	 * @param child the vertex to be added first to the list of children.
	 */
	public void addChildFirst(PSWVertex child) {
		this.children.addFirst(child);
	}
	
	/**
	 * Adds a child to the end of the list of children.
	 * @param child the child to be added to the end of the list of children.
	 */
	public void addChild(PSWVertex child) {
		this.children.add(child);
	}
	
	/**
	 * Removes a child from the list of children.
	 * @param child the child to be removed.
	 * @return true if the list did contain the child; false if it was not found.
	 */
	public boolean removeChild(PSWVertex child) {
		return this.children.remove(child);
	}
	
	/**
	 * Computes min, max and weight properties recursively for the subtree rooted
	 * at this vertex, and counts the leaves.
	 */
	public void computePSW() {
		if (this.isLeaf()) {
			this.minMaxWeight = new IntTriple(this.number, this.number, 0);
			this.noOfLeaves = 1;
		} else {
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int weight = 0;
			this.noOfLeaves = 0;
			for (PSWVertex c : this.children) {
				c.computePSW();
				min = Math.min(min, c.getMin());
				max = Math.max(max, c.getMax());
				weight += (c.getWeight() + 1);
				this.noOfLeaves += c.noOfLeaves;
			}
			this.minMaxWeight = new IntTriple(min, max, weight);
		}
	}
}
