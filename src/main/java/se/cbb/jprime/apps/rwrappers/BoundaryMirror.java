package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper. For a sample matrix with values bounded to the positive domain,
 * performs a mirroring of data around the origin to all remaining quadrants/octants/...
 * Invoke toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class BoundaryMirror {
	
	private String data;
	private int noOfDims;
	private String out;
	
	/**
	 * Constructor.
	 * @param in name of input data as column-oriented matrix.
	 * @param noOfDims number of dimensions, i.e. columns of matrix.
	 * @param out name of output variable holding augmented matrix.
	 * @throws Exception.
	 */
	public BoundaryMirror(String in, int noOfDims, String out) throws IllegalArgumentException {
		this.data = in;
		this.noOfDims = noOfDims;
		this.out = out;
		if (noOfDims < 1 || noOfDims > 3) {
			throw new IllegalArgumentException("Invalid no of dimensions (must be be between 1 and 3).");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Mirroring positive data to all quadrants/octants/...\n");
		switch (this.noOfDims) {
		case 1:
			sb.append(this.out + " <- matrix(c(" + this.data + ", -" + this.data + "), ncol=1);\n");
			break;
		case 2:
			sb.append("utmp <- " + this.data + "[,1];\n");
			sb.append("vtmp <- " + this.data + "[,2];\n");
			sb.append(this.out + " <- matrix(c(utmp,utmp,-utmp,-utmp,vtmp,-vtmp,vtmp,-vtmp), ncol=2)\n");
			break;
		case 3:
			sb.append("utmp <- " + this.data + "[,1];\n");
			sb.append("vtmp <- " + this.data + "[,2];\n");
			sb.append("wtmp <- " + this.data + "[,3];\n");
			sb.append(this.out + " <- matrix(c(utmp,utmp,utmp,utmp,-utmp,-utmp,-utmp,-utmp,vtmp,vtmp,-vtmp,-vtmp,vtmp,vtmp,-vtmp,-vtmp,wtmp,-wtmp,wtmp,-wtmp,wtmp,-wtmp,wtmp,-wtmp), ncol=3);\n");
			break;
		default:
			break;
		}
		return sb.toString();
	}

}
