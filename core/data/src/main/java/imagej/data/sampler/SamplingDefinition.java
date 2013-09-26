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

package imagej.data.sampler;

import imagej.data.Data;
import imagej.data.display.ImageDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.SpaceUtils;

/**
 * SamplingDefinitions define regions of space and are used by the
 * SamplerService to pull data out of existing ImageDisplays.
 * 
 * @author Barry DeZonia
 */
public class SamplingDefinition {

	// -- instance variables --

	private final ImageDisplay display;
	private final Map<AxisType, AxisSubrange> axisSubranges;
	private String err;

	// -- private base class constructor --

	private SamplingDefinition(final ImageDisplay display) {
		this.display = display;
		this.axisSubranges = new HashMap<AxisType, AxisSubrange>();
		this.err = null;
	}

	// -- public interface --

	/** Returns the input ImageDisplay of the SamplingDefinition. */
	public ImageDisplay getDisplay() {
		return display;
	}

	/** Returns the current value of the error string of the SamplingDefinition. */
	public String getError() {
		return err;
	}

	/** Returns the axes that are present in the input data. */
	public AxisType[] getInputAxes() {
		return SpaceUtils.getAxisTypes(display);
	}

	/**
	 * Returns a multidimensional set of input axis values generated from the
	 * input data of this SamplingDefinition.
	 * <p>
	 * For example, if the sampling definition has two axes defined as "1-4" and
	 * "1-3" calling this routine would return something like this: [[1,2,3,4] ,
	 * [1,2,3]]
	 */
	public List<List<Long>> getInputRanges() {
		final List<List<Long>> axesDefs = new ArrayList<List<Long>>();
		for (int i = 0; i < display.numDimensions(); i++) {
			final AxisType axisType = display.axis(i).type();
			final AxisSubrange subrange = axisSubranges.get(axisType);
			final List<Long> axisValues = subrange.getIndices();
			axesDefs.add(axisValues);
		}
		return Collections.unmodifiableList(axesDefs);
	}

