package se.cbb.jprime.io;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.TimesMap;

/**
 * Extension of a <code>NewickTree</code> which parses the PrIME-specific meta info and
 * provides simplified access to such data (alongside the original topology).
 * It is required that vertices are numbered from 0 to |V(T)|-1, and that
 * meta info tags have the form "[&&PRIME ...]".
 * <p/>
 * Basically, all info apart from topology and vertex numbering may be retrieved
 * as arrays from this class (indexed by vertex number).
 * <p/>
 * Whenever there is duplication of information (e.g. for vertex numbers,
 * branch lengths), meta tags will have precedence and the tree be updated
 * accordingly.
 * <p/>
 * Vertex times and arc times may coexist, and similarly a "tree top time" may coexist alongside
 * an arc time of the root. However, one may verify compatibility of such properties (see
 * also <code>PrIMENewickTreeVerifier</code>).
 * <p/>
 * The following properties are handled:
 * <pre>
 * Property         "Pure Newick":  PrIME meta:   Note:
 * ---------------------------------------------------------------------
 * Topology         Y                             
 * Tree name                        Y             
 * Tree top time                    Y             
 * Vertex numbers   Y               Y             Meta overrides pure.
 * Vertex names     Y               Y             Meta overrides pure.
 * Vertex weights                   Y             This class.
 * Branch lengths   Y               Y             Meta overrides pure.
 * Vertex times                     Y             This class.
 * Arc times                        Y             This class.
 * </pre>
 * 
 * @author Joel Sjöstrand.
 */
public class PrIMENewickTree extends NewickTree {
	
	/**
	 * Tree properties.
	 */
	public enum MetaProperty {
		TREE_NAME        ("NAME=\"?(\\w+)\"?"),
		TREE_TOP_TIME    ("TT=\"?([0-9\\+\\-\\.e]+)\"?"),
		VERTEX_NUMBERS   ("ID=\"?([0-9]+)\"?"),
		VERTEX_NAMES     ("NAME=\"?(\\w+)\"?"),
		BRANCH_LENGTHS   ("BL=\"?([0-9\\+\\-\\.e]+)\"?"),
		VERTEX_WEIGHTS   ("NW=\"?([0-9\\+\\-\\.e]+)\"?"),
		VERTEX_TIMES     ("NT=\"?([0-9\\+\\-\\.e]+)\"?"),
		ARC_TIMES        ("ET=\"?([0-9\\+\\-\\.e]+)\"?"),
		IS_DUPLICATION   ("\\sD=\"?([0-9]+)\"?");
		
		public static final String REGEXP_PREFIX = "\\[&&PRIME [^\\]]*";
		public static final String REGEXP_SUFFIX = "[^\\]]*\\]";
		
		private Pattern pattern;

		private MetaProperty(String regexp) {
			this.pattern = Pattern.compile(REGEXP_PREFIX + regexp + REGEXP_SUFFIX);
		}
		
		/**
		 * Returns the value contained in a meta tag corresponding to this property.
		 * If not found or input is null, null is returned.
		 * @param meta the complete meta string.
		 * @return the value, null if not found or input is null.
		 */
		public String getValue(String meta) {
			if (meta == null)
				return null;
			Matcher m = this.pattern.matcher(meta);
			return (m.find() ? m.group(1) : null);
		}
	}
	
	/**
	 * Controls which properties are considered when creating
	 * a times map, in order of precedence. 'VT' is for vertex times,
	 * 'AT' is for arc times, and 'BL' is for branch lengths.
	 */
	public enum TimesPropertySelector {
		VT_AT_BL,		// VT, then AT, then BL, then fail.
		VT_AT,   		// VT, then AT, then fail. 
		BL    			// BL, then fail.
	}
	
	/** Size of the underlying tree. */
	private int noOfVertices;
	
	/** Tree name. */
	private String treeName = null;
	
	/** Tree "top time". */
	private Double treeTopTime = null;
	
	/** Vertex names flag. */
	private boolean hasVertexNames = false;
	
	/** Branch lengths flag. */
	private boolean hasBranchLengths = false;
	
	/** Vertex weights. */
	private double[] vertexWeights = null;
	
	/** Vertex weights. */
	private double[] vertexTimes = null;
	
	/** Whether a node is a duplication or speciation */
	private int[] isDuplication = null;
	
	/** Arc times. Implicitly defines vertex times. */
	private double[] arcTimes = null;
	
	/**
	 * Constructor. Note: there are also factory methods available in NewickTreeReader.
	 * @param tree the tree to read info from.
	 * @param strict if true, validates tree using runStandardTestSuite() of
	 * class PrIMENewickTreeVerifier.
	 * @throws NewickIOException.
	 */
	public PrIMENewickTree(NewickTree tree, boolean strict) throws NewickIOException {
		super(tree, true);
		List<NewickVertex> vertices = tree.getVerticesAsList();
		this.noOfVertices = vertices.size();
		parseTreeData();
		for (NewickVertex v : vertices) {
			parseVertexData(v);
		}
		if (strict) {
			PrIMENewickTreeVerifier.runStandardTestSuite(this);
		}
	}
	
