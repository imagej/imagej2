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

package imagej.plugins.commands.imglib;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.menu.MenuConstants;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.meta.ImgPlus;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Rotates an existing image by a user specified angle. The resultant pixel
 * values are some combination of the original neighboring pixels using the
 * user specified interpolation method.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", headless = true, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Rotate...", mnemonic = 'r') })
public class RotateImageXY<T extends RealType<T>> extends ContextCommand {

	// -- constants --

	private static final String LANCZOS = "Lanczos";
	private static final String LINEAR = "Linear";
	private static final String NEAREST_NEIGHBOR = "Nearest Neighbor";

	private static final String DEGREES = "Degrees";
	private static final String RADIANS = "Radians";
	
	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Angle")
	private double angle;
	
	@Parameter(label = "Unit", choices = { DEGREES, RADIANS })
	private String angleUnit = DEGREES;

	@Parameter(label = "Interpolation", choices = { LINEAR, NEAREST_NEIGHBOR,
		LANCZOS }, persist = false)
	private String method = LINEAR;
	
	@Parameter
	private DatasetService datasetService;

	// -- constructors --

	public RotateImageXY() {}

	// -- accessors --

	/**
	 * Sets the Dataset that the rotate operation will be run upon.
	 */
	public void setDataset(Dataset ds) {
		dataset = ds;
	}

	/**
	 * Gets the Dataset that the rotate operation will be run upon.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets the angle of counterclockwise rotation to apply. The angle units
	 * are specified with setUnit().
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	/**
	 * Gets the angle of counterclockwise rotation to apply. The angle units
	 * are available from getUnit().
	 */
	public double getAngle() {
		return angle;
	}
	
	/**
	 * Sets the current angle unit setting (radians or dgrees). Use one of the
	 * unit String constants exposed by this class.
	 */
	public void setUnit(String unit) {
		if (unit.equals(DEGREES)) angleUnit = unit;
		else if (unit.equals(RADIANS)) angleUnit = unit;
		else throw new IllegalArgumentException("Unknown angle unit: "+unit);
	}
	
	/**
	 * Gets the current angle unit setting (radians or degrees). Returns one of
	 * the unit String constants exposed by this class.
	 */
	public String getUnit() {
		return angleUnit;
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

	// -- Command methods --

	@Override
	public void run() {
		double ang = radians(angle);
		resampleData(dataset, ang);
	}

	// -- helpers --

	private double radians(double angle) {
		if (angleUnit.equals(RADIANS)) return angle;
		return (angle * 2.0 * Math.PI) / 360.0;
	}
	
	private void resampleData(Dataset ds, double angleInRadians) {

		// TODO: resampling needs a copy of original data. We should be able to
		// instead come up with smarter algo that duplicates less (like a plane
		// at a time assuming interpolator only looks in curr plane).

		@SuppressWarnings("unchecked")
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
		final double cos = Math.cos(angleInRadians);
		final double sin = Math.sin(angleInRadians);
		final double[] center = new double[dest.numDimensions()];
		center[0] = (dest.dimension(0) / 2.0) - 0.5;
		center[1] = (dest.dimension(1) / 2.0) - 0.5;
		final double[] delta = new double[dest.numDimensions()];
		final long[] d = new long[dest.numDimensions()];
		while (c2.hasNext()) {
			c2.fwd();
			c2.localize(d);
			findDeltas(center, d, cos, sin, delta);
			inter.setPosition(center[0] + delta[0], 0);
			inter.setPosition(center[1] + delta[1], 1);
			for (int i = 2; i < d.length; i++) {
				inter.setPosition(d[i], i);
			}
			c2.get().set(inter.get());
		}

		ds.update(); // TODO WHY DOESN'T ORGAN UPDATE???
	}

	private void findDeltas(double[] ctr, long[] pt, double cos, double sin, double[] delta) {
		double dx = pt[0] - ctr[0];
		double dy = pt[1] - ctr[1];
	    double xPrime = dx * cos - dy * sin;
	    double yPrime = dx * sin + dy * cos;
		delta[0] = xPrime;
		delta[1] = yPrime;
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
}