	/**
	 * Returns the axes that will be present in the output data. Those input axes
	 * whose size is 1 are automatically collapsed.
	 */
	public AxisType[] getOutputAxes() {
		final AxisType[] inputAxes = getInputAxes();
		final List<List<Long>> inputRanges = getInputRanges();
		int dimCount = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) dimCount++;
		}
		final AxisType[] outputAxes = new AxisType[dimCount];
		int d = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) outputAxes[d++] = inputAxes[i];
		}
		return outputAxes;
	}

	/**
	 * Returns the dimensions that will be present in the output data. Those input
	 * dimensions whose size is 1 are automatically collapsed.
	 */
	public long[] getOutputDims() {
		final List<List<Long>> inputRanges = getInputRanges();
		int dimCount = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) dimCount++;
		}
		final long[] outputDims = new long[dimCount];
		int d = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			final int dimSize = inputRanges.get(i).size();
			if (dimSize > 1) outputDims[d++] = dimSize;
		}
		return outputDims;
	}

	/**
	 * Returns the calibration values that will be present in the output data.
	 */
	public double[] getOutputCalibration(final AxisType[] outputAxes) {
		final double[] cal = new double[outputAxes.length];
		int a = 0;
		for (int i = 0; i < outputAxes.length; i++) {
			final int axisIndex = display.dimensionIndex(outputAxes[i]);
			if (axisIndex >= 0) cal[a++] = display.calibration(i);
		}
		return cal;
	}

	/**
	 * Replaces the current constraining definition of a given axis within the
	 * current SamplingDefinition with a given subrange.
	 * 
	 * @param axis The axis to associate the constraint with
	 * @param subrange The new subrange defining the constraint
	 * @return True if the contraint is well defined. False otherwise (and the
	 *         existing constraint for the axis is unchanged).
	 */
	public boolean constrain(final AxisType axis, final AxisSubrange subrange) {
		if (subrange.getError() != null) {
			err = subrange.getError();
			return false;
		}
		final Data data = display.getActiveView().getData();
		final int axisIndex = data.dimensionIndex(axis);
		if (axisIndex < 0) {
			err = "Undefined axis " + axis + " for display " + display.getName();
			return false;
		}
		final List<Long> indices = subrange.getIndices();
		double dimension = data.realMax(axisIndex) - data.realMin(axisIndex);
		if (indices.get(0) >= dimension) {
			err =
				"Axis range fully beyond dimensions of display " + display.getName() +
					" for axis " + axis;
			return false;
		}
		if (indices.get(indices.size() - 1) > dimension) {
			err =
				"Axis range partially beyond dimensions of display " + display.getName() +
					" for axis " + axis;
			return false;
		}
		axisSubranges.put(axis, subrange);
		return true;
	}

	// -- public static construction methods --

	/**
	 * A convenience method for defining a SamplingDefinition that returns a
	 * single UV plane of an ImageDisplay. U and V are defined by the user.
	 * 
	 * @param display The ImageDisplay to sample
	 * @param uAxis The U axis of the sample space
	 * @param vAxis The V axis of the sample space
	 * @return The specified SamplingDefinition
	 */
	public static SamplingDefinition sampleUVPlane(final ImageDisplay display,
		final AxisType uAxis, final AxisType vAxis)
	{
		final SamplingDefinition definition = new SamplingDefinition(display);
		final Data data = display.getActiveView().getData();
		for (int i = 0; i < data.numDimensions(); i++) {
			AxisType axisType = data.axis(i).type();
			if ((axisType == uAxis) || (axisType == vAxis)) {
				final int axisIndex = display.dimensionIndex(axisType);
				final long size = display.dimension(axisIndex);
				final AxisSubrange subrange = new AxisSubrange(0, size - 1);
				definition.constrain(axisType, subrange);
			}
			else { // other axis
				final long pos = display.getLongPosition(axisType);
				final AxisSubrange subrange = new AxisSubrange(pos);
				definition.constrain(axisType, subrange);
			}
		}
		return definition;
	}

	/**
	 * A convenience method for defining a SamplingDefinition that returns a
	 * single XY plane of an ImageDisplay.
	 * 
	 * @param display The ImageDisplay to sample
	 * @return The specified SamplingDefinition
	 */
	public static SamplingDefinition sampleXYPlane(final ImageDisplay display) {
		return sampleUVPlane(display, Axes.X, Axes.Y);
	}

	/**
	 * A convenience method for defining a SamplingDefinition that returns a
	 * composite (multichannel) UV plane of an ImageDisplay. U and V are defined
	 * by the user.
	 * 
	 * @param display The ImageDisplay to sample
	 * @param uAxis The U axis of the sample space
	 * @param vAxis The V axis of the sample space
	 * @return The specified SamplingDefinition
	 */
	public static SamplingDefinition sampleCompositeUVPlane(
		final ImageDisplay display, final AxisType uAxis, final AxisType vAxis)
	{
		if ((uAxis == Axes.CHANNEL) || (vAxis == Axes.CHANNEL)) {
			throw new IllegalArgumentException(
					"UV composite plane - cannot specify channels as one of the axes");
		}
		final SamplingDefinition definition = new SamplingDefinition(display);
		final Data data = display.getActiveView().getData();
		for (int i = 0; i < data.numDimensions(); i++) {
			AxisType axisType = data.axis(i).type();
			if ((axisType == uAxis) || (axisType == vAxis) ||
				(axisType == Axes.CHANNEL))
			{
				final int axisIndex = display.dimensionIndex(axisType);
				final long size = display.dimension(axisIndex);
				final AxisSubrange subrange = new AxisSubrange(0, size - 1);
				definition.constrain(axisType, subrange);
			}
			else { // other axis
				final long pos = display.getLongPosition(axisType);
				final AxisSubrange subrange = new AxisSubrange(pos);
				definition.constrain(axisType, subrange);
			}
		}
		return definition;
	}

	/**
	 * A convenience method for defining a SamplingDefinition that returns a
	 * composite (multichannel) XY plane of an ImageDisplay.
	 * 
	 * @param display The ImageDisplay to sample
	 * @return The specified SamplingDefinition
	 */
	public static SamplingDefinition sampleCompositeXYPlane(
		final ImageDisplay display)
	{
		return sampleCompositeUVPlane(display, Axes.X, Axes.Y);
	}

	/**
	 * A convenience method for defining a SamplingDefinition that returns a
	 * complete copy of an ImageDisplay.
	 * 
	 * @param display The ImageDisplay to sample
	 * @return The specified SamplingDefinition
	 */
	public static SamplingDefinition sampleAllPlanes(final ImageDisplay display) {
		final SamplingDefinition definition = new SamplingDefinition(display);
		for (int i = 0; i < display.numDimensions(); i++) {
			final AxisType axisType = display.axis(i).type();
			final long size = display.dimension(i);
			final AxisSubrange subrange = new AxisSubrange(0, size - 1);
			definition.constrain(axisType, subrange);
		}
		return definition;
	}

}
