package se.cbb.jprime.apps.analysisextraction;
/**
 * Abstract base class for parameters.
 * 
 * @author Joel Sjöstrand.
 */
public abstract class Parameter {
	
	/** Name, e.g. 'Density2.mean'. */
	public String name;
	
	/**
	 * Constructor.
	 * @param name the parameter name.
	 * @param type the parameter type, e.g. 'Tree'.
	 */
	public Parameter(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the parameter type, e.g. 'tree' or 'float'.
	 * @return the type.
	 */
	public abstract String getType();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.name != null)
			sb.append(this.name);
		return sb.toString();
	}
}
