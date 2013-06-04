/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.util.ColorRGB;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.type.numeric.RealType;

/**
 * A linkage between a {@link Dataset} and a {@link Display}. The view takes
 * care of mapping the N-dimensional data into a representation suitable for
 * showing onscreen.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public interface DatasetView extends DataView {

	ARGBScreenImage getScreenImage();

	int getCompositeDimIndex();

	CompositeXYProjector<? extends RealType<?>> getProjector();

	/** Gets the number of channels in the displayed data. */
	int getChannelCount();

	/**
	 * Gets the minimum value in the <em>display</em> range, for the given
	 * channel.
	 * <p>
	 * NB: This is a different value than that given by
	 * {@link Dataset#getChannelMinimum(int)}; the latter is the minimum
	 * <em>data</em> value for that channel, independent of any visualization.
	 * </p>
	 */
	double getChannelMin(int c);

	/**
	 * Gets the maximum value in the <em>display</em> range, for the given
	 * channel.
	 * <p>
	 * NB: This is a different value than that given by
	 * {@link Dataset#getChannelMaximum(int)}; the latter is the maximum
	 * <em>data</em> value for that channel, independent of any visualization.
	 * </p>
	 */
	double getChannelMax(int c);

	/**
	 * Sets the minimum and maximum values of the <em>display</em> range, for the
	 * given channel.
	 * <p>
	 * NB: This is a different range than that set by
	 * {@link Dataset#setChannelMinimum(int, double)} and
	 * {@link Dataset#setChannelMaximum(int, double)}; the latter methods set the
	 * minimum and maximum <em>data</em> values for that channel, independent of
	 * any visualization. They are typically kept synced with the actual data via
	 * code such as {@link ComputeMinMax}.
	 * </p>
	 */
	void setChannelRange(int c, double min, double max);

	/**
	 * Sets the minimum and maximum values of the <em>display</em> range, globally
	 * for all channels.
	 * <p>
	 * NB: This is a different range than that set by
	 * {@link Dataset#setChannelMinimum(int, double)} and
	 * {@link Dataset#setChannelMaximum(int, double)}; the latter methods set the
	 * minimum and maximum <em>data</em> values for that channel, independent of
	 * any visualization. They are typically kept synced with the actual data via
	 * code such as {@link ComputeMinMax}.
	 * </p>
	 */
	void setChannelRanges(double min, double max);

	/**
	 * Autoscales the <em>display</em> range to match the <em>data</em> range, for
	 * the given channel.
	 * <p>
	 * NB: The <em>data</em> range is obtained first from
	 * {@link Dataset#getChannelMinimum(int)} and
	 * {@link Dataset#getChannelMaximum(int)}; if they are not set there, they are
	 * computed and cached for later use.
	 * </p>
	 */
	void autoscale(int c);

	void setComposite(boolean composite);

	List<ColorTable> getColorTables();

	void setColorTable(ColorTable colorTable, int channel);

	void resetColorTables(boolean grayscale);

	ColorMode getColorMode();

	void setColorMode(ColorMode colorMode);

	@Override
	Dataset getData();
	
	/**
	 * @return The current XY slice of the underlying data.
	 */
	RandomAccessibleInterval<? extends RealType<?>> xyPlane();
	
	/**
	 * @param interval - An interval to extract the current XY slice from.
	 * @return The provided interval as a fixed XY slice using the current view position.
	 */
	RandomAccessibleInterval<? extends RealType<?>> xyPlane(RandomAccessibleInterval<? extends RealType<?>> interval);
	
	ColorRGB getColor(ChannelCollection channels);

}
