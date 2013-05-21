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

package imagej.legacy.translate;

import ij.VirtualStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;

/**
 * This class allows a merged color {@link Dataset} to be treated as a
 * {@link VirtualStack} of int[] data.
 * 
 * @author Barry DeZonia
 */
public class MergedRgbVirtualStack extends VirtualStack {

	// -- instance variables --

	private final Dataset ds;
	private final int[] plane;
	private final RandomAccess<? extends RealType<?>> accessor;
	private final ImageProcessor processor;
	private final int w;
	private final int h;
	private final int xAxis;
	private final int yAxis;
	private final int cAxis;
	private final int size;
	private final long[] planeDims;
	private final long[] planePos;
	private final long[] pos;

	// -- MergedRgbVirtualStack methods --

	/**
	 * Constructs a MergedRgbVirtualStack from a {@link Dataset}. The Dataset must
	 * return true when querying isRGBMerged(). Thus the data must have 3 channels
	 * and be backed by unsigned 8-bit data. Additional checking is done for
	 * validity of total plane count (<= Integer.MAX_VALUE) and the size of the
	 * planes (also <= Integer.MAX_VALUE).
	 * 
	 * @param ds The merged color Dataset to wrap
	 */
	public MergedRgbVirtualStack(Dataset ds) {
		if (!ds.isRGBMerged()) {
			throw new IllegalArgumentException("Dataset is not merged color");
		}
		long[] dims = ds.getDims();
		AxisType[] axes = ds.getAxes();
		planeDims = new long[dims.length - 3];
		planePos = new long[dims.length - 3];
		int pDims = 0;
		long sz = 1;
		for (int i = 0; i < axes.length; i++) {
			AxisType at = axes[i];
			if (at.equals(Axes.X)) continue;
			if (at.equals(Axes.Y)) continue;
			if (at.equals(Axes.CHANNEL)) continue;
			sz *= dims[i];
			planeDims[pDims++] = dims[i];
		}
		if (sz > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Dataset has too many planes");
		}
		xAxis = ds.getAxisIndex(Axes.X);
		yAxis = ds.getAxisIndex(Axes.Y);
		cAxis = ds.getAxisIndex(Axes.CHANNEL);
		if (xAxis == -1 || yAxis == -1 || cAxis == -1) {
			throw new IllegalArgumentException("Dataset does not have correct axes");
		}
		if (dims[xAxis] * dims[yAxis] > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("XY dims too large");
		}
		this.ds = ds;
		this.w = (int) dims[xAxis];
		this.h = (int) dims[yAxis];
		this.plane = new int[w * h];
		this.accessor = ds.getImgPlus().randomAccess();
		this.processor = new ColorProcessor(w, h, plane);
		this.size = (int) sz;
		this.pos = new long[dims.length];
	}

	public Dataset getDataset() {
		return ds;
	}

	// -- VirtualStack/ImageStack methods --

	@Override
	public ImageProcessor getProcessor(int n) {
		positionToPlane(n);
		for (int x = 0; x < w; x++) {
			accessor.setPosition(x, xAxis);
			for (int y = 0; y < h; y++) {
				accessor.setPosition(y, yAxis);
				accessor.setPosition(0, cAxis);
				int r = (int) accessor.get().getRealDouble();
				accessor.fwd(cAxis);
				int g = (int) accessor.get().getRealDouble();
				accessor.fwd(cAxis);
				int b = (int) accessor.get().getRealDouble();
				int argb = (255 << 24) | (r << 16) | (g << 8) | b;
				int index = y * w + x;
				plane[index] = argb;
			}
		}
		return processor;
	}

	@Override
	public int getBitDepth() {
		return 24;
	}

	/** Obsolete. Short images are always unsigned. */
	@Override
	public void
		addUnsignedShortSlice(final String sliceLabel, final Object pixels)
	{}

	/** Adds the image in 'ip' to the end of the stack. */
	@Override
	public void addSlice(final String sliceLabel, final ImageProcessor ip) {}

	/**
	 * Adds the image in 'ip' to the stack following slice 'n'. Adds the slice to
	 * the beginning of the stack if 'n' is zero.
	 */
	@Override
	public void addSlice(final String sliceLabel, final ImageProcessor ip,
		final int n)
	{}

	@Override
	public void addSlice(ImageProcessor ip) {}

	@Override
	public void addSlice(String name) {}

	@Override
	public void addSlice(String sliceLabel, Object pixels) {}

	/** Deletes the specified slice, were 1<=n<=nslices. */
	@Override
	public void deleteSlice(final int n) {}

	/** Deletes the last slice in the stack. */
	@Override
	public void deleteLastSlice() {}

	/**
	 * Updates this stack so its attributes, such as min, max, calibration table
	 * and color model, are the same as 'ip'.
	 */
	@Override
	public void update(final ImageProcessor ip) {}

	/** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	@Override
	public Object getPixels(final int n) {
		return getProcessor(n).getPixels();
	}

	/**
	 * Assigns a pixel array to the specified slice, were 1<=n<=nslices.
	 */
	@Override
	public void setPixels(final Object pixels, final int n) {}

	/**
	 * Returns the stack as an array of 1D pixel arrays. Note that the size of the
	 * returned array may be greater than the number of slices currently in the
	 * stack, with unused elements set to null.
	 */
	@Override
	public Object[] getImageArray() {
		return null;
	}

	/**
	 * Returns the slice labels as an array of Strings. Note that the size of the
	 * returned array may be greater than the number of slices currently in the
	 * stack. Returns null if the stack is empty or the label of the first slice
	 * is null.
	 */
	@Override
	public String[] getSliceLabels() {
		return null;
	}

	/**
	 * Returns the label of the specified slice, were 1<=n<=nslices. Returns null
	 * if the slice does not have a label. For DICOM and FITS stacks, labels may
	 * contain header information.
	 */
	@Override
	public String getSliceLabel(final int n) {
		return "" + n;
	}

	/**
	 * Returns a shortened version (up to the first 60 characters or first newline
	 * and suffix removed) of the label of the specified slice. Returns null if
	 * the slice does not have a label.
	 */
	@Override
	public String getShortSliceLabel(final int n) {
		return getSliceLabel(n);
	}

	/** Sets the label of the specified slice, were 1<=n<=nslices. */
	@Override
	public void setSliceLabel(final String label, final int n) {}

	/** Returns true if this is a 3-slice RGB stack. */
	@Override
	public boolean isRGB() {
		return false;
	}

	/** Returns true if this is a 3-slice HSB stack. */
	@Override
	public boolean isHSB() {
		return false;
	}

	/**
	 * Returns true if this is a virtual (disk resident) stack. This method is
	 * overridden by the VirtualStack subclass.
	 */
	@Override
	public boolean isVirtual() {
		return true;
	}

	/** Frees memory by deleting a few slices from the end of the stack. */
	@Override
	public void trim() {}

	/** Returns the number of slices in this stack */
	@Override
	public int getSize() {
		return size;
	}

	@Override
	public void setBitDepth(final int bitDepth) {}

	@Override
	public String getDirectory() {
		return null;
	}

	@Override
	public String getFileName(final int n) {
		return null;
	}

	// -- private helpers --

	private void positionToPlane(int pNum) {
		if (planeDims.length == 0) return; // already there
		IntervalIndexer.indexToPosition(pNum - 1, planeDims, planePos);
		int j = 0;
		for (int i = 0; i < pos.length; i++) {
			if (i == xAxis || i == yAxis || i == cAxis) pos[i] = 0;
			else pos[i] = planePos[j++];
		}
		accessor.setPosition(pos);
	}

}
