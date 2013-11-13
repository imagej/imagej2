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

import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.iterableinterval.unary.Resample;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Resamples an existing image into a Dataset of specified dimensions. The
 * dimensions can be manipulated separately. The resultant pixel is some
 * combination of the original neighboring pixels using some user specified
 * interpolation method.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "init", headless = true, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Adjust", mnemonic = 'a'),
	@Menu(label = "Resize", mnemonic = 'r') })
public class ResizeImage<T extends RealType<T>> extends ContextCommand {

	// -- constants --

	private static final String LANCZOS = "Lanczos";
	private static final String LINEAR = "Linear";
	private static final String NEAREST_NEIGHBOR = "Nearest Neighbor";
	private static final String PERIODICAL = "Periodic";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Dimensions", persist = false)
	private String dimensionsString;

	@Parameter(label = "Interpolation", choices = { LINEAR, NEAREST_NEIGHBOR,
		LANCZOS, PERIODICAL }, persist = false)
	private String method = LINEAR;

	@Parameter(label = "Constrain XY aspect ratio", persist = false)
	private boolean constrain;

	@Parameter
	private DatasetService datasetService;


	// -- non-parameter fields --

	private String err = null;

	private List<Long> dimensions = new ArrayList<Long>();

	private int xAxis;

	private int yAxis;

	// -- constructors --

	public ResizeImage() {}

	// -- accessors --

	/**
	 * Sets the Dataset that the resize operation will be run upon.
	 */
	public void setDataset(Dataset ds) {
		dataset = ds;
		init();
	}

