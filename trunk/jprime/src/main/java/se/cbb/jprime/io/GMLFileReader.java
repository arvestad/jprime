package se.cbb.jprime.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import se.cbb.jprime.io.GMLKeyValuePair.ValueType;
import se.cbb.jprime.misc.CharQueue;
import se.cbb.jprime.misc.Pair;

/**
 * Reads Graph Modelling Language (GML) files.
 * GML is also known as Graph Meta Language.
 * See <code>GMLGraph</code> documentation for an example.
 * <p/>
 * BNF grammar according to manual:
 * <pre>
 * {@code
 * <GML>        ::= <List>
 * <List>       ::= <Empty> | <KeyValue> (<Whitespace>+ <KeyValue>)*
 * <KeyValue>   ::= <Key> <Whitespace>+ <Value>
 * <Value>      ::= <Integer> | <Real> | <String> | '[' <List> ']'
 * <Key>        ::= ['A'-'Z''a'-'z']['A'-'Z''a'-'z''0'-'9']*
 * <Integer>    ::= <Sign> <Digit>+
 * <Real>       ::= <Sign> <Digit>+ '.' <Digit>+ <Mantissa>
 * <String>     ::= '"' <Instring> '"'
 * <Sign>       ::= <Empty> | '+' | '-'
 * <Digit>      ::= ['0'-'9']
 * <Mantissa>   ::= <Empty> | E <Sign> <Digit>+ | 'e' <Sign> <Digit>+
 * <Instring>   ::= <ASCII excl. '&'and '"'> | '&' <character>* ';'
 * <Whitespace> ::= <Space> | <Tab> | <Newline>
 * }
 * </pre>
 * We are a bit more lenient in some cases here, such as:
 * <pre>
 * {@code
 * <String>     ::= '"' <Not '"'>* '"'
 * }
 * </pre>
 * Actual transform of nested GML key-value pairs into a graph is handled elsewhere.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLFileReader {
	
	/**
	 * Reads a GML file. Leading and trailing spaces are ignored.
	 * @param f the input file.
	 * @return the GML list.
	 */
	public static List<GMLKeyValuePair> readGML(File f) throws GMLIOException, IOException {
		byte[] buffer = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		} finally {
			if (fis != null) try { fis.close(); } catch (IOException ex) {}
		}
	    String str = new String(buffer);
	    return readGML(str);
	}
	
	/**
	 * Reads a GML string. Leading and trailing spaces are ignored.
	 * @param str the input.
	 * @return the GML list.
	 */
	public static List<GMLKeyValuePair> readGML(String str) throws GMLIOException {
		CharQueue q = new CharQueue();
		// Not very optimised, but so what...
		String[] lns = str.trim().split("\n");
		for (String ln : lns) {
			// Look for comment lines. Simple trimming can be a bit dangerous, since we may truncate actual content.
			boolean ignore = false;
			for (int i = 0; i < ln.length(); ++i) {
				if (ln.charAt(i) == '#') {
					ignore = true;
					break;
				}
				if (!Character.isWhitespace(ln.charAt(i))) {
					ignore = false;
					break;
				}
			}
			if (!ignore) {
				q.put(ln);
			}
		}
		// Parse.
		return readGML(q);
	}
	
	/**
	 * Parses <code>GML</code>.
	 * @param q queue.
	 * @return the list of key-value pairs.
	 * @throws GMLIOException
	 */
	private static List<GMLKeyValuePair> readGML(CharQueue q) throws GMLIOException {
		try {
			List<GMLKeyValuePair> list = readList(q);
			if (!q.isEmpty()) {
				throw new GMLIOException("GML file has trailing characters after supposed end.");
			}
			return list;
		} catch (Exception e) {
			throw new GMLIOException("Error parsing GML file. " + getErrMsg(q, 100), e);
		}
	}

	/**
	 * Parses <code>List</code>.
	 * @param q queue.
	 * @return the list of key-value pairs.
	 * @throws GMLIOException.
	 */
	private static List<GMLKeyValuePair> readList(CharQueue q) throws GMLIOException {
		ArrayList<GMLKeyValuePair> list = new ArrayList<GMLKeyValuePair>(1024);
		while (!q.isEmpty()) {
			// Remove whitespace.
			while (Character.isWhitespace(q.peek())) {
				q.get();
			}
			list.add(readKeyValue(q));
		}
		return list;
	}

	/**
	 * Parses <code>KeyValue</code>.
	 * @param q queue.
	 * @return the key-value pair.
	 * @throws GMLIOException.
	 */
	private static GMLKeyValuePair readKeyValue(CharQueue q) throws GMLIOException {
		String key = readKey(q);
		// Remove whitespace.
		while (Character.isWhitespace(q.peek())) {
			q.get();
		}
		Pair<ValueType, Object> val = readValue(q);
		return new GMLKeyValuePair(key, val.first, val.second);
	}

	/**
	 * Parses <code>Key</code>.
	 * @param q queue.
	 * @return the key.
	 * @throws GMLIOException.
	 */
	private static String readKey(CharQueue q) throws GMLIOException {
		StringBuilder sb = new StringBuilder(64);
		char c = q.get();
		if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
			throw new GMLIOException("Error parsing GML file. Expected [A-Za-z].");
		}
		sb.append(c);
		while (q.peek() >= 'A' && q.peek() <= 'Z' || q.peek() >= 'a' && q.peek() <= 'z' || q.peek() >= '0' && q.peek() <= '9') {
			sb.append(q.get());
		}
		return sb.toString();
	}
	
	/**
	 * Parses <code>Value</code>.
	 * @param q queue.
	 * @return the value.
	 * @throws GMLIOException.
	 */
	private static Pair<ValueType, Object> readValue(CharQueue q) throws GMLIOException {
		if (q.peek() == '[') {
			// LIST.
			q.get();
			List<GMLKeyValuePair> list = readList(q);
			if (q.peek() != ']') {
				throw new GMLIOException("Error parsing GML file. Expected ].");
			}
			q.get();
			return new Pair<GMLKeyValuePair.ValueType, Object>(ValueType.LIST, list);
		}
		if (q.peek() == '"') {
			String string = readString(q);
			return new Pair<GMLKeyValuePair.ValueType, Object>(ValueType.STRING, string);
		}
		
		// INTEGER OR REAL.
		StringBuilder sb = new StringBuilder(64);
		// Good enough...
		while (q.peek() >= '0' && q.peek() <= '9' || q.peek() == 'E' || q.peek() == 'e' || q.peek() == '+' || q.peek() == '-') {
			sb.append(q.get());
		}
		try {
			String s = sb.toString();
			if (s.contains(".")) {
				// REAL.
				Double d = new Double(s);
				return new Pair<GMLKeyValuePair.ValueType, Object>(ValueType.REAL, d);
			}
			// INTEGER.
			Integer i = new Integer(s);
			return new Pair<GMLKeyValuePair.ValueType, Object>(ValueType.INTEGER, i);
		} catch (Exception e) {
			throw new GMLIOException("Error parsing GML file. Invalid number format.");
		}
	}

	/**
	 * Parses <code>String</code>.
	 * @param q queue.
	 * @return the string turned into unicode.
	 * @throws GMLIOException.
	 */
	private static String readString(CharQueue q) throws GMLIOException {
		if (q.peek() != '"') {
			throw new GMLIOException("Error parsing GML file. Expected \" to start string.");
		}
		q.get();
		StringBuilder sb = new StringBuilder(1024);
		while (q.peek() != '"' && !q.isEmpty()) {
			sb.append(q.get());
		}
		if (q.peek() != '"') {
			throw new GMLIOException("Error parsing GML file. Expected \" to end string.");
		}
		q.get();
		// We translate ISO 8859 entities (&xxx;) into Unicode.
		String s = sb.toString();
		return StringEscapeUtils.unescapeHtml4(s);
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
