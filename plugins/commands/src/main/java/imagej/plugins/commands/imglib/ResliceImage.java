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

package imagej.plugins.commands.imglib;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Updates dimensions and data values of an image {@link Dataset}. The user
 * supplies spacings for each dimension. The new data is the result of slicing
 * the original image at specified spacings. When spacings are not equal to
 * integral spacings the resulting data has different dimensions. The output
 * data is computed using settable interpolation methods.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", headless = true, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Stacks", mnemonic = 't'),
	@Menu(label = "Reslice...", mnemonic = 't') })
public class ResliceImage<T extends RealType<T>> extends ContextCommand {

	// -- constants --

	private static final String LANCZOS = "Lanczos";
	private static final String LINEAR = "Linear";
	private static final String NEAREST_NEIGHBOR = "Nearest Neighbor";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Origin", persist = false)
	private String originString;

	@Parameter(label = "Spacings", persist = false)
	private String spacingsString;

	@Parameter(label = "Interpolation", choices = { LINEAR, NEAREST_NEIGHBOR,
		LANCZOS }, persist = false)
	private String method = LINEAR;

	@Parameter(label = "Use user units", persist = false)
	private boolean useUserUnits;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private StatusService statusService;

	// -- non-parameter fields --

	private String err = null;

	private List<Long> origin = new ArrayList<Long>();

	private List<Double> spacings = new ArrayList<Double>();

	// -- constructors --

	public ResliceImage() {}

	// -- accessors --

	/**
	 * Sets the Dataset that the reslice operation will be run upon.
	 */
	public void setDataset(Dataset ds) {
		dataset = ds;
		init();
	}

	/**
	 * Gets the Dataset that the reslice operation will be run upon.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets the origin point of the reslicing for the user specified axis to the
	 * smallest value of the axis.
	 */
	public void setOriginMin(int d) {
		origin.set(d, 0L);
		originString = originString();
	}

	/**
	 * Returns true if the origin point of the reslicing for the user specified
	 * axis is to be the smallest value of the axis.
	 */
	public boolean originMin(int d) {
		return origin.get(d) == 0;
	}

	/**
	 * Sets the origin point of the reslicing for the user specified axis to the
	 * largest value of the axis.
	 */
	public void setOriginMax(int d) {
		origin.set(d, dataset.dimension(d) - 1);
		originString = originString();
	}

	/**
	 * Returns true if the origin point of the reslicing for the user specified
	 * axis is to be the largest value of the axis.
	 */
	public boolean originMax(int d) {
		return origin.get(d) != 0;
	}

