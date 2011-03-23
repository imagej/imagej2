//
// AWTDisplayController.java
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

package imagej.gui.swing.display;

import imagej.display.DisplayController;
import imagej.display.ImageDisplayWindow;
import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.process.Index;

import java.awt.image.BufferedImage;

import loci.formats.gui.AWTImageTools;
import mpicbg.imglib.image.Image;

/**
 * TODO
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class AWTDisplayController implements DisplayController {

	private Dataset dataset;
	private Image<?> image;
	private int[] dims;
	private AxisLabel[] dimLabels;
	private int xIndex, yIndex;
	private int[] pos;
	private final ImageDisplayWindow imgWindow;
	private final SimpleImageDisplay display;
	private Object currentPlane;

	@Override
	public Object getCurrentPlane() {
		return currentPlane;
	}

	@Override
	public AxisLabel[] getDimLabels() {
		return dimLabels;
	}

	@Override
	public int[] getDims() {
		return dims;
	}

	@Override
	public int[] getPos() {
		return pos;
	}

	private String imageName;

	// private NavigableImagePanel imgPanel;

	public AWTDisplayController(final SimpleImageDisplay display) {
		this.display = display;
		this.imgWindow = this.display.getImageDisplayWindow();
		setDataset(this.display.getDataset());
	}

	@Override
	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		image = dataset.getImage();
		dims = image.getDimensions();
		dimLabels = dataset.getMetadata().getAxes();
		imageName = dataset.getMetadata().getName();
		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dimLabels.length; i++) {
			if (dimLabels[i] == AxisLabel.X) {
				xIndex = i;
			}
			if (dimLabels[i] == AxisLabel.Y) {
				yIndex = i;
			}
		}
		if (xIndex < 0) {
			throw new IllegalArgumentException("No X dimension");
		}
		if (yIndex < 0) {
			throw new IllegalArgumentException("No Y dimension");
		}
		imgWindow.setTitle(imageName);
		// display first image plane
		pos = new int[dims.length - 2];
		updatePosition();
	}

	@Override
	public void updatePosition(final int posIndex, final int newValue) {
		pos[posIndex] = newValue;
		updatePosition();
	}

	// -- Helper methods --

	private void updatePosition() {
		final BufferedImage bImage = getImagePlane();
		if (bImage != null) {
			display.getImageCanvas().setImage(bImage);
		}
		// this is ugly... just trying it out.
		// imgPanel.setNavigationImageEnabled(true);
		imgWindow.setLabel(makeLabel());
	}

	private String makeLabel() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (AxisLabel.isXY(dimLabels[i])) {
				continue;
			}
			p++;
			if (dims[i] == 1) {
				continue;
			}
			sb.append(dimLabels[i] + ": " + (pos[p] + 1) + "/" + dims[i] +
				"; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		sb.append(dataset.getPixelType());
		return sb.toString();
	}

	private BufferedImage getImagePlane() {
		// FIXME - how to get a subset with different axes?
		final int no = Index.positionToRaster(dims, pos);
		final Object plane = dataset.getPlane(no);
		currentPlane = plane;
		if (plane instanceof byte[]) {
			return AWTImageTools.makeImage((byte[]) plane, dims[xIndex],
				dims[yIndex], dataset.isSigned());
		}
		else if (plane instanceof short[]) {
			return AWTImageTools.makeImage((short[]) plane, dims[xIndex],
				dims[yIndex], dataset.isSigned());
		}
		else if (plane instanceof int[]) {
			return AWTImageTools.makeImage((int[]) plane, dims[xIndex],
				dims[yIndex], dataset.isSigned());
		}
		else if (plane instanceof float[]) {
			return AWTImageTools.makeImage((float[]) plane, dims[xIndex],
				dims[yIndex]);
		}
		else if (plane instanceof double[]) {
			return AWTImageTools.makeImage((double[]) plane, dims[xIndex],
				dims[yIndex]);
		}
		else {
			throw new IllegalStateException("Unknown data type: " +
				plane.getClass().getName());
		}
	}

}
