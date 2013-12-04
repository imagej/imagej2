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
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.function.real.RealSumFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Reduces the size of an image by integral scale factors. The scale factors can
 * be manipulated separately per dimension (i.e. scale X by 2 but Y by 3). The
 * resultant pixel is some combination (min, avg, sum, etc.) of the original
 * neighboring pixels. The combination method can be specified as needed.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", headless = true, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Bin...", mnemonic = 'b') })
public class Binner<T extends RealType<T>> extends ContextCommand {

	// -- constants --

	private static final String AVERAGE = "Average";
	private static final String SUM = "Sum";
	private static final String MIN = "Min";
	private static final String MAX = "Max";
	private static final String MEDIAN = "Median";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Dimension reduction factors", persist = false)
	private String dimFactors;

	// TODO - autodiscover OPS functions like mean, harmonic mean, etc. and use
	// them. For now just copy IJ1's capabilities.

	@Parameter(label = "Value method",
		choices = { AVERAGE, SUM, MIN, MAX, MEDIAN })
	private String method = AVERAGE;

	@Parameter
	private DatasetService datasetService;

	// -- non-parameter fields --

	private String err = null;

	private List<Integer> factors = new ArrayList<Integer>();

	// -- constructors --

	public Binner() {}

	// -- accessors --

	/**
	 * Sets the Dataset that the bin operation will be run upon.
	 */
	public void setDataset(Dataset ds) {
		dataset = ds;
		init();
	}

