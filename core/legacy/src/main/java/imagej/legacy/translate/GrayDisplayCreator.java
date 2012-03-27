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

package imagej.legacy.translate;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.ext.display.DisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Creates {@link ImageDisplay}s from {@link ImagePlus}es containing gray data
 * values.
 * 
 * @author Barry DeZonia
 */
public class GrayDisplayCreator implements DisplayCreator {

	// -- instance variables --

	private final ImageJ context;

	private final GrayPixelHarmonizer pixelHarmonizer;
	private final ColorTableHarmonizer colorTableHarmonizer;
	private final MetadataHarmonizer metadataHarmonizer;
	private final CompositeHarmonizer compositeHarmonizer;
	private final PlaneHarmonizer planeHarmonizer;
	private final OverlayHarmonizer overlayHarmonizer;
	private final PositionHarmonizer positionHarmonizer;

	// NB - OverlayHarmonizer required because IJ1 plugins can hatch displays
	// while
	// avoiding the Harmonizer. Not required in the Display->ImagePlus direction
	// as
	// Harmonizer always catches that case.

	// -- constructor --

	public GrayDisplayCreator(final ImageJ context) {
		this.context = context;
		pixelHarmonizer = new GrayPixelHarmonizer();
		colorTableHarmonizer = new ColorTableHarmonizer(context);
		metadataHarmonizer = new MetadataHarmonizer();
		compositeHarmonizer = new CompositeHarmonizer();
		planeHarmonizer = new PlaneHarmonizer();
		overlayHarmonizer = new OverlayHarmonizer(context);
		positionHarmonizer = new PositionHarmonizer();
	}

	// -- public interface --

	@Override
	public ImageDisplay createDisplay(final ImagePlus imp) {
		return createDisplay(imp, LegacyUtils.getPreferredAxisOrder());
	}

	@Override
	public ImageDisplay createDisplay(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		if (imp.getType() == ImagePlus.COLOR_RGB) return colorCase(imp,
			preferredOrder);
		return grayCase(imp, preferredOrder);
	}

	// -- private interface --

	private ImageDisplay colorCase(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		final Dataset ds = makeGrayDatasetFromColorImp(imp, preferredOrder);
		setDatasetGrayDataFromColorImp(ds, imp);
		metadataHarmonizer.updateDataset(ds, imp);
		compositeHarmonizer.updateDataset(ds, imp);

		final DisplayService displayService =
			context.getService(DisplayService.class);
		// CTR FIXME
		final ImageDisplay display =
			(ImageDisplay) displayService.createDisplay(ds.getName(), ds);

		colorTableHarmonizer.updateDisplay(display, imp);
		overlayHarmonizer.updateDisplay(display, imp);
		positionHarmonizer.updateDisplay(display, imp);

		return display;
	}

	private ImageDisplay grayCase(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		Dataset ds;
		if (preferredOrder[0] == Axes.X && preferredOrder[1] == Axes.Y) {
			ds = makeExactDataset(imp, preferredOrder);
		}
		else {
			ds = makeGrayDatasetFromGrayImp(imp, preferredOrder);
			pixelHarmonizer.updateDataset(ds, imp);
		}
		metadataHarmonizer.updateDataset(ds, imp);
		compositeHarmonizer.updateDataset(ds, imp);

		final DisplayService displayService =
			context.getService(DisplayService.class);
		// CTR FIXME
		final ImageDisplay display =
			(ImageDisplay) displayService.createDisplay(ds.getName(), ds);

		colorTableHarmonizer.updateDisplay(display, imp);
		overlayHarmonizer.updateDisplay(display, imp);
		positionHarmonizer.updateDisplay(display, imp);

		return display;
	}

	/**
	 * Makes a gray {@link Dataset} from a Color {@link ImagePlus} whose channel
	 * count > 1. The Dataset will have isRgbMerged() false, 3 times as many
	 * channels as the input ImagePlus, and bitsperPixel == 8. Does not populate
	 * the data of the returned Dataset. That is left to other utility methods.
	 * Does not set metadata of Dataset. Throws exceptions if input ImagePlus is
	 * not RGB.
	 */
	private Dataset makeGrayDatasetFromColorImp(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{

		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException("this method designed for "
				+ "creating a color Dataset from a multichannel RGB ImagePlus");
		}

		final int[] inputDims = new int[] { x, y, c * 3, z, t };
		final AxisType[] axes = LegacyUtils.orderedAxes(preferredOrder, inputDims);
		final long[] dims = LegacyUtils.orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final DatasetService datasetService =
			context.getService(DatasetService.class);
		final Dataset ds =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		DatasetUtils.initColorTables(ds);

		return ds;
	}

