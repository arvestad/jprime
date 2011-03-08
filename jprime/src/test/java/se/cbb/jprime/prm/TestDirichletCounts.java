package se.cbb.jprime.prm;

import org.junit.* ;

import se.cbb.jprime.math.IntegerInterval;
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
		this.i = new IntAttribute("I", this.c, false, 4, DependencyConstraints.NONE, new IntegerInterval(-1, 1));
		this.ilat = new IntAttribute("ILat", this.c, true, 4, DependencyConstraints.NONE, new IntegerInterval(0,2));
		this.blat = new BooleanAttribute("BLat", this.c, true, 4, DependencyConstraints.NONE);
		this.b.addEntity(true);
		this.b.addEntity(false);
		this.b.addEntity(true);
		this.b.addEntity(false);
		this.i.addEntityAsNormalisedInt(2);
		this.i.addEntityAsNormalisedInt(0);
		this.i.addEntityAsNormalisedInt(1);
		this.i.addEntityAsNormalisedInt(0);
		this.ilat.addEntity(2);
		this.ilat.addEntity(1);
		this.ilat.addEntity(0);
		this.ilat.addEntity(2);
		this.blat.addEntityAsNormalisedInt(0);
		this.blat.addEntityAsNormalisedInt(0);
		this.blat.addEntityAsNormalisedInt(1);
		this.blat.addEntityAsNormalisedInt(1);
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
		Dependencies deps = new Dependencies(this.blat);
		deps.put(new Dependency(this.blat, null, this.i));
		deps.put(new Dependency(this.blat, null, this.ilat));
		deps.put(new Dependency(this.blat, null, this.b));
		DirichletCounts dc = new DirichletCounts(deps, 1.5);
		assertEquals(0.0825, dc.getCount(new int[]{1,2,2,0}), 1e-6);
		assertEquals(0.335, dc.getCount(new int[]{1,2,1,1}), 1e-6);
		assertEquals(0.50, dc.getSummedCount(new int[]{1,2,1}), 1e-6);
		assertEquals(0.0, dc.getCount(new int[]{0,0,0,0}), 1e-6);
	}
	
	
	@Test
	public void TestChildOnly() {
		Dependencies deps = new Dependencies(this.i);
		DirichletCounts dc = new DirichletCounts(deps, 1.5);
		assertEquals(2, dc.getCount(new int[]{0}), 1e-6);
		assertEquals(1, dc.getCount(new int[]{1}), 1e-6);
		assertEquals(1, dc.getCount(new int[]{2}), 1e-6);
	}
}
