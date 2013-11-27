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

package imagej.core.commands.imglib;

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
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Translates an existing image by a set of possibly nonintegral deltas. The
 * deltas can be manipulated by dimension. The resultant pixel is some
 * combination of the original neighboring pixels using some user specified
 * interpolation method.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", headless = true, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Translate...", mnemonic = 't') })
public class TranslateImage<T extends RealType<T>> extends ContextCommand {

	// -- constants --

	private static final String LANCZOS = "Lanczos";
	private static final String LINEAR = "Linear";
	private static final String NEAREST_NEIGHBOR = "Nearest Neighbor";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Deltas", persist = false)
	private String deltasString;

	@Parameter(label = "Interpolation", choices = { LINEAR, NEAREST_NEIGHBOR,
		LANCZOS }, persist = false)
	private String method = LINEAR;
	
	@Parameter(label = "Use user units", persist = false)
	private boolean useUserUnits;

	@Parameter
	private DatasetService datasetService;

	// -- non-parameter fields --

	private String err = null;

	private List<Double> deltas = new ArrayList<Double>();

	// -- constructors --

	public TranslateImage() {}

	// -- accessors --

	/**
	 * Sets the Dataset that the translate operation will be run upon.
	 */
	public void setDataset(Dataset ds) {
		dataset = ds;
		init();
	}

	/**
	 * Gets the Dataset that the translate operation will be run upon.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets the translation delta for a given axis to a given value. The delta
	 * units are determined by the value of getUseUserUnits().
	 */
	public void setDelta(int d, double delta) {
		if (d < 0 || d >= deltas.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (deltas.size() - 1) + ")");
		}
		deltas.set(d, delta);
		deltasString = deltasString();
	}

	/**
	 * Gets the translation delta for a given axis. The delta units are
	 * determined by the value of getUseUserUnits().
	 */
	public double getDelta(int d) {
		if (d < 0 || d >= deltas.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (deltas.size() - 1) + ")");
		}
		return deltas.get(d);
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
	 * Sets whether input deltas field values are in user units or pixel units.
	 * If input val is true then use user units else use pixel units.
	 */
	public void setUseUserUnits(boolean val) {
		useUserUnits = val;
	}
	
	/**
	 * Gets whether input deltas field values are in user units or pixel units.
	 * Returns tru if currently in user units and false if currently in pixel units.
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
		List<Double> delts = parseDeltas(dataset, deltasString);
		if (delts == null) {
			cancel(err);
			return;
		}
		if (useUserUnits) toPixelUnits(dataset, delts);
		resampleData(dataset, delts);
	}

	// -- initializers --

	protected void init() {
		deltas.clear();
		for (int i = 0; i < dataset.numDimensions(); i++) {
			deltas.add(0.0);
		}
		deltasString = deltasString();
	}

	// -- helpers --

	private List<Double> parseDeltas(Dataset ds, String spec) {
		if (spec == null) {
			err = "Deltas specification string is null.";
			return null;
		}
		String[] terms = spec.split(",");
		if (terms.length == 0) {
			err = "Deltas specification string is empty.";
			return null;
		}
		List<Double> delts = new ArrayList<Double>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			delts.add(0.0);
		}
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim();
			String[] parts = term.split("=");
			if (parts.length != 2) {
				err =
					"Err in deltas specification string: each"
						+ " delta must be two numbers separated by an '=' sign.";
				return null;
			}
			int axisIndex;
			double delta;
			try {
				axisIndex = Integer.parseInt(parts[0].trim());
				delta = Double.parseDouble(parts[1].trim());
			}
			catch (NumberFormatException e) {
				err =
					"Err in deltas specification string: each"
						+ " delta must be two numbers separated by an '=' sign.";
				return null;
			}
			if (axisIndex < 0 || axisIndex >= ds.numDimensions()) {
				err = "An axis index is outside dimensionality of input dataset.";
				return null;
			}
			deltas.set(axisIndex, delta);
		}
		return deltas;
	}

	private void resampleData(Dataset ds, List<Double> delts) {

		// TODO: resampling needs a copy of original data. We should be able to
		// instead come up with smarter algo that duplicates less (like a plane
		// at a time assuming interpolator only looks in curr plane).

		ImgPlus<T> dest = (ImgPlus<T>) ds.getImgPlus();
		ImgPlus<T> src = dest.copy();

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
				new OutOfBoundsConstantValueFactory<T, RandomAccessibleInterval<T>>(
					zero)));

		final Cursor<T> c2 = Views.iterable(dest).localizingCursor();
		final double[] delta = new double[dest.numDimensions()];
		for (int i = 0; i < delta.length; i++)
			delta[i] = delts.get(i);
		final long[] d = new long[dest.numDimensions()];
		while (c2.hasNext()) {
			c2.fwd();
			c2.localize(d);
			for (int i = 0; i < d.length; i++) {
				inter.setPosition(d[i] - delta[i], i);
			}
			c2.get().set(inter.get());
		}

		ds.update(); // TODO WHY DOESN'T ORGAN UPDATE???
	}

	private String deltasString() {
		String str = "";
		for (int i = 0; i < deltas.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += i + "=" + deltas.get(i);
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

	private void toPixelUnits(Dataset ds, List<Double> delts) {
		for (int i = 0; i < delts.size(); i++) {
			CalibratedAxis axis = ds.axis(i);
			double userUnit = delts.get(i);
			double pixelUnit = axis.rawValue(userUnit) - axis.rawValue(0);
			delts.set(i, pixelUnit);
		}
	}
}
