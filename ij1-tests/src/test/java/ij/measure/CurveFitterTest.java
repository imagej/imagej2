//
// CurveFitterTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.Assert;
import ij.IJInfo;

import org.junit.Test;

// note - fully testing this would be prohibitive (would need an independent curve fitter)
//  will create tests from current behavior and test against that for now and the future.
//  if in the future we change curve fitting code these tests may break.
//  choosing an "isEquals" tolerance may be key. I'll make a local tolerance that can be
//  adjusted in case future implementation needs to relax it.

/**
 * Unit tests for {@link CurveFitter}.
 *
 * @author Barry DeZonia
 */
public class CurveFitterTest {

	private static final double Tolerance = 0.001;
	CurveFitter cf;
	double[] xs,ys;

	// helper method : add noise to smooth data
/*
	private static double[] makeNoisy(double[] d, int percent)
	{
		if ((percent < 0) || (percent > 100))
			throw new IllegalArgumentException("Percent must be between 0 and 100");
		
		double[] output = new double[d.length];
		double delta = percent / 100.0;

		for (int i = 0; i < d.length; i++)
		{
			double tmp = 0.0;
			switch (i % 3)
			{
				case 0:
					tmp = d[i] * (1+delta);
					break;
				case 1:
					tmp = d[i] * (1-delta);
					break;
				case 2:
					tmp = d[i];
					break;
			}
			output[i] = tmp;
		}
		
		return output;
	}
*/

	// helper method : get a y value from input x data. taken from imagej source code in CurveFitter

	private static double getYValue(int func, double x, double[] coeffs)
	{
		double y;
		double[] p = coeffs;
        switch (func) {
	        case CurveFitter.STRAIGHT_LINE:
	            return p[0] + p[1]*x;
	        case CurveFitter.POLY2:
	        	return p[0] + p[1]*x + p[2]* x*x;
	        case CurveFitter.POLY3:
	        	return p[0] + p[1]*x + p[2]*x*x + p[3]*x*x*x;
	        case CurveFitter.POLY4:
	        	return p[0] + p[1]*x + p[2]*x*x + p[3]*x*x*x + p[4]*x*x*x*x;
	        case CurveFitter.EXPONENTIAL:
	        	return p[0]*Math.exp(p[1]*x);
	        case CurveFitter.EXP_WITH_OFFSET:
	        	return p[0]*Math.exp(p[1]*x*-1)+p[2];
	        case CurveFitter.EXP_RECOVERY:
	        	return p[0]*(1-Math.exp(-p[1]*x))+p[2];
	        case CurveFitter.GAUSSIAN:
	        	return p[0]+(p[1]-p[0])*Math.exp(-(x-p[2])*(x-p[2])/(2.0*p[3]*p[3]));
	        case CurveFitter.POWER:
	            if (x == 0.0)
	            	return 0.0;
	            else
	            	return p[0]*Math.exp(p[1]*Math.log(x)); //y=ax^b
	        case CurveFitter.LOG:
	            if (x == 0.0)
	                x = 0.5;
	            return p[0]*Math.log(p[1]*x);
	        case CurveFitter.RODBARD:
				double ex;
				if (x == 0.0)
					ex = 0.0;
				else
					ex = Math.exp(Math.log(x/p[2])*p[1]);
				y = p[0]-p[3];
				y = y /(1.0+ex);
				return y+p[3];
	        case CurveFitter.GAMMA_VARIATE:
	            if (p[0] >= x) return 0.0;
	            if (p[1] <= 0) return -100000.0;
	            if (p[2] <= 0) return -100000.0;
	            if (p[3] <= 0) return -100000.0;
	            
	            double pw = Math.pow((x - p[0]), p[2]);
	            double e = Math.exp((-(x - p[0]))/p[3]);
	            return p[1]*pw*e;
	        case CurveFitter.LOG2:
	        	double tmp = x-p[2];
	        	if (tmp<0.001) tmp = 0.001;
	        	return p[0]+p[1]*Math.log(tmp);
	        case CurveFitter.RODBARD2:
				if (x<=p[0])
					y = 0.0;
				else {
					y = (p[0]-x)/(x-p[3]);
					y = Math.exp(Math.log(y)*(1.0/p[1]));  //y=y**(1/b)
					y = y*p[2];
				}
				return y;
	        default:
	        	return 0.0;
        }
	}
	
	// helper method : get smooth data
	
	private static double[] getYValues(int func, double[] xValues, double[] coeffs)
	{
		double[] yValues = new double[xValues.length];
		
		for (int i = 0; i < xValues.length; i++)
			yValues[i] = getYValue(func, xValues[i], coeffs);
		
		return yValues;
	}
	
