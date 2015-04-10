package se.cbb.jprime.mcmc;

/**
 * Interface for objects that can produce info 
 * regarding a run, typically concerning application settings and behaviour.
 * 
 * @author Joel Sjöstrand.
 */
public interface InfoProvider {
	
	/**
	 * Returns a string detailing this object prior to the run.
	 * May return null. Example of preferred format:
	 * <pre>
	 * &lt;prefix&gt;MY UPPERCASE HEADER
	 * &lt;prefix&gt;Lowercase property 1
	 * &lt;prefix&gt;Lowercase property 2
	 * &lt;linebreak&gt
	 * </pre>
	 * @param prefix prepends each line.
	 * @return pre-run info.
	 */
	public String getPreInfo(String prefix);
	
	/**
	 * Returns a string detailing this object after the run.
	 * May return null. Example of preferred format:
	 * <pre>
	 * &lt;prefix&gt;MY UPPERCASE HEADER
	 * &lt;prefix&gt;Lowercase property 1
	 * &lt;prefix&gt;Lowercase property 2
	 * &lt;linebreak&gt
	 * </pre>
	 * @param prefix prepends each line.
	 * @return post-run info.
	 */
	public String getPostInfo(String prefix);
}
