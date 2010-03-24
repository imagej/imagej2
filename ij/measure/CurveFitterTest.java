package ij.measure;

import static org.junit.Assert.*;
import ij.IJInfo;
import ij.io.Assert;

import org.junit.Test;

// note - fully testing this would be prohibitive (would need an independent curve fitter)
//  will create tests from current behavior and test against that for now and the future.
//  if in the future we change curve fitting code these tests may break.
//  choosing an "isEquals" tolerance may be key. I'll make a local tolerance that can be
//  adjusted in case future implementation needs to relax it.

public class CurveFitterTest {

	private static final double Tolerance = 0.0001;
	CurveFitter cf;
	double[] xs,ys;

	@Test
	public void testConstants() {

		assertEquals(0,CurveFitter.STRAIGHT_LINE);
		assertEquals(1,CurveFitter.POLY2);
		assertEquals(2,CurveFitter.POLY3);
		assertEquals(3,CurveFitter.POLY4);
		assertEquals(4,CurveFitter.EXPONENTIAL);
		assertEquals(5,CurveFitter.POWER);
		assertEquals(6,CurveFitter.LOG);
		assertEquals(7,CurveFitter.RODBARD);
		assertEquals(8,CurveFitter.GAMMA_VARIATE);
		assertEquals(9,CurveFitter.LOG2);
		assertEquals(10,CurveFitter.RODBARD2);
		assertEquals(11,CurveFitter.EXP_WITH_OFFSET);
		assertEquals(12,CurveFitter.GAUSSIAN);
		assertEquals(13,CurveFitter.EXP_RECOVERY);
		
		assertEquals(500,CurveFitter.IterFactor);
		
		assertEquals(CurveFitter.fitList[0],"Straight Line");
		assertEquals(CurveFitter.fitList[1],"2nd Degree Polynomial");
		assertEquals(CurveFitter.fitList[2],"3rd Degree Polynomial");
		assertEquals(CurveFitter.fitList[3],"4th Degree Polynomial");
		assertEquals(CurveFitter.fitList[4],"Exponential");
		assertEquals(CurveFitter.fitList[5],"Power");
		assertEquals(CurveFitter.fitList[6],"Log");
		assertEquals(CurveFitter.fitList[7],"Rodbard");
		assertEquals(CurveFitter.fitList[8],"Gamma Variate");
		assertEquals(CurveFitter.fitList[9],"y = a+b*ln(x-c)");
		assertEquals(CurveFitter.fitList[10],"Rodbard (NIH Image)");
		assertEquals(CurveFitter.fitList[11],"Exponential with Offset");
		assertEquals(CurveFitter.fitList[12],"Gaussian");
		assertEquals(CurveFitter.fitList[13],"Exponential Recovery");

		assertEquals(CurveFitter.fList[0],"y = a+bx");
		assertEquals(CurveFitter.fList[1],"y = a+bx+cx^2");
		assertEquals(CurveFitter.fList[2],"y = a+bx+cx^2+dx^3");
		assertEquals(CurveFitter.fList[3],"y = a+bx+cx^2+dx^3+ex^4");
		assertEquals(CurveFitter.fList[4],"y = a*exp(bx)");
		assertEquals(CurveFitter.fList[5],"y = ax^b");
		assertEquals(CurveFitter.fList[6],"y = a*ln(bx)");
		assertEquals(CurveFitter.fList[7],"y = d+(a-d)/(1+(x/c)^b)");
		assertEquals(CurveFitter.fList[8],"y = a*(x-b)^c*exp(-(x-b)/d)");
		assertEquals(CurveFitter.fList[9],"y = a+b*ln(x-c)");
		assertEquals(CurveFitter.fList[10],"y = d+(a-d)/(1+(x/c)^b)");
		assertEquals(CurveFitter.fList[11],"y = a*exp(-bx) + c");
		assertEquals(CurveFitter.fList[12],"y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))");
		assertEquals(CurveFitter.fList[13],"y=a*(1-exp(-b*x)) + c"); 
	}

	@Test
	public void testCurveFitter() {
	
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// null case - crashes with a NullPtrExcep
			cf = new CurveFitter(null,null);
			assertNotNull(cf);

			// note it is possible to construct with xs and ys being different lengths!
		}

		// x len && y len both 0
		xs = new double[] {};
		ys = new double[] {};
		cf = new CurveFitter(xs,ys);
		assertNotNull(cf);
		assertEquals(xs,cf.getXPoints());
		assertEquals(ys,cf.getYPoints());

