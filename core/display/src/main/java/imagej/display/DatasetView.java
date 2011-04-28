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

package imagej.display;

import imagej.data.Dataset;
import imagej.data.event.DatasetChangedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.util.Dimensions;
import imagej.util.Index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * A view into a {@link Dataset}, for use with a {@link Display}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class DatasetView implements DisplayView,
	EventSubscriber<DatasetChangedEvent>
{

	private final Display display;
	private final Dataset dataset;

	private final int channelDimIndex;

	private final long[] dims, planeDims;
	private final long[] position, planePos;

	/**
	 * Default color tables, one per channel, used when the {@link Dataset} 
	 * doesn't have one for a particular plane.
	 */
	private final ArrayList<ColorTable8> defaultLUTs;

	private final ARGBScreenImage screenImage;
	private final CompositeXYProjector<? extends RealType<?>, ARGBType> projector;
	private final ArrayList<RealLUTConverter<? extends RealType<?>>> converters =
		new ArrayList<RealLUTConverter<? extends RealType<?>>>();

	private int offsetX, offsetY;

	public DatasetView(final Display display, final Dataset dataset) {
		this.display = display;
		this.dataset = dataset;

		channelDimIndex = getChannelDimIndex(dataset);

		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		dims = new long[img.numDimensions()];
		img.dimensions(dims);
		planeDims = Dimensions.getDims3AndGreater(dims);
		position = new long[dims.length];
		planePos = new long[planeDims.length];

		defaultLUTs = new ArrayList<ColorTable8>();
		initializeDefaultLUTs();

		final int width = (int) img.dimension(0);
		final int height = (int) img.dimension(1);
		screenImage = new ARGBScreenImage(width, height);

		final int min = 0, max = 255;
		final boolean composite = isComposite(dataset);
		projector = createProjector(min, max, composite);
		projector.map();

		// update display when linked dataset changes
		Events.subscribe(DatasetChangedEvent.class, this);
	}

	// -- DatasetView methods --

	public int getCompositeDimIndex() {
		return channelDimIndex;
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

	public XYProjector<? extends RealType<?>, ARGBType> getProjector() {
		return projector;
	}

	public List<RealLUTConverter<? extends RealType<?>>> getConverters() {
		return Collections.unmodifiableList(converters);
	}

	public List<ColorTable8> getChannelLUTs() {
		return Collections.unmodifiableList(defaultLUTs);
	}

	public void setComposite(final boolean composite) {
		projector.setComposite(composite);
	}

	// -- DisplayView methods --

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public long[] getPlanePosition() {
		return planePos;
	}

	@Override
	public long getPlaneIndex() {
		return Index.indexNDto1D(planeDims, planePos);
	}

	@Override
	public void setPosition(final int value, final int dim) {
		projector.setPosition(value, dim);
		projector.localize(position);
		for (int i = 0; i < planePos.length; i++)
			planePos[i] = position[i + 2];

		// update color tables
		if (dim != channelDimIndex) updateLUTs();

		projector.map();
		display.update();
	}

	@Override
	public ARGBScreenImage getImage() {
		return screenImage;
	}

	@Override
	public int getImageWidth() {
		return screenImage.image().getWidth(null);
	}

	@Override
	public int getImageHeight() {
		return screenImage.image().getHeight(null);
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final DatasetChangedEvent event) {
		if (event.getObject() == dataset) {
			projector.map();
			display.update();
		}
	}

	// -- Helper methods --

	private static boolean isComposite(final Dataset dataset) {
		return dataset.getCompositeChannelCount() > 1;
	}

	private static int getChannelDimIndex(final Dataset dataset) {
		return dataset.getAxisIndex(Axes.CHANNEL);
	}

	private void initializeDefaultLUTs() {
		final int channelCount = (int) getChannelCount();
		defaultLUTs.clear();
		defaultLUTs.ensureCapacity(channelCount);
		if (channelCount > 1) {
			// multi-channel: use RGBCMY
			for (int i = 0; i < channelCount; i++) {
				final ColorTable8 lut;
				switch (i) {
					case 0:
						lut = ColorTables.RED;
						break;
					case 1:
						lut = ColorTables.GREEN;
						break;
					case 2:
						lut = ColorTables.BLUE;
						break;
					case 3:
						lut = ColorTables.CYAN;
						break;
					case 4:
						lut = ColorTables.MAGENTA;
						break;
					case 5:
						lut = ColorTables.YELLOW;
						break;
					default:
						lut = ColorTables.GRAYS;
				}
				defaultLUTs.add(lut);
			}
		}
		else {
			// single channel: use grayscale
			defaultLUTs.add(ColorTables.GRAYS);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompositeXYProjector<? extends RealType<?>, ARGBType>
		createProjector(final int min, final int max, final boolean composite)
	{
		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			converters.add(new RealLUTConverter(min, max, null));
		}
		final CompositeXYProjector proj =
			new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
		proj.setComposite(composite);
		updateLUTs();
		return proj;
	}

	private void updateLUTs() {
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			final ColorTable8 lut = getCurrentLUT(c);
			converters.get(c).setLUT(lut);
		}
	}

	private ColorTable8 getCurrentLUT(final int cPos) {
		planePos[channelDimIndex - 2] = cPos;
		final int no = (int) Index.indexNDto1D(planeDims, planePos);
		final ColorTable8 lut = dataset.getColorTable8(no);
		if (lut != null) return lut; // return dataset-specific LUT
		return defaultLUTs.get(cPos); // return default channel LUT
	}

	private long getChannelCount() {
		return channelDimIndex < 0 ? 1 : dims[channelDimIndex];
	}

}
