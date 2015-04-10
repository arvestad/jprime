package se.cbb.jprime.topology;

/**
 * Simple enumeration which can be used for specifying how much of a
 * tree (vertices or arcs) some property or characteristic (e.g. branch lengths,
 * names) applies to.
 * <p/>
 * For convenience, one may choose to verify if a coverage is in fact a superset
 * or (more rarely) a subset of another coverage. E.g. if one has retrieved the coverage
 * of vertices with names, one may wish to proceed as long as that is a superset of the leaves.
 * <p/>
 * In case of small trees where several enumeration alternatives are in fact equal,
 * it is up to implementing functions to produce consistent results (e.g. for a one-vertex
 * tree don't mix 'ALL', 'ONLY_ROOT' and 'ONLY_LEAVES').
 * 
 * @author Joel Sjöstrand.
 */
public enum TreePropertyCoverage {
	ALL,
	ALL_BUT_ROOT,
	ALL_BUT_LEAVES,
	ALL_BUT_ROOT_AND_LEAVES,
	ONLY_ROOT_AND_LEAVES,
	ONLY_ROOT,
	ONLY_LEAVES,
	NONE;
	
	/**
	 * Returns true if the coverage is a superset of another
	 * coverage. A coverage is considered a superset of itself.
	 * @param subset the tentative subset.
	 * @return true if the specified coverage is a subset of this coverage.
	 */
	public boolean isSupersetOf(TreePropertyCoverage subset) {
		return isSubset(subset, this);
	}
	
	/**
	 * Returns true if the coverage is a subset of another
	 * coverage. A coverage is considered a subset of itself.
	 * @param superset the tentative superset.
	 * @return true if the specified coverage is a superset of this coverage.
	 */
	public boolean isSubsetOf(TreePropertyCoverage superset) {
		return isSubset(this, superset);
	}
	
	/**
	 * Returns true if a specified coverage is a subset of another
	 * coverage. A coverage is considered a subset of itself.
	 * @param subset the tentative subset.
	 * @param superset the tentative superset.
	 * @return true if the subset-superset relation is fulfilled.
	 */
	public static boolean isSubset(TreePropertyCoverage subset, TreePropertyCoverage superset) {
		switch (subset) {
		case ALL:
			return (superset == ALL);
		case ALL_BUT_ROOT:
			return (superset == ALL || superset == ALL_BUT_ROOT);
		case ALL_BUT_LEAVES:
			return (superset == ALL || superset == ALL_BUT_LEAVES);
		case ALL_BUT_ROOT_AND_LEAVES:
			return (superset == ALL || superset == ALL_BUT_ROOT ||
					superset == ALL_BUT_LEAVES || superset == ALL_BUT_ROOT_AND_LEAVES);
		case ONLY_ROOT_AND_LEAVES:
			return (superset == ALL || superset == ONLY_ROOT_AND_LEAVES);
		case ONLY_ROOT:
			return (superset == ALL || superset == ALL_BUT_LEAVES ||
					superset == ONLY_ROOT_AND_LEAVES || superset == ONLY_ROOT);
		case ONLY_LEAVES:
			return (superset == ALL || superset == ALL_BUT_ROOT ||
					superset == ONLY_ROOT_AND_LEAVES || superset == ONLY_LEAVES);
		case NONE:
			return true;
		default:
			return false;	
		}
	}
}
