//
// PositionHarmonizer.java
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
		final AxisType[] axes = disp.getAxes();
		final long[] dimensions = disp.getDims();
		for (int i = 0; i < axes.length; i++) {
			final long pos = getIJ2AxisPosition(imp, dimensions, axes, i);
			if (pos >= 0)
				disp.setPosition(pos, axes[i]);
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
		int cPos = (int) calcIJ1ChannelPos(disp);
		int zPos = (int) disp.getLongPosition(Axes.Z);
		int tPos = (int) disp.getLongPosition(Axes.TIME);
		imp.setPosition(cPos+1, zPos+1, tPos+1); 
	}

	// -- helpers --
	
	private long getIJ2AxisPosition(ImagePlus imp, long[] dims, AxisType[] axes, int axisNum) {
		AxisType axis = axes[axisNum];
		if (axis == Axes.X) return -1;
		if (axis == Axes.Y) return -1;
		if (axis == Axes.Z) return imp.getSlice()-1;
		if (axis == Axes.TIME) return imp.getFrame()-1;
		if (!LegacyUtils.hasNonIJ1Axes(axes)) return imp.getChannel()-1;
		// else we have a set of axes that are not directly compatible with IJ1.
		// So the nonIJ1 axes are encoded in order as channels.
		return calcIJ2AxisPosition(dims, axes, axisNum, imp.getChannel()-1);
	}
	
	private long calcIJ2AxisPosition(long[] dims, AxisType[] axes, int axisNum, int ij1Channel) {
		long pos[] = new long[dims.length];
		LegacyUtils.fillChannelIndices(dims, axes, ij1Channel, pos);
		return pos[axisNum];
	}
	
	private long calcIJ1ChannelPos(ImageDisplay display) {
		final AxisType[] axes = display.getAxes();
		long lastDim = 1;
		long pos = 0;
		for (int i = 0; i < axes.length; i++) {
			pos *= lastDim;
			AxisType axis = axes[i];
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
		  // TODO - getLongPosition(i) excludes X and Y axes or not ????
			pos += display.getLongPosition(i);
			lastDim = display.dimension(i);
		}
		return pos;
	}
}
