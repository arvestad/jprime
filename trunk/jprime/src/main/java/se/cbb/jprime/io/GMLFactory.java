package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a factory class per se, but creates typical top-level objects from a list of GML elements.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLFactory {

	/**
	 * Extracts all GML graphs from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML graphs.
	 * @throws GMLIOException.
	 */
	@SuppressWarnings("unchecked")
	public static List<GMLGraph> getGraphs(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<GMLGraph> graphs = new ArrayList<GMLGraph>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("graph")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.LIST) {
					throw new GMLIOException("Invalid attribute for GML graph: expected list [...].");
				}
				graphs.add(new GMLGraph((List<GMLKeyValuePair>) kv.value));
			}
		}
		return graphs;
	}

	/**
	 * Extracts all GML Version tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML Version tags.
	 */
	public static ArrayList<Integer> getVersions(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<Integer> versions = new ArrayList<Integer>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("Version")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML Version: expected integer.");
				}
				versions.add((Integer) kv.value);
			}
		}
		return versions;
	}

	/**
	 * Extracts all GML Creator tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML Creator tags.
	 */
	public static ArrayList<String> getCreators(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<String> creators = new ArrayList<String>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("Creator")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML Creator: expected string.");
				}
				creators.add((String) kv.value);
			}
		}
		return creators;
	}
	
	/**
	 * Extracts all GML ID tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML ID tags.
	 */
	public static ArrayList<Integer> getIDs(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("id")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML id: expected integer.");
				}
				ids.add((Integer) kv.value);
			}
		}
		return ids;
	}
	
	/**
	 * Extracts all GML label tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML label tags.
	 */
	public static ArrayList<String> getLabels(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<String> labels = new ArrayList<String>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("label")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML label: expected string.");
				}
				labels.add((String) kv.value);
			}
		}
		return labels;
	}
	
	/**
	 * Extracts all GML comment tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML comment tags.
	 */
	public static ArrayList<String> getComments(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<String> comments = new ArrayList<String>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("comment")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML comment: expected string.");
				}
				comments.add((String) kv.value);
			}
		}
		return comments;
	}
	
	/**
	 * Extracts all GML name tags from parsed GML content.
	 * Only the top-most level of elements is considered.
	 * @param gml the content.
	 * @return all GML name tags.
	 */
	public static ArrayList<String> getNames(List<GMLKeyValuePair> gml) throws GMLIOException {
		ArrayList<String> names = new ArrayList<String>();
		for (GMLKeyValuePair kv : gml) {
			if (kv.key.equalsIgnoreCase("name")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML name: expected string.");
				}
				names.add((String) kv.value);
			}
		}
		return names;
	}
	
}
