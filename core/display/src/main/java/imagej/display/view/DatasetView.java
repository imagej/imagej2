//
// DatasetView.java
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

package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.ImageCanvas;
import imagej.util.Dimensions;
import imagej.util.Index;

import java.util.ArrayList;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * A view into a {@link Dataset}, for use with a {@link Display}.
 * 
 * @author Grant Harris
 */
public class DatasetView {

	private final Dataset dataset;
	private final int channelDimIndex;
	private final ArrayList<ColorTable8> luts;
	private final boolean composite;

	private final ARGBScreenImage screenImage;
	private final XYProjector<? extends RealType<?>, ARGBType> projector;
	private final ArrayList<RealLUTConverter<? extends RealType<?>>> converters =
		new ArrayList<RealLUTConverter<? extends RealType<?>>>();

	private final long[] dims, planeDims;
	private final long[] position, planePos;

	private int offsetX, offsetY;
	private ImageCanvas imgCanvas;

	public DatasetView(final Dataset dataset, final int channelDimIndex,
		final ArrayList<ColorTable8> luts, final boolean composite)
	{
		this.dataset = dataset;
		this.channelDimIndex = channelDimIndex;
		this.luts = luts;
		this.composite = composite;

		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();
		final int width = (int) img.dimension(0);
		final int height = (int) img.dimension(1);
		screenImage = new ARGBScreenImage(width, height);
		final int min = 0, max = 255;

		projector = createProjector(min, max);
		projector.map();

		dims = new long[img.numDimensions()];
		img.dimensions(dims);
		planeDims = Dimensions.getDims3AndGreater(dims);
		position = new long[dims.length];
		planePos = new long[planeDims.length];
	}

	public int getChannelDimIndex() {
		return channelDimIndex;
	}

	public ArrayList<ColorTable8> getLuts() {
		return luts;
	}

	public void setImgCanvas(final ImageCanvas imgCanvas) {
		this.imgCanvas = imgCanvas;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(final int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(final int offsetY) {
		this.offsetY = offsetY;
	}

	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return dataset.getImgPlus();
	}

	public ARGBScreenImage getScreenImage() {
		return screenImage;
	}

	public ArrayList<RealLUTConverter<? extends RealType<?>>> getConverters() {
		return converters;
	}

	public XYProjector<? extends RealType<?>, ARGBType> getProjector() {
		return projector;
	}

	public void setPosition(final int value, final int dim) {
		projector.setPosition(value, dim);
		projector.localize(position);
		for (int i = 0; i < planePos.length; i++) planePos[i] = position[i + 2];

		// update color tables
		if (dim != channelDimIndex) {
			final int channelCount = (int) dims[channelDimIndex];
			for (int c = 0; c < channelCount; c++) {
				final ColorTable8 lut = getCurrentLUT(c);
				converters.get(c).setLUT(lut);
			}
		}

		projector.map();
		if (imgCanvas != null) imgCanvas.updateImage();
	}

	// -- Helper methods --

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private XYProjector<? extends RealType<?>, ARGBType> createProjector(
		final int min, final int max)
	{
		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		if (channelDimIndex < 0) {
			// no channels dimension
			final RealLUTConverter<? extends RealType<?>> converter;
			if (luts == null) {
				// no LUTs available
				converter = new RealLUTConverter(min, max, null);
			}
			else {
				// use LUT for display
				converter = new RealLUTConverter(min, max, luts.get(0));
			}
			converters.add(converter);
			return new XYProjector(img, screenImage, converter);
		}

		// channels dimension exists
		for (int i = 0; i < luts.size(); i++) {
			final ColorTable8 lut = luts.get(i);
			converters.add(new RealLUTConverter(min, max, lut));
		}
		final CompositeXYProjector proj =
			new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
		proj.setComposite(composite);
		return proj;
	}

	private ColorTable8 getCurrentLUT(final int cPos) {
		planePos[channelDimIndex - 2] = cPos;
		final int no = (int) Index.indexNDto1D(planeDims, planePos);
		return dataset.getColorTable8(no);
	}

}
