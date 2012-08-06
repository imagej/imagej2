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


/**
 * @author Barry DeZonia
 */
public class SamplingDefinition {
	private ImageDisplay display;
	private Map<AxisType,AxisSubrange> axisSubranges;
	private String err;
	
	private SamplingDefinition(ImageDisplay display) {
		this.display = display;
		this.axisSubranges = new HashMap<AxisType,AxisSubrange>();
		this.err = null;
	}
	
	public ImageDisplay getDisplay() { return display; }
	
	public String getError() { return err; }


	public AxisType[] getInputAxes() {
		return display.getAxes();
	}
	
	public List<List<Long>> getInputRanges() {
		AxisType[] axes = display.getAxes();
		List<List<Long>> axesDefs = new ArrayList<List<Long>>();
		for (AxisType axis : axes) {
			AxisSubrange subrange = axisSubranges.get(axis);
			List<Long> axisValues = subrange.getIndices();
			axesDefs.add(axisValues);
		}
		return Collections.unmodifiableList(axesDefs);
	}
	
	public AxisType[] getOutputAxes() {
		AxisType[] inputAxes = getInputAxes();
		List<List<Long>> inputRanges = getInputRanges();
		int dimCount = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) dimCount++;
		}
		AxisType[] outputAxes = new AxisType[dimCount];
		int d =  0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) outputAxes[d++] = inputAxes[i];
		}
		return outputAxes;
	}
	
	public long[] getOutputDims() {
		List<List<Long>> inputRanges = getInputRanges();
		int dimCount = 0;
		for (int i = 0; i < inputRanges.size(); i++) {
			if (inputRanges.get(i).size() > 1) dimCount++;
		}
		long[] outputDims = new long[dimCount];
		int d =  0;
		for (int i = 0; i < inputRanges.size(); i++) {
			int dimSize = inputRanges.get(i).size();
			if (dimSize > 1) outputDims[d++] = dimSize;
		}
		return outputDims;
	}
	
	public static SamplingDefinition sampleUVPlane(
		ImageDisplay display,	AxisType uAxis, AxisType vAxis)
	{
		SamplingDefinition definition = new SamplingDefinition(display);
		Data data = display.getActiveView().getData();
		AxisType[] axes = data.getAxes();
		for (AxisType axis : axes) {
			if ((axis == uAxis) || (axis == vAxis)) {
				int axisIndex = display.getAxisIndex(axis);
				long size = display.getExtents().dimension(axisIndex);
				AxisSubrange subrange = new AxisSubrange(display, 0, size-1);
				definition.constrain(axis, subrange);
			}
			else { // other axis
				long pos = display.getLongPosition(axis);
				AxisSubrange subrange = new AxisSubrange(display, pos);
				definition.constrain(axis, subrange);
			}
		}
		return definition;
	}
	
	public static SamplingDefinition sampleXYPlane(ImageDisplay display) {
		return sampleUVPlane(display, Axes.X, Axes.Y);
	}
	
	public static SamplingDefinition sampleCompositeUVPlane(
		ImageDisplay display, AxisType uAxis, AxisType vAxis)
	{
		if ((uAxis == Axes.CHANNEL) || (vAxis == Axes.CHANNEL))
			throw new IllegalArgumentException(
				"UV composite plane - cannot specify channels as one of the axes");
		SamplingDefinition definition = new SamplingDefinition(display);
		Data data = display.getActiveView().getData();
		AxisType[] axes = data.getAxes();
		for (AxisType axis : axes) {
			if ((axis == uAxis) || (axis == vAxis) || (axis == Axes.CHANNEL)) {
				int axisIndex = display.getAxisIndex(axis);
				long size = display.getExtents().dimension(axisIndex);
				AxisSubrange subrange = new AxisSubrange(display, 0, size-1);
				definition.constrain(axis, subrange);
			}
			else { // other axis
				long pos = display.getLongPosition(axis);
				AxisSubrange subrange = new AxisSubrange(display, pos);
				definition.constrain(axis, subrange);
			}
		}
		return definition;
	}

	public static SamplingDefinition sampleCompositeXYPlane(ImageDisplay display){
		return sampleCompositeUVPlane(display, Axes.X, Axes.Y);
	}
	
	public static SamplingDefinition sampleAllPlanes(ImageDisplay display) {
		SamplingDefinition definition = new SamplingDefinition(display);
		AxisType[] axes = display.getAxes();
		for (int i = 0; i < axes.length; i++) {
			AxisType axis = axes[i];
			long size = display.dimension(i);
			AxisSubrange subrange = new AxisSubrange(display, 0, size-1);
			definition.constrain(axis, subrange);
		}
		return definition;
	}
	
	public boolean constrain(AxisType axis, AxisSubrange subrange) {
		if (subrange.getError() != null) {
			err = subrange.getError();
			return false;
		}
		Data data = display.getActiveView().getData();
		int axisIndex = data.getAxisIndex(axis);
		if (axisIndex < 0) {
			err = "Undefined axis " + axis + " for display " + display.getName();
			return false;
		}
		List<Long> indices = subrange.getIndices();
		if (data.dimension(axisIndex) < indices.get(0)) {
			err = "Axis range fully beyond dimensions of display " +
					display.getName() + " for axis " + axis;
			return false;
		}
		axisSubranges.put(axis,  subrange);
		return true;
	}
}

