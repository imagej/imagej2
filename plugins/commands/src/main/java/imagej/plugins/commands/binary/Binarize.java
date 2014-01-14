/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.binary;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.autoscale.AutoscaleService;
import imagej.data.autoscale.DataRange;
import imagej.data.display.ImageDisplayService;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.menu.MenuConstants;

import java.util.Arrays;

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
 * Creates or updates a mask {@link Dataset} of type {@link BitType}. Uses an
 * input Dataset that will be thresholded. Thresholding options can be specified
 * which are used to discriminate pixel values. The input Dataset can become the
 * mask if desired.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Binary", mnemonic = 'b'), @Menu(label = "Binarize...") },
	headless = true)
public class Binarize<T extends RealType<T>> extends ContextCommand {

	// TODO - is the following approach even necessary?

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

	@Parameter
	private Dataset inputData;

	@Parameter(persist = false, autoFill = false, required = false)
	private Dataset inputMask = null;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset outputMask = null;

	@Parameter(label = "Threshold method")
	private ThresholdMethod method = null; // TODO: Not String: scriptable?

	@Parameter(label = "Mask pixels", choices = { INSIDE, OUTSIDE })
	private String maskPixels = INSIDE;

	@Parameter(label = "Mask color", choices = { WHITE, BLACK })
	private String maskColor = WHITE;

	@Parameter(label = "Fill mask foreground")
	private boolean fillFg = true;

	@Parameter(label = "Fill mask background")
	private boolean fillBg = true;

	@Parameter(label = "Threshold each plane")
	private boolean thresholdEachPlane = false;

	@Parameter(label = "Change input")
	private boolean changeInput = false;

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
	 * Sets the threshold method to use for pixel discrimination.
	 * 
	 * @param thresholdMethod The name of the threshold method to use.
	 */
	public void setThresholdMethod(ThresholdMethod thresholdMethod) {
		method = thresholdMethod;
	}

	/**
	 * Gets the threshold method used for pixel discrimination.
	 */
	public ThresholdMethod thresholdMethod() {
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

	/**
	 * Sets whether to fill foreground pixels of binary mask or not to the given
	 * specified value.
	 * 
	 * @param val The specified value.
	 */
	public void setFillMaskForeground(boolean val) {
		fillFg = val;
	}

	/**
	 * Gets whether to fill foreground pixels of binary mask or not.
	 */
	public boolean fillMaskForeground() {
		return fillFg;
	}

	/**
	 * Sets whether to fill background pixels of binary mask or not to the given
	 * specified value.
	 * 
	 * @param val The specified value.
	 */
	public void setFillMaskBackground(boolean val) {
		fillBg = val;
	}

	/**
	 * Gets whether to fill background pixels of binary mask or not.
	 */
	public boolean fillMaskBackground() {
		return fillBg;
	}

	/**
	 * Sets the reference input data.
	 * 
	 * @param dataset
	 */
	public void setInputData(Dataset dataset) {
		inputData = dataset;
	}

	/**
	 * Gets the reference input data.
	 */
	public Dataset inputData() {
		return inputData;
	}

	/**
	 * Sets the preexisting mask (if any).
	 */
	public void setInputMask(Dataset dataset) {
		inputMask = dataset;
	}

	/**
	 * Gets the preexisting mask (if any).
	 */
	public Dataset inputMask() {
		return inputMask;
	}

	/**
	 * Gets the output mask.
	 */
	public Dataset outputMask() {
		return outputMask;
	}

	/**
	 * Sets whether to change input data to contain the output mask.
	 */
	public void setChangeInput(boolean val) {
		changeInput = val;
	}

	/**
	 * Gets whether to change input data to contain the output mask.
	 */
	public boolean changeInput() {
		return changeInput;
	}

	/**
	 * Sets the threshold method to the default algorithm.
	 */
	public void setDefaultThresholdMethod() {
		method = threshSrv.getThresholdMethod("Default");
	}

	// -- Command methods --

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		long[] dims = IntervalUtils.getDims(inputData);
		String err = checkInputMask(inputMask, dims);
		if (err != null) {
			cancel(err);
			return;
		}
		CalibratedAxis[] axes = new CalibratedAxis[dims.length];
		inputData.axes(axes);
		AxisType[] types = new AxisType[dims.length];
		for (int i = 0; i < dims.length; i++) {
			types[i] = axes[i].type();
		}
		Dataset mask =
			inputMask != null ? inputMask : datasetSrv.create(new BitType(), dims,
				"Mask", types, isVirtual(inputData));
		mask.setAxes(axes);
		RandomAccess<BitType> maskAccessor =
			(RandomAccess<BitType>) mask.getImgPlus().randomAccess();
		RandomAccess<? extends RealType<?>> dataAccessor =
			inputData.getImgPlus().randomAccess();
		DataRange minMax = calcDataRange(inputData);
		Histogram1d<T> histogram = null;
		boolean testLess = maskPixels.equals(INSIDE);
		DoubleType val = new DoubleType();
		// TODO - use Views and Cursors instead of PointSets and RandomAccess?
		// Better performance? Especially for CellImgs?
		if (thresholdEachPlane && planeCount(inputData) > 1) {
			// threshold each plane separately
			long[] planeSpace = planeSpace(inputData);
			PointSetIterator pIter = new HyperVolumePointSet(planeSpace).iterator();
			while (pIter.hasNext()) {
				long[] planePos = pIter.next();
				histogram = buildHistogram(inputData, planePos, minMax, histogram);
				double cutoffVal = cutoff(histogram, method, testLess, val);
				PointSet planeData = planeData(inputData, planePos);
				PointSetIterator iter = planeData.iterator();
				while (iter.hasNext()) {
					updateMask(iter.next(), testLess, cutoffVal, dataAccessor,
						maskAccessor);
				}
			}
		}
		else { // threshold entire dataset once
			histogram = buildHistogram(inputData, null, minMax, null);
			double cutoffVal = cutoff(histogram, method, testLess, val);
			PointSet fullData = fullData(dims);
			PointSetIterator iter = fullData.iterator();
			while (iter.hasNext()) {
				updateMask(iter.next(), testLess, cutoffVal, dataAccessor, maskAccessor);
			}
		}
		assignColorTables(mask);
		if (changeInput) {
			// TODO - should inputData be ItemIO.BOTH????
			inputData.setImgPlus(mask.getImgPlus());
		}
		else outputMask = mask;
	}

