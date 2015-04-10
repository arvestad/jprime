package se.cbb.jprime.prm;

import org.junit.* ;

import se.cbb.jprime.prm.ProbAttribute.DependencyConstraints;

import static org.junit.Assert.*;

/**
 * JUnit test class for DirichletCounts.
 * 
 * @author Joel Sjöstrand.
 */
public class TestDirichletCounts {

	private PRMClass c;
	private BooleanAttribute b;
	private IntAttribute i;
	private IntAttribute ilat;
	private BooleanAttribute blat;
	
	@Before
	public void createAttributesAndEntities() {
		this.c = new PRMClass("TestPRMClass");
		this.b = new BooleanAttribute("B", this.c, false, 4, DependencyConstraints.NONE);
		this.i = new IntAttribute("I", this.c, false, 4, DependencyConstraints.NONE, 3);
		this.ilat = new IntAttribute("ILat", this.c, true, 4, DependencyConstraints.NONE, 3);
		this.blat = new BooleanAttribute("BLat", this.c, true, 4, DependencyConstraints.NONE);
		this.b.useSharpSoftCompletion();
		this.i.useSharpSoftCompletion();
		this.ilat.useSharpSoftCompletion();
		this.blat.useSharpSoftCompletion();
		this.b.addEntity(true);
		this.b.addEntity(false);
		this.b.addEntity(true);
		this.b.addEntity(false);
		this.i.addEntityAsInt(2);
		this.i.addEntityAsInt(0);
		this.i.addEntityAsInt(1);
		this.i.addEntityAsInt(0);
		this.ilat.addEntity(2);
		this.ilat.addEntity(1);
		this.ilat.addEntity(0);
		this.ilat.addEntity(2);
		this.blat.addEntityAsInt(0);
		this.blat.addEntityAsInt(0);
		this.blat.addEntityAsInt(1);
		this.blat.addEntityAsInt(1);
		double[] pd = this.ilat.getEntityProbDistribution(0);
		pd[0] = 0.25;
		pd[1] = 0.50;
		pd[2] = 0.25;
		pd = this.blat.getEntityProbDistribution(0);
		pd[0] = 0.33;
		pd[1] = 0.67;
	}
	
	@Test
	public void TestLatentChild() {
		// Configurations created (current hard assignment first for latent variables):
		//
		// b:  i:  ilat:    blat:
		//----------------------------------
		// 1   2   2*0.25   0*0.33   Index 0
		// 1   2   2*0.25   1*0.67
		// 1   2   1*0.50   0*0.33
		// 1   2   1*0.50   1*0.67
		// 1   2   0*0.25   0*0.33
		// 1   2   0*0.25   1*0.67
		//----------------------------------
		// 0   0   1*1.00   0*1.00   Index 1 -- see also index 3.
		// 0   0   1*1.00   1*0.00
		// 0   0   2*0.00   0*1.00
		// 0   0   2*0.00   1*0.00
		// 0   0   0*0.00   0*0.00
		// 0   0   0*0.00   1*0.00
		//----------------------------------
		// 1   1   0*1.00   1*1.00   Index 2
		// 1   1   0*1.00   0*0.00
		// 1   1   1*0.00   1*1.00
		// 1   1   1*0.00   0*0.00
		// 1   1   2*0.00   1*1.00
		// 1   1   2*0.00   0*0.00
		//----------------------------------
		// 0   0   2*1.00   1*1.00   Index 3 -- see also index 1.
		// 0   0   2*1.00   0*0.00
		// 0   0   1*0.00   1*1.00
		// 0   0   1*0.00   0*0.00
		// 0   0   0*0.00   1*1.00
		// 0   0   0*0.00   0*0.00
		Dependencies deps = new Dependencies(this.blat);
		deps.put(new Dependency(this.blat, null, this.i, true));
		deps.put(new Dependency(this.blat, null, this.ilat, true));
		deps.put(new Dependency(this.blat, null, this.b, true));
		// Order retrieved will be B,I,ILAT due to internal sorting.
		// First pseudo count==0.0 => ML.
		DirichletCounts dc = new DirichletCounts(deps, 0.0);
		assertEquals(0.0825, dc.getCount(new int[]{1,2,2,0}), 1e-6);
		assertEquals(0.335, dc.getCount(new int[]{1,2,1,1}), 1e-6);
		assertEquals(0.50, dc.getSummedCount(new int[]{1,2,1}), 1e-6);
		assertEquals(0.0, dc.getCount(new int[]{0,0,0,0}), 1e-6);
		assertEquals(0.33, dc.getExpectedConditionalProb(new int[]{1,2,2,0}), 1e-6);
		assertEquals(0.67, dc.getExpectedConditionalProb(new int[]{1,2,2,1}), 1e-6);
		assertEquals(0.67, dc.getExpectedConditionalProb(new int[]{1,2,1,1}), 1e-6);
		assertEquals(0.5, dc.getExpectedConditionalProb(new int[]{0,0,0,0}), 1e-6);
		assertEquals(0.5, dc.getExpectedConditionalProb(new int[]{0,0,0,1}), 1e-6);
		assertEquals(0.0, dc.getExpectedConditionalProb(new int[]{0,0,1,1}), 1e-6);
		assertEquals(1.0, dc.getExpectedConditionalProb(new int[]{0,0,1,0}), 1e-6);
		// ...then pseudo count==1.0.
		dc = new DirichletCounts(deps, 1.0);
		assertEquals(0.5, dc.getExpectedConditionalProb(new int[]{0,0,0,0}), 1e-6);
		assertEquals(0.5, dc.getExpectedConditionalProb(new int[]{0,0,0,1}), 1e-6);
		assertEquals(0.333333333333, dc.getExpectedConditionalProb(new int[]{0,0,1,1}), 1e-6);
		assertEquals(0.666666666666, dc.getExpectedConditionalProb(new int[]{0,0,1,0}), 1e-6);
	}
	
	
	@Test
	public void TestChildOnly() {
		// Configurations created:
		//
		// i:
		// ------
		// 2
		// 0
		// 1
		// 0
		Dependencies deps = new Dependencies(this.i);
		DirichletCounts dc = new DirichletCounts(deps, 0.0);
		assertEquals(2, dc.getCount(new int[]{0}), 1e-6);
		assertEquals(1, dc.getCount(new int[]{1}), 1e-6);
		assertEquals(1, dc.getCount(new int[]{2}), 1e-6);
		assertEquals(0.5, dc.getExpectedConditionalProb(new int[]{0}), 1e-6);
		assertEquals(0.25, dc.getExpectedConditionalProb(new int[]{1}), 1e-6);
		assertEquals(0.25, dc.getExpectedConditionalProb(new int[]{2}), 1e-6);
	}
}
