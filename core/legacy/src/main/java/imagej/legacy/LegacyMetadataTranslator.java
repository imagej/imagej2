//
// LegacyMetadataTranslator.java
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

/**
 * LegacyMetaDataTranslator - moves metadata back and forth between
 * Datasets and ImagePluses.
 * 
 * @author Barry DeZonia
 */
package imagej.legacy;

import net.imglib2.img.Axes;
import ij.ImagePlus;
import ij.measure.Calibration;
import imagej.data.Dataset;

public class LegacyMetadataTranslator {
	
	public void setDatasetMetadata(Dataset ds, ImagePlus imp) {
		// copy calibration info where possible
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		Calibration cal = imp.getCalibration();
		if (xIndex >= 0)
			ds.setCalibration(cal.pixelWidth, xIndex);
		if (yIndex >= 0)
			ds.setCalibration(cal.pixelHeight, yIndex);
		if (cIndex >= 0)
			ds.setCalibration(1, cIndex);
		if (zIndex >= 0)
			ds.setCalibration(cal.pixelDepth, zIndex);
		if (tIndex >= 0)
			ds.setCalibration(cal.frameInterval, tIndex);
	}
	
	public void setImagePlusMetadata(Dataset ds, ImagePlus imp) {
		// copy calibration info where possible
		Calibration cal = imp.getCalibration();
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0)
			cal.pixelWidth = ds.calibration(xIndex);
		if (yIndex >= 0)
			cal.pixelHeight = ds.calibration(yIndex);
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0)
			cal.pixelDepth = ds.calibration(zIndex);
		if (tIndex >= 0)
			cal.frameInterval = ds.calibration(tIndex);
	}
}
