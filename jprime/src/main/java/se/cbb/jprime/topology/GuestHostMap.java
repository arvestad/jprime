package se.cbb.jprime.topology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a list of guest tree leaf names and a list of
 * host tree leaf names, and mappings between them.
 * Does not implement "sigma" functionality for mappings between
 * reconciled trees, see <code>MPRMap</code> for that functionality,
 * and <code>LeafLeafMap</code> for a simpler case.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestHostMap {
	
	/** Guest-to-host map. */
	private HashMap<String, String> guestToHostLeafMap;
	
	/** Host-to-guest map. */
	private HashMap<String, Set<String>> hostToGuestLeafMap;
	
	/**
	 * Creates an empty map.
	 */
	public GuestHostMap() {
		this.guestToHostLeafMap = new HashMap<String, String>();
		this.hostToGuestLeafMap = new HashMap<String, Set<String>>();
	}
	
	/**
	 * Adds a host-guest leaf pair. The Host leaf may already exist
	 * in other pair constellations.
	 * @param guestLeaf the guest leaf name.
	 * @param hostLeaf the host leaf name.
	 */
	public void add(String guestLeaf, String hostLeaf) {
		this.guestToHostLeafMap.put(guestLeaf, hostLeaf);
		Set<String> s = this.hostToGuestLeafMap.get(hostLeaf);
		if (s == null) {
			s = new HashSet<String>();
			this.hostToGuestLeafMap.put(hostLeaf, s);
		}
		s.add(guestLeaf);
	}
	
	/**
	 * Returns all guest leaf names.
	 * @return the names.
	 */
	public Set<String> getAllGuestLeafNames() {
		return this.guestToHostLeafMap.keySet();
	}
	
	/**
	 * Returns all host leaf names.
	 * @return the names.
	 */
	public Set<String> getAllHostLeafNames() {
		return this.hostToGuestLeafMap.keySet();
	}
	
	/**
	 * Returns the host leaf names of a guest leaf.
	 * @param guestLeaf the guest leaf.
	 * @return the host leaf.
	 */
	public String getHostLeafName(String guestLeaf) {
		return this.guestToHostLeafMap.get(guestLeaf);
	}
	
	/**
	 * Returns the guest leaf names of a host leaf.
	 * @param guestLeaf the guest leaf.
	 * @return the host leaf.
	 */
	public Set<String> getGuestLeafNames(String hostLeaf) {
		return this.hostToGuestLeafMap.get(hostLeaf);
	}
}
