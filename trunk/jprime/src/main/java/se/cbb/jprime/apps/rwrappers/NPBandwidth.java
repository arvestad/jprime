package se.cbb.jprime.apps.rwrappers;

/**
 * R wrapper for KDE bandwidth object in package np.
 * Use toString() to retrieve R code.
 * 
 * @author Joel Sjöstrand.
 */
public class NPBandwidth {

	/**
	 * Available bandwidth methods. Rule-of-thumb assumes a Gaussian kernel (see e.g. Scott, 19XX)
	 * and just makes use of the IQR. CV methods are significantly more
	 * computationally intensive. See also some interesting remarks in Skold & Roberts, 2003.
	 */
	public static enum BandwidthMethod {
		RULE_OF_THUMB                       ("normal-reference"),
		CROSS_VALIDATION_MAXIMUM_LIKELIHOOD ("cv.ml"),
		CROSS_VALIDATION_LEAST_SQUARES      ("cv.ls");
		
		String rString;
		
		private BandwidthMethod(String rString) {
			this.rString = rString;
		}
		
		@Override
		public String toString() {
			return this.rString;
		}
		
		public static BandwidthMethod valueOfRString(String rString) {
			for (BandwidthMethod bwm : BandwidthMethod.values()) {
				if (rString.equals(bwm.rString))
					return bwm;
			}
			throw new IllegalArgumentException("Invalid bandwidth method.");
		}
	}
	
	/**
	 * Available kernels. Epanechnikov and Gaussian are recommended.
	 * Gaussian is recommended when rule-of-thumb bandwidth is used.
	 */
	public static enum Kernel {
		GAUSSIAN      ("gaussian"),
		EPANECHNIKOV  ("epanechnikov"),
		RECTANGULAR   ("rectangular"),
		TRIANGULAR    ("triangular"),
		BIWEIGHT      ("biweight"),
		COSINE        ("cosine"),
		OPTCOSINE     ("optcosine");
		
		String rString; 
		
		private Kernel(String rString) {
			this.rString = rString;
		}
		
		@Override
		public String toString() {
			return this.rString;
		}
		
		public static Kernel valueOfRString(String rString) {
			for (Kernel k : Kernel.values()) {
				if (rString.equals(k.rString))
					return k;
			}
			throw new IllegalArgumentException("Invalid kernel.");
		}
	}
	
	private String data; 
	private BandwidthMethod bw;
	private Kernel kernel;
	private String out;
	
	/**
	 * Constructor.
	 * @param in name of training data matrix.
	 * @param bwm bandwidth method.
	 * @param kernel kernel.
	 * @param out name of variable containing bandwidth object.
	 */
	public NPBandwidth(String in, String bwm, String kernel, String out) {
		this.data = in;
		this.bw = BandwidthMethod.valueOfRString(bwm);
		this.kernel = Kernel.valueOfRString(kernel);
		this.out = out;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Creating np unconditional bandwidth object.\n");
		sb.append("library('np');\n");
		sb.append(out + " <- npudensbw(dat=" + this.data + ", bwmethod='"
				+ this.bw + "', ckertype='" + this.kernel + "');\n");
		return sb.toString();
	}
	
}
