//
// AbstractDatasetView.java
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
import imagej.data.Extents;
import imagej.data.event.DatasetRGBChangedEvent;
import imagej.data.event.DatasetTypeChangedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
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
public abstract class AbstractDatasetView extends AbstractDisplayView
	implements DatasetView
{

	private final Dataset dataset;

	/** The dimensional index representing channels, for compositing. */
	private int channelDimIndex;

	/**
	 * Default color tables, one per channel, used when the {@link Dataset} 
	 * doesn't have one for a particular plane.
	 */
	private ArrayList<ColorTable8> defaultLUTs;

	private ARGBScreenImage screenImage;

	private CompositeXYProjector<? extends RealType<?>, ARGBType> projector;

	private final ArrayList<RealLUTConverter<? extends RealType<?>>> converters =
		new ArrayList<RealLUTConverter<? extends RealType<?>>>();

	private ArrayList<EventSubscriber<?>> subscribers;
	
	public AbstractDatasetView(final Display display, final Dataset dataset) {
		super(display, dataset);
		this.dataset = dataset;
		subscribeToEvents();
	}

	// -- DatasetView methods --

	@Override
	public ARGBScreenImage getScreenImage() {
		return screenImage;
	}

	@Override
	public int getCompositeDimIndex() {
		return channelDimIndex;
	}

	@Override
	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return dataset.getImgPlus();
	}

	@Override
	public CompositeXYProjector<? extends RealType<?>, ARGBType> getProjector() {
		return projector;
	}

	@Override
	public List<RealLUTConverter<? extends RealType<?>>> getConverters() {
		return Collections.unmodifiableList(converters);
	}

	@Override
	public void setComposite(final boolean composite) {
		projector.setComposite(composite);
	}

	@Override
	public List<ColorTable8> getColorTables() {
		return Collections.unmodifiableList(defaultLUTs);
	}

	@Override
	public void setColorTable(final ColorTable8 colorTable, final int channel) {
		defaultLUTs.set(channel, colorTable);
		updateLUTs();
	}

	@Override
	public void resetColorTables(final boolean grayscale) {
		final int channelCount = (int) getChannelCount();
		defaultLUTs.clear();
		defaultLUTs.ensureCapacity(channelCount);
		if (grayscale || channelCount == 1) {
			for (int i = 0; i < channelCount; i++) {
				defaultLUTs.add(ColorTables.GRAYS);
			}
		}
		else {
			for (int c = 0; c < channelCount; c++) {
				defaultLUTs.add(ColorTables.getDefaultColorTable(c));
			}
		}
		updateLUTs();
	}

	@Override
	public ColorMode getColorMode() {
		final boolean composite = projector.isComposite();
		if (composite) {
			return ColorMode.COMPOSITE;
		}
		final List<ColorTable8> colorTables = getColorTables();
		for (final ColorTable8 colorTable : colorTables) {
			if (colorTable != ColorTables.GRAYS) {
				return ColorMode.COLOR;
			}
		}
		return ColorMode.GRAYSCALE;
	}

	@Override
	public void setColorMode(final ColorMode colorMode) {
		resetColorTables(colorMode == ColorMode.GRAYSCALE);
		projector.setComposite(colorMode == ColorMode.COMPOSITE);
		projector.map();
	}

	// -- DisplayView methods --

	@Override
	public Dataset getDataObject() {
		return dataset;
	}

	@Override
	public long getPosition(final int dim) {
		if ((dim == Axes.X.ordinal()) || (dim == Axes.Y.ordinal())) return 0;
		return projector.getLongPosition(dim);
	}
	
	@Override
	public void setPosition(final long value, final int dim) {
		if ((dim == Axes.X.ordinal()) || (dim == Axes.Y.ordinal())) return;
		final long currentValue = projector.getLongPosition(dim);
		if (value == currentValue) {
			return; // no change
		}
		projector.setPosition(value, dim);

		// update color tables
		if (dim != channelDimIndex) {
			updateLUTs();
		}

		projector.map();

		super.setPosition(value, dim);
	}

	@Override
	public void rebuild() {
		channelDimIndex = getChannelDimIndex();

		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		dims = new long[img.numDimensions()];
		img.dimensions(dims);
		planeDims = new long[dims.length-2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = dims[i+2];
		Extents extents = new Extents(planeDims);
		planePosObj = extents.createPosition();
		position = new long[dims.length];
		planePos = new long[planeDims.length];

		if (defaultLUTs == null || defaultLUTs.size() != getChannelCount()) {
			defaultLUTs = new ArrayList<ColorTable8>();
			resetColorTables(false);
		}

		final int width = (int) img.dimension(0);
		final int height = (int) img.dimension(1);
		screenImage = new ARGBScreenImage(width, height);

		final boolean composite = isComposite();
		projector = createProjector(composite);
		projector.map();
	}

	// -- Helper methods --

	private boolean isComposite() {
		return dataset.getCompositeChannelCount() > 1 || dataset.isRGBMerged();
	}

	private int getChannelDimIndex() {
		return dataset.getAxisIndex(Axes.CHANNEL);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompositeXYProjector<? extends RealType<?>, ARGBType>
		createProjector(final boolean composite)
	{
		converters.clear();
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			autoscale(c);
			converters
				.add(new RealLUTConverter(dataset.getImgPlus().getChannelMinimum(c),
					dataset.getImgPlus().getChannelMaximum(c), null));
		}
		final CompositeXYProjector proj =
			new CompositeXYProjector(dataset.getImgPlus(), screenImage, converters,
				channelDimIndex);
		proj.setComposite(composite);
		updateLUTs();
		return proj;
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	public void autoscale(final int c) {
		// Get min/max from metadata
		double min = dataset.getImgPlus().getChannelMinimum(c);
		double max = dataset.getImgPlus().getChannelMaximum(c);
		if (Double.isNaN(max) || Double.isNaN(min)) {
			// not provided in metadata, so calculate the min/max
			// TODO: this currently applies the global min/max to all channels...
			// need to change GetImgMinMax to find min/max per channel
			final GetImgMinMax<? extends RealType<?>> cmm =
				new GetImgMinMax(dataset.getImgPlus().getImg());
			cmm.process();
			min = cmm.getMin().getRealDouble();
			max = cmm.getMax().getRealDouble();
			dataset.getImgPlus().setChannelMinimum(c, min);
			dataset.getImgPlus().setChannelMaximum(c, max);
		}
		if (min == max) { // if all black or all white, use range for type
			final RealType<?> type = dataset.getType();
			dataset.getImgPlus().setChannelMinimum(c, type.getMinValue());
			dataset.getImgPlus().setChannelMaximum(c, type.getMaxValue());
		}

	}

	private void updateLUTs() {
		if (converters.size() == 0) {
			return; // converters not yet initialized
		}
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			final ColorTable8 lut = getCurrentLUT(c);
			converters.get(c).setLUT(lut);
		}
	}

	private ColorTable8 getCurrentLUT(final int cPos) {
		if (channelDimIndex >= 0) {
			planePos[channelDimIndex - 2] = cPos;
		}
		planePosObj.setPosition(planePos);
		final int no = (int) planePosObj.getIndex();
		final ColorTable8 lut = dataset.getColorTable8(no);
		if (lut != null) {
			return lut; // return dataset-specific LUT
		}
		return defaultLUTs.get(cPos); // return default channel LUT
	}

	private long getChannelCount() {
		return channelDimIndex < 0 ? 1 : dims[channelDimIndex];
	}

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<DatasetTypeChangedEvent> typeChangeSubscriber =
			new EventSubscriber<DatasetTypeChangedEvent>() {

				@Override
				public void onEvent(final DatasetTypeChangedEvent event) {
					if (dataset == event.getObject()) {
						rebuild();
					}
				}

			};
		subscribers.add(typeChangeSubscriber);
		Events.subscribe(DatasetTypeChangedEvent.class, typeChangeSubscriber);

		final EventSubscriber<DatasetRGBChangedEvent> rgbChangeSubscriber =
			new EventSubscriber<DatasetRGBChangedEvent>() {

				@Override
				public void onEvent(final DatasetRGBChangedEvent event) {
					if (dataset == event.getObject()) {
						rebuild();
					}
				}

			};
		subscribers.add(rgbChangeSubscriber);
		Events.subscribe(DatasetRGBChangedEvent.class, rgbChangeSubscriber);

		final EventSubscriber<DatasetUpdatedEvent> updateSubscriber =
			new EventSubscriber<DatasetUpdatedEvent>() {

				@Override
				public void onEvent(final DatasetUpdatedEvent event) {
					if (event instanceof DatasetTypeChangedEvent) {
						return;
					}
					if (event instanceof DatasetRGBChangedEvent) {
						return;
					}
					if (dataset == event.getObject()) {
						projector.map();
					}
				}

			};
		subscribers.add(updateSubscriber);
		Events.subscribe(DatasetUpdatedEvent.class, updateSubscriber);
	}

}
