package se.cbb.jprime.apps.analysisextraction;
/**
 * Tree-type parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class TreeParameter extends Parameter {
	
	/** MAP tree in Newick format. */
	public String mapTree;
	
	/** Coverage of MAP tree in posterior. */
	public Double mapTreeProbability;
	
	/**
	 * Best state tree in Newick format w.r.t. probability density.
	 * Preferably, refers to all visited states rather than sampled states.
	 */
	public String bestStateTree;
	
	/**
	 * Constructor.
	 * @param name the parameter name.
	 * @param type the parameter type, e.g. 'Tree'.
	 */
	public TreeParameter(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "tree";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.mapTree != null)
			sb.append(this.name + " MAP tree: " + this.mapTree + '\n');
		if (this.mapTreeProbability != null)
			sb.append(this.name + " MAP tree probability: " + this.mapTreeProbability + '\n');
		if (this.bestStateTree != null)
			sb.append(this.name + " best state tree: " + this.bestStateTree + '\n');
		return sb.toString();
	}
}
