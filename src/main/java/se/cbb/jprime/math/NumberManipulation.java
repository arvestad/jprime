package se.cbb.jprime.math;

/**
 * Convenience functions for int / double manipulation.
 * 
 * @author Joel Sjöstrand.
 */
public class NumberManipulation {

	/**
	 * Rounding as per <code>http://stackoverflow.com/questions/202302/rounding-to-an-arbitrary-number-of-significant-digits</code>.
	 * @param num number to round.
	 * @param n number of significant figures.
	 * @return the rounded number.
	 */
	public static double roundToSignificantFigures(double num, int n) {
	    if (num == 0) {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted / magnitude;
	}
	
}
