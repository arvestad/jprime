package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Holds a "pure" multifurcating Newick vertex or tree,
 * typically used only as an intermediate form after reading tree input before
 * creating more specialised representations (bifurcating, ultrametric, non-node based, etc.).
 * Additional meta info (provided between brackets for a vertex) is
 * held only as a string.
 * <p/>
 * Each vertex has containers for vertex number (ID), name, branch length, and additional meta info.
 * All containers except number may take on empty values, in which case null is preferred.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickVertex {
	
	/** Vertex number. */
	private int number = -1;
	
	/** Vertex name. */
	private String name = null;
	
	/** Branch length. */
	private Double branchLength = null;
	
	/** Meta info between brackets (brackets normally also included). */
	private String meta = null;
	
	/** Parent. */
	private NewickVertex parent = null;
	
	/** Child vertices. */
	private ArrayList<NewickVertex> children = null;
	
	/**
	 * Constructor. Creates an empty vertex.
	 */
	public NewickVertex() {
	}
	
	/**
	 * Constructor.
	 * @param name vertex name.
	 * @param branchLength branch length.
	 * @param meta additional vertex info provided between brackets.
	 */
	public NewickVertex(int number, String name, Double branchLength, String meta) {
		this.setValues(number, name, branchLength, meta);
	}
	
	/**
	 * Copy constructor.
	 * Note: the parent and children of v are not copied, ie. their references are not changed.
	 * @param v NewickVertex to copy.
	 */
	public NewickVertex(NewickVertex v) {
		this.number = v.number;
		this.name = v.name;
		this.branchLength = v.branchLength;
		this.meta = v.meta;
		this.parent = v.parent;
		this.children = v.children;
	}
	
	/**
	 * Sets all values.
	 * @param name vertex name.
	 * @param branchLength branch length.
	 * @param meta additional vertex info provided between brackets.
	 */
	public void setValues(int number, String name, Double branchLength, String meta) {
		this.number = number;
		this.name = name;
		this.branchLength = branchLength;
		this.meta = meta;
	}
	
	/**
	 * Returns the vertex number.
	 * @return the vertex number;
	 */
	public int getNumber() {
		return this.number;
	}
	
	/**
	 * Sets the vertex number.
	 * @param number the new number.
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	
	/**
	 * Returns the name. May be null.
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the name. May be null.
	 * @param name the name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the branch length. May be null.
	 * @return the branch length.
	 */
	public Double getBranchLength() {
		return this.branchLength;
	}
	
	/**
	 * Sets the branch length. May be null.
	 * @param branchLength the branch length.
	 */
	public void setBranchLength(Double branchLength) {
		this.branchLength = branchLength;
	}
	
	/**
	 * Returns the meta info (usually provided between brackets) of this vertex.
	 * Information may be enclosed in brackets.
	 * May be null.
	 * @return the additional info.
	 */
	public String getMeta() {
		return this.meta;
	}
	
	/**
	 * Sets the meta info (usually provided between brackets) of this vertex.
	 * May be null.
	 * @param meta the additional info.
	 */
	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	/**
	 * Returns the parent. Null if the root.
	 * @return the parent.
	 */
	public NewickVertex getParent() {
		return this.parent;
	}
	
	/**
	 * Sets the parent.
	 * @param parent the new parent.
	 */
	public void setParent(NewickVertex parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the children. May be empty or null for a leaf, the latter being preferred.
	 * @return the children.
	 */
	public ArrayList<NewickVertex> getChildren() {
		return this.children;
	}
	
	/**
	 * Sets children. May be null if no children.
	 * @param children the children.
	 */
	public void setChildren(ArrayList<NewickVertex> children) {
		this.children = children;
	}
	
	/**
	 * Returns the number of children.
	 * @return the number of children.
	 */
	public int getNoOfChildren() {
		return (this.children == null ? 0 : this.children.size());
	}
	
	/**
	 * Returns the vertex degree.
	 * @return the vertex degree.
	 */
	public int getDegree() {
		int degree = this.getNoOfChildren();
		if (!this.isRoot())
			degree++;
		return degree;
	}
	
	/**
	 * Returns true if name is not null.
	 * Empty string returns true.
	 * @return true if vertex has name.
	 */
	public boolean hasName() {
		return (this.name != null);
	}
	
	/**
	 * Returns true if branch length is not null.
	 * @return true if vertex has branch length.
	 */
	public boolean hasBranchLength() {
		return (this.branchLength != null);
	}
	
	/**
	 * Returns true if meta info is not null.
	 * Empty string returns true, as does
	 * empty string enclosed in brackets.
	 * @return true if vertex has meta info.
	 */
	public boolean hasMeta() {
		return (this.meta != null);
	}
	
	/**
	 * Returns true if this vertex is not a leaf.
	 * @return true if not a leaf.
	 */
	public boolean hasChildren() {
		return (this.children != null && !this.children.isEmpty());
	}
	
	/**
	 * Returns true if this vertex lacks a parent.
	 * @return true if lacking a parent.
	 */
	public boolean isRoot() {
		return (this.parent == null);
	}
	
	/**
	 * Returns true if children is null or empty.
	 * @return true if leaf.
	 */
	public boolean isLeaf() {
		return (this.children == null || this.children.isEmpty());
	}
	
	/**
	 * Returns true if a vertex is bifurcating, i.e. has either 0 or 2 children.
	 * Alternatively, one may consider the whole subtree rooted at the vertex.
	 * @param doRecurse true to consider the whole subtree rooted at the vertex.
	 * @return true if bifurcating.
	 */
	public boolean isBifurcating(boolean doRecurse) {
		int k = this.getNoOfChildren();
		if (k != 0 && k != 2)
			return false;
		if (doRecurse && k > 0) {
			for (NewickVertex n : this.children) {
				if (!n.isBifurcating(true))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if a vertex is collapsable, i.e. has a single child.
	 * Alternatively, one may consider the whole subtree rooted at the vertex.
	 * @param doRecurse true to consider the whole subtree rooted at the vertex.
	 * @return true if collapsable.
	 */
	public boolean isCollapsable(boolean doRecurse) {
		int k = this.getNoOfChildren();
		if (k == 1)
			return true;
		if (doRecurse && k > 0) {
			for (NewickVertex n : this.children) {
				if (n.isCollapsable(true))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the number of descendants.
	 * @param properOnly false to count the vertex itself among its descendants.
	 * @return the number of proper or improper descendants.
	 */
	public int getNoOfDescendants(boolean properOnly) {
		int k = (properOnly ? 0 : 1);
		if (this.isLeaf())
			return k;
		for (NewickVertex n : this.children) {
			k += n.getNoOfDescendants(properOnly);
		}
		return k;
	}
	
	/**
	 * Returns the number of leaves of the subtree rooted at this vertex.
	 * @return the number of leaves.
	 */
	public int getNoOfLeaves() {
		if (this.isLeaf()) { return 1; }
		int k = 0;
		for (NewickVertex n : this.children) {
			k += n.getNoOfLeaves();
		}
		return k;
	}
	
	/**
	 * Relabels the vertex numbers of the subtree rooted at this vertex post-order,
	 * starting from number no at the first leaf.
	 * @param num the number to start labelling from.
	 * @return the next free available number label.
	 */
	public int renumber(int num) {
		if (this.children != null) {
			for (NewickVertex child : this.children) {
				num = child.renumber(num);
			}
		}
		this.number = num;
		return (num + 1);
	}
	
	/**
	 * Recursively sorts the children of this vertex according to vertex names.
	 * If lacking name, a vertex is instead represented by the "smallest" name corresponding
	 * to the subtrees of its children. Unnamed children are stored last.
	 * Assumes that vertex names are unique (i.e., beware of bootstrap value names!).
	 * @return the name representing the subtree rooted at this vertex.
	 * @throws NewickIOException if there was a collision due to duplicate names while sorting.
	 */
	public String sort() throws NewickIOException {
		if (this.isLeaf()) {
			return this.name;
		}
		// Store children with names in sorted tree map.
		// We append all unnamed children at the end.
		TreeMap<String, NewickVertex> named = new TreeMap<String, NewickVertex>();
		ArrayList<NewickVertex> unnamed = new ArrayList<NewickVertex>();
		for (NewickVertex c : this.children) {
			String name = c.sort();
			if (name == null || name.equals("")) {
				unnamed.add(c);
			} else {
				if (named.put(name, c) != null) {
					throw new NewickIOException("Cannot sort subtree at NewickVertex when there are duplicate names.");
				}
			}
		}
		this.children.clear();
		this.children.addAll(named.values());
		this.children.addAll(unnamed);
		String firstChildName = (named.isEmpty() ? null : named.firstKey());
		return (this.hasName() ? this.name : firstChildName);
	}

	
	@Override
	public String toString() {
		// Conforms with serialisation of a Newick tree.
		int n = this.getNoOfChildren();
		StringBuilder sb = new StringBuilder(16 + 16 * n);
		if (n > 0) {
			sb.append('(');
			for (NewickVertex c : this.children) {
				sb.append(c.toString());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(')');
		}
		if (this.hasName()) {
			sb.append(this.name);
		}
		if (this.hasBranchLength()) {
			sb.append(':').append(this.branchLength);
		}
		if (this.hasMeta()) {
			sb.append(this.meta);
		}
		return sb.toString();
	}
	
}
