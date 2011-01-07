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
		assertEquals(1000, prm.skeleton.getClass("Gene").getNoOfEntities());
		assertEquals(80, prm.skeleton.getClass("Array").getNoOfEntities());
		assertEquals(1000*80, prm.skeleton.getClass("Expression").getNoOfEntities());
	}

	public Skeleton skeleton;
	
	public SimpleMicroarrayPRM() throws FileNotFoundException {
		this.skeleton = new Skeleton();
		
		// Create classes.
		PRMClass genes = new PRMClass("Gene", new String[]{"ID"}, new String[]{"A1", "A2", "A3", "Cluster"});
		PRMClass arrays = new PRMClass("Array", new String[]{"ID"}, new String[]{"Cluster"});
		PRMClass expressions = new PRMClass("Expression", new String[]{"ID", "GeneID", "ArrayID"}, new String[]{"Level"});
		
		// Read class entities.
		this.readGeneFile(genes);
		this.readArrayFiles(arrays);
		this.readExpressionFiles(expressions);
		this.skeleton.putClass(genes.getName(), genes);
		this.skeleton.putClass(arrays.getName(), arrays);
		this.skeleton.putClass(expressions.getName(), expressions);
		
		// Create relations and relation entities.
		Relation e2g = new Relation(expressions, 1, genes, Relation.Type.MANY_TO_ONE);
		Relation e2a = new Relation(expressions, 2, arrays, Relation.Type.MANY_TO_ONE);
		this.skeleton.putRelation(e2g.getName(), e2g);
		this.skeleton.putRelation(e2a.getName(), e2a);
		e2g.cacheEntities();
		e2a.cacheEntities();
	}
	
	/**
	 * Reads the gene file. Sets the hidden variable to 0 by default.
	 * @throws FileNotFoundException.
	 */
	public void readGeneFile(PRMClass genes) throws FileNotFoundException {
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
	private void readArrayFiles(PRMClass arrays) throws FileNotFoundException {
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
	private void readExpressionFiles(PRMClass expressions) throws FileNotFoundException {
		File f;
		Scanner sc;
		// One file per array (80 in total).
		for (int i = 0; i < 80; ++i) {
			f = new File(this.getClass().getResource("/microarray/synthetic/exp_array_" + i + ".out").getFile());
			sc = new Scanner(f);
			String[] lvls = sc.nextLine().trim().split(",");
			// One expression level per gene (1000 in total).
			for (int j = 0; j < 1000; ++j) {
				String gID = "G" + j;
				String aID = "A" + i;
				String ID = gID + "-" + aID;
				IntAttribute lvl = new IntAttribute(Integer.parseInt(lvls[j]));
				expressions.putEntity(new String[]{ID, gID, aID}, new AttributeEntity[]{lvl});
			}
			sc.close();
		}
	}
	
}
