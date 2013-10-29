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

package imagej.core.commands.binary;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.autoscale.AutoscaleService;
import imagej.data.autoscale.DataRange;
import imagej.data.display.ImageDisplayService;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable8;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.IntervalUtils;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Changes a {@link Dataset} to be of type {@link BitType}. Thresholding options
 * can be specified which are used to discriminate pixel values.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Binary", mnemonic = 'b'), @Menu(label = "Make Binary") },
	headless = true)
public class MakeBinary<T extends RealType<T>> extends DynamicCommand {

	// TODO - this is a DynamicCommand so that thresh methods can be discovered
	// at runtime. That is all. I don't think this will interfere with headless
	// operation. One just sets the method parameter to one that exists (which can
	// be enumerated if needed from the AutothresholdService). So this should
	// allow a headless hook. We will need to test this.

	// NB - to simplify headless operation this plugin uses primitives for its
	// @Parameter fields. Using enums might be safer but complicates headless
	// operation from a script for instance.

	// -- constants --
	
	public static final String INSIDE = "Inside threshold";
	public static final String OUTSIDE = "Outside threshold";
	public static final String WHITE = "White";
	public static final String BLACK = "Black";
	public static final String DEFAULT_METHOD = "Default";
	
		// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Threshold method")
	private String method = DEFAULT_METHOD;

	@Parameter(label = "Mask pixels", choices = { INSIDE, OUTSIDE })
	private String maskPixels = INSIDE;

	@Parameter(label = "Mask color", choices = { WHITE, BLACK })
	private String maskColor = WHITE;

	@Parameter(label = "Threshold each plane")
	private boolean thresholdEachPlane = false;

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private ImageDisplayService imgDispSrv;

	@Parameter
	private DatasetService datasetSrv;

	@Parameter
	private AutoscaleService autoscaleSrv;

	// -- accessors --

	/**
	 * Sets the threshold method to use for pixel discrimination. The method is
	 * given as the name of a {@link ThresholdMethod} currently registered with
	 * the {@link ThresholdService}.
	 * 
	 * @param thresholdMethod The name of the threshold method to use.
	 */
	public void setThresholdMethod(String thresholdMethod) {
		if (threshSrv.getThresholdMethod(thresholdMethod) == null) {
			throw new IllegalArgumentException(
				"Unknown threshold method specification: " + thresholdMethod);
		}
		method = thresholdMethod;
	}

	/**
	 * Gets the threshold method used for pixel discrimination. The method is the
	 * name of a {@link ThresholdMethod} currently registered with the
	 * {@link ThresholdService}.
	 */
	public String thresholdMethod() {
		return method;
	}

	/**
	 * Sets which pixels are considered part of the mask. Those either inside or
	 * outside the threshold range. Use the constants MakeBinary.INSIDE or
	 * MakeBinary.OUTSIDE.
	 * 
	 * @param insideOrOutside One of the values INSIDE or OUTSIDE.
	 */
	public void setMaskPixels(String insideOrOutside) {
		if (insideOrOutside.equals(INSIDE)) maskPixels = INSIDE;
		else if (insideOrOutside.equals(OUTSIDE)) maskPixels = OUTSIDE;
		else throw new IllegalArgumentException(
			"Unknown mask pixel specification: " + insideOrOutside);
	}

	/**
	 * Gets which pixels are considered part of the mask. Either inside or outside
	 * the threshold range. Returns one of the constants MakeBinary.INSIDE or
	 * MakeBinary.OUTSIDE.
	 */
	public String maskPixels() {
		return maskPixels;
	}

	/**
	 * Sets the color of the mask pixels. Either black or white. Use the constants
	 * MakeBinary.BLACK or MakeBinary.WHITE.
	 * 
	 * @param blackOrWhite One of the values BLACK or WHITE.
	 */
	public void setMaskColor(String blackOrWhite) {
		if (blackOrWhite.equals(BLACK)) maskColor = BLACK;
		else if (blackOrWhite.equals(WHITE)) maskColor = WHITE;
		else throw new IllegalArgumentException(
			"Unknown mask color specification: " + blackOrWhite);
	}

	/**
	 * Gets the color of the mask pixels. Either black or white. One of the
	 * constants MakeBinary.BLACK or MakeBinary.WHITE.
	 */
	public String maskColor() {
		return maskColor;
	}

