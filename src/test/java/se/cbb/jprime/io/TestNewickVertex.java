package se.cbb.jprime.io;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * JUnit Test Case.
 * 
 * @author Vincent Llorens.
 */
public class TestNewickVertex {

	@Test
	public void copyConstructor() {
		NewickVertex v = new NewickVertex();
		NewickVertex vCopy = new NewickVertex(v);
		assertTrue(v != vCopy);
	}

}
