package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper. Filters a data matrix by just retrieving a subset of the columns and 
 * a specified number of the samples. Invoke toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleFilter {

	private String data;
	private int[] cols;
	private Integer noOfSamples;
	private String out;
	
	/**
	 * Constructor.
	 * @param in name of input data matrix.
	 * @param cols columns to keep after filtering (indexed from 1), null keeps all columns.
	 * @param noOfSamples max number of samples to keep, null keeps all samples.
	 * @param out name of output matrix holding sorted data.
	 */
	public SampleFilter(String in, int[] cols, Integer noOfSamples, String out) {
		this.data = in;
		this.cols = cols;
		this.noOfSamples = noOfSamples;
		this.out = out;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String data = this.data;
		sb.append("# Filtering samples keeping only certain columns/points.\n");
		if (this.cols != null) {
			sb.append("coltmp <- c(");
			for (int i : cols) {
				sb.append(i + ", ");
			}
			sb.delete(sb.length()-2, sb.length()).append(");\n");
			sb.append(this.out + " <- " + data + "[, coltmp, drop=TRUE];\n");
			data = this.out; // Continue filtering on already processed steps.
		}
		if (this.noOfSamples != null) {
			sb.append("if (is.vector(" + data + ")) {\n");
			sb.append("   " + this.out + " <- " + data + "[1:min(length(" + data + ")[1], " + this.noOfSamples + ")];\n");
			sb.append("} else {\n");
			sb.append("   " + this.out + " <- " + data + "[1:min(dim(" + data + ")[1], " + this.noOfSamples + "), ];\n");
			sb.append("}\n");
		}
		if (this.cols == null && this.noOfSamples == null) {
			sb.append(this.out + " <- " + data + ";\n");
		}
		return sb.toString();
	}
	

}
