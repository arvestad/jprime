package se.cbb.jprime.io;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;

/**
 * Wrapper which parses the PrIME-specific meta info of a NewickTree and
 * provides simplified access to such data (alongside the original topology).
 * It is required that vertices are numbered from 0 to |V(T)|-1, and that
 * meta info tags have the form "[&&PRIME ...]".
 * <p/>
 * Basically, all info apart from topology and vertex numbering may be retrieved
 * as arrays from this class (indexed by vertex number).
 * <p/>
 * Whenever there is duplication of information (e.g. for vertex numbers,
 * branch lengths), meta tags will have precedence and the NewickTree be updated
 * accordingly.
 * <p/>
 * Vertex times and arc times may coexist, and similarly a "tree top time" may coexist alongside
 * an arc time of the root. However, one may verify compatibility of such properties (see
 * also PrIMENewickTreeVerifier).
 * <p/>
 * The following properties are handled:
 * <pre>
 * Property         "Pure Newick":  PrIME meta:   Access:
 * ------------------------------------------------------------
 * Topology         Y                             NewickTree.
 * Tree name                        Y             This class.
 * Tree top time                    Y             This class.
 * Vertex numbers   Y               Y             NewickTree.
 * Vertex names     Y               Y             Both.
 * Vertex weights                   Y             This class.
 * Branch lengths   Y               Y             Both.
 * Vertex times                     Y             This class.
 * Arc times                        Y             This class.
 * </pre>
 * 
 * @author Joel Sjöstrand.
 */
public class PrIMENewickTree {
	
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
		ARC_TIMES        ("ET=\"?([0-9\\+\\-\\.e]+)\"?");
		
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
	
	/** The underlying tree. */
	private NewickTree newickTree;
	
	/** Size of the underlying tree. */
	private int noOfVertices;
	
	/** Tree name. */
	private String treeName = null;
	
	/** Tree "top time". */
	private double treeTopTime = Double.NaN;
	
	/** Vertex names. */
	private String[] vertexNames = null;
	
	/** Branch lengths. */
	private double[] branchLengths = null;
	
	/** Vertex weights. */
	private double[] vertexWeights = null;
	
	/** Vertex weights. */
	private double[] vertexTimes = null;
	
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
		this.newickTree = tree;
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
			return (!Double.isNaN(this.treeTopTime));
		case VERTEX_NUMBERS:
			return true;
		case VERTEX_NAMES:
			return (this.vertexNames != null);
		case BRANCH_LENGTHS:
			return (this.branchLengths != null);
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
		String meta = this.newickTree.getMeta();
		String val;
		
		val = MetaProperty.TREE_NAME.getValue(meta);
		if (val != null) {
			this.treeName = val;
		}
		val = MetaProperty.TREE_TOP_TIME.getValue(meta);
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
			n.setNumber(x);
		}
		
		// Read "pure Newick" properties.
		if (n.hasName()) {
			setVertexName(x, n.getName());
		}
		if (n.hasBranchLength()) {
			setBranchLength(x, n.getBranchLength());
		}
		
		// Read meta properties.
		val = MetaProperty.VERTEX_NAMES.getValue(meta);
		if (val != null) {
			setVertexName(x, val);
		}
		val = MetaProperty.BRANCH_LENGTHS.getValue(meta);
		if  (val != null) {
			double bl = Double.parseDouble(val);
			setBranchLength(x, bl);
			n.setBranchLength(bl);
		}
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
	}
	
	/**
	 * Sets a vertex name. All empty names are null.
	 * @param x the vertex.
	 * @param name the name.
	 */
	private void setVertexName(int x, String name) {
		if (this.vertexNames == null) {
			this.vertexNames = new String[this.noOfVertices];
		}
		this.vertexNames[x] = name;
	}
	
	/**
	 * Sets a branch length. All empty branch lengths are NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @param x the vertex.
	 * @param branchLength the branch length.
	 */
	private void setBranchLength(int x, double branchLength) {
		if (this.branchLengths == null) {
			this.branchLengths = new double[this.noOfVertices];
			for (int i = 0; i < this.branchLengths.length; ++i) {
				this.branchLengths[i] = Double.NaN;
			}
		}
		this.branchLengths[x] = branchLength;
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
	 * Ultrametricity or compatibility with vertex times is not verified.
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
	 * Returns the underlying plain Newick tree. Note: this has been
	 * updated to comply with duplicate fields found in PrIME meta info.
	 * @return the tree.
	 */
	public NewickTree getNewickTree() {
		return this.newickTree;
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
	 * branch length or arc time preceding the root. Is NaN if empty.
	 * @return the top time.
	 */
	public double getTreeTopTime() {
		return this.treeTopTime;
	}
	
	/**
	 * Returns the vertex names. If lacking values altogether, returns
	 * null. Single uninitialised items are set to null.
	 * @return the vertex names.
	 */
	public String[] getVertexNames() {
		return this.vertexNames;
	}
	
	/**
	 * Returns the branch lengths. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @return the branch lengths.
	 */
	public double[] getBranchLengths() {
		return this.branchLengths;
	}
	
	/**
	 * Returns the vertex weights. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @return the weights.
	 */
	public double[] getVertexWeights() {
		return this.vertexWeights;
	}
	
	/**
	 * Returns the vertex times. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @return the vertex times.
	 */
	public double[] getVertexTimes() {
		return this.vertexTimes;
	}
	
	/**
	 * Returns the arc times. If lacking values altogether, returns
	 * null. Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @return the arc times.
	 */
	public double[] getArcTimes() {
		return this.arcTimes;
	}
	
	/**
	 * Returns a map of the names indexed by vertex numbers.
	 * @return the map.
	 */
	public StringMap getVertexNamesMap() {
		return new StringMap("VertexNames", this.getVertexNames());
	}
	
	/**
	 * Returns a map of the branch lengths indexed by vertex numbers.
	 * @return the map.
	 */
	public DoubleMap getBranchLengthsMap() {
		return new DoubleMap("BranchLengths", this.getBranchLengths());
	}
	
	/**
	 * Returns a map of the branch lengths indexed by vertex numbers.
	 * @return the map.
	 */
	public DoubleMap getVertexWeightsMap() {
		return new DoubleMap("VertexWeights", this.getVertexWeights());
	}
	
	/**
	 * Returns a map of the times (representing both vertex times and arc times)
	 * indexed by vertex numbers.
	 * @return the map.
	 */
	public TimesMap getTimesMap() {
		return new TimesMap("Times", this.vertexTimes, this.arcTimes);
	}
	
}
