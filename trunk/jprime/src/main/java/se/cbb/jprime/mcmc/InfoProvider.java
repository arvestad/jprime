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
	 * May return null.
	 * @return pre-run info.
	 */
	public String getPreInfo();
	
	/**
	 * Returns a string detailing this object after the run.
	 * May return null.
	 * @return post-run info.
	 */
	public String getPostInfo();
}
