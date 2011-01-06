package se.cbb.jprime.prm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.* ;

import static org.junit.Assert.*;

/**
 * Simple microarray PRM example.
 * 
 * @author Joel Sjöstrand.
 */
public class SimpleMicroarrayPRM {

	@Test
	public void main() throws FileNotFoundException {
		SimpleMicroarrayPRM prm = new SimpleMicroarrayPRM();
		prm.readGeneFile();
		prm.readArrayFiles();
		prm.readExpressionFiles();
		assertEquals(1000, prm.genes.getNoOfEntities());
		assertEquals(80, prm.arrays.getNoOfEntities());
		assertEquals(1000*80, prm.expressions.getNoOfEntities());
	}

	private PRMClass genes;
	private PRMClass arrays;
	private PRMClass expressions;
	
	public SimpleMicroarrayPRM() {
		genes = new PRMClass("Genes", new String[]{"ID"}, new String[]{"A1", "A2", "A3", "Cluster"});
		arrays = new PRMClass("Arrays", new String[]{"ID"}, new String[]{"Cluster"});
		expressions = new PRMClass("Expressions", new String[]{"ID"}, new String[]{"Level"},
				new PRMClass[]{genes, arrays});
	}
	
	/**
	 * Reads the gene file. Sets the hidden variable to 0 by default.
	 * @throws FileNotFoundException.
	 */
	public void readGeneFile() throws FileNotFoundException {
		File f = new File(this.getClass().getResource("/microarray/synthetic/genesAttributes.out").getFile());
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split(",");
			AttributeEntity[] atts = new AttributeEntity[]{
					new BooleanAttribute(parts[1].contains("A1")),
					new BooleanAttribute(parts[1].contains("A2")),
					new BooleanAttribute(parts[1].contains("A3")),
					new IntAttribute(0)
			};
			genes.putEntity(new String[]{parts[0]}, atts);
		}
		sc.close();
	}
	
	/**
	 * Reads the microarray file.
	 * @throws FileNotFoundException.
	 */
	private void readArrayFiles() throws FileNotFoundException {
		File f = new File(this.getClass().getResource("/microarray/synthetic/ArrayCluster.out").getFile());
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split("[\t ]+");
			AttributeEntity[] atts = new AttributeEntity[]{ new IntAttribute(Integer.parseInt(parts[1]))};
			arrays.putEntity(new String[]{parts[0]}, atts);
		}
		sc.close();
	}

	/**
	 * Reads the expression level files.
	 * @throws FileNotFoundException.
	 */
	private void readExpressionFiles() throws FileNotFoundException {
		File f;
		Scanner sc;
		// One file per array (80 in total).
		for (int i = 0; i < 80; ++i) {
			f = new File(this.getClass().getResource("/microarray/synthetic/exp_array_" + i + ".out").getFile());
			sc = new Scanner(f);
			String[] lvls = sc.nextLine().trim().split(",");
			// One expression level per gene (1000 in total).
			for (int j = 0; j < 1000; ++j) {
				String ID = "G" + j + "A" + i;
				IntAttribute lvl = new IntAttribute(Integer.parseInt(lvls[j]));
				expressions.putEntity(new String[]{ID}, new AttributeEntity[]{lvl}, new String[]{"G"+j, "A"+i});
			}
			sc.close();
		}
	}
	
}
