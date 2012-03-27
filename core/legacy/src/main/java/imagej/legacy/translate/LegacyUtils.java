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
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.macro.Interpreter;
import imagej.data.Dataset;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * A bag of static methods used throughout the translation layer
 * 
 * @author Barry DeZonia
 */
public class LegacyUtils {

	// -- static variables --

	private final static AxisType[] defaultAxes = new AxisType[] { Axes.X,
		Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };

	// -- public static methods --

	// TODO - deleteImagePlus() could be better located in some other class.
	// It's use of an ImagePlus does not fit in with theme of other methods
	// in this class.

	/**
	 * Modifies IJ1 data structures so that there are no dangling references to an
	 * obsolete ImagePlus.
	 */
	public static void deleteImagePlus(final ImagePlus imp) {
		final ImagePlus currImagePlus = WindowManager.getCurrentImage();
		if (imp == currImagePlus) WindowManager.setTempCurrentImage(null);
		final ImageWindow ij1Window = imp.getWindow();
		if (ij1Window == null) Interpreter.removeBatchModeImage(imp);
		else {
			imp.changes = false;
			if (!ij1Window.isClosed()) ij1Window.close();
		}
	}

	/**
	 * Returns true if any of the given Axes cannot be represented in an IJ1
	 * ImagePlus.
	 */
	static boolean hasNonIJ1Axes(final AxisType[] axes) {
		for (final AxisType axis : axes) {
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.CHANNEL) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			return true;
		}
		return false;
	}

	/**
	 * Returns the number of channels required in IJ1 to represent all the axes of
	 * an IJ2 Dataset. Incompatible IJ2 axes are encoded as extra channels in IJ1.
	 */
	static long ij1ChannelCount(final long[] dims, final AxisType[] axes) {
		long cCount = 1;
		int axisIndex = 0;
		for (final AxisType axis : axes) {
			final long axisSize = dims[axisIndex++];
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			cCount *= axisSize;
		}
		return cCount;
	}

	/**
	 * Determines if a Dataset's dimensions cannot be represented within an IJ1
	 * ImageStack. Returns true if the Dataset does not have X or Y axes. Returns
	 * true if the XY plane size is greater than Integer.MAX_VALUE. Returns true
	 * if the number of planes is greater than Integer.MAX_VALUE.
	 */
	public static boolean dimensionsIJ1Compatible(final Dataset ds) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] dims = ds.getDims();

		final long xCount = xIndex < 0 ? 1 : dims[xIndex];
		final long yCount = yIndex < 0 ? 1 : dims[yIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];

		final long cCount = LegacyUtils.ij1ChannelCount(dims, ds.getAxes());
		final long ij1ChannelCount = ds.isRGBMerged() ? (cCount / 3) : cCount;

		// check width exists
		if (xIndex < 0) return false;

		// check height exists
		if (yIndex < 0) return false;

		// check plane size not too large
		if ((xCount * yCount) > Integer.MAX_VALUE) return false;

		// check number of planes not too large
		if (ij1ChannelCount * zCount * tCount > Integer.MAX_VALUE) return false;

		return true;
	}

	// -- package access static methods --

	static AxisType[] getPreferredAxisOrder() {
		return defaultAxes;
	}

	/**
	 * Makes a set of axes in a preferred order. The preferred order may not
	 * include all 5 default axes. This method always returns axes populated with
	 * X, Y, and any other nontrivial dimensions. Output axes are filled in the
	 * preferred order and then unspecified axes of nontrivial dimension are
	 * concatenated in default order
	 */
	static AxisType[] orderedAxes(final AxisType[] preferredOrder,
		final int[] fullDimensions)
	{
		int dimCount = 0;
		for (int i = 0; i < fullDimensions.length; i++) {
			if (defaultAxes[i] == Axes.X || defaultAxes[i] == Axes.Y ||
//				defaultAxes[i] == Axes.CHANNEL ||
				getDim(defaultAxes[i], fullDimensions) > 1)
			{
				dimCount++;
			}
		}
		final AxisType[] axes = new AxisType[dimCount];
		int index = 0;
		for (final AxisType axis : preferredOrder) {
			for (final AxisType other : defaultAxes) {
				if (axis == other) {
					if (axis == Axes.X || axis == Axes.Y ||
//						axis == Axes.CHANNEL ||
						getDim(axis, fullDimensions) > 1)
					{
						axes[index++] = axis;
					}
					break;
				}
			}
		}
		for (final AxisType axis : defaultAxes) {
			boolean present = false;
			for (final AxisType other : preferredOrder) {
				if (axis == other) {
					present = true;
					break;
				}
			}
			if (!present) {
				if (axis == Axes.X || axis == Axes.Y ||
//					axis == Axes.CHANNEL ||
					getDim(axis, fullDimensions) > 1)
				{
					axes[index++] = axis;
				}
			}
		}
		return axes;
	}

	/**
	 * makes a set of dimensions in a given Axis order. Assumes that all
	 * nontrivial dimensions have already been prescreened to be included
	 */
	static long[] orderedDims(final AxisType[] axes, final int[] fullDimensions)
	{
		final long[] orderedDims = new long[axes.length];
		int index = 0;
		for (final AxisType axis : axes) {
			orderedDims[index++] = getDim(axis, fullDimensions);
		}
		return orderedDims;
	}

	/**
	 * tests that a given {@link Dataset} can be represented as a color
	 * {@link ImagePlus}. Some of this test maybe overkill if by definition
	 * rgbMerged can only be true if channels == 3 and type = ubyte are also true.
	 */
	static boolean isColorCompatible(final Dataset ds) {
		if (!ds.isRGBMerged()) return false;
		if (!ds.isInteger()) return false;
		if (ds.isSigned()) return false;
		if (ds.getType().getBitsPerPixel() != 8) return false;
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (cIndex < 0) return false;
		if (ds.getImgPlus().dimension(cIndex) % 3 != 0) return false;
		return true;
	}

	/**
	 * Copies a {@link Dataset}'s dimensions and axis indices into provided
	 * arrays. The order of dimensions is formatted to be X,Y,C,Z,T. If an axis is
	 * not present in the Dataset its value is set to 1 and its index is set to
	 * -1. Combines all non XYZT axis dimensions into multiple C dimensions.
	 */
	static void getImagePlusDims(final Dataset dataset,
		final int[] outputIndices, final int[] outputDims)
	{
		final long[] dims = dataset.getDims();

		final AxisType[] axes = dataset.getAxes();

		// get axis indices
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);

		final long xCount = xIndex < 0 ? 1 : dims[xIndex];
		final long yCount = yIndex < 0 ? 1 : dims[yIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];
		final long cCount = ij1ChannelCount(dims, axes);

		// NB - cIndex tells what dimension is channel in Dataset. For a
		// Dataset that encodes other axes as channels this info is not so
		// useful. But for Datasets that can be represented exactly it is.
		// So pass along info but API consumers must be careful to not make
		// assumptions.

		// set output values : indices
		outputIndices[0] = xIndex;
		outputIndices[1] = yIndex;
		outputIndices[2] = cIndex;
		outputIndices[3] = zIndex;
		outputIndices[4] = tIndex;

		// set output values : dimensions
		outputDims[0] = (int) xCount;
		outputDims[1] = (int) yCount;
		outputDims[2] = (int) cCount;
		outputDims[3] = (int) zCount;
		outputDims[4] = (int) tCount;
	}

	/**
	 * Throws an Exception if the planes of a Dataset are not compatible with IJ1.
	 */
	static void assertXYPlanesCorrectlyOriented(final int[] dimIndices) {
		if (dimIndices[0] != 0) {
			throw new IllegalArgumentException(
				"Dataset does not have X as the first axis");
		}
		if (dimIndices[1] != 1) {
			throw new IllegalArgumentException(
				"Dataset does not have Y as the second axis");
		}
	}

	/**
	 * Returns true if a {@link Dataset} can be represented by reference in IJ1.
	 */
	static boolean datasetIsIJ1Compatible(final Dataset ds) {
		if (ds == null) return true;
		final AxisType[] axes = ds.getAxes();
		if (LegacyUtils.hasNonIJ1Axes(axes)) return false;
		return ij1StorageCompatible(ds) && ij1TypeCompatible(ds);
	}

	/**
	 * Fills IJ1 incompatible indices of a position array. The channel from IJ1 is
	 * rasterized into potentially multiple indices in IJ2's position array. For
	 * instance an IJ2 image with CHANNELs and SPECTRA gets encoded with multiple
	 * channels in IJ1. When coming back from IJ1 need to rasterize the single
	 * IJ1 channel position back into (CHANNEL,SPECTRA) pairs.
	 * 
	 * @param dims - the dimensions of the IJ2 Dataset
	 * @param axes - the axes labels that match the Dataset dimensions
	 * @param ij1Channel - the channel value in IJ1 (to be decoded for IJ2)
	 * @param pos - the position array to fill with rasterized values
	 */
	static void fillChannelIndices(final long[] dims, final AxisType[] axes,
		final long ij1Channel, final long[] pos)
	{
		long workingIndex = ij1Channel;
		for (int i = 0; i < dims.length; i++) {
			final AxisType axis = axes[i];
			// skip axes we don't encode as channels
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			// calc index of encoded channels
			final long subIndex = workingIndex % dims[i];
			pos[i] = subIndex;
			workingIndex = workingIndex / dims[i];
		}
	}
	
	static long calcIJ1ChannelPos(long[] dims, AxisType[] axes, long[] pos) {
		long multiplier = 1;
		long ij1Pos = 0;
		for (int i = 0; i < axes.length; i++) {
			AxisType axis = axes[i];
			// skip axes we don't encode as channels
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			ij1Pos += multiplier * pos[i];
			multiplier *= dims[i];
		}
		return ij1Pos;
	}
	
	// -- private helper methods --

	/**
	 * Gets a dimension for a given axis from a list of dimensions in XYCZT order.
	 */
	private static int getDim(final AxisType axis, final int[] fullDimensions) {
		if (axis == Axes.X) return fullDimensions[0];
		else if (axis == Axes.Y) return fullDimensions[1];
		else if (axis == Axes.CHANNEL) return fullDimensions[2];
		else if (axis == Axes.Z) return fullDimensions[3];
		else if (axis == Axes.TIME) return fullDimensions[4];
		else throw new IllegalArgumentException(
			"incompatible dimension type specified");
	}

	/** Returns true if a {@link Dataset} is backed by {@link PlanarAccess}. */
	private static boolean ij1StorageCompatible(final Dataset ds) {
		return ds.getImgPlus().getImg() instanceof PlanarAccess<?>;
	}

	/**
	 * Returns true if a {@link Dataset} has a type that can be directly
	 * represented in an IJ1 ImagePlus.
	 */
	private static boolean ij1TypeCompatible(final Dataset ds) {
		final RealType<?> type = ds.getType();
		final int bitsPerPix = type.getBitsPerPixel();
		final boolean integer = ds.isInteger();
		final boolean signed = ds.isSigned();

		Object plane;
		if ((bitsPerPix == 8) && !signed && integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof byte[]) return true;
		}
		else if ((bitsPerPix == 16) && !signed && integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof short[]) return true;
		}
		else if ((bitsPerPix == 32) && signed && !integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof float[]) return true;
		}
		return false;
	}

}
