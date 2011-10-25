//
//
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

import net.imglib2.img.Axes;
import ij.ImagePlus;
import ij.measure.Calibration;
import imagej.data.Dataset;


/**
 * Synchronizes metadata bidirectionally between a Dataset and an ImagePlus
 *  
 * @author Barry DeZonia
 *
 */
public class MetadataHarmonizer implements DataHarmonizer {

	/** Sets a {@link Dataset}'s metadata to match a given {@link ImagePlus}. */
	@Override
	public void updateDataset(Dataset ds, ImagePlus imp) {
		ds.setName(imp.getTitle());
		// copy calibration info where possible
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final Calibration cal = imp.getCalibration();
		if (xIndex >= 0) ds.setCalibration(cal.pixelWidth, xIndex);
		if (yIndex >= 0) ds.setCalibration(cal.pixelHeight, yIndex);
		if (cIndex >= 0) ds.setCalibration(1, cIndex);
		if (zIndex >= 0) ds.setCalibration(cal.pixelDepth, zIndex);
		if (tIndex >= 0) ds.setCalibration(cal.frameInterval, tIndex);
		// no need to ds.update() - these calls should track that themselves
	}

	/** Sets an {@link ImagePlus}' metadata to match a given {@link Dataset}. */
	@Override
	public void updateLegacyImage(Dataset ds, ImagePlus imp) {
		imp.setTitle(ds.getName());
		// copy calibration info where possible
		final Calibration cal = imp.getCalibration();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) cal.pixelWidth = calValue(ds,xIndex,1);
		if (yIndex >= 0) cal.pixelHeight = calValue(ds,yIndex,1);
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0) cal.pixelDepth = calValue(ds,zIndex,1);
		if (tIndex >= 0) cal.frameInterval = calValue(ds,tIndex,0);
	}
	
	// NB : IJ1 hates the NaNs that IJ2 defaults to. So make this method in order
	// to set calibration values safely. This allows things like IJ1's Show Info
	// command to avoid displaying NaNs. As a consequence NaN calib values in IJ2
	// will get driven to 1.0's & 0.0's after running a plugin.
	
	private double calValue(Dataset ds, int axisIndex, double defaultValue) {
		double value = ds.calibration(axisIndex);
		if (Double.isNaN(value)) return defaultValue;
		return value;
	}
}
