package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper for KDE unconditional density object in package np.
 * Use toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class NPDensity {
	
	private String bw; 
	private String trainingData;
	private String estimationData;
	private String out;

	/**
	 * Constructor.
	 * @param bw np bandwidth object.
	 * @param trainingData training points used to create density estimate, null to use
	 *        same data as used when creating bandwidth object.
	 * @param estimationData data for which density is estimated, null to use
	 *        same data as used when creating bandwidth object.
	 * @param out name of np density object.
	 */
	public NPDensity(String bw, String trainingData, String estimationData, String out) {
		super();
		this.bw = bw;
		this.trainingData = trainingData;
		this.estimationData = estimationData;
		this.out = out;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Estimating np KDE density.\n");
		sb.append("library('np');\n");
		String tdat = (this.trainingData == null ? "" : (", tdat=" + this.trainingData));
		String edat = (this.estimationData == null ? "" : (", edat=" + this.estimationData));
		sb.append(out + " <- npudens(" + this.bw + tdat + edat + ");\n");
		return sb.toString();
	}
	
}
