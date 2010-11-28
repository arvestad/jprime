package se.cbb.jprime.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.cbb.jprime.misc.CharQueue;

/**
 * <b>Note: This should be handled by a proper JFlex parser in the future.</b>
 * <p/>
 * Handles input of Newick trees, creating node-based, intermediate
 * representations. These may then be converted to more efficient, specialised representations.
 * Includes methods to read "pure" Newick trees. For those where PrIME "meta info" has
 * been included, see <code>PrIMENewickTreeReader</code>.
 * <p/>
 * Input is parsed with a traditional manual "recursive left-to-right" implementation
 * according to this slightly modified and simplified grammar:
 * <pre>
 * &lt;treeset&gt;      ::= &lt;tree&gt; &lt;treeset&gt;  |  empty
 * &lt;tree&gt;         ::= &lt;subtree&gt; &lt;meta&gt; ";"
 * &lt;subtree&gt;      ::= "(" &lt;subtree&gt; "," &lt;subtreeset&gt; ")" &lt;info&gt;  |  &lt;info&gt;
 * &lt;subtreeset&gt;   ::= &lt;subtree&gt;  |  &lt;subtree&gt; "," &lt;subtreeset&gt;
 * &lt;info&gt;         ::= &lt;name&gt; &lt;branchlength&gt; &lt;meta&gt;
 * &lt;name&gt;         ::= empty  |  string
 * &lt;branchlength&gt; ::= empty  |  ":" number
 * &lt;meta&gt;         ::= empty  |  "[" string "]"
 * </pre>
 * This means that multifurcating trees are allowed, but not
 * those where the root is a leaf.
 * <p/>
 * Blanks and newlines are allowed everywhere except within
 * meta tags (where only blanks are allowed) and name and number
 * tags (where neither is allowed). Disallowed is alternative use of
 * parenthesis, brackets, comma signs and so forth, although
 * parsing is not guaranteed to catch such if input is otherwise well-formed.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickTreeReader {
	
	/**
	 * Reads consecutive plain Newick trees from input, disregarding meta info.
	 * Empty input is allowed and renders an empty list.
	 * Vertices of each tree are numbered post-order starting with 0 at first leaf.
	 * @param str the input.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return the trees in a list.
	 */
	public static List<NewickTree> readTrees(String str, boolean doSort) throws NewickIOException {
		List<NewickTree> trees;
		CharQueue q = NewickStringAlgorithms.strip(str, Integer.MAX_VALUE);
		try {
			trees = readTreeSet(q, doSort);
		} catch (Exception e) {
			throw new NewickIOException("Error parsing Newick tree. " + getErrMsg(q, 100), e);
		}
		return trees;
	}

	/**
	 * Reads consecutive plain Newick trees from a file, disregarding meta info.
	 * Empty input is allowed and renders an empty list.
	 * Vertices of each tree are numbered post-order starting with 0 at first leaf.
	 * Not optimised for large files.
	 * @param f the input file.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return the trees in a list.
	 * @throws IOException.
	 */
	public static List<NewickTree> readTrees(File f, boolean doSort) throws NewickIOException, IOException {
		byte[] buffer = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		} finally {
			if (fis != null) try { fis.close(); } catch (IOException ex) {}
		}
	    String str = new String(buffer);
	    return readTrees(str, doSort);
	}
	
	/**
	 * Reads exactly one plain Newick tree from input, disregarding meta info.
	 * Additional trees following the first tree will be ignored and not cause parse errors.
	 * If input is just comprised of the tree, a semi-colon at the end is not compulsory.
	 * Vertices are numbered post-order starting with 0 at first leaf.
	 * @param str the input.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return the tree.
	 */
	public static NewickTree readTree(String str, boolean doSort) throws NewickIOException {
		NewickTree tree;
		CharQueue q = NewickStringAlgorithms.strip(str, 1);
		q.put(';');  // Since not compulsory.
		try {
			tree = readTree(q, doSort);
		} catch (Exception e) {
			throw new NewickIOException("Error parsing Newick tree. " + getErrMsg(q, 100), e);
		}
		return tree;
	}
	
	/**
	 * Reads exactly one plain Newick tree from a file, disregarding meta info.
	 * Additional trees following the first tree will be ignored and not cause parse errors.
	 * If input is just comprised of the tree, a semi-colon at the end is not compulsory.
	 * Vertices are numbered post-order starting with 0 at first leaf.
	 * Not optimised for large files.
	 * @param f the input file.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return the tree.
	 * @throws IOException.
	 */
	public static NewickTree readTree(File f, boolean doSort) throws NewickIOException, IOException {
		byte[] buffer = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		} finally {
			if (fis != null) try { fis.close(); } catch (IOException ex) {}
		}
	    String str = new String(buffer);
	    return readTree(str, doSort);
	}
	
	/**
	 * Parses according to &lt;treeset&gt;.
	 * @param q remaining characters.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return list of parsed trees.
	 * @throws NewickIOException.
	 */
	private static List<NewickTree> readTreeSet(CharQueue q, boolean doSort) throws NewickIOException {
		ArrayList<NewickTree> trees = new ArrayList<NewickTree>();		
		while (!q.isEmpty()) {
			trees.add(readTree(q, doSort));
		}
		return trees;
	}
	
	/**
	 * Parses according to &lt;tree&gt;.
	 * @param q remaining characters.
	 * @param doSort true to sort the tree according to vertex names.
	 * @return parsed tree.
	 * @throws NewickIOException.
	 */
	private static NewickTree readTree(CharQueue q, boolean doSort) throws NewickIOException {
		NewickVertex n = readSubtree(q);
		String meta = readMeta(q);
		if (q.isEmpty() || q.get() != ';')
			throw new NewickIOException("Expected semi-colon when reading Newick tree.");
		return new NewickTree(n, meta, true, doSort);
	}
	
	/**
	 * Parses according to &lt;subtree&gt;.
	 * @param q remaining characters.
	 * @return root of parsed subtree.
	 * @throws NewickIOException.
	 */
	private static NewickVertex readSubtree(CharQueue q) throws NewickIOException {
		NewickVertex n = new NewickVertex();
		if (q.peek() == '(') {
			q.get();
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>();
			children.add(readSubtree(q));
			if (q.isEmpty() || q.get() != ',')
				throw new NewickIOException("Expected comma when reading Newick tree.");
			readSubtreeSet(q, children);
			n.setChildren(children);
			for (NewickVertex child : children) { child.setParent(n); }
			if (q.isEmpty() || q.get() != ')')
				throw new NewickIOException("Expected right parenthesis when reading Newick tree.");
			readInfo(q, n);
		} else {
			readInfo(q, n);
		}
		return n;
	}
	
	/**
	 * Parses according to &lt;subtreeset&gt;.
	 * @param q remaining characters.
	 * @param set appends roots of parsed subtrees to this list.
	 * @throws NewickIOException
	 */
	private static void readSubtreeSet(CharQueue q, ArrayList<NewickVertex> set) throws NewickIOException {
		set.add(readSubtree(q));
		if (q.peek() == ',') {
			q.get();
			readSubtreeSet(q, set);
		}
	}
	
	/**
	 * Parses according to &lt;info&gt;.
	 * @param q remaining characters.
	 * @param n stores retrieved info in this node.
	 */
	private static void readInfo(CharQueue q, NewickVertex n) throws NewickIOException {
		String name = readName(q);
		Double branchLength = readBranchLength(q);
		String meta = readMeta(q);
		n.setValues(-1, name, branchLength, meta);
	}
	
	/**
	 * Parses according to &lt;name&gt;.
	 * @param q remaining characters.
	 * @return parsed name, null if empty.
	 */
	private static String readName(CharQueue q) {
		StringBuilder name = new StringBuilder(16);
		char c = q.peek();
		while (!q.isEmpty() && c != ',' && c != ';' && c != ')' && c != '[' && c != ':') {
			name.append(q.get());
			c = q.peek();
		}
		return (name.length() == 0 ? null : name.toString());
	}
	
	/**
	 * Parses according to &lt;meta&gt;.
	 * @param q remaining characters.
	 * @return parsed meta info, null if empty.
	 */
	private static String readMeta(CharQueue q) {
		char c = q.peek();
		if (c == '[') {
			StringBuilder meta = new StringBuilder(32);
			do {
				c = q.get();
				meta.append(c);
			} while (c != ']');
			return meta.toString();
		}
		return null;
	}
	
	/**
	 * Parses according to &lt;branchlength&gt;.
	 * @param q remaining characters.
	 * @return parsed branch length, null if empty.
	 */
	private static Double readBranchLength(CharQueue q) throws NewickIOException {
		Double branchLength = null;
		char c = q.peek();
		if (c == ':') {
			StringBuilder num = new StringBuilder(16);
			q.get();
			if (q.isEmpty())
				throw new NewickIOException("Expected branch length when reading Newick tree.");
			num.append(q.get());
			c = q.peek();
			while (!q.isEmpty() && c != ',' && c != ';' && c != ')' && c != '[') {
				num.append(q.get());
				c = q.peek();
			}
			branchLength = Double.parseDouble(num.toString());
		}
		return branchLength;
	}
	
	/**
	 * Helper. Returns error message showing remainder of queue.
	 * @param q the queue.
	 * @param maxLength max characters to include.
	 * @return error message.
	 */
	private static String getErrMsg(CharQueue q, int maxLength) {
		StringBuilder sb = new StringBuilder(maxLength + 100);
		sb.append("First " + maxLength  + " remaining unparsed characters:\n");
		int i = 0;
		while (i < maxLength && !q.isEmpty()) {
			sb.append(q.get());
			++i;
		}
		return sb.toString();
	}
}
