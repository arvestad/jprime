package se.cbb.jprime.math;

/**
 * Not a chi^2 distribution per se; contains related functions however.
 * 
 * @author Joel Sjöstrand.
 * @author Bengt Sennblad.
 * @author Martin Linder.
 */
public class ChiSquared {

	/**
	 * Quantile function for the chi^2 distribution, i.e. the inverse, F^-1(p), of its CDF, F.
	 * <p/>
	 * Algorithm AS 91.
	 * Reference: Best & Roberts, 1975, Appl. Statist 24(3):385-388
	 * <p/>
	 * This method is also known as <code>ppchi2()</code>.
	 * @param p the probability. For numerical reasons, must be in range [0.000002,0.999998].
	 * @param k degrees-of-freedom. Must be greater than 0.
	 * @return x so that F(x)=P(X<=x)=p.
	 */
	public static double quantile(double p, double k) {
		if (p < 2e-6 || p > 0.999998 || k <= 0.0) {
			throw new IllegalArgumentException("Invalid arguments for chi^2 quantile function.");
		}
		final double E = 0.5e-6;
		final double AA = Math.log(2.0);
		// double ppchi2 = -1.0;
		double XX = 0.5 * k;
		double C = XX - 1.0;
		double G = Gamma.lnGamma(XX); 

		double ch;
		if (k < (-1.24 * Math.log(p))) {
			// Approximation for small chi-squares.
			ch = Math.pow(p * XX * Math.exp(G + XX * AA), 1.0 / XX);     
			if (ch < E) {
				return ch;
			}
		} else {
			if (k <= 0.32) {
				// Approximation for k <= 0.32 using Newton-Raphson.
				ch = 0.4;
				double a = Math.log(1.0 - p);
				double Q;
				do {
					Q  = ch;
					double P1 = 1.0 + ch * (4.67 + ch);
					double P2 = ch * (6.73 + ch * (6.66 + ch));
					double T  = -0.5 + (4.67 + 2.0 * ch) / P1 - (6.73 + ch * (13.32 + 3.0 * ch)) / P2;
					ch  = ch - (1.0 - Math.exp(a + G + 0.5 * ch + C * AA) * P2/ P1) / T;
				} while (Math.abs(Q / ch - 1) > 0.01); 
			} else {
				// Note that p has been tested above.
				double X = Normal.quantile(p);
				// Starting approximation using Wilson and Hilferty estimate.
				double P1 = 0.222222 / k;
				ch = k * Math.pow((X * Math.sqrt(P1) + 1.0 - P1), 3);
				if (ch > (2.2 * k + 6.0)) {
					// Approximation for p tending to 1.
					ch = -2.0 * (Math.log(1.0 - p) - C * Math.log(0.5 * ch) + G);
				}
			}
		}
		//Call to algorithm AS 32 and calculation of seven-term Taylor series.
		double Q = ch;
		do {
			Q = ch;
			double P1 = 0.5 * ch;
			double P2 = p - Gamma.incGammaRatio(P1, XX);
			double T  = P2 * Math.exp(XX * AA + G + P1 - C * Math.log(ch)); 
			double B  = T / ch;
			double A  = 0.5 * T - B * C;
			double S1 = (210 + A * (140 + A * (105 + A * (84 + A * (70 + 60 * A))))) / 420;
			double S2 = (420 + A * (735 + A * (966 + A * (1141 + 1278 * A)))) / 2520;
			double S3 = (210 + A * (462 + A * (707 + 932 * A))) / 2520;
			double S4 = (252 + A * (672 + 1182 * A) + C * (294 + A * (889 + 1740 * A))) / 5040;
			double S5 = (84 + 264 * A + C * (175 + 606 * A)) / 2520;
			double S6 = (120 + C * (346 + 127 * C)) / 5040;
			ch = ch + T * (1 + 0.5 * T * S1 - B * C * (S1 - B * (S2 - B * (S3 - B * (S4 - B * (S5 - B * S6))))));
		} while (Math.abs(Q/ch -1) > E);  
		return(ch);
	}
}
