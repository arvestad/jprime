package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper. Sorts a data matrix according to a specified column.
 * Invoke toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleSorter {

	private String data;
	private int sortColumn;
	private boolean sortAscending;
	private String out;
	
	/**
	 * Constructor.
	 * @param in name of input data matrix.
	 * @param sortColumn column index which to sort according to (indexed from 1).
	 * @param sortAscending true to sort ascending; false to sort descending.
	 * @param out name of output matrix holding sorted data.
	 */
	public SampleSorter(String in, int sortColumn, boolean sortAscending, String out) {
		this.data = in;
		this.sortColumn = sortColumn;
		this.sortAscending = sortAscending;
		this.out = out;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Sorting samples.\n");
		String dir = (this.sortAscending ? "decreasing=FALSE" : "decreasing=TRUE");
		sb.append(this.out + " <- " + this.data + "[order(" + this.data
				+ "[ ," + sortColumn + "], " + dir + "), ];\n");
		return sb.toString();
	}
	
}
