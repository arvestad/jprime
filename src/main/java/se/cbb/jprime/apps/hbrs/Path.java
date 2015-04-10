package se.cbb.jprime.apps.hbrs;

import java.util.LinkedList;

/**
 * Represents a path of vertices.
 * 
 * @author Joel Sjöstrand.
 */
public class Path {
	
	/** Vertices. */
	private LinkedList<Integer> vertices;
	
	/** Cached for speed. */
	private int hashCode;
	
	/**
	 * Constructor.
	 * @param x single-vertex path.
	 */
	public Path(int x) {
		this.vertices = new LinkedList<Integer>();
		this.vertices.add(x);
		this.hashCode = Math.abs(x);
	}
	
	/**
	 * Copy-constructor.
	 * @param p template path y1,...,yk.
	 */
	public Path(Path p) {
		this.vertices = new LinkedList<Integer>(p.vertices);
		this.hashCode = p.hashCode;
	}
	
	/**
	 * Copy-constructor which appends a vertex.
	 * @param p template path y1,...,yk.
	 * @param x vertex appended after yk.
	 */
	public Path(Path p, int x) {
		this(p);
		this.vertices.add(x);
		this.hashCode *= 31;
		this.hashCode += Math.abs(x);
	}

	/**
	 * Returns the last element of the path.
	 * @return the last element.
	 */
	public int getHead() {
		return this.vertices.getLast();
	}

	/**
	 * Returns the first element of the path.
	 * @return the first element.
	 */
	public int getTail() {
		return this.vertices.getFirst();
	}

	
	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (vertices.equals(other.vertices))
			return true;
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.vertices.size() * 3);
		sb.append(this.vertices.get(0));
		for (int i = 1 ; i < this.vertices.size(); ++i) {
			sb.append("->").append(this.vertices.get(i));
		}
		return sb.toString();
	}
	
	/**
	 * Returns the vertices of this path in order from source to sink.
	 * @return the path.
	 */
	public LinkedList<Integer> getVertices() {
		return this.vertices;
	}
	
}