	/**
	 * Gets the Dataset that the bin operation will be run upon.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets the reduction factor for a given dimension.
	 */
	public void setFactor(int d, int factor) {
		if (d < 0 || d >= factors.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (factors.size() - 1) + ")");
		}
		factors.set(d, factor);
		dimFactors = factorsString();
	}

	/**
	 * Gets the reduction factor for a given dimension.
	 */
	public int getFactor(int d) {
		if (d < 0 || d >= factors.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (factors.size() - 1) + ")");
		}
		return factors.get(d);
	}

	// TODO - have a set method and get method that take a Function factory that
	// can return a MedianFunction etc. This allows a lot of flexibility to how
	// data is combined.

	/**
	 * Sets the value method used to combine pixels. Use the constant strings that
	 * are exposed by this class.
	 */
	public void setValueMethod(String str) {
		if (str.equals(AVERAGE)) method = AVERAGE;
		else if (str.equals(MAX)) method = MAX;
		else if (str.equals(MEDIAN)) method = MEDIAN;
		else if (str.equals(MIN)) method = MIN;
		else if (str.equals(SUM)) method = SUM;
		else throw new IllegalArgumentException("Unknown value method: " + str);
	}

	/**
	 * Gets the value method used to combine pixels. One of the constant strings
	 * that are exposed by this class.
	 */
	public String getValueMethod() {
		return method;
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
		List<Integer> reductions = parseReductions(dataset, dimFactors);
		if (reductions == null) {
			cancel(err);
			return;
		}
		reduceData(dataset, reductions);
	}

	// -- initializers --
	
	protected void init() {
		factors.clear();
		for (int i = 0; i < dataset.numDimensions(); i++) {
			factors.add(1);
		}
		dimFactors = factorsString();
	}
	
	// -- helpers --

	private List<Integer> parseReductions(Dataset ds, String spec) {
		if (spec == null) {
			err = "Dimension reduction specification string is null.";
			return null;
		}
		String[] terms = spec.split(",");
		if (terms.length == 0) {
			err = "Dimension reduction specification string is empty.";
			return null;
		}
		List<Integer> reductions = new ArrayList<Integer>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			reductions.add(1);
		}
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim();
			String[] parts = term.split("=");
			if (parts.length != 2) {
				err =
					"Err in dimension reduction specification string: each"
						+ " dimension must be two numbers separated by an '=' sign.";
				return null;
			}
			int axisIndex;
			int factor;
			try {
				axisIndex = Integer.parseInt(parts[0].trim());
				factor = Integer.parseInt(parts[1].trim());
			}
			catch (NumberFormatException e) {
				err =
					"Err in dimension reduction specification string: each"
						+ " dimension must be two numbers separated by an '=' sign.";
				return null;
			}
			if (axisIndex < 0 || axisIndex >= ds.numDimensions()) {
				err = "An axis index is outside dimensionality of input dataset.";
				return null;
			}
			if (factor < 1 || factor > ds.dimension(axisIndex)) {
				err =
					"Reduction factor " + i + " must be between 1 and " +
						ds.dimension(axisIndex) + ".";
				return null;
			}
			reductions.set(axisIndex, factor);
		}
		return reductions;
	}

	private void reduceData(Dataset ds, List<Integer> reductionFactors) {

		// make the correct Function from the dataset based upon valueMethod.
		Function<PointSet, T> func = function(ds);

		// setup neighborhood to calc from
		PointSet neigh = neighborhood(reductionFactors);

		// make new dimensioned data
		Dataset newDs = newData(ds, reductionFactors);

		// walk each pixel in new dimension, find neighborhood of related point in
		// original space, calc value, and set in newDs.
		long[] currPos = new long[newDs.numDimensions()];
		long[] lastPos = new long[newDs.numDimensions()];
		long[] translation = new long[ds.numDimensions()];
		Cursor<T> cursor = (Cursor<T>) newDs.getImgPlus().localizingCursor();
		T var = cursor.get().createVariable();
		while (cursor.hasNext()) {
			cursor.next();
			cursor.localize(currPos);
			findTranslation(lastPos, currPos, reductionFactors, translation);
			for (int i = 0; i < lastPos.length; i++) {
				lastPos[i] = currPos[i];
			}
			neigh.translate(translation);
			func.compute(neigh, var);
			cursor.get().set(var);
		}

		// TODO
		// update scale of newData's axes?
		// HOW!!!!????
		// This might point out a way to say: setAxis(new ScaleAxis(getAxis())). Can
		// chain these. Thus any axis can be built up from others. It's a thought at
		// least.

		// update data
		ds.setImgPlus(newDs.getImgPlus());
	}

	private String factorsString() {
		String str = "";
		for (int i = 0; i < factors.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += i + "=" + factors.get(i);
		}
		return str;
	}

	private Function<PointSet, T> function(Dataset ds) {
		Img<T> img = (Img<T>) ds.getImgPlus();
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobFactory =
			new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(
				Boundary.DOUBLE);
		T var = img.firstElement();
		Function<long[], T> imgFunc =
			new RealImageFunction<T, T>(img, oobFactory, var);
		if (method == AVERAGE) return new RealArithmeticMeanFunction<T>(imgFunc);
		else if (method == MAX) return new RealMaxFunction<T>(imgFunc);
		else if (method == MEDIAN) return new RealMedianFunction<T>(imgFunc);
		else if (method == MIN) return new RealMinFunction<T>(imgFunc);
		else if (method == SUM) return new RealSumFunction<T>(imgFunc);
		else throw new IllegalArgumentException("unknown method: " + method);
	}

	private PointSet neighborhood(List<Integer> reductionFactors) {
		long[] dims = neighSize(reductionFactors);
		return new HyperVolumePointSet(dims);
	}

	private Dataset newData(Dataset origDs, List<Integer> reductionFactors) {
		long[] newDims = newDims(origDs, reductionFactors);
		String name = origDs.getName();
		AxisType[] axisTypes = new AxisType[origDs.numDimensions()];
		for (int i = 0; i < axisTypes.length; i++) {
			axisTypes[i] = origDs.axis(i).type();
		}
		int bitsPerPixel = origDs.getImgPlus().firstElement().getBitsPerPixel();
		boolean signed = origDs.isSigned();
		boolean floating = !origDs.isInteger();
		boolean virtual =
			AbstractCellImg.class.isAssignableFrom(origDs.getImgPlus().getImg()
				.getClass());
		return datasetService.create(newDims, name, axisTypes, bitsPerPixel,
			signed, floating, virtual);
	}

	private void findTranslation(long[] newDsLastPos, long[] newDsCurrPos,
		List<Integer> reductionFactors, long[] origDsTranslation)
	{
		for (int i = 0; i < origDsTranslation.length; i++) {
			origDsTranslation[i] = newDsCurrPos[i] - newDsLastPos[i];
			origDsTranslation[i] *= reductionFactors.get(i);
		}
	}

	private long[] neighSize(List<Integer> reductionFactors) {
		long[] neighSize = new long[reductionFactors.size()];
		for (int i = 0; i < neighSize.length; i++) {
			neighSize[i] = reductionFactors.get(i);
		}
		return neighSize;
	}

	private long[] newDims(Dataset ds, List<Integer> reductionFactors) {
		long[] dims = new long[ds.numDimensions()];
		for (int i = 0; i < dims.length; i++) {
			dims[i] = ds.dimension(i) / reductionFactors.get(i);
			if (ds.dimension(i) % reductionFactors.get(i) != 0) dims[i]++;
		}
		return dims;
	}

}
