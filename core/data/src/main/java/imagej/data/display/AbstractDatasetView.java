/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.display;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.event.DatasetRGBChangedEvent;
import imagej.data.event.DatasetTypeChangedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * A view into a {@link Dataset}, for use with a {@link ImageDisplay}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public abstract class AbstractDatasetView extends AbstractDataView implements
	DatasetView
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

	private CompositeXYProjector<? extends RealType<?>> projector;

	private final ArrayList<RealLUTConverter<? extends RealType<?>>> converters =
		new ArrayList<RealLUTConverter<? extends RealType<?>>>();

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public AbstractDatasetView(final Dataset dataset) {
		super(dataset);
		this.dataset = dataset;
		final EventService eventService = getEventService();
		subscribers = eventService == null ? null : eventService.subscribe(this);
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
	public CompositeXYProjector<? extends RealType<?>> getProjector() {
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
		// TODO - temp hacks towards fixing bug #668
		// For now we'll keep this method lightweight and dumb and require
		// callers to map() and update()
		// projector.map();
		// getDisplay().update();
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
			// TODO - replace with !ColorTables.isGrayColorTable(colorTable)
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

	// -- DataView methods --

	@Override
	public Dataset getData() {
		return dataset;
	}

	@Override
	public int getPreferredWidth() {
		return getScreenImage().image().getWidth(null);
	}

	@Override
	public int getPreferredHeight() {
		return getScreenImage().image().getHeight(null);
	}

	@Override
	public void rebuild() {
		channelDimIndex = getChannelDimIndex();

		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		// Make sure any calls to updateLUTs are ignored. If they happen before
		// the converters are correctly defined (in setupProjector) an exception
		// can get thrown. Basically if you add a channel to an image the converter
		// size() can be out of sync.
		converters.clear();

		if (defaultLUTs == null || defaultLUTs.size() != getChannelCount()) {
			defaultLUTs = new ArrayList<ColorTable8>();
			resetColorTables(false);
		}

		final int width = (int) img.dimension(0);
		final int height = (int) img.dimension(1);
		screenImage = new ARGBScreenImage(width, height);

		final boolean composite = isComposite();
		setupProjector(composite);
		// NB - it's imperative that instance variable "projector" is correctly
		// setup before calling updateLUTs()
		updateLUTs();
		projector.map();
	}

	// -- PositionableByAxis methods --

	@Override
	public long getLongPosition(final AxisType axis) {
		if (Axes.isXY(axis)) return 0;
		final int dim = getData().getAxisIndex(axis);
		if (dim < 0) return 0;
		if (projector == null) return 0;
		// It is possible that projector is out of sync with data or view. Choose a
		// sensible default value to avoid exceptions.
		if (dim >= projector.numDimensions()) return 0;
		return projector.getLongPosition(dim);
	}

	@Override
	public void setPosition(final long position, final AxisType axis) {
		if (Axes.isXY(axis)) return;
		final int dim = getData().getAxisIndex(axis);
		if (dim < 0) return;
		final long currentValue = projector.getLongPosition(dim);
		if (position == currentValue) {
			return; // no change
		}
		projector.setPosition(position, dim);

		// update color tables
		if (dim != channelDimIndex) {
			updateLUTs();
		}

		projector.map();

		super.setPosition(position, axis);
	}

	// -- Helper methods --

	private boolean isComposite() {
		return dataset.getCompositeChannelCount() > 1 || dataset.isRGBMerged();
	}

	private int getChannelDimIndex() {
		return dataset.getAxisIndex(Axes.CHANNEL);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupProjector(final boolean composite)
	{
		converters.clear();
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			autoscale(c);
			converters
				.add(new RealLUTConverter(dataset.getImgPlus().getChannelMinimum(c),
					dataset.getImgPlus().getChannelMaximum(c), null));
		}
		projector =
			new CompositeXYProjector(dataset.getImgPlus(), screenImage, converters,
				channelDimIndex);
		projector.setComposite(composite);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void autoscale(final int c) {
		// CTR FIXME - Autoscaling needs to be reworked.

		// Get min/max from metadata
		double min = dataset.getImgPlus().getChannelMinimum(c);
		double max = dataset.getImgPlus().getChannelMaximum(c);
		if (Double.isNaN(max) || Double.isNaN(min)) {
			// not provided in metadata, so calculate the min/max
			// TODO: this currently applies the global min/max to all channels...
			// need to change ComputeMinMax to find min/max per channel
			final ComputeMinMax<? extends RealType<?>> cmm =
				new ComputeMinMax(dataset.getImgPlus());
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
		final Position pos = getPlanePosition();
		if (channelDimIndex >= 0) {
			pos.setPosition(cPos, channelDimIndex - 2);
		}
		final int no = (int) pos.getIndex();
		final ColorTable8 lut = dataset.getColorTable8(no);
		if (lut != null) {
			return lut; // return dataset-specific LUT
		}
		return defaultLUTs.get(cPos); // return default channel LUT
	}

	private long getChannelCount() {
		if (channelDimIndex < 0) return 1;
		return dataset.getExtents().dimension(channelDimIndex);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DatasetTypeChangedEvent event) {
		if (dataset == event.getObject()) {
			rebuild();
		}
	}

	@EventHandler
	protected void onEvent(final DatasetRGBChangedEvent event) {
		if (dataset == event.getObject()) {
			rebuild();
		}
	}

	@EventHandler
	protected void onEvent(final DatasetUpdatedEvent event) {
		// FIXME: eliminate hacky logic here
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

}
