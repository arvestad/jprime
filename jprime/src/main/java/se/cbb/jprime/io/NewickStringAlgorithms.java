package se.cbb.jprime.io;

import se.cbb.jprime.misc.CharQueue;

/**
 * Contains algorithms for manipulation of Newick trees on a string-level.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickStringAlgorithms {

	/** Non-meta comment pattern. */
	//public static final Pattern COMMENT_PATTERN = Pattern.compile("\\[[^\\&][^\\&][^\\]]*\\]");
	
	/**
	 * Takes as input a string with one or more Newick trees
	 * and strips it of all whitespace, returning the remainder as a queue.
	 * ALSO: Strips away comments '[...]' unless they indicate tree formatting
	 * like '[&&...]'.
	 * One may choose to only include input trees up to a certain number
	 * based on counting semi-colons. Assumes well-formed input.
	 * @param str the input.
	 * @param maxSemiColons stop filling queue when this many semi-colons
	 * have been encountered.
	 * @return the queue.
	 */
	public static CharQueue strip(String str, int maxSemiColons) {
		// Strip non-meta comments.
		str = str.replaceAll("\\[[^\\&][^\\&][^\\]]*\\]", "");
		
		// Strip away all blanks unless in meta tags.
		CharQueue q = new CharQueue();
		int count = 0;
		boolean inMeta = false;
		for (int i = 0; i < str.length() && count <= maxSemiColons; ++i) {
			char c = str.charAt(i);
			switch (c) {
				case '[': inMeta = true;  break;
				case ']': inMeta = false; break;
				case ';': ++count;        break;
			}
			
			if (inMeta || (c != ' ' && c != '\t' && c != '\n')) {
				q.put(c);
			}
		}
		return q;
	}
	
	/**
	 * Takes as input a string with one or more Newick trees
	 * and strips it of all whitespace, all branch lengths, and all meta info,
	 * returning the remainder as a queue.
	 * One may choose to only include input trees up to a certain number
	 * based on counting semi-colons. Assumes well-formed input.
	 * @param str the input.
	 * @param maxSemiColons stop filling queue when this many semi-colons
	 * have been encountered.
	 * @return the queue.
	 */
	public static CharQueue stripToNamesOnly(String str, int maxSemiColons) {
		CharQueue q = new CharQueue();
		int count = 0;
		for (int i = 0; i < str.length() && count <= maxSemiColons; ++i) {
			char c = str.charAt(i);
			if (c == '[') {
				// Strip meta info.
				do {
					c = str.charAt(++i);
				} while (c != ']');
				c = str.charAt(++i);
			} else if (c == ':') {
				// Strip branch length.
				do {
					c = str.charAt(++i);
				} while ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == '+' || c == '-');
				c = str.charAt(++i);
				// Note: Keep strippin' if meta info follows...
				if (c == '[') {
					do {
						c = str.charAt(++i);
					} while (c != ']');
					c = str.charAt(++i);
				}
			} else if (c == ';') {
				++count;
			}
			
			if (c != ' ' && c != '\t' && c != '\n') {
				q.put(c);
			}
		}
		return q;
	}
}
