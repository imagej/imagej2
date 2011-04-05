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
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

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
	private int[] planeDims;
	private AxisLabel[] dimLabels;
	private String imageName;
	private int xIndex, yIndex;
	private int[] pos;
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
	public int[] getDims() {
		return dims;
	}

	@Override
	public int[] getPos() {
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
		planeDims = new int[dims.length-2];
		int d = 0;
		for (int i = 0; i < dims.length; i++) {
			if ((i == xIndex) || (i == yIndex))
				continue;
			planeDims[d++] = dims[i];
		}
		imgWindow.setTitle(imageName);
		// display first image plane
		pos = new int[dims.length - 2];
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
		final int no = Index.positionToRaster(planeDims, pos);
		final Object plane = dataset.getPlane(no);
		currentPlane = plane;
		return makeImage();
	}
	
	/*
	// TODO - make type info always available in Imglib and not repeatedly
	// hatch cursor. good workaround would be to set for Dataset once.
	// TODO - make numBits (which represents the number of bits of data
	// this type is represented as in memory. For example, 12-bit data is
	// stored in a big int[] packed 12 bits at a time. So need to know I have
	// 12-bits of info storedin an int[]. But if we chose to put 12-bit ints
	// into 16-bit shorts with no packing we'd want to be able to represent
	// that. Notice that its still not genral enough. Image 3 bits per pixel
	// stored in bytes. Either the 3rd pixel will straddle two bytes or they
	// could only be encoded to use the first six bits of each byte.
	private int getNumBits(Dataset ds) {
		Cursor<? extends RealType<?>> cursor =
			(Cursor<? extends RealType<?>>) ds.getImage().createCursor();
		RealType<?> type = cursor.getType();
		cursor.close();
		// FIXME - temp hacky way to get this working for now
		if (type instanceof BitType)
			return 1;
		else if (type instanceof ByteType)
			return 8;
		else if (type instanceof UnsignedByteType)
			return 8;
		else if (type instanceof Unsigned12BitType)
			return 12;
		else if (type instanceof ShortType)
			return 16;
		else if (type instanceof UnsignedShortType)
			return 16;
		else if (type instanceof IntType)
			return 32;
		else if (type instanceof UnsignedIntType)
			return 32;
		else if (type instanceof FloatType)
			return 32;
		else if (type instanceof LongType)
			return 64;
		else if (type instanceof DoubleType)
			return 64;
		
		return 0;
	}
	*/

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
	// TODO - notice that ProbeTool also makes assumption that plane of data does
	// not have any encoded types like 1-bit or 12-bit etc. The whole concept of
	// getting the current plane is broken.
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