	/**
	 * Sets the reslice spacing for a given axis to a given value. The value is
	 * interpreted as data units or user units based upon the value returned by
	 * getUseUserUnits().
	 */
	public void setSpacing(int d, double spacing) {
		if (d < 0 || d >= spacings.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (spacings.size() - 1) + ")");
		}
		spacings.set(d, spacing);
		spacingsString = spacingsString();
	}

	/**
	 * Gets the reslice spacing for a given axis. The value is interpreted as data
	 * units or user units based upon the value returned by getUseUserUnits().
	 */
	public double getSpacing(int d) {
		if (d < 0 || d >= spacings.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (spacings.size() - 1) + ")");
		}
		return spacings.get(d);
	}

	// TODO - have a set method and get method that take a InterpFactory. This
	// allows more flexibility on how data is combined.

	/**
	 * Sets the interpolation method used to combine pixels. Use the constant
	 * strings that are exposed by this class.
	 */
	public void setInterpolationMethod(String str) {
		if (str.equals(LINEAR)) method = LINEAR;
		else if (str.equals(NEAREST_NEIGHBOR)) method = NEAREST_NEIGHBOR;
		else if (str.equals(LANCZOS)) method = LANCZOS;
		else throw new IllegalArgumentException("Unknown interpolation method: " +
			str);
	}

	/**
	 * Gets the interpolation method used to combine pixels. One of the constant
	 * strings that are exposed by this class.
	 */
	public String getInterpolationMethod() {
		return method;
	}

	/**
	 * Sets whether reslice spacing field values are in user units or pixel units.
	 * If input val is true then use user units else use pixel units.
	 */
	public void setUseUserUnits(boolean val) {
		useUserUnits = val;
	}

	/**
	 * Gets whether reslice spacing field values are in user units or pixel units.
	 * Returns true if currently in user units and false if currently in pixel
	 * units.
	 */
	public boolean useUserUnits() {
		return useUserUnits;
	}

	/**
	 * Returns the current error message if any.
	 */
	public String getError() {
		return err;
	}

	// -- Command methods --

	@Override
	public void run() {
		List<Long> orig = parseOrigin(dataset, originString);
		if (orig == null) {
			cancel(err);
			return;
		}
		List<Double> spaces = parseSpacings(dataset, spacingsString);
		if (spaces == null) {
			cancel(err);
			return;
		}
		if (useUserUnits) toPixelUnits(dataset, spaces);
		statusService.showStatus("Resampling data ...");
		resampleData(dataset, orig, spaces);
		statusService.showStatus("Done.");
	}

	// -- initializers --

	protected void init() {
		origin.clear();
		spacings.clear();
		for (int i = 0; i < dataset.numDimensions(); i++) {
			origin.add(0L);
			spacings.add(1.0);
		}
		originString = originString();
		spacingsString = spacingsString();
	}

	// -- helpers --

	private List<Long> parseOrigin(Dataset ds, String spec) {
		if (spec == null) {
			err = "Origin specification string is null.";
			return null;
		}
		String[] terms = spec.split(",");
		if (terms.length == 0) {
			err = "Origin specification string is empty.";
			return null;
		}
		List<Long> orig = new ArrayList<Long>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			orig.add(0L);
		}
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim();
			String[] parts = term.split("=");
			if (parts.length != 2) {
				err =
					"Err in origin specification string: each subentry must be"
						+ " a number and then an '=' sign, and then either"
						+ " 'min' or 'max'.";
				return null;
			}
			int axisIndex;
			long value;
			try {
				axisIndex = Integer.parseInt(parts[0].trim());
				String val = parts[1].trim().toLowerCase();
				if (val.equals("min")) value = 0;
				else if (val.equals("max")) value = ds.dimension(i) - 1;
				else {
					err =
						"Err in origin specification: each origin spec must be"
							+ " [axis number]=min or [axis number]=max.";
					return null;
				}
			}
			catch (NumberFormatException e) {
				err =
					"Err in origin specification string: each subentry must be"
						+ " a number and then an '=' sign, and then either"
						+ " 'min' or 'max'.";
				return null;
			}
			if (axisIndex < 0 || axisIndex >= ds.numDimensions()) {
				err = "An axis index is outside dimensionality of input dataset.";
				return null;
			}
			orig.set(axisIndex, value);
		}
		return orig;
	}

	private List<Double> parseSpacings(Dataset ds, String spec) {
		if (spec == null) {
			err = "Spacings specification string is null.";
			return null;
		}
		String[] terms = spec.split(",");
		if (terms.length == 0) {
			err = "Spacings specification string is empty.";
			return null;
		}
		List<Double> spaces = new ArrayList<Double>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			spaces.add(1.0);
		}
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim();
			String[] parts = term.split("=");
			if (parts.length != 2) {
				err =
					"Err in spacings specification string: each"
						+ " spacing must be two numbers separated by an '=' sign.";
				return null;
			}
			int axisIndex;
			double space;
			try {
				axisIndex = Integer.parseInt(parts[0].trim());
				space = Double.parseDouble(parts[1].trim());
			}
			catch (NumberFormatException e) {
				err =
					"Err in spacings specification string: each"
						+ " spacing must be two numbers separated by an '=' sign.";
				return null;
			}
			if (axisIndex < 0 || axisIndex >= ds.numDimensions()) {
				err = "An axis index is outside dimensionality of input dataset.";
				return null;
			}
			spaces.set(axisIndex, space);
		}
		return spaces;
	}

	private void resampleData(Dataset ds, List<Long> orig, List<Double> spaces) {

		// TODO: resampling needs a copy of original data. We should be able to
		// instead come up with smarter algo that duplicates less (like a plane
		// at a time assuming interpolator only looks in curr plane).

		long[] destDims = newSize(ds, spaces);
		ImgPlus<T> dest = newData(destDims, ds);
		ImgPlus<T> src = (ImgPlus<T>) ds.getImgPlus();

		// TODO: fill empty pixels with the current background color
		// ChannelCollection. For now fill with zero. Later need to make a OOB
		// interp method that checks the curr position along a "channel" axis and
		// looks up in a ChannelCollection. But ChannelCollection is a IJ2 class.
		// This is the second instance where it would be useful to move to Imglib.
		// The other being asking the current color a projector would return given
		// a set of input values as a ChannelCollection.

		T zero = src.firstElement().createVariable();
		zero.setZero();

		InterpolatorFactory<T, RandomAccessible<T>> ifac = getInterpolator();

		final RealRandomAccess<T> inter =
			ifac.create(Views.extend(src,
				new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(
					Boundary.DOUBLE)));

		final Cursor<T> c2 = Views.iterable(dest).localizingCursor();
		final int numDims = ds.numDimensions();
		final long[] start = new long[numDims];
		final double[] space = new double[numDims];
		final long[] maxes = new long[numDims];
		for (int i = 0; i < numDims; i++) {
			start[i] = orig.get(i);
			space[i] = spaces.get(i);
			maxes[i] = dest.dimension(i) - 1;
		}
		final long[] p = new long[numDims];
		while (c2.hasNext()) {
			c2.fwd();
			c2.localize(p);
			for (int i = 0; i < p.length; i++) {
				double pos = position(start[i], space[i], p[i], maxes[i]);
				inter.setPosition(pos, i);
			}
			c2.get().set(inter.get());
		}

		ds.setImgPlus(dest);
	}

	private String originString() {
		String str = "";
		for (int i = 0; i < origin.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += i + "=" + (origin.get(i) == 0 ? "min" : "max");
		}
		return str;
	}

	private String spacingsString() {
		String str = "";
		for (int i = 0; i < spacings.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += i + "=" + spacings.get(i);
		}
		return str;
	}

	private InterpolatorFactory<T, RandomAccessible<T>> getInterpolator() {
		if (method.equals(LINEAR)) {
			return new NLinearInterpolatorFactory<T>();
		}
		else if (method.equals(NEAREST_NEIGHBOR)) {
			return new NearestNeighborInterpolatorFactory<T>();
		}
		else if (method.equals(LANCZOS)) {
			return new LanczosInterpolatorFactory<T>();
		}
		else throw new IllegalArgumentException("unknown interpolation method: " +
			method);
	}

	private void toPixelUnits(Dataset ds, List<Double> spaces) {
		for (int i = 0; i < spaces.size(); i++) {
			CalibratedAxis axis = ds.axis(i);
			double userUnit = spaces.get(i);
			double pixelUnit = axis.rawValue(userUnit) - axis.rawValue(0);
			spaces.set(i, pixelUnit);
		}
	}

	private double position(long org, double spacing, long pos, long max)
	{
		if (org == 0) return spacing * pos;
		// else org > 0
		return org - spacing * (max - pos);
	}

	private long[] newSize(Dataset ds, List<Double> spaces) {
		long[] newDims = new long[ds.numDimensions()];
		ds.dimensions(newDims);
		for (int i = 0; i < newDims.length; i++) {
			newDims[i] = Math.round(newDims[i] / spaces.get(i));
			if (newDims[1] < 1) newDims[i] = 1;
		}
		return newDims;
	}

	private ImgPlus<T> newData(long[] newDims, Dataset base) {
		ImgPlus<T> imgPlus = (ImgPlus<T>) base.getImgPlus();
		T type = imgPlus.firstElement();
		ImgFactory<T> factory = imgPlus.factory();
		Img<T> result = factory.create(newDims, type);
		return new ImgPlus<T>(result, base);
	}
}

