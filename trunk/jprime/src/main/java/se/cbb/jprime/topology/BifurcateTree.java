package se.cbb.jprime.topology;

import java.util.ArrayList;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.misc.Pair;

/**
 * The purpose of this class is to make a rooted bifurcating tree from a
 * rooted non-bifurcating tree.
 * The non-bifurcating tree internal nodes must be of degree 3.
 * 
 * This can be used for example when using Fast Phylo to have a good guest starting tree,
 * because Fast Phylo returns a non-bifurcating tree.
 * 
 * @author Vincent Llorens.
 *
 */
public class BifurcateTree {
	
	/**
	 * Finds the best rooted bifurcating tree from all possible rooted bifurcating tree from the
	 * non-bifurcating guest tree G.
	 * The rooting and bifurcation is done by picking an edge e, inserting a vertex v on e, set v as the root of G. 
	 * @param G non-bifurcating guest NewickTree.
	 * @param S rooted bifurcating host NewickTree.
	 * @param gsMap mapping between guest tree leaves and host tree leaves.
	 * @return rooted bifurcating NewickTree with the minimum number of duplications and losses.
	 */
	public static Pair<RBTree, NamesMap> bestBifurcatingTree(NewickTree G, NewickTree S, GuestHostMap gsMap) {
		RBTree bestTree = null;
		RBTree currentRBTree = null;
		RBTree sRBTree = null;
		MPRMap mprMap = null;
		NamesMap gNames = null;
		NamesMap sNames = null;
		int bestDupLossScore = Integer.MAX_VALUE;
		ArrayList<NewickVertex[]> edges = G.getEdges();
		try {
			sRBTree = new RBTree(S, "HostTree");
			sNames = S.getVertexNamesMap(true, "HostTreeNames");
			for (NewickVertex[] e : edges) {
				NewickTree currentTree = makeBifurcatingTree(G, e[0].getNumber(), e[1].getNumber());
				currentRBTree = new RBTree(currentTree, "currentTree");
				gNames = currentTree.getVertexNamesMap(true, "GuestTreeNames");
				mprMap = new MPRMap(gsMap, currentRBTree, gNames, sRBTree, sNames);
				int score = mprMap.getTotalNoOfDuplications() + mprMap.getTotalNoOfLosses();
				if (score < bestDupLossScore) {
					bestDupLossScore = score;
					bestTree = currentRBTree;
				}
			}
		} catch (NewickIOException ne) {
			ne.getStackTrace();
		} catch (TopologyException te) {
			te.getStackTrace();
		}
		return new Pair<RBTree, NamesMap>(bestTree, gNames);
	}

	/**
	 * Make a bifurcating tree from a non-bifurcating tree.
	 * Put a new vertex between a and b, split the edge (a, b), this new vertex is set as the root and it has
	 * two children: a and b.
	 * @param G non-bifurcating guest tree.
	 * @param numberA one end of the selected edge to be split.
	 * @param numberB other end of the selected edge to be split.
	 * @return rooted bifurcating tree.
	 * @throws NewickIOException if trying to make a bifurcation between to non-neighbour vertices.
	 */
	private static NewickTree makeBifurcatingTree(NewickTree G, int numberA, int numberB) throws NewickIOException {
		NewickTree newTree = new NewickTree(G);
		Double branchLength = null;
		try {
			NewickVertex a = newTree.getVertex(numberA);
			NewickVertex b = newTree.getVertex(numberB);
			if (!(a.getParent() == b || b.getParent() == a)) {
				throw new NewickIOException("Cannot initiate a bifurcating tree from two non-neighbour vertices.");
			}
			// Create a new vertex that will be the root.
			int numberNewRoot = G.getNoOfVertices();
			NewickVertex newRoot = new NewickVertex(numberNewRoot, "artificial_root", branchLength, null); // TODO branchlength?
			// Set the children of this vertex to be a and b.
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>();
			children.add(a);
			children.add(b);
			newRoot.setChildren(children);
			newTree.setRoot(newRoot);
			rebuildTree(a, newRoot, a, b);
			rebuildTree(b, newRoot, a, b);
		} catch (NewickIOException e) {
			throw new NewickIOException(e.getMessage(), e);
		}
		return newTree;
	}
	
	/**
	 * Rebuild a tree by changing the kinship of the vertices.
	 * Propagates from a vertex currentVertex and change the kinship according to the direction in which
	 * the propagation from currentVertex to the leaves is done.
	 * @param currentVertex vertex from which the propagation is done.
	 * @param newParent new parent of currentVertex, indicates from where the propagation came from.
	 * @param a vertex to exclude when assigning new children.
	 * @param b another vertex to exclude when assigning new children. See the call from rootNonBifurcatingTree for
	 * a better understanding of this argument.
	 * @throws NewickIOException if a non-leaf vertex is not of degree 3.
	 */
	private static void rebuildTree(NewickVertex currentVertex, NewickVertex newParent, NewickVertex a, NewickVertex b) throws NewickIOException {
		if(!currentVertex.isLeaf()) {
			if (currentVertex.getDegree() != 3) {
				throw new NewickIOException("Cannot make a bifurcating tree if non-leaf vertices are not of degree 3.");
			}
			ArrayList<NewickVertex> newChildren = new ArrayList<NewickVertex>();
			newChildren.add(currentVertex.getParent());
			newChildren.addAll(currentVertex.getChildren());
			newChildren.remove(a);
			newChildren.remove(b);
			newChildren.remove(newParent);
			currentVertex.setParent(newParent);
			currentVertex.getChildren().clear();
			for(NewickVertex v : newChildren) {
				if (v != null) {
					currentVertex.getChildren().add(v);
					rebuildTree(v, currentVertex, a, b);
				}
			}
		} else {
			currentVertex.setParent(newParent);
		}
	}
}