		// valid case x len == y len
		xs = new double[] {3};
		ys = new double[] {7};
		cf = new CurveFitter(xs,ys);
		assertNotNull(cf);
		assertEquals(xs,cf.getXPoints());
		assertEquals(ys,cf.getYPoints());
		
	}

	@Test
	public void testDoFitInt() {
		// TODO - run same tests as testDoFit(int,false)
	}

	@Test
	public void testDoFitIntBoolean() {
		// note - true case runs gui stuff - can't test
		// false cases
		xs = new double[] {1,2,3};
		ys = new double[] {4,6,8};
		cf = new CurveFitter(xs,ys);
		/* TODO
		cf.doFit(CurveFitter.STRAIGHT_LINE,false);
		assertEquals(2,cf.getNumParams());
		Assert.assertDoubleArraysEqual(new double[]{0,0,0},cf.getResiduals());
		*/
	}

	@Test
	public void testDoCustomFit() {
		//fail("Not yet implemented");
	}

    @Test
	public void testGetNumParams() {
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.POLY2);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.POLY3);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.POLY4);
		assertEquals(5,cf.getNumParams());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.POWER);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.LOG);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.LOG2);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals(3,cf.getNumParams());
		cf.doCustomFit("y=a*x*x*x+b*x*x+c*x+d", new double[]{1,2,3,4}, false);
		assertEquals(4,cf.getNumParams());
	}

	@Test
	public void testFDoubleArrayDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testFIntDoubleArrayDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetParams() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetResiduals() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetSumResidualsSqr() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetSD() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRSquared() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetFitGoodness() {
		int tmp;
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.RODBARD);
		assertEquals(1.0,cf.getFitGoodness(),Tolerance);  // TODO - paste in actual results
	}

	private String removeTime(String str)
	{
		String tmp = "";

		for (String s : str.split("\n"))
			if (!s.startsWith("Time"))
				tmp += s + "\n";
		
		return tmp;
	}
	
	@Test
	public void testGetResultString() {
		int tmp;
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.RODBARD);
		String exp,actual;
		exp = removeTime(
				"\nFormula: y = d+(a-d)/(1+(x/c)^b)\nTime: 9ms\nNumber of iterations: 8000 (8000)\nNumber of restarts: 0 (2)" + 
				"\nSum of residuals squared: 0.0000\nStandard deviation: 0.0000\nR^2: 1.0000\nParameters:\n  a = 2.0002" + 
				"\n  b = 1.0001\n  c = 63234.9017\n  d = 126581.3747"
			);
		actual = removeTime(cf.getResultString());
		assertEquals(exp,actual);
	}

	@Test
	public void testGetIterations() {
		// simply a getter - nothing to test - put in place code for compile time check
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getIterations();
	}

	@Test
	public void testSetAndGetMaxIterations() {
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getMaxIterations();
		cf.setMaxIterations(tmp+500);
		assertEquals(tmp+500,cf.getMaxIterations());
	}

	@Test
	public void testSetAndGetRestarts() {
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getRestarts();
		cf.setRestarts(tmp+500);
		assertEquals(tmp+500,cf.getRestarts());
	}

	@Test
	public void testSetInitialParameters() {
		// TODO
		//  init params only is a setter but it affects doFit so must try doFit twice (w/wo params) and compare results
	}

	@Test
	public void testGetMax() {
		double[] tmp;
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// null list
			tmp = null;
			assertEquals(0,CurveFitter.getMax(tmp));
			
			// empty list
			tmp = new double[]{};
			assertEquals(0,CurveFitter.getMax(tmp));
		}
		
		// one item
		tmp = new double[]{1};
		assertEquals(0,CurveFitter.getMax(tmp));
		
		// two items : max at end of list
		tmp = new double[]{0,1};
		assertEquals(1,CurveFitter.getMax(tmp));

		// two items : max at beginning of list
		tmp = new double[]{1,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// two maximums in list - getmax() returns the first one found when multiple positions are max
		tmp = new double[]{0,1,0,1};
		assertEquals(1,CurveFitter.getMax(tmp));

		// ascending list
		tmp = new double[]{0,1,2,3};
		assertEquals(3,CurveFitter.getMax(tmp));

		// descending list
		tmp = new double[]{3,2,1,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// negative values
		tmp = new double[]{-203,-108,-410,-155};
		assertEquals(1,CurveFitter.getMax(tmp));

		// MAX_VALUE
		tmp = new double[]{0,0,Double.MAX_VALUE,0,0};
		assertEquals(2,CurveFitter.getMax(tmp));

		// MIN_VALUE
		tmp = new double[]{0,0,-Double.MAX_VALUE,0,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// NaN
		tmp = new double[]{0,0,Double.NaN,0,0};
		assertEquals(0,CurveFitter.getMax(tmp));
	}

	@Test
	public void testGetXandYPoints() {
		xs = new double[] {1,2,3};
		ys = new double[] {4,6,8};
		cf = new CurveFitter(xs,ys);
		Assert.assertDoubleArraysEqual(xs, cf.getXPoints(),Tolerance);
		Assert.assertDoubleArraysEqual(ys, cf.getYPoints(),Tolerance);
	}

	@Test
	public void testGetFit() {
		
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x",new double[] {1,2,3,4},false);
		assertEquals(20,cf.getFit());

		// test a basic ones
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(CurveFitter.STRAIGHT_LINE,cf.getFit());
		cf.doFit(CurveFitter.POLY2);
		assertEquals(CurveFitter.POLY2,cf.getFit());
		cf.doFit(CurveFitter.POLY3);
		assertEquals(CurveFitter.POLY3,cf.getFit());
		cf.doFit(CurveFitter.POLY4);
		assertEquals(CurveFitter.POLY4,cf.getFit());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(CurveFitter.EXPONENTIAL,cf.getFit());
		cf.doFit(CurveFitter.POWER);
		assertEquals(CurveFitter.POWER,cf.getFit());
		cf.doFit(CurveFitter.LOG);
		assertEquals(CurveFitter.LOG,cf.getFit());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals(CurveFitter.RODBARD,cf.getFit());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(CurveFitter.GAMMA_VARIATE,cf.getFit());
		cf.doFit(CurveFitter.LOG2);
		assertEquals(CurveFitter.LOG2,cf.getFit());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals(CurveFitter.EXP_WITH_OFFSET,cf.getFit());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(CurveFitter.GAUSSIAN,cf.getFit());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals(CurveFitter.EXP_RECOVERY,cf.getFit());

		// note - important to know - doFit() changes RODBARD2 into RODBARD
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals(CurveFitter.RODBARD,cf.getFit());
	}

	@Test
	public void testGetName() {
		
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x*x",new double[] {6,5,4,3},false);
		assertEquals("User-defined",cf.getName());

		// test a basic ones
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals("Straight Line",cf.getName());
		cf.doFit(CurveFitter.POLY2);
		assertEquals("2nd Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.POLY3);
		assertEquals("3rd Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.POLY4);
		assertEquals("4th Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals("Exponential",cf.getName());
		cf.doFit(CurveFitter.POWER);
		assertEquals("Power",cf.getName());
		cf.doFit(CurveFitter.LOG);
		assertEquals("Log",cf.getName());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals("Rodbard",cf.getName());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals("Gamma Variate",cf.getName());
		cf.doFit(CurveFitter.LOG2);
		assertEquals("y = a+b*ln(x-c)",cf.getName());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals("Exponential with Offset",cf.getName());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals("Gaussian",cf.getName());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals("Exponential Recovery",cf.getName());

		// note - important to know - RODBARD2 gets changed to RODBARD via doFit()
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals("Rodbard",cf.getName());  // not "Rodbard (NIH Image)" as maybe expected
	}

	@Test
	public void testGetFormula() {
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x",new double[] {1,2,3,4},false);
		assertEquals("y=a*x*x*x*x*x*x",cf.getFormula());

		// test a basic ones

		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals("y = a+bx",cf.getFormula());
		cf.doFit(CurveFitter.POLY2);
		assertEquals("y = a+bx+cx^2",cf.getFormula());
		cf.doFit(CurveFitter.POLY3);
		assertEquals("y = a+bx+cx^2+dx^3",cf.getFormula());
		cf.doFit(CurveFitter.POLY4);
		assertEquals("y = a+bx+cx^2+dx^3+ex^4",cf.getFormula());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals("y = a*exp(bx)",cf.getFormula());
		cf.doFit(CurveFitter.POWER);
		assertEquals("y = ax^b",cf.getFormula());
		cf.doFit(CurveFitter.LOG);
		assertEquals("y = a*ln(bx)",cf.getFormula());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals("y = d+(a-d)/(1+(x/c)^b)",cf.getFormula());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals("y = a*(x-b)^c*exp(-(x-b)/d)",cf.getFormula());
		cf.doFit(CurveFitter.LOG2);
		assertEquals("y = a+b*ln(x-c)",cf.getFormula());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals("y = d+(a-d)/(1+(x/c)^b)",cf.getFormula());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals("y = a*exp(-bx) + c",cf.getFormula());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals("y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))",cf.getFormula());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals("y=a*(1-exp(-b*x)) + c",cf.getFormula());
	}

}
