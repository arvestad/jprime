package se.cbb.jprime.topology;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Holds a list of guest tree leaf names and a list of
 * host tree leaf names, and mappings between them. It has no knowledge of
 * tree topologies, and as such,
 * does not implement "sigma" functionality for mappings between
 * reconciled trees, see <code>MPRMap</code> for that functionality,
 * and <code>LeafLeafMap</code> for a simpler case.
 * <p/>
 * See <code>GuestHostMapReader</code> for factory methods.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestHostMap {
	
	/** Guest-to-host map. */
	private LinkedHashMap<String, String> guestToHostLeafMap;
	
	/** Host-to-guest map. */
	private LinkedHashMap<String, Set<String>> hostToGuestLeafMap;
	
	/**
	 * Creates an empty map.
	 */
	public GuestHostMap() {
		this.guestToHostLeafMap = new LinkedHashMap<String, String>();
		this.hostToGuestLeafMap = new LinkedHashMap<String, Set<String>>();
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
	 * May return null.
	 * @param guestLeaf the guest leaf.
	 * @return the host leaf.
	 */
	public String getHostLeafName(String guestLeaf) {
		return this.guestToHostLeafMap.get(guestLeaf);
	}
	
	/**
	 * Returns the guest leaf names of a host leaf.
	 * May return null.
	 * @param guestLeaf the guest leaf.
	 * @return the host leaf.
	 */
	public Set<String> getGuestLeafNames(String hostLeaf) {
		return this.hostToGuestLeafMap.get(hostLeaf);
	}
	
	/**
	 * Returns the number of name-pairs of this map.
	 * @return the number of name-pairs.
	 */
	public int getNoOfLeafNames() {
		return this.guestToHostLeafMap.size();
	}
}
