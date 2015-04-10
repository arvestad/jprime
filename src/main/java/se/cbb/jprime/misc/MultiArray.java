package se.cbb.jprime.misc;

/**
 * Interface for multidimensional containers.
 * 
 * @author Joel Sjöstrand.
 */
public interface MultiArray {

	/**
	 * Returns the Class object representing the component type of elements
	 * contained within.
	 * @return the component type.
	 */
	public java.lang.Class<?> getComponentType();
	
   /**
    * Returns the lengths in all dimensions of the array.
    * @return the lengths.
    */
	public int[] getLengths();
	
	/**
	 * Returns the number of dimensions of the array.
	 * @return the number of dimensions.
	 */
	public int getRank();

}
