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

package imagej.legacy.translate;


import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ij.ImagePlus;
import imagej.data.display.ImageDisplay;

/**
 * This class is responsible for harmonizing slider position values (and active
 * plane position) between IJ1 and IJ2.
 * 
 * @author Barry DeZonia
 *
 */
public class PositionHarmonizer implements DisplayHarmonizer {

	/**
	 * Updates the given {@link ImageDisplay}'s position to match that of its
	 * paired {@link ImagePlus}.
	 */
	@Override
	public void updateDisplay(ImageDisplay disp, ImagePlus imp) {
		final long[] dimensions = disp.getDims();
		final AxisType[] axes = disp.getAxes();
		final long[] workspace = new long[dimensions.length];
		fillIJ2Position(disp, imp, dimensions, axes, workspace);
		for (int i = 0; i < axes.length; i++) {
			final long pos = workspace[i];
			disp.setPosition(pos, i);
		}
	}

	/**
	 * Updates the given {@link ImagePlus}'s position to match that of its
	 * paired {@link ImageDisplay}.
	 */
	@Override
	public void updateLegacyImage(ImageDisplay disp, ImagePlus imp) {
		// When this is called we know that we have a IJ1 compatible display. So we
		// can make assumptions about dimensional sizes re: safe casting.
		final int cPos = (int) calcIJ1ChannelPos(disp);
		final int zPos = (int) disp.getLongPosition(Axes.Z);
		final int tPos = (int) disp.getLongPosition(Axes.TIME);
		imp.setPosition(cPos+1, zPos+1, tPos+1); 
	}

	// -- helpers --
	
	private long calcIJ1ChannelPos(ImageDisplay disp) {
		final long[] dims = disp.getDims();
		final AxisType[] axes = disp.getAxes();
		final long[] pos = new long[axes.length];
		for (int i = 0; i < axes.length; i++)
			pos[i] = disp.getLongPosition(i);
		return LegacyUtils.calcIJ1ChannelPos(dims, axes, pos);
	}
	
	private void fillIJ2Position(ImageDisplay disp, ImagePlus imp,
		long[] dimensions, AxisType[] axes, long[] workspace)
	{
		fillIndex(disp, Axes.X, workspace);
		fillIndex(disp, Axes.Y, workspace);
		fillIndex(disp, Axes.Z, workspace);
		fillIndex(disp, Axes.TIME, workspace);
		LegacyUtils.fillChannelIndices(
			dimensions, axes, imp.getChannel()-1, workspace);
	}
	
	private void fillIndex(ImageDisplay disp, AxisType axis, long[] workspace) {
		final int index = disp.getAxisIndex(axis);
		if (index != -1) workspace[index] = disp.getLongPosition(index); 
	}
}
