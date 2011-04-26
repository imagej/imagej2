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

import imagej.data.Dataset;
import imagej.display.DisplayController;
import imagej.display.ImageDisplayWindow;
import imagej.display.view.DatasetView;
import imagej.display.view.DatasetViewBuilder;
import imagej.display.view.LutXYProjector;
import imagej.util.Index;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.swing.SwingUtilities;

import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class AWTDisplayController implements DisplayController {

	private Dataset dataset;
	private long[] dims;
	private long[] planeDims;
	private Axis[] dimLabels;
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
	public Axis[] getDimLabels() {
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

	DatasetView view;
	boolean composite = false;

	boolean displayAsComposite(final Dataset dataset) {
		boolean isComposite = false;
		int compositeChannelCount = dataset.getImgPlus().getCompositeChannelCount();
		// return 1-N channels, usually 1 or N
		isComposite = compositeChannelCount > 1;
		System.out.println("compositeChannelCount = " + compositeChannelCount);
		return isComposite;
	}

	@Override
	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		//dataset.getImgPlus();
		composite = true;
		//view = DatasetViewBuilder.createView(dataset.getName(), dataset);
		view = DatasetViewBuilder.createCompositeView(dataset.getName(), dataset);
		//view = DatasetViewBuilder.createMultichannelView(dataset.getName(), dataset);
		dims = dataset.getDims();
		dimLabels = dataset.getAxes();
		imageName = dataset.getName();
		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dimLabels.length; i++) {
			if (dimLabels[i] == Axes.X) {
				xIndex = i;
			} else if (dimLabels[i] == Axes.Y) {
				yIndex = i;
			}
		}
		if (xIndex < 0) {
			throw new IllegalArgumentException("No X dimension");
		}
		if (yIndex < 0) {
			throw new IllegalArgumentException("No Y dimension");
		}
		planeDims = new long[dims.length - 2];
		int d = 0;
		for (int i = 0; i < dims.length; i++) {
			if ((i == xIndex) || (i == yIndex)) {
				continue;
			}
			planeDims[d++] = dims[i];
		}
		imgWindow.setTitle(imageName);
		// display first image plane
		pos = new long[dims.length - 2];
		update();
		// FIXME - disabled. make this UI call in SimpleImageDisplay to
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
		int projDimIndex = posIndex+2;  // convert from planar dims (adjust for x,y dims)
		if (!composite) {
			view.getProjector().setPosition(newValue, projDimIndex);

			if (view.getChannelDimIndex() > 0) {
				if (projDimIndex == view.getChannelDimIndex()) {
					((LutXYProjector) view.getProjector()).setLut(view.getLuts().get(newValue));
				}
			}
		} else { // is composite
			// if more than one channel, and the dim changed is the Channel dim, change the lut.
			// values needs to increment n, where n = number of channels 
			view.getProjector().setPosition(newValue, projDimIndex);
		}
		update();
	}

//	@Override
//	public void updatePosition(final int posIndex, final int newValue) {
//		pos[posIndex] = newValue;
//		view.setPosition(newValue, xIndex);
//		update();
//	}
	@Override
	public void update() {
		view.getProjector().map();
		// tell display components to repaint
//		
//		if (!SwingUtilities.isEventDispatchThread()) {
//			SwingUtilities.invokeLater(new Runnable() {
//
//				public void run() {
		BufferedImage bi = (BufferedImage) view.getScreenImage().image();
		display.getImageCanvas().setImage(bi);
//				}

//			});
//		}
		// this is ugly... just trying it out.
		// imgPanel.setNavigationImageEnabled(true);
		imgWindow.setLabel(makeLabel());
	}

//		@Override
//	public void update() {
//		final BufferedImage bImage = getImagePlane();
//		if (bImage != null) {
//			display.getImageCanvas().setImage(bImage);
//		}
//		// this is ugly... just trying it out.
//		// imgPanel.setNavigationImageEnabled(true);
//		imgWindow.setLabel(makeLabel());
//	}
	// -- Helper methods --
	private BufferedImage getImagePlane() {
		// FIXME - how to get a subset with different axes?
		final long no = Index.indexNDto1D(planeDims, pos); // <<<<<<<<<<<<<--------------
		if (no < 0 || no > Integer.MAX_VALUE) {
			throw new IllegalStateException("Plane out of range: " + no);
		}
		final Object plane = dataset.getPlane((int) no);
		currentPlane = plane;
		return makeImage();
	}

	/**
	 * Creates a BufferedImage of the plane of data referenced by the current
	 * position index. For now it only creates BufferedImages of 16-bit gray type.
	 * Represents data values graphically by scaling data to 16-bit unsigned
	 * range.
	 * <p>
	 * Rewritten to not use AWTTools::makeImage(). That code could not support
	 * 1-bit, 12-bit, and 64-bit data. For 64-bit data AWT does not support it
	 * internally as a type. For the other two AWTTools assume the input plane is
	 * a primitive type and not an encoded type (i.e. 12-bit is represented as
	 * 12-bit packings within an int[]). This assumption is bad.
	 * </p>
	 */
	private BufferedImage makeImage() {
		final long width = dataset.getImgPlus().dimension(xIndex);
		if (width < 0 || width > Integer.MAX_VALUE) {
			throw new IllegalStateException("Width out of range: " + width);
		}
		final int w = (int) width;
		final long height = dataset.getImgPlus().dimension(yIndex);
		if (height < 0 || height > Integer.MAX_VALUE) {
			throw new IllegalStateException("Height out of range: " + height);
		}
		final int h = (int) height;
		final BufferedImage bImage =
				new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		final int[] dataInShortRangeEncodedAsInts = new int[w * h];
		final long[] position = dims.clone();
		int p = 0;
		for (int i = 0; i < position.length; i++) {
			if ((i == xIndex) || (i == yIndex)) {
				continue;
			}
			position[i] = pos[p++];
		}
		final RandomAccess<? extends RealType<?>> randomAccess =
				dataset.getImgPlus().randomAccess();
		// create a grayscale image representation of data
		// TODO - broken for float types - may need to range of actual pixel values
		final double rangeMin = randomAccess.get().getMinValue();
		final double rangeMax = randomAccess.get().getMaxValue();
		for (int y = 0; y < h; y++) {
			position[yIndex] = y;
			for (int x = 0; x < w; x++) {
				position[xIndex] = x;
				randomAccess.setPosition(position);
				final double value = randomAccess.get().getRealDouble();
				final int shortVal =
						(int) (0xffff * ((value - rangeMin) / (rangeMax - rangeMin)));
				dataInShortRangeEncodedAsInts[y * w + x] = shortVal;
			}
		}
		final WritableRaster raster = bImage.getRaster();
		raster.setPixels(0, 0, w, h, dataInShortRangeEncodedAsInts);
		return bImage;
	}

	private String makeLabel() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (Axes.isXY(dimLabels[i])) {
				continue;
			}
			p++;
			if (dims[i] == 1) {
				continue;
			}
			sb.append(dimLabels[i] + ": " + (pos[p] + 1) + "/" + dims[i] + "; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		sb.append(dataset.getTypeLabel());
		return sb.toString();
	}

}
