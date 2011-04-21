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

package imagej.awt;

import imagej.data.AxisLabel;
import imagej.data.Dataset;
import imagej.display.DisplayController;
import imagej.display.ImageDisplayWindow;
import imagej.util.Index;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

//import loci.formats.gui.AWTImageTools;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class AWTDisplayController implements DisplayController {

	private Dataset dataset;
	private Img<?> image;
	private long[] dims;
	private long[] planeDims;
	private AxisLabel[] dimLabels;
	private String imageName;
	private int xIndex, yIndex;
	private long[] pos;
	private final ImageDisplayWindow imgWindow;
	private final AWTDisplay display;
	private Object currentPlane;

	public AWTDisplayController(final AWTDisplay display) {
		this.display = display;
		imgWindow = display.getImageDisplayWindow();
		setDataset(display.getDataset());
	}

	@Override
	public Object getCurrentPlane() {
		return currentPlane;
	}

	@Override
	public AxisLabel[] getDimLabels() {
		return dimLabels;
	}

	@Override
	public long[] getDims() {
		return dims;
	}

	@Override
	public long[] getPos() {
		return pos;
	}

	@Override
	public Dataset getDataset() {
		return dataset;
	}
	
	@Override
	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		image = dataset.getImage();
		dims = new long[image.numDimensions()];
		image.dimensions(dims);
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
		planeDims = new long[dims.length-2];
		int d = 0;
		for (int i = 0; i < dims.length; i++) {
			if ((i == xIndex) || (i == yIndex)) continue;
			planeDims[d++] = dims[i];
		}
		imgWindow.setTitle(imageName);
		// display first image plane
		pos = new long[dims.length - 2];
		update();
		//FIXME - disabled. make this UI call in SimpleImageDisplay to
		// avoid incorrectly calculating image canvas dimensions.
		// Reenable if actually needed.
		display.getImageDisplayWindow().pack();

		// TODO
		// maybe the best way to handle a change in Dataset, rather than pack()
		// everytime, would be to fire an event here that says my Dataset has
		// changed. the display that owns this Dataset could subscribe to Dataset
		// changed events and it can decide whether its appropriate for the frame
		// to be repacked.
	}

	@Override
	public void updatePosition(final int posIndex, final int newValue) {
		pos[posIndex] = newValue;
		update();
	}

	@Override
	public void update() {
		final BufferedImage bImage = getImagePlane();
		if (bImage != null) {
			display.getImageCanvas().setImage(bImage);
		}
		// this is ugly... just trying it out.
		// imgPanel.setNavigationImageEnabled(true);
		imgWindow.setLabel(makeLabel());
	}

	// -- Helper methods --

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
		final int no = Index.indexNDto1D(planeDims, pos);
		final Object plane = dataset.getPlane(no);
		currentPlane = plane;
		return makeImage();
	}
	
	/** 
	 * <p>
	 * creates a BufferedImage of the plane of data referenced by the current
	 * position index. For now it only creates BufferedImages of 16-bit gray
	 * type. Represents data values graphically by scaling data to 16-bit
	 * unsigned range.
	 * </p>
	 * 
	 * <p>
	 * rewritten to not use AWTTools::makeImage(). That code could not support
	 * 1-bit, 12-bit, and 64-bit data. For 64-bit data AWT does not support it
	 * internally as a type. For the other two AWTTools assume the input plane
	 * is a primitive type and not an encoded type (i.e. 12-bit is represented
	 * as 12-bit packings within an int[]). This assumption is bad.
	 * </p>
	 */
	private BufferedImage makeImage() {
		int w = dataset.getImage().getDimension(xIndex);
		int h = dataset.getImage().getDimension(yIndex);
		BufferedImage bImage = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		int[] dataInShortRangeEncodedAsInts = new int[w*h];
		int[] position = dims.clone();
		int p = 0;
		for (int i = 0; i < position.length; i++) {
			if ((i == xIndex) || (i == yIndex))
				continue;
			position[i] = pos[p++];
		}
		LocalizableByDimCursor<? extends RealType<?>> cursor =
			(LocalizableByDimCursor<? extends RealType<?>>)
			dataset.getImage().createLocalizableByDimCursor();
		// create a grayscale image representation of data
		// TODO - broken for float types - may need to range of actual pixel values
		double rangeMin = cursor.getType().getMinValue();
		double rangeMax = cursor.getType().getMaxValue();
		for (int y = 0; y < h; y++) {
			position[yIndex] = y;
			for (int x = 0; x < w; x++) {
				position[xIndex] = x;
				cursor.setPosition(position);
				double value = cursor.getType().getRealDouble();
				int shortVal = (int) (0xffff * ((value - rangeMin) / (rangeMax - rangeMin)));
				dataInShortRangeEncodedAsInts[y*w + x] = shortVal;
			}
		}
		cursor.close();
		WritableRaster raster = bImage.getRaster();
		raster.setPixels(0, 0, w, h, dataInShortRangeEncodedAsInts);
		return bImage;
	}
}
