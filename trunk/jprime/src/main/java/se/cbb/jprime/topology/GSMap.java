package se.cbb.jprime.topology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a list of guest tree leaf identifiers and a list of
 * host tree leaf identifiers, and mappings between them.
 * Does not implement "sigma" functionality for mappings between
 * reconciled trees.
 * 
 * @author Joel Sjöstrand.
 */
public class GSMap {
	
	/** Guest-to-host map. */
	private HashMap<String, String> guestToHostLeafMap;
	
	/** Host-to-guest map. */
	private HashMap<String, Set<String>> hostToGuestLeafMap;
	
	/**
	 * Creates an empty map.
	 */
	public GSMap() {
		this.guestToHostLeafMap = new HashMap<String, String>();
		this.hostToGuestLeafMap = new HashMap<String, Set<String>>();
	}
	
	/**
	 * Adds a host-guest leaf pair. The Host leaf may already exist
	 * in other pair constellations.
	 * @param hostLeaf the host leaf ID.
	 * @param guestLeaf the guest leaf ID.
	 */
	public void add(String hostLeaf, String guestLeaf) {
		this.guestToHostLeafMap.put(guestLeaf, hostLeaf);
		Set<String> s = this.hostToGuestLeafMap.get(hostLeaf);
		if (s == null) {
			s = new HashSet<String>();
			this.hostToGuestLeafMap.put(hostLeaf, s);
		}
		s.add(guestLeaf);
	}
	
	/**
	 * Returns the guest leaf IDs.
	 * @return the IDs.
	 */
	public Set<String> getGuestLeafIDs() {
		return this.guestToHostLeafMap.keySet();
	}
	
	/**
	 * Returns the host leaf IDs.
	 * @return the IDs.
	 */
	public Set<String> getHostLeafIDs() {
		return this.hostToGuestLeafMap.keySet();
	}
	
	/**
	 * Returns the host leaf ID of a guest leaf.
	 * @param guestLeafID the guest leaf.
	 * @return the host leaf.
	 */
	public String getHostLeafID(String guestLeafID) {
		return this.guestToHostLeafMap.get(guestLeafID);
	}
	
	/**
	 * Returns the guest leaf IDs of a host leaf.
	 * @param guestLeafID the guest leaf.
	 * @return the host leaf.
	 */
	public Set<String> getGuestLeafIDs(String hostLeafID) {
		return this.hostToGuestLeafMap.get(hostLeafID);
	}
}
