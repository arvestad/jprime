package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper. For a sample matrix with values bounded to the positive domain, adds points along all axes from
 * the origin to the third quartile (of each dimension). For instance, in the two-dimensional case, adds
 * points (x_i,0) and (0,y_i). Points, are added equidistantly. Invoke toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class BoundaryAugmenter {

	private String data;
	private int noOfDims;
	private int noOfPts;
	private String out;
	
	/**
	 * Constructor.
	 * @param in name of input data as column-oriented matrix.
	 * @param noOfDims number of dimensions, i.e. columns of matrix.
	 * @param noOfPts number of points to be added along each axis.
	 * @param out name of output variable holding augmented matrix.
	 * @throws Exception.
	 */
	public BoundaryAugmenter(String in, int noOfDims, int noOfPts, String out) throws IllegalArgumentException {
		this.data = in;
		this.noOfDims = noOfDims;
		this.noOfPts = noOfPts;
		this.out = out;
		if (noOfDims < 1 || noOfDims > 3) {
			throw new IllegalArgumentException("Invalid no of dimensions (must be be between 1 and 3).");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Augmenting points along axes from origin up to third quartile.\n");
		sb.append("augtmp <- " + "rep(0, " + this.noOfPts + "+1);\n");
		switch (this.noOfDims) {
		case 1:
			sb.append("utmp <- " + this.data + ";\n");
			sb.append("q3tmp <- quantile(utmp)[4];\n");
			sb.append("u0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append(this.out + " <- c(utmp,u0tmp);\n");
			break;
		case 2:
			sb.append("utmp <- " + this.data + "[,1];\n");
			sb.append("vtmp <- " + this.data + "[,2];\n");
			sb.append("q3tmp <- quantile(utmp)[4];\n");
			sb.append("u0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append("q3tmp <- quantile(vtmp)[4];\n");
			sb.append("v0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append(this.out + " <- matrix(c(utmp,u0tmp,augtmp,vtmp,augtmp,v0tmp), ncol=2);\n");
			break;
		case 3:
			sb.append("utmp <- " + this.data + "[,1];\n");
			sb.append("vtmp <- " + this.data + "[,2];\n");
			sb.append("wtmp <- " + this.data + "[,3];\n");
			sb.append("q3tmp <- quantile(utmp)[4];\n");
			sb.append("u0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append("q3tmp <- quantile(vtmp)[4];\n");
			sb.append("v0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append("q3tmp <- quantile(wtmp)[4];\n");
			sb.append("w0tmp <- seq(from=0, to=q3tmp, by=q3tmp/" + this.noOfPts + ");\n");
			sb.append(this.out + " <- matrix(c(utmp,u0tmp,augtmp,augtmp,vtmp,augtmp,v0tmp,augtmp,wtmp,augtmp,augtmp,w0tmp), ncol=3);\n");
			break;
		default:
			break;	
		}
		return sb.toString();
	}
	
}
