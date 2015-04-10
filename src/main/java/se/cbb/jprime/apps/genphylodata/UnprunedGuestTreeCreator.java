package se.cbb.jprime.apps.genphylodata;

import java.util.List;
import se.cbb.jprime.math.PRNG;

/**
 * Interface of the all-mighty unpruned tree creators.
 * 
 * @author Joel Sjöstrand.
 */
public interface UnprunedGuestTreeCreator {

	/**
	 * Creates an unpruned tree. Appropriate labelling of vertices may not be in place in the returned tree.
	 * @param prng PRNG.
	 * @return the root.
	 */
	public GuestVertex createUnprunedTree(PRNG prng);

	/**
	 * Returns the host leaves.
	 * @return the host leaves.
	 */
	public List<Integer> getHostLeaves();
	
	/**
	 * Returns the host as a string.
	 * @return the host.
	 */
	public String getHost();

	/**
	 * Creates auxiliary info on a tree (pruned or unpruned) created by the same instance.
	 * @param guestRoot guest tree root.
	 * @param doML include ML estimates.
	 * @return the info.
	 */
	public String getInfo(GuestVertex guestRoot, boolean doML);

	/**
	 * Creates a leaf map on a tree (pruned or unpruned) created by the same instance.
	 * @param guestRoot the guest root. May be null.
	 * @return the leaf map.
	 */
	public String getLeafMap(GuestVertex guestRoot);

	/**
	 * Creates a guest-to-host mapping info on a tree (pruned or unpruned) created by the same instance.
	 * @param guestRoot the guest root. May be null.
	 * @return the info.
	 */
	public String getSigma(GuestVertex guestRoot);
	
}
