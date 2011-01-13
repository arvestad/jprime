package se.cbb.jprime.prm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.* ;
import org.uncommons.maths.random.MersenneTwisterRNG;

import se.cbb.jprime.math.IntegerInterval;
import se.cbb.jprime.prm.ProbAttribute.DependencyConstraints;
import se.cbb.jprime.prm.Relation.Type;

import static org.junit.Assert.*;

/**
 * Simple microarray PRM example.
 * 
 * @author Joel Sjöstrand.
 */
public class SimpleMicroarrayPRM {

	@Test
	public void main() throws FileNotFoundException {
		// Test skeleton.
		assertEquals(1000, this.skeleton.getPRMClass("Gene").getNoOfEntities());
		assertEquals(80, this.skeleton.getPRMClass("Array").getNoOfEntities());
		assertEquals(1000 * 80, this.skeleton.getPRMClass("Measurement").getNoOfEntities());
		
		this.generateStructures();
	}

	private MersenneTwisterRNG rng;
	
	private Skeleton skeleton;
	
	private ArrayList<Structure> structures;
	
	public SimpleMicroarrayPRM() throws FileNotFoundException {
		this.rng = new MersenneTwisterRNG();
		this.skeleton = new Skeleton("SimpleMicroarraySkeleton");
		this.structures = new ArrayList<Structure>(20);
		
		// Fill skeleton.
		this.readGeneFile();
		this.readArrayFiles();
		this.readMeasurementFiles();
	}
	
	/**
	 * Reads the gene file. Sets the hidden variable to 0 by default.
	 * @throws FileNotFoundException.
	 */
	public void readGeneFile() throws FileNotFoundException {
		// Skeleton part.
		PRMClass genes = new PRMClass("Gene");
		FixedAttribute id = new FixedAttribute("ID", genes, 1024);
		BooleanAttribute a1 = new BooleanAttribute("A1", genes, 1024, DependencyConstraints.NONE);
		BooleanAttribute a2 = new BooleanAttribute("A2", genes, 1024, DependencyConstraints.NONE);
		BooleanAttribute a3 = new BooleanAttribute("A3", genes, 1024, DependencyConstraints.NONE);
		IntegerInterval clusterRange = new IntegerInterval(1,12);
		IntAttribute cluster = new IntAttribute("Cluster", genes, 1024, DependencyConstraints.NONE,
				clusterRange);
		this.skeleton.addPRMClass(genes);
		
		// Read values. Assign random values to latent variable.
		System.out.println(this.getClass().getResource("."));
		File f = new File(this.getClass().getResource("/microarray/synthetic/genesAttributes.out").getFile());
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split(",");
			id.addEntity(parts[0]);
			a1.addEntity(parts[1].contains("A1"));
			a2.addEntity(parts[1].contains("A2"));
			a3.addEntity(parts[1].contains("A3"));
			cluster.addEntity(clusterRange.getRandom(this.rng));
		}
		sc.close();
	}
	
	/**
	 * Reads the microarray file.
	 * @throws FileNotFoundException.
	 */
	private void readArrayFiles() throws FileNotFoundException {
		// Skeleton part
		PRMClass arrays = new PRMClass("Array");
		FixedAttribute id = new FixedAttribute("ID", arrays, 128);
		IntAttribute cluster = new IntAttribute("Cluster", arrays, 128, DependencyConstraints.NONE,
				new IntegerInterval(1, 4));
		this.skeleton.addPRMClass(arrays);
		
		// Read values.
		File f = new File(this.getClass().getResource("/microarray/synthetic/ArrayCluster.out").getFile());
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { continue; }
			String[] parts = ln.split("[\t ]+");
			id.addEntity(parts[0]);
			cluster.addEntity(Integer.parseInt(parts[1]));
		}
		sc.close();
	}

	/**
	 * Reads the expression level files.
	 * @throws FileNotFoundException.
	 */
	private void readMeasurementFiles() throws FileNotFoundException {
		// Skeleton part.
		PRMClass measurements = new PRMClass("Measurement");
		FixedAttribute id = new FixedAttribute("ID", measurements, 8192);
		FixedAttribute gID = new FixedAttribute("GeneID", measurements, 8192);
		FixedAttribute aID = new FixedAttribute("ArrayID", measurements, 8192);
		IntAttribute level = new IntAttribute("Level", measurements, 8192, DependencyConstraints.NONE,
				new IntegerInterval(-1, 1));
		PRMClass genes = this.skeleton.getPRMClass("Gene");
		PRMClass arrays = this.skeleton.getPRMClass("Array");
		// Relations add themselves to their classes...
		new Relation(gID, genes.getFixedAttribute("ID"), Type.MANY_TO_ONE, true);
		new Relation(aID, arrays.getFixedAttribute("ID"), Type.MANY_TO_ONE, true);
		this.skeleton.addPRMClass(measurements);
		
		// Read values.
		File f;
		Scanner sc;
		// One file per array (80 in total).
		for (int i = 0; i < 80; ++i) {
			f = new File(this.getClass().getResource("/microarray/synthetic/exp_array_" + i + ".out").getFile());
			sc = new Scanner(f);
			String[] lvls = sc.nextLine().trim().split(",");
			// One expression level per gene (1000 in total).
			for (int j = 0; j < 1000; ++j) {
				id.addEntity("G" + j + "-" + "A" + i);
				gID.addEntity("G" + j);
				aID.addEntity("A" + i);
				level.addEntity(Integer.parseInt(lvls[j]));
			}
			sc.close();
		}
	}
	
	public void generateStructures() {
		Structure s = RandomStructureGenerator.createRandomStructure(this.rng, this.skeleton, 5, 100, 3);
		this.structures.add(s);
		System.out.println(s);
	}
	
}
