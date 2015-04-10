package se.cbb.jprime.misc;

import java.util.ArrayList;

/**
 * Simple class intended for algorithms concerning
 * arrays and ArrayLists.
 * 
 * @author Joel Sjöstrand.
 */
public class ArrayAlgorithms {

	/**
	 * Converts an ArrayList of Integers to an array of unboxed ints.
	 * An empty list renders an empty non-null array.
	 * @param list the list.
	 * @return the array.
	 */
	public static int[] toIntArray(ArrayList<Integer> list) {
		int[] a = new int[list.size()];
		for (int i = 0; i < a.length; ++i) {
			a[i]= ((Integer) list.get(i)).intValue();
		}
		return a;
	}
	
	/**
	 * Converts an ArrayList of Doubles to an array of unboxed doubles.
	 * An empty list renders an empty non-null array.
	 * @param list the list.
	 * @return the array.
	 */
	public static double[] toDoubleArray(ArrayList<Double> list) {
		double[] a = new double[list.size()];
		for (int i = 0; i < a.length; ++i) {
			a[i]= ((Double) list.get(i)).doubleValue();
		}
		return a;
	}
	
	/**
	 * Converts an ArrayList of Characters to an array of unboxed chars.
	 * An empty list renders an empty non-null array.
	 * @param list the list.
	 * @return the array.
	 */
	public static char[] toCharArray(ArrayList<Character> list) {
		char[] a = new char[list.size()];
		for (int i = 0; i < a.length; ++i) {
			a[i]= ((Character) list.get(i)).charValue();
		}
		return a;
	}
	
	/**
	 * Converts an ArrayList of Booleans to an array of unboxed booleans.
	 * An empty list renders an empty non-null array.
	 * @param list the list.
	 * @return the array.
	 */
	public static boolean[] toBooleanArray(ArrayList<Boolean> list) {
		boolean[] a = new boolean[list.size()];
		for (int i = 0; i < a.length; ++i) {
			a[i]= ((Boolean) list.get(i)).booleanValue();
		}
		return a;
	}
}
