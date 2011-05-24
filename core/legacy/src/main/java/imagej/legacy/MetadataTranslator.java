//
// MetadataTranslator.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.measure.Calibration;
import imagej.data.Dataset;
import net.imglib2.img.Axes;

/**
 * MetadataTranslator moves metadata back and forth between {@link Dataset}s and
 * {@link ImagePlus}es.
 * 
 * @author Barry DeZonia
 */
public class MetadataTranslator {

	public void setDatasetMetadata(final Dataset ds, final ImagePlus imp) {
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
	}

	public void setImagePlusMetadata(final Dataset ds, final ImagePlus imp) {
		imp.setTitle(ds.getName());
		// copy calibration info where possible
		final Calibration cal = imp.getCalibration();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) cal.pixelWidth = ds.calibration(xIndex);
		if (yIndex >= 0) cal.pixelHeight = ds.calibration(yIndex);
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0) cal.pixelDepth = ds.calibration(zIndex);
		if (tIndex >= 0) cal.frameInterval = ds.calibration(tIndex);
	}

}