	/**
	 * Assigns the data values of a gray {@link Dataset} from a paired
	 * multichannel color {@link ImagePlus}. Assumes the Dataset and ImagePlus
	 * have compatible dimensions. Gets values via {@link ImageProcessor}::get().
	 * Does not change the Dataset's metadata.
	 */
	private void setDatasetGrayDataFromColorImp(final Dataset ds,
		final ImagePlus imp)
	{
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
							final int value = proc.get(xi, yi);
							final int rValue = (value >> 16) & 0xff;
							final int gValue = (value >> 8) & 0xff;
							final int bValue = (value >> 0) & 0xff;
							accessor.setPosition(ci * 3 + 0, cIndex);
							accessor.get().setReal(rValue);
							accessor.setPosition(ci * 3 + 1, cIndex);
							accessor.get().setReal(gValue);
							accessor.setPosition(ci * 3 + 2, cIndex);
							accessor.get().setReal(bValue);
						}
					}
				}
			}
		}
		ds.update();
	}

	/**
	 * Makes a planar {@link Dataset} whose dimensions match a given
	 * {@link ImagePlus}. Data is exactly the same as plane references are shared
	 * between the Dataset and the ImagePlus. Assumes it will never be called with
	 * any kind of color ImagePlus. Does not set metadata of Dataset.
	 */
	private Dataset makeExactDataset(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		final int[] inputDims = new int[] { x, y, c, z, t };
		final AxisType[] axes = LegacyUtils.orderedAxes(preferredOrder, inputDims);
		final long[] dims = LegacyUtils.orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final DatasetService datasetService =
			context.getService(DatasetService.class);
		final Dataset ds =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		planeHarmonizer.updateDataset(ds, imp);

		DatasetUtils.initColorTables(ds);

		return ds;
	}

	/**
	 * Makes a gray {@link Dataset} from a gray {@link ImagePlus}. Assumes it will
	 * never be given a color RGB Imageplus. Does not populate the data of the
	 * returned Dataset. That is left to other utility methods. Does not set
	 * metadata of Dataset.
	 */
	private Dataset makeGrayDatasetFromGrayImp(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{

		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final int[] inputDims = new int[] { x, y, c, z, t };
		final AxisType[] axes = LegacyUtils.orderedAxes(preferredOrder, inputDims);
		final long[] dims = LegacyUtils.orderedDims(axes, inputDims);
		final String name = imp.getTitle();

		final boolean signed;
		final boolean floating;
		final int bitsPerPixel;

		if (isBinary(imp)) {
			bitsPerPixel = 1;
			signed = false;
			floating = false;
		}
		else { // not binary
			bitsPerPixel = imp.getBitDepth();
			signed = isSigned(imp);
			floating = isFloating(imp);
		}

		final DatasetService datasetService =
			context.getService(DatasetService.class);
		final Dataset ds =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		DatasetUtils.initColorTables(ds);

		return ds;
	}

	/**
	 * Determines whether an ImagePlus is an IJ1 binary image (i.e. it is unsigned
	 * 8 bit data with only values 0 & 255 present)
	 */
	private boolean isBinary(final ImagePlus imp) {
		final int numSlices = imp.getStackSize();
		if (numSlices == 1) return imp.getProcessor().isBinary();
		final ImageStack stack = imp.getStack();
		for (int i = 1; i <= numSlices; i++) {
			if (!stack.getProcessor(i).isBinary()) return false;
		}
		return true;
	}

	/** Returns true if an {@link ImagePlus} is of type GRAY32. */
	private boolean isGray32PixelType(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}

	/** Returns true if an {@link ImagePlus} is backed by a signed type. */
	private boolean isSigned(final ImagePlus imp) {
		// TODO - ignores IJ1's support of signed 16 bit. OK?
		return isGray32PixelType(imp);
	}

	/** Returns true if an {@link ImagePlus} is backed by a floating type. */
	private boolean isFloating(final ImagePlus imp) {
		return isGray32PixelType(imp);
	}
}