	// -- initializer --

	@SuppressWarnings("unused")
	private void init() {
		setDefaultThresholdMethod();
	}

	// -- helpers --

	// returns true if a given dataset is stored in a CellImg structure

	private boolean isVirtual(Dataset ds) {
		final Img<?> img = ds.getImgPlus().getImg();
		return AbstractCellImg.class.isAssignableFrom(img.getClass());
	}

	// gets the range of the pixel values in a dataset

	private DataRange calcDataRange(Dataset ds) {
		return autoscaleSrv.getDefaultIntervalRange(ds.getImgPlus());
	}

	// returns the number of planes in a dataset

	private long planeCount(Dataset ds) {
		long count = 1;
		for (int d = 0; d < ds.numDimensions(); d++) {
			AxisType type = ds.axis(d).type();
			if (type == Axes.X || type == Axes.Y) continue;
			count *= ds.dimension(d);
		}
		return count;
	}

	// returns the dimensions of the space that contains the planes of a dataset.
	// this is the dataset dims minus the X and Y axes.

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

	// calculates the histogram of a portion of the dataset. if planePos is null
	// the region is the entire dataset. Otherwise it is the single plane.

	private Histogram1d<T> buildHistogram(Dataset ds, long[] planePos,
		DataRange minMax, Histogram1d<T> existingHist)
	{
		long[] min = new long[ds.numDimensions()];
		long[] max = min.clone();
		int xIndex = ds.dimensionIndex(Axes.X);
		int yIndex = ds.dimensionIndex(Axes.Y);
		// TODO - figure out what to do when no X axis or no Y axis present
		int i = 0;
		for (int d = 0; d < ds.numDimensions(); d++) {
			if (planePos == null || d == xIndex || d == yIndex) {
				min[d] = 0;
				max[d] = ds.dimension(d) - 1;
			}
			else {
				// it's a plane dimension from within planePos
				min[d] = planePos[i];
				max[d] = planePos[i];
				i++;
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

	// allocates a histogram after determining a good size

	private Histogram1d<T> allocateHistogram(boolean dataIsIntegral,
		DataRange dataRange)
	{
		double range = dataRange.getExtent();
		if (dataIsIntegral) range++;
		Real1dBinMapper<T> binMapper = null;
		// TODO - size of histogram affects speed of all autothresh methods
		// What is the best way to determine size?
		// Do we want some power of two as size? For now yes.
		final int maxBinCount = 16384;
		for (int binCount = 256; binCount <= maxBinCount; binCount *= 2) {
			if (range <= binCount) {
				binMapper =
					new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
						binCount, false);
				break;
			}
		}
		if (binMapper == null) {
			binMapper =
				new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
					maxBinCount, false);
		}
		return new Histogram1d<T>(binMapper);
	}

	// determines the data value that delineates the threshold point

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private double cutoff(Histogram1d<T> hist, ThresholdMethod thresholdMethod,
		boolean testLess, DoubleType val)
	{
		long threshIndex = thresholdMethod.getThreshold(hist);
		if (testLess) hist.getUpperBound(threshIndex, (T) (RealType) val);
		else hist.getLowerBound(threshIndex, (T) (RealType) val);
		return val.getRealDouble();
	}

	// updates the mask pixel values for which the data values are on the correct
	// side of the cutoff value.

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
			if (fillFg) {
				maskAccessor.setPosition(pos);
				maskAccessor.get().set(true);
			}
		}
		else { // not part of mask
			if (fillBg) {
				maskAccessor.setPosition(pos);
				maskAccessor.get().set(false);
			}
		}
	}

	// returns a PointSet that represents the points in a plane of a dataset

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

	// returns a PointSet that represents all the points in a dataset

	private PointSet fullData(long[] dims) {
		return new HyperVolumePointSet(dims);
	}

	// sets each dataset plane's color table

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

	// A color table where 0 pixels are black and all others are white

	private ColorTable8 white() {
		return makeTable(0, 255);
	}

	// A color table where 0 pixels are white and all others are black

	private ColorTable8 black() {
		return makeTable(255, 0);
	}

	// fills the first value of a CoplorTable with given value and sets the rest
	// values with the other given value.

	private ColorTable8 makeTable(int first, int rest) {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		byte n;
		n = (byte) rest;
		Arrays.fill(r, n);
		Arrays.fill(g, n);
		Arrays.fill(b, n);
		n = (byte) first;
		r[0] = n;
		g[0] = n;
		b[0] = n;
		return new ColorTable8(r, g, b);
	}

	// checks for incompatibilities between input data and input mask

	private String checkInputMask(Dataset mask, long[] dims) {

		// if no mask specified then nothing to check
		if (mask == null) return null;

		// check that mask is of type BitType
		if (!(mask.getImgPlus().firstElement() instanceof BitType))
		{
			return "Mask is not a binary image";
		}

		// check that passed in mask is correct size
		for (int d = 0; d < dims.length; d++) {
			long dim = dims[d];
			if (mask.dimension(d) != dim) {
				return "Mask shape not same as input data";
			}
		}

		// no errors found
		return null;
	}
}