	/*
	// helper method : get noisy data
	
	private static double[] getNoisyYValues(int func, double[] xValues, double[] coeffs, int percent)
	{
		return makeNoisy(getYValues(func,xValues,coeffs),percent);
	}
	
	 */
	
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
		// note - no need to test. just calls doFit() with false as param. I test doFit() next.
	}
	
	// helper method
	
	private void tryFit(int func, double[] xs, double[] ys, double[] cs, double error)
	{
		cf = new CurveFitter(xs,ys);
		cf.doFit(func,false);
		
		double[] actParams = cf.getParams();
		
		double[] expParams = new double[actParams.length];
		System.arraycopy(cs, 0, expParams, 0, actParams.length-1);
		expParams[actParams.length-1] = error;
		
		/*
		System.out.println("================================================================");

		System.out.print("xs = ");
		for (int i = 0; i < xs.length; i++) System.out.print(xs[i]+" ");
		System.out.println();
		
		System.out.print("ys = ");
		for (int i = 0; i < ys.length; i++) System.out.print(ys[i]+" ");
		System.out.println();

		System.out.println("exp =");
		for (int i = 0; i < expParams.length; i++)
			System.out.println(expParams[i]);
		
		System.out.println("act = ");
		for (int i = 0; i < actParams.length; i++)
			System.out.println(actParams[i]);
		*/
		
		Assert.assertDoubleArraysEqual(expParams, actParams, Tolerance);
	}

	// helper method
	
	private void tryFunc(int func, double[] xVals, double[] coeffs)
	{
		double[] yVals;
		
		// old idea: noisy data and get good fit. Didn't work out in practice and not strictly regression testing.
		//if (percent == 0)
		//	yVals = getYValues(func, xVals, coeffs);
		//else
		//	yVals = getNoisyYValues(func, xVals, coeffs, percent);
		
		yVals = getYValues(func, xVals, coeffs);
		
		tryFit(func,xVals,yVals,coeffs,0);
	}
	
	@Test
	public void testDoFitIntBoolean() {
		
		// note - doFit(func,true) case runs gui stuff - can't test
		//        doFit(func,false) cases all follow

		// perfect fit tests
		
		double[] xs1 = new double[] {1,2,3,4,5};
		double[] xs2 = new double[] {4,5,6,7,8};
		double[] xs3 = new double[] {2,8,15,22,31};
		double[] xs4 = new double[] {1,2,3,4,5,6,7};
		double[] coeffs1 = new double[] {2,3,4,5,6};
		double[] coeffs2 = new double[] {7,2,5,9,3};
		double[] coeffs3 = new double[] {8,3,-4,15,0.5};  // for sets where c[2] must be < 0
		double[] coeffs4 = new double[] {2,3,-4,5,6};     // for sets where c[2] must be < 0
		double[] coeffs5 = new double[] {2,3,4,5,6};
		double[] coeffs6 = new double[] {4,2,3,7,4};
		
		
		tryFunc(CurveFitter.POLY4, xs1, coeffs1);
		tryFunc(CurveFitter.POLY4, xs2, coeffs2);
		tryFunc(CurveFitter.POLY4, xs3, coeffs3);
		tryFunc(CurveFitter.LOG2, xs2, coeffs3);
		tryFunc(CurveFitter.LOG2, xs4, coeffs4);
		tryFunc(CurveFitter.STRAIGHT_LINE,xs2,coeffs2);
		tryFunc(CurveFitter.STRAIGHT_LINE,xs4,coeffs5);
		tryFunc(CurveFitter.POLY2, xs2, coeffs2);
		tryFunc(CurveFitter.POLY2, xs4, coeffs5);
		tryFunc(CurveFitter.POLY3, xs2, coeffs2);
		tryFunc(CurveFitter.POLY3, xs4, coeffs5);
		tryFunc(CurveFitter.EXPONENTIAL, xs2, coeffs2);
		tryFunc(CurveFitter.EXPONENTIAL, xs4, coeffs5);
		tryFunc(CurveFitter.POWER, xs2, coeffs2);
		tryFunc(CurveFitter.POWER, xs4, coeffs5);
		tryFunc(CurveFitter.RODBARD, xs4, coeffs5);
		tryFunc(CurveFitter.RODBARD, xs2, coeffs2);
		tryFunc(CurveFitter.GAMMA_VARIATE, xs4, coeffs5);
		tryFunc(CurveFitter.GAMMA_VARIATE, xs2, coeffs6);
		tryFunc(CurveFitter.EXP_WITH_OFFSET, xs2, coeffs2);
		tryFunc(CurveFitter.EXP_WITH_OFFSET, xs4, coeffs5);
		tryFunc(CurveFitter.GAUSSIAN, xs2, coeffs2);
		tryFunc(CurveFitter.GAUSSIAN, xs4, coeffs5);
		tryFunc(CurveFitter.EXP_RECOVERY, xs2, coeffs2);
		tryFunc(CurveFitter.EXP_RECOVERY, xs4, coeffs5);
		tryFunc(CurveFitter.LOG, xs2, coeffs2);
		tryFunc(CurveFitter.LOG, xs4, coeffs5);
		// TODO - test the following functions when time allows.
		// Broken as of 4-1-10
		//   some of the problems may be due to user error (RODBARD2 sometimes returns NaNs here)
		//tryFunc(CurveFitter.RODBARD2, xs2, coeffs2);
		//tryFunc(CurveFitter.RODBARD2, xs4, coeffs5);
		//tryFunc(CurveFitter.GAMMA_VARIATE, xs2, coeffs2);

		// approximate fit tests : just a regression test versus actual code as of 4-1-10.
		
		double[] xs = {1,2,3};
		tryFit(CurveFitter.STRAIGHT_LINE, xs, new double[] {4,6.5,8.2}, new double[] {2.03333,2.10000},0.10667);
		tryFit(CurveFitter.POLY2, xs, new double[] {10.6,24.2,33}, new double[] {-7.8,20.8,-2.4},0);
		tryFit(CurveFitter.POLY3, xs, new double[] {6,63,104.2}, new double[] {-60.16957,68.54422,-1.26957,-1.10507},0);
		tryFit(CurveFitter.POLY4, xs, new double[] {106.3,97.2,55.5}, new double[] {104.18837,3.55676,-0.35439,-0.59594,-0.49480},0);
		tryFit(CurveFitter.EXPONENTIAL, xs, new double[] {12,44,115}, new double[] {5.46507,1.01669},14.86054);
		tryFit(CurveFitter.POWER, xs, new double[] {3,15,99}, new double[] {0.62482,4.61051},5.71156);
		tryFit(CurveFitter.LOG, xs, new double[] {7,11,14}, new double[] {6.30697,2.98619},0.11307);
		tryFit(CurveFitter.RODBARD, xs, new double[] {10,15,11}, new double[] {-1.12844,32.03943,0.95991,13},8);
		tryFit(CurveFitter.GAMMA_VARIATE, xs, new double[] {1,17,14}, new double[] {1.11196,46.72393,1.06347,1.00370},1);
		tryFit(CurveFitter.LOG2, xs, new double[] {1,3,5}, new double[] {-729.49201,165.80871,-80.90891},0);
		tryFit(CurveFitter.RODBARD2, xs, new double[] {808,244,612}, new double[] {2.5,71.53400,830.68842,-9.87467},0.5);
		tryFit(CurveFitter.EXP_WITH_OFFSET, xs, new double[] {44,88,257}, new double[] {182.24613,14.64237,129.66677},25288.68033);
		tryFit(CurveFitter.GAUSSIAN, xs, new double[] {1,58,14},  new double[] {0.03542,68.17796,2.24211,-0.42566},0);
		tryFit(CurveFitter.EXP_RECOVERY, xs, new double[] {44,22,12},  new double[] {-88.73333,0.78845,92.4},0);
	}

	@Test
	public void testDoCustomFit() {
		
		xs = new double[] {1,2,3};
		ys = new double[] {3,251,1004};
		cf = new CurveFitter(xs,ys);
		
		// equation has no x's
		assertEquals(0,cf.doCustomFit("y=a*h", new double[]{5,8,11}, false));
		
		// equation has no y's
		assertEquals(0,cf.doCustomFit("z=a*x", new double[]{5,8,11}, false));
		
		// equation has none of a,b,c,d,e
		assertEquals(0,cf.doCustomFit("y=m*x", new double[]{5,8,11}, false));
		
		if (IJInfo.RUN_GUI_TESTS)
		{
			// TODO - this test requires GUI interaction
			
			// equation has bad syntax
			assertEquals(0,cf.doCustomFit("y=a*x+BadSyntax", new double[]{5,8,11}, false));
		}
		
		// otherwise success - test a couple custom fits and the underlying results
		
		assertEquals(1,cf.doCustomFit("y=a*x", new double[]{5,8,11}, false));
		assertEquals(251.21464,cf.getParams()[0],Tolerance);

		assertEquals(2,cf.doCustomFit("y=a*x*x+b", new double[]{5,8,11}, false));
		assertEquals(127.72446,cf.getParams()[0],Tolerance);
		assertEquals(-176.71432,cf.getParams()[1],Tolerance);

		assertEquals(4,cf.doCustomFit("y=a*x*x*x+b*x*x+c*x+d", new double[]{5,8,11}, false));
		assertEquals(42.57646,cf.getParams()[0],Tolerance);
		assertEquals(-2.95875,cf.getParams()[1],Tolerance);
		assertEquals(-41.15896,cf.getParams()[2],Tolerance);
		assertEquals(4.54125,cf.getParams()[3],Tolerance);
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

	private boolean fEqBaseline(int func, double pt, double[] coeffs)
	{
		double me,ij;
		
		ij = CurveFitter.f(func,coeffs, pt);
		me = getYValue(func,pt,coeffs);

		/*
		System.out.println("================================================================");

		System.out.print("x = ");
		System.out.print(pt);
		System.out.println();
		
		System.out.print("coeffs = ");
		for (int i = 0; i < coeffs.length; i++) System.out.print(coeffs[i]+" ");
		System.out.println();

		System.out.print("ij = ");
		System.out.print(ij);
		System.out.println();

		System.out.print("me = ");
		System.out.print(me);
		System.out.println();
		*/
		
		if (Double.isNaN(me) && Double.isNaN(ij))
			return true;
		
		if (Double.isInfinite(me) && Double.isInfinite(ij))
			return true;
		
		return Math.abs(me - ij) < Assert.DOUBLE_TOL;
	}
	
	@Test
	public void testFDoubleArrayDouble() {
		// no need to test general case as next test routine does so
		// but must test custom cases

		double[] coeffs;
		double x,y;
		
		xs = new double[] {1,2,3,4,5,6,7};  // these vals irrelevant - just need something for constructor
		ys = new double[] {4,2,6,4,5,8,9};  // these vals irrelevant - just need something for constructor
		cf = new CurveFitter(xs,ys);

		coeffs = new double[] {2};
		x = 5;
		y = 2*x*x;
		cf.doCustomFit("y=a*x*x",coeffs,false);
		assertEquals(y,cf.f(coeffs, x),Assert.DOUBLE_TOL);

		coeffs = new double[] {2,3};
		x = 5;
		y = 2*x+3;
		cf.doCustomFit("y=a*x+b",coeffs,false);
		assertEquals(y,cf.f(coeffs, x),Assert.DOUBLE_TOL);

		coeffs = new double[] {2,3,4};
		x = 2;
		y = 2*x*x*x*x+3*x*x+4;
		cf.doCustomFit("y=a*x*x*x*x+b*x*x+c",coeffs,false);
		assertEquals(y,cf.f(coeffs, x),Assert.DOUBLE_TOL);
	}

	@Test
	public void testFIntDoubleArrayDouble() {

		double[][] coeffs = new double[][] {{3,5,7,9,11},{1,7,4,19,15},{-13,12,55,74,99}};
		double[] pts = new double[] {14.2,6.8,-4,88};
		
		for (int i = 0; i < pts.length; i++)
		{
			for (int j = 0; j < coeffs.length; j++)
			{
				assertTrue(fEqBaseline(CurveFitter.STRAIGHT_LINE,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.POLY2,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.POLY3,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.POLY4,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.LOG,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.EXPONENTIAL,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.POWER,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.RODBARD,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.RODBARD2,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.GAMMA_VARIATE,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.LOG2,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.EXP_WITH_OFFSET,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.GAUSSIAN,pts[i],coeffs[j]));
				assertTrue(fEqBaseline(CurveFitter.EXP_RECOVERY,pts[i],coeffs[j]));
			}
		}
	}

	@Test
	public void testGetParams() {
		// note - can't really test this - it just modifies internal state of private vars
		// put in compile time checks
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.POLY4);
		assertNotNull(cf.getParams());
	}

	@Test
	public void testGetResiduals() {
		double[] resids;
		
		// setup initial data
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {14,96,44,82,51,77,57};
		cf = new CurveFitter(xs,ys);

		// try one method
		cf.doFit(CurveFitter.EXP_RECOVERY);
		resids = cf.getResiduals();
		assertNotNull(resids);
		assertEquals(0.0,resids[0],Tolerance);
		assertEquals(28.16667,resids[1],Tolerance);
		assertEquals(-23.83337,resids[2],Tolerance);
		assertEquals(14.16663,resids[3],Tolerance);
		assertEquals(-16.83337,resids[4],Tolerance);
		assertEquals(9.16663,resids[5],Tolerance);
		assertEquals(-10.83337,resids[6],Tolerance);

		// try another method
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		resids = cf.getResiduals();
		assertNotNull(resids);
		assertEquals(-46.14286,resids[0],Tolerance);
		assertEquals(35.85714,resids[1],Tolerance);
		assertEquals(-16.14286,resids[2],Tolerance);
		assertEquals(21.85714,resids[3],Tolerance);
		assertEquals(-9.14286,resids[4],Tolerance);
		assertEquals(16.85714,resids[5],Tolerance);
		assertEquals(-3.14286,resids[6],Tolerance);
	}

	@Test
	public void testGetSumResidualsSqr() {
		// setup initial data
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {14,96,44,82,51,77,57};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.POLY4);
		assertEquals(1794.37229,cf.getSumResidualsSqr(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(1890.14122,cf.getSumResidualsSqr(),Tolerance);
	}

	@Test
	public void testGetSD() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {96,108,77,55,51};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.LOG2);
		assertEquals(10.45104,cf.getSD(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(10.44390,cf.getSD(),Tolerance);
	}

	@Test
	public void testGetRSquared() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,33,24,48,62};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.LOG);
		assertEquals(0.83968,cf.getRSquared(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(0.84155,cf.getRSquared(),Tolerance);
	}

	@Test
	public void testGetFitGoodness() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,99,18,24,3};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(0.77884,cf.getFitGoodness(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.POLY3);
		assertEquals(-0.59996,cf.getFitGoodness(),Tolerance);  // negative fit measure is not a bug
	}

	// helper method : remove the Time substring from the expected results - otherwise assertions fail randomly
	
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
		
		String exp,actual;

		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);

		// test one method : choose arbitrarily
		cf.doFit(CurveFitter.RODBARD);
		exp = removeTime(
				"\nFormula: y = d+(a-d)/(1+(x/c)^b)\nTime: 9ms\nNumber of iterations: 8000 (8000)\nNumber of restarts: 0 (2)" + 
				"\nSum of residuals squared: 0.0000\nStandard deviation: 0.0000\nR^2: 1.0000\nParameters:\n  a = 2.0002" + 
				"\n  b = 1.0001\n  c = 63234.9017\n  d = 126581.3747"
			);
		actual = removeTime(cf.getResultString());
		assertEquals(exp,actual);

		// test another method : choose arbitrarily
		cf.doFit(CurveFitter.GAUSSIAN);
		exp = removeTime(
				"\nFormula: y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))\nTime: 9ms\nNumber of iterations: 8000 (8000)\nNumber of restarts: 0 (2)" + 
				"\nSum of residuals squared: 0.0000\nStandard deviation: 0.0000\nR^2: 1.0000\nParameters:\n  a = -2717823.7529" + 
				"\n  b = 1746275.5590\n  c = 1348680.9656\n  d = 1353787.2431"
			);
		actual = removeTime(cf.getResultString());
		assertEquals(exp,actual);
	}

	@Test
	public void testGetIterations() {
		// simply a getter - nothing to test - put in place code for compile time check
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		cf.getIterations();
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
		/*
		//TODO - figure out a way to test this. setInitialParameters() simply tweaks internals so that maybe
		//   the speed of the fit is improved. But I can't seem to affect fit speed with any inputs. The below code seems
		//   like a logical test but does not work.
		   
		int iters1, iters2;
		double[] results1,results2;
		
		xs = new double[] {1,2,3,4,5,6,7,8};
		ys = new double[] {10,20,30,40,50,60,70,80};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		results1 = cf.getParams().clone();
		iters1 = cf.getIterations();
		cf.setInitialParameters(new double[] {14,-3});
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		results2 = cf.getParams().clone();
		iters2 = cf.getIterations();
		System.out.println(iters1 + " " + iters2);
		Assert.assertDoubleArraysEqual(results1,results2,Tolerance);
		assertTrue(iters1 != iters2);
		 */
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

		// test the basic ones
		
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
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals(CurveFitter.RODBARD2,cf.getFit());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals(CurveFitter.EXP_WITH_OFFSET,cf.getFit());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(CurveFitter.GAUSSIAN,cf.getFit());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals(CurveFitter.EXP_RECOVERY,cf.getFit());
	}

	@Test
	public void testGetName() {
		
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x*x",new double[] {6,5,4,3},false);
		assertEquals("User-defined",cf.getName());

		// test the basic ones
		
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
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals("Rodbard (NIH Image)",cf.getName());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals("Exponential with Offset",cf.getName());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals("Gaussian",cf.getName());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals("Exponential Recovery",cf.getName());
	}

	@Test
	public void testGetFormula() {
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x",new double[] {1,2,3,4},false);
		assertEquals("y=a*x*x*x*x*x*x",cf.getFormula());

		// test the basic ones

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