	/**
	 * Returns the size of the tree.
	 * @return the number of vertices.
	 */
	@Override
	public int getNoOfVertices() {
		return this.noOfVertices;
	}
	
	/**
	 * Returns true if there is one or more values for a property.
	 * Node numbers always exist and are accessed from within topology.
	 * @param property the characteristic.
	 * @return true if there is at least one value.
	 */
	public boolean hasProperty(MetaProperty property) {
		switch (property) {
		case TREE_NAME:
			return (this.treeName != null);
		case TREE_TOP_TIME:
			return (this.treeTopTime != null);
		case VERTEX_NUMBERS:
			return true;
		case VERTEX_NAMES:
			return this.hasVertexNames;
		case BRANCH_LENGTHS:
			return this.hasBranchLengths;
		case VERTEX_WEIGHTS:
			return (this.vertexWeights != null);
		case VERTEX_TIMES:
			return (this.vertexTimes != null);
		case ARC_TIMES:
			return (this.arcTimes != null);
		default:
			return false;
		}
	}

	/**
	 * Parses PrIME meta info corresponding to the tree.
	 */
	private void parseTreeData() {
		String val;
		
		val = MetaProperty.TREE_NAME.getValue(this.meta);
		if (val != null) {
			this.treeName = val;
		}
		val = MetaProperty.TREE_TOP_TIME.getValue(this.meta);
		if (val != null) {
			this.treeTopTime = Double.parseDouble(val);
		}
	}
	
	/**
	 * Parses PrIME meta info (and regular info) of a Newick vertex.
	 * @param n the vertex.
	 */
	private void parseVertexData(NewickVertex n) {
		String meta = n.getMeta();
		String val;
		
		// First determine number.
		int x = n.getNumber();
		val = MetaProperty.VERTEX_NUMBERS.getValue(meta);
		if (val != null) {
			x = Integer.parseInt(val);
			n.setNumber(x);   // Override node value.
		}
		
		// Read properties where "pure" and "meta" overlap. Let the latter override.
		if (n.hasName()) { this.hasVertexNames = true; }
		val = MetaProperty.VERTEX_NAMES.getValue(meta);
		if (val != null) {
			n.setName(val);  // Override.
			this.hasVertexNames = true;
		}
		if (n.hasBranchLength()) { this.hasBranchLengths = true; }
		val = MetaProperty.BRANCH_LENGTHS.getValue(meta);
		if  (val != null) {
			double bl = Double.parseDouble(val);
			n.setBranchLength(bl);  // Override.
			this.hasBranchLengths = true;
		}
		
		// Read "exclusive" meta properties.
		val = MetaProperty.VERTEX_WEIGHTS.getValue(meta);
		if  (val != null) {
			setVertexWeight(x, Double.parseDouble(val));
		}
		val = MetaProperty.VERTEX_TIMES.getValue(meta);
		if  (val != null) {
			setVertexTime(x, Double.parseDouble(val));
		}
		val = MetaProperty.ARC_TIMES.getValue(meta);
		if  (val != null) {
			setArcTime(x, Double.parseDouble(val));
		}
		val = MetaProperty.IS_DUPLICATION.getValue(meta);
		if  (val != null) {
			int dupval = Integer.parseInt(val);
			setDuplicationFlag(x, dupval==1 ? 0: 1);
		}
	}
	
	/**
	 * Sets a vertex weight. All empty values are NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @param x the vertex.
	 * @param weight the weight.
	 */
	private void setVertexWeight(int x, double weight) {
		if (this.vertexWeights == null) {
			this.vertexWeights = new double[this.noOfVertices];
			for (int i = 0; i < this.vertexWeights.length; ++i) {
				this.vertexWeights[i] = Double.NaN;
			}
		}
		this.vertexWeights[x] = weight;
	}
	
	/**
	 * Sets a vertex time. All empty values are NaN
	 * (for which one checks by Double.isNaN(val)).
	 * Compatibility with parent/children or arc times is not verified.
	 * @param x the vertex.
	 * @param time the time.
	 */
	private void setVertexTime(int x, double time) {
		if (this.vertexTimes == null) {
			this.vertexTimes = new double[this.noOfVertices];
			for (int i = 0; i < this.vertexTimes.length; ++i) {
				this.vertexTimes[i] = Double.NaN;
			}
		}
		this.vertexTimes[x] = time;
	}
	
	/**
	 * Sets an arc time. All empty values are NaN
	 * (for which one checks by Double.isNaN(val)).
	 * Ultrametricity or compatibility with vertex times are not verified.
	 * @param x the vertex.
	 * @param time the time.
	 */
	private void setArcTime(int x, double time) {
		if (this.arcTimes == null) {
			this.arcTimes = new double[this.noOfVertices];
			for (int i = 0; i < this.arcTimes.length; ++i) {
				this.arcTimes[i] = Double.NaN;
			}
		}
		this.arcTimes[x] = time;
	}
	
