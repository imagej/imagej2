//
// RoiHelpers.java
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

package ij.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ij.Assert;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class RoiHelpers {
	
	public static boolean find(int val, int[] arr) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == val)
				return true;
		return false;
	}
	
	public static void validateResult(ImageProcessor proc, int refVal, int[] expectedNonZeroes)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		for (int i = 0; i < h*w; i++)
			if (find(i,expectedNonZeroes))
				assertEquals(refVal,proc.get(i));
			else
				assertEquals(0,proc.get(i));
	}
	
	public static void validateNonzeroResult(ImageProcessor proc, int[] expectedNonZeroes)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		for (int i = 0; i < h*w; i++)
			if (find(i,expectedNonZeroes))
				assertTrue(proc.get(i) != 0);
			else
				assertEquals(0,proc.get(i));
	}
	
	public static void printValues(ImageProcessor proc)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		System.out.println(""+w+"x"+h+" pixels ----------------");
		for (int i = 0; i < w*h; i++)
			if (proc.get(i) != 0)
				System.out.println("("+i+") == "+proc.get(i));
	}
	
	public static void printNonzeroIndices(ImageProcessor proc)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		for (int i = 0; i < w*h; i++)
			if (proc.get(i) != 0)
				System.out.print(""+i+",");
		System.out.println();
	}
	
	// helper
	public static int extent(int[] vals)
	{
		int min = 0;
		int max = 0;
		
		for (int i = 0; i < vals.length; i++)
		{
			if (i == 0)
			{
				max = vals[0];
				min = vals[0];
			}
			else
			{
				if (vals[i] < min) min = vals[i];
				if (vals[i] > max) max = vals[i];
			}
		}
		
		return max - min;
	}

	// helper
	public static void printIntArr(String label, int[] vals)
	{
		System.out.println(label);
		for (int i = 0; i < vals.length; i++)
		{
			System.out.print(vals[i]);
			if (i != (vals.length-1))
				System.out.print(",");
		}
		System.out.println();
	}
	
	public static boolean doubleArraysEqual(Double[] a, Double[] b, double tol) {
		
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i < a.length; i++)
			if (Math.abs(a[i]-b[i]) > tol)
				return false;
		
		return true;
	}

	private static ArrayList<Double> getCoords(Polygon p) {
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (PathIterator iter = p.getPathIterator(null); !iter.isDone();)
		{
			double[] coords = new double[2];
			iter.currentSegment(coords);
			vals.add(coords[0]);
			vals.add(coords[1]);
			iter.next();
		}
		return vals;
	}

	public static boolean polysEqual(Polygon a, Polygon b){
		Double[] da = new Double[]{}, db = new Double[]{};
		ArrayList<Double> ptsA = getCoords(a);
		ArrayList<Double> ptsB = getCoords(b);
		return doubleArraysEqual(ptsA.toArray(da), ptsB.toArray(db), Assert.DOUBLE_TOL);
	}
	
	public static boolean rectsEqual(Rectangle a, Rectangle b) {
		if (a.x != b.x) return false;
		if (a.y != b.y) return false;
		if (a.width != b.width) return false;
		if (a.height != b.height) return false;
		return true;
	}

	// helper
	public static ImagePlus getCalibratedImagePlus()
	{
		ImagePlus ip = new ImagePlus("Zakky",new ShortProcessor(5,5,new short[5*5],null));
		Calibration cal = new Calibration();
		cal.pixelWidth = 14.1;
		cal.pixelHeight = 8.7;
		ip.setCalibration(cal);
		return ip;
	}
	
}
