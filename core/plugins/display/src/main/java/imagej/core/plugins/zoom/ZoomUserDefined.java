//
// ZoomUserDefined.java
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

package imagej.core.plugins.zoom;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import imagej.data.display.ImageDisplay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.IntCoords;

/**
 * Zooms in on the center of the image at the user-specified magnification
 * level.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Zoom", mnemonic = 'z'), @Menu(label = "Set...", weight = 6) })
public class ZoomUserDefined implements ImageJPlugin {

	// -- instance variables --
	
	@Parameter
	private ImageDisplay display;

	@Parameter(label = "Zoom (%) :", min = "0.1", max = "500000")
	private double userDefinedScale = 100;

	@Parameter(label = "X center:")
	private long centerX = 1L;
	
	@Parameter(label = "Y center:")
	private long centerY = 1L;
	
	// -- public interface --
	
	@Override
	public void run() {
		double percentX = 1.0 * centerX / getDim(display, Axes.X);
		double percentY = 1.0 * centerY / getDim(display, Axes.Y);
		int cx = (int) (percentX * display.getCanvas().getCanvasWidth());
		int cy = (int) (percentY * display.getCanvas().getCanvasHeight());
		IntCoords center = new IntCoords(cx, cy);
		display.getCanvas().setZoom(userDefinedScale/100.0, center);
	}

	public double getUserDefinedScale() {
		return userDefinedScale;
	}

	public void setUserDefinedScale(final double userDefinedScale) {
		this.userDefinedScale = userDefinedScale;
	}

	// -- private helpers --
	
	// TO BE USED WHEN PARAMETER INITIALIZERS ARE IN PLACE

	private void calcCenterX() {
		centerX = getDim(display, Axes.X) / 2;
	}
	
	private void calcCenterY() {
		centerY = getDim(display, Axes.Y) / 2;
	}
	
	private static long getDim(ImageDisplay display, Axis axis) {
		long[] dims = display.getDims();
		int axisIndex = display.getAxisIndex(axis);
		if (axisIndex < 0) return 1;
		return dims[axisIndex];
	}
}