	/**
	 * Sets the duplication value. All empty values are Integer.MAX_VALUE
	 * (for which one checks by comparing with Integer.MAX_VALUE)
	 * @param x the vertex.
	 * @param value the value (0 or 1).
	 */
	private void setDuplicationFlag(int x, int value) {
		if (this.isDuplication == null) {
			this.isDuplication = new int[this.noOfVertices];
			for (int i = 0; i < this.isDuplication.length; ++i) {
				this.isDuplication[i] = Integer.MAX_VALUE;
			}
		}
		this.isDuplication[x] = value;
	}
	
	/**
	 * Returns the tree name (null if lacking name).
	 * @return the tree name.
	 */
	public String getTreeName() {
		return this.treeName;
	}
	
	/**
	 * Return the "top time", i.e. typically an explicitly stored
	 * branch length or arc time preceding the root. Is null if empty.
	 * @return the top time.
	 */
	public Double getTreeTopTime() {
		return this.treeTopTime;
	}
	
	/**
	 * Returns the vertex weights. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * See also getWeightsMap().
	 * @return the weights.
	 */
	public double[] getVertexWeights() {
		return this.vertexWeights;
	}
	
	/**
	 * Returns the vertex times. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * See also getTimesMap().
	 * @return the vertex times.
	 */
	public double[] getVertexTimes() {
		return this.vertexTimes;
	}
	
	/**
	 * Returns the arc times. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * See also getTimesMap().
	 * @return the arc times.
	 */
	public double[] getArcTimes() {
		return this.arcTimes;
	}
	
	/**
	 * Returns isDuplication values. If lacking values altogether, returns
	 * null. Single uninitialised items are set to Integer.MAX_VALUE.
	 * (for which one checks by comparing with Integer.MAX_VALUE).
	 * @return array of duplication flags.
	 */
	public int[] getDuplicationValues() {
		return this.isDuplication;
	}
	
	/**
	 * Returns a map of the vertex weights indexed by vertex numbers.
	 * If weights are lacking altogether, null is returned.
	 * @param name the name of the map.
	 * @return the map.
	 */
	public DoubleMap getVertexWeightsMap(String name) {
		return (this.vertexWeights != null ? new DoubleMap(name, this.vertexWeights) : null);
	}
	
	/**
	 * Returns a map of the times (representing both vertex times and arc times)
	 * indexed by vertex numbers. If the top time property is set, this overrides 
	 * the root value. Considers both vertex times, arc times and Newick branch lengths
	 * (in that order).
	 * @param name the name of the map.
	 * @return the map.
	 * @throws NewickIOException if missing times.
	 */
	public TimesMap getTimesMap(String name) throws NewickIOException {
		return getTimesMap(TimesPropertySelector.VT_AT_BL, name);
	}
	
	/**
	 * Returns a map of the times (representing both vertex times and arc times)
	 * indexed by vertex numbers. If the top time property is set, this overrides 
	 * the root value. The first encountered time property will act as a template,
	 * ignoring duplicate information (e.g. when there are both vertex and arc times).
	 * @param sel controls which properties are considered for creating the map.
	 * @param name the name of the map.
	 * @return the map.
	 * @throws NewickIOException if missing times.
	 */
	public TimesMap getTimesMap(TimesPropertySelector sel, String name) throws NewickIOException {
		boolean consVT = false;
		boolean consAT = false;
		boolean consBL = false;
		switch (sel) {
		case VT_AT_BL:
			consVT = true;
			consAT = true;
			consBL = true;
			break;
		case VT_AT:
			consVT = true;
			consAT = true;
			break;
		case BL:
			consBL = true;
			break;
		}
		
		// Compute both absolute and relative times.
		double[] vt = null;
		double[] at = null;
		if (consVT && this.vertexTimes != null) {
			vt = this.vertexTimes;
			at = PrIMENewickTreeVerifier.absoluteToRelative(this.getVerticesAsList(), vt);
			if (this.arcTimes != null) { at[this.root.getNumber()] = this.arcTimes[this.root.getNumber()]; }
		} else if (consAT && this.arcTimes != null) {
			at = this.arcTimes;
			vt = PrIMENewickTreeVerifier.relativeToAbsolute(this.getVerticesAsList(), at);
		} else if (consBL && this.hasBranchLengths) {
			at = this.getBranchLengths();
			vt = PrIMENewickTreeVerifier.relativeToAbsolute(this.getVerticesAsList(), at);
		} else {
			throw new NewickIOException("Cannot create time map; times missing in tree.");
		}
		
		// Explicit top time has precedence.
		if (this.treeTopTime != null) { at[this.root.getNumber()] = this.treeTopTime.doubleValue(); }
		return new TimesMap(name, vt, at);
	}
	
}
