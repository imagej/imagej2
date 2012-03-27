/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.plugins.restructure;

import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.ImgPlus;


/** Class responsible for simple remapping of color tables from one indexing
 * scheme to another.
 * 
 * @author Barry DeZonia
 *
 */
public class ColorTableRemapper {

	// -- exported interface --
	
	public interface RemapAlgorithm {
		boolean isValidSourcePlane(long i);
		void remapPlanePosition(long[] origPlaneDims, long[] origPlanePos, long[] newPlanePos);
	}

	// -- instance variables --
	
	private RemapAlgorithm remapper;
	
	// -- constructor --
	
	public ColorTableRemapper(RemapAlgorithm remapper) {
		this.remapper = remapper;
	}
	
	// -- public interface --
	
	public void remapColorTables(ImgPlus<?> srcImgPlus, ImgPlus<?> dstImgPlus)
	{
		long[] origDims = new long[srcImgPlus.numDimensions()];
		long[] newDims = new long[dstImgPlus.numDimensions()];
		long[] origPlaneDims = new long[origDims.length-2];
		long[] newPlaneDims = new long[newDims.length-2];
		long[] origPlanePos = new long[origPlaneDims.length];
		long[] newPlanePos = new long[newPlaneDims.length];
		srcImgPlus.dimensions(origDims);
		dstImgPlus.dimensions(newDims);
		for (int i = 0; i < origPlaneDims.length; i++)
			origPlaneDims[i] = origDims[i+2];
		for (int i = 0; i < newPlaneDims.length; i++)
			newPlaneDims[i] = newDims[i+2];
		for (int tNum = 0; tNum < srcImgPlus.getColorTableCount(); tNum++) {
			if (!remapper.isValidSourcePlane(tNum)) continue;
			toND(origPlaneDims, tNum, origPlanePos);
			remapper.remapPlanePosition(origPlaneDims, origPlanePos, newPlanePos);
			long newLongIndex = to1D(newPlaneDims, newPlanePos);
			int newIndex = intIndex(newLongIndex);
			ColorTable8 c8 = srcImgPlus.getColorTable8(tNum);
			ColorTable16 c16 = srcImgPlus.getColorTable16(tNum);
			dstImgPlus.setColorTable(c8, newIndex);
			dstImgPlus.setColorTable(c16, newIndex);
		}
	}

	public static void toND(long[] dims, long index, long[] pos) {
		long divisor = 1;
		for (int i = 0; i < dims.length; i++)
			divisor *= dims[i];
		long val = index;
		for (int i = pos.length-1; i >= 0; i--) {
			divisor /= dims[i];
			pos[i] = val / divisor;
			val = val % divisor;
		}
	}
	
	public static long to1D(long[] dims, long[] pos) {
		long val = 0;
		long multiplier = 1;
		for (int i = 0; i < dims.length; i++) {
			val += multiplier * pos[i];
			multiplier *= dims[i];
		}
		return val;
	}

	// -- private helpers --
	
	private static int intIndex(long val) {
		if (val < 0)
			throw new IllegalArgumentException(
					"color table remap problem: bad index calculation");
		if (val > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"color table remap problem: too many planes in output dataset");
		return (int) val;
	}
	
}