	/**
	 * Gets the Dataset that the resize operation will be run upon.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets the given dimension of the resized image.
	 */
	public void setDimension(int d, long size) {
		if (d < 0 || d >= dimensions.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (dimensions.size() - 1) + ")");
		}
		dimensions.set(d, size);
		if (constrain) {
			if (d == xAxis) {
				double sz = size;
				sz *= dataset.dimension(yAxis);
				sz /= dataset.dimension(xAxis);
				long ySize = Math.round(sz);
				dimensions.set(yAxis, ySize);
			}
			else if (d == yAxis) {
				double sz = size;
				sz *= dataset.dimension(xAxis);
				sz /= dataset.dimension(yAxis);
				long xSize = Math.round(sz);
				dimensions.set(xAxis, xSize);
			}
		}
		dimensionsString = dimensionsString();
	}

	/**
	 * Gets the given dimension of the resized image.
	 */
	public long getDimension(int d) {
		if (d < 0 || d >= dimensions.size()) {
			throw new IllegalArgumentException("dimension " + d +
				" out of bounds (0," + (dimensions.size() - 1) + ")");
		}
		return dimensions.get(d);
	}

	// TODO - have a set method and get method that take a Resample.Mode. This
	// allows more flexibility on how data is combined.

	/**
	 * Sets the interpolation method used to combine pixels. Use the constant
	 * strings that are exposed by this class.
	 */
	public void setInterpolationMethod(String str) {
		if (str.equals(PERIODICAL)) method = PERIODICAL;
		else if (str.equals(LINEAR)) method = LINEAR;
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
	 * Sets whether command will constrain XY aspect ratio.
	 */
	public void setConstrainXY(Boolean val) {
		constrain = val;
	}

	/**
	 * Gets whether command will constrain XY aspect ratio.
	 */
	public boolean getConstrainXY() {
		return constrain;
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
		List<Long> dims = parseDimensions(dataset, dimensionsString);
		if (dims == null) {
			cancel(err);
			return;
		}
		if (constrain) constrain(dims);
		resampleData(dataset, dims);
	}

	// -- initializers --

	protected void init() {
		dimensions.clear();
		for (int i = 0; i < dataset.numDimensions(); i++) {
			dimensions.add(dataset.dimension(i));
		}
		dimensionsString = dimensionsString();
		xAxis = dataset.dimensionIndex(Axes.X);
		yAxis = dataset.dimensionIndex(Axes.Y);
	}

	// -- helpers --

	private List<Long> parseDimensions(Dataset ds, String spec) {
		if (spec == null) {
			err = "Dimension specification string is null.";
			return null;
		}
		String[] terms = spec.split(",");
		if (terms.length == 0) {
			err = "Dimension specification string is empty.";
			return null;
		}
		List<Long> dims = new ArrayList<Long>();
		for (int i = 0; i < ds.numDimensions(); i++) {
			dims.add(ds.dimension(i));
		}
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim();
			String[] parts = term.split("=");
			if (parts.length != 2) {
				err =
					"Err in dimension specification string: each"
						+ " dimension must be two numbers separated by an '=' sign.";
				return null;
			}
			int axisIndex;
			long size;
			try {
				axisIndex = Integer.parseInt(parts[0].trim());
				size = Long.parseLong(parts[1].trim());
			}
			catch (NumberFormatException e) {
				err =
					"Err in dimension specification string: each"
						+ " dimension must be two numbers separated by an '=' sign.";
				return null;
			}
			if (axisIndex < 0 || axisIndex >= ds.numDimensions()) {
				err = "An axis index is outside dimensionality of input dataset.";
				return null;
			}
			if (size < 1) {
				err = "Dimension " + i + " must be greater than 0";
				return null;
			}
			dimensions.set(axisIndex, size);
		}
		return dimensions;
	}

	private void constrain(List<Long> dims) {

		// NB if both X & Y edited by user it uses X as the fixed dimension.

		// if X was edited by user
		if (dims.get(xAxis) != dataset.dimension(xAxis)) {
			double sz = dims.get(xAxis);
			sz *= dataset.dimension(yAxis);
			sz /= dataset.dimension(xAxis);
			long ySize = Math.round(sz);
			dims.set(yAxis, ySize);
		}
		// else if y edited by user
		else if (dims.get(yAxis) != dataset.dimension(yAxis)) {
			double sz = dims.get(yAxis);
			sz *= dataset.dimension(xAxis);
			sz /= dataset.dimension(yAxis);
			long xSize = Math.round(sz);
			dims.set(xAxis, xSize);
		}
	}

	private void resampleData(Dataset ds, List<Long> dims) {

		ImgPlus<? extends RealType<?>> origImgPlus = ds.getImgPlus();
		int numDims = origImgPlus.numDimensions();
		CalibratedAxis[] axes = new CalibratedAxis[numDims];
		ds.axes(axes);
		AxisType[] axisTypes = new AxisType[numDims];
		for (int i = 0; i < numDims; i++) {
			axisTypes[i] = axes[i].type();
		}
		Dataset newDs = newData(ds, dims);
		newDs.setAxes(axes);
		if (ds.getCompositeChannelCount() == numChannels(ds)) {
			newDs.setCompositeChannelCount(numChannels(newDs));
		}
		Resample<T> resampleOp = new Resample<T>(resampleMode());
		resampleOp.compute((Img<T>) origImgPlus, (Img<T>) newDs.getImgPlus());
		ds.setImgPlus(newDs.getImgPlus());
	}

	private String dimensionsString() {
		String str = "";
		for (int i = 0; i < dimensions.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += i + "=" + dimensions.get(i);
		}
		return str;
	}

	private Dataset newData(Dataset origDs, List<Long> dims) {
		long[] newDims = newDims(origDs, dims);
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

	private long[] newDims(Dataset ds, List<Long> dimsList) {
		long[] dims = new long[ds.numDimensions()];
		for (int i = 0; i < dims.length; i++) {
			dims[i] = dimsList.get(i);
		}
		return dims;
	}

	private Resample.Mode resampleMode() {
		if (method.equals(LANCZOS)) return Resample.Mode.LANCZOS;
		else if (method.equals(LINEAR)) return Resample.Mode.LINEAR;
		else if (method.equals(NEAREST_NEIGHBOR)) return Resample.Mode.NEAREST_NEIGHBOR;
		else if (method.equals(PERIODICAL)) return Resample.Mode.PERIODICAL;
		else throw new IllegalArgumentException("Unknown interpolation method: " +
			method);
	}

	private int numChannels(Dataset ds) {
		int index = ds.dimensionIndex(Axes.CHANNEL);
		if (index < 0) return 1;
		return (int) ds.dimension(index);
	}

}