	/**
	 * Set whether to threshold each plane separately or to threshold the whole
	 * image at once.
	 */
	public void setThresholdEachPlane(boolean val) {
		thresholdEachPlane = val;
	}

	/**
	 * Gets whether to threshold each plane separately or to threshold the whole
	 * image at once.
	 */
	public boolean thresholdEachPlane() {
		return thresholdEachPlane;
	}

	// -- Command methods --

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		long[] dims = IntervalUtils.getDims(dataset);
		CalibratedAxis[] axes = new CalibratedAxis[dims.length];
		dataset.axes(axes);
		AxisType[] types = new AxisType[dims.length];
		for (int i = 0; i < dims.length; i++) {
			types[i] = axes[i].type();
		}
		Dataset mask =
			datasetSrv.create(new BitType(), dims, dataset.getName(), types,
				isVirtual(dataset));
		RandomAccess<BitType> maskAccessor =
			(RandomAccess<BitType>) mask.getImgPlus().randomAccess();
		RandomAccess<? extends RealType<?>> dataAccessor =
			dataset.getImgPlus().randomAccess();
		DataRange minMax = calcDataRange(dataset);
		ThresholdMethod thresholdMethod = threshSrv.getThresholdMethod(method);
		Histogram1d<T> histogram = null;
		boolean testLess = maskPixels.equals(INSIDE);
		DoubleType val = new DoubleType();
		if (thresholdEachPlane && planeCount(dataset) > 1) {
			// threshold each plane separately
			long[] planeSpace = planeSpace(dataset);
			PointSetIterator pIter = new HyperVolumePointSet(planeSpace).iterator();
			while (pIter.hasNext()) {
				long[] planePos = pIter.next();
				histogram = buildHistogram(dataset, planePos, minMax, histogram);
				double cutoffVal = cutoff(histogram, thresholdMethod, testLess, val);
				PointSet planeData = planeData(dataset, planePos);
				PointSetIterator iter = planeData.iterator();
				while (iter.hasNext()) {
					updateMask(iter.next(), testLess, cutoffVal, dataAccessor,
						maskAccessor);
				}
			}
		}
		else { // threshold entire dataset once
			histogram = buildHistogram(dataset, null, minMax, null);
			double cutoffVal = cutoff(histogram, thresholdMethod, testLess, val);
			PointSet fullData = fullData(dims);
			PointSetIterator iter = fullData.iterator();
			while (iter.hasNext()) {
				updateMask(iter.next(), testLess, cutoffVal, dataAccessor, maskAccessor);
			}
		}
		dataset.setImgPlus(mask.getImgPlus());
		dataset.setAxes(axes);
		assignColorTables(dataset);
	}
	// -- initializer --

	@SuppressWarnings({ "unused", "unchecked" })
	private void init() {
		MutableModuleItem<String> item =
			(MutableModuleItem<String>) getInfo().getInput("method");
		item.setChoices(threshSrv.getThresholdMethodNames());
	}

	// -- helpers --

	private boolean isVirtual(Dataset ds) {
		final Img<?> img = ds.getImgPlus().getImg();
		return AbstractCellImg.class.isAssignableFrom(img.getClass());
	}

	private DataRange calcDataRange(Dataset ds) {
		return autoscaleSrv.getDefaultIntervalRange(ds.getImgPlus());
	}

	private long planeCount(Dataset ds) {
		long count = 1;
		for (int d = 0; d < ds.numDimensions(); d++) {
			AxisType type = ds.axis(d).type();
			if (type == Axes.X || type == Axes.Y) continue;
			count *= ds.dimension(d);
		}
		return count;
	}

	long[] planeSpace(Dataset ds) {
		long[] planeSpace = new long[ds.numDimensions() - 2];
		int i = 0;
		for (int d = 0; d < ds.numDimensions(); d++) {
			AxisType type = ds.axis(d).type();
			if (type == Axes.X || type == Axes.Y) continue;
			planeSpace[i++] = ds.dimension(d);
		}
		return planeSpace;
	}

	private Histogram1d<T> buildHistogram(Dataset ds, long[] planePos,
		DataRange minMax, Histogram1d<T> existingHist)
	{
		long[] min = new long[ds.numDimensions()];
		long[] max = min.clone();
		max[0] = ds.dimension(0) - 1;
		max[1] = ds.dimension(1) - 1;
		for (int d = 2; d < ds.numDimensions(); d++) {
			if (planePos == null) {
				min[d] = 0;
				max[d] = ds.dimension(d) - 1;
			}
			else { // plane only
				min[d] = planePos[d - 2];
				max[d] = planePos[d - 2];
			}
		}
		@SuppressWarnings("unchecked")
		Img<T> img = (Img<T>) ds.getImgPlus();
		IntervalView<T> view = Views.interval(img, min, max);
		IterableInterval<T> data = Views.iterable(view);
		final Histogram1d<T> histogram;
		if (existingHist == null) {
			histogram = allocateHistogram(ds.isInteger(), minMax);
		}
		else {
			existingHist.resetCounters();
			histogram = existingHist;
		}
		histogram.countData(data);
		return histogram;
	}

	private Histogram1d<T> allocateHistogram(boolean dataIsIntegral,
		DataRange dataRange)
	{
		double range = dataRange.getExtent();
		if (dataIsIntegral) range++;
		Real1dBinMapper<T> binMapper = null;
		// TODO - size of histogram affects speed of all autothresh methods
		// What is the best way to determine size?
		// Do we want some power of two as size? For now yes.
		final int MaxBinCount = 16384;
		for (int binCount = 256; binCount <= MaxBinCount; binCount *= 2) {
			if (range <= binCount) {
				binMapper =
					new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
						binCount,
						false);
				break;
			}
		}
		if (binMapper == null) {
			binMapper =
				new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
					MaxBinCount,
					false);
		}
		return new Histogram1d<T>(binMapper);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private double cutoff(Histogram1d<T> hist, ThresholdMethod thresholdMethod,
		boolean testLess, DoubleType val)
	{
		long threshIndex = thresholdMethod.getThreshold(hist);
		if (testLess) hist.getUpperBound(threshIndex, (T) (RealType) val);
		else hist.getLowerBound(threshIndex, (T) (RealType) val);
		return val.getRealDouble();
	}

	private void updateMask(long[] pos, boolean testLess, double cutoffVal,
		RandomAccess<? extends RealType<?>> dataAccessor,
		RandomAccess<BitType> maskAccessor)
	{
		dataAccessor.setPosition(pos);
		boolean partOfMask;
		if (testLess) {
			partOfMask = dataAccessor.get().getRealDouble() <= cutoffVal;
		}
		else { // test greater
			partOfMask = dataAccessor.get().getRealDouble() >= cutoffVal;
		}
		if (partOfMask) {
			maskAccessor.setPosition(pos);
			maskAccessor.get().set(true);
		}
	}

	private PointSet planeData(Dataset ds, long[] planePos) {
		long[] pt1 = new long[ds.numDimensions()];
		long[] pt2 = new long[ds.numDimensions()];
		int i = 0;
		for (int d = 0; d < ds.numDimensions(); d++) {
			AxisType type = ds.axis(d).type();
			if (type == Axes.X || type == Axes.Y) {
				pt1[d] = 0;
				pt2[d] = ds.dimension(d) - 1;
			}
			else {
				pt1[d] = planePos[i];
				pt2[d] = planePos[i];
				i++;
			}
		}
		return new HyperVolumePointSet(pt1, pt2);
	}

	private PointSet fullData(long[] dims) {
		return new HyperVolumePointSet(dims);
	}

	private void assignColorTables(Dataset ds) {
		ColorTable8 table = maskColor.equals(WHITE) ? white() : black();
		long planeCount = planeCount(ds);
		if (planeCount > Integer.MAX_VALUE) {
			// TODO: for now just set all color tables. Later: throw exception?
			planeCount = Integer.MAX_VALUE;
		}
		ds.initializeColorTables((int) planeCount);
		for (int i = 0; i < planeCount; i++) {
			ds.setColorTable(table, i);
		}
	}

	private ColorTable8 white() {
		return makeTable(0, 255);
	}

	private ColorTable8 black() {
		return makeTable(255, 0);
	}

	private ColorTable8 makeTable(int first, int rest) {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		byte n;
		n = (byte) rest;
		fill(r, n);
		fill(g, n);
		fill(b, n);
		n = (byte) first;
		r[0] = n;
		g[0] = n;
		b[0] = n;
		return new ColorTable8(r, g, b);
	}

	private void fill(byte[] bytes, byte val) {
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = val;
	}
}
