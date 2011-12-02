//
// AbstractData.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.data;

import imagej.ImageJ;
import imagej.data.event.DataCreatedEvent;
import imagej.data.event.DataDeletedEvent;
import imagej.data.roi.Overlay;
import imagej.event.EventService;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.meta.AxisType;

/**
 * Base implementation of {@link Data}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see Dataset
 * @see Overlay
 */
public abstract class AbstractData implements Data, Comparable<Data>,
	Externalizable
{

	// TODO: Eliminate these parallel lists in favor of a single List<Axis>.
	private final List<AxisType> axes = new ArrayList<AxisType>();
	private final List<Double> calibrations = new ArrayList<Double>();

	protected final EventService eventService;

	private String name;

	private int refs = 0;

	public AbstractData() {
		eventService = ImageJ.get(EventService.class);
	}

	// -- AbstractData methods --

	/**
	 * Informs interested parties that the data object has become relevant and
	 * should be registered. Called the first time the reference count is
	 * incremented. Classes that extend this class may choose to override this
	 * method to publish more specific events.
	 */
	protected void register() {
		eventService.publish(new DataCreatedEvent(this));
	}

	/**
	 * Informs interested parties that the data object is no longer relevant and
	 * should be deleted. Called when the reference count is decremented to zero.
	 * Classes that extend this class may choose to override this method to
	 * publish more specific events.
	 */
	protected void delete() {
		eventService.publish(new DataDeletedEvent(this));
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getName();
	}

	// -- Data methods --

	@Override
	public void incrementReferences() {
		refs++;
		if (refs == 1) register();
	}

	@Override
	public void decrementReferences() {
		if (refs == 0) {
			throw new IllegalStateException(
				"decrementing reference count when it is already 0");
		}
		refs--;
		if (refs == 0) delete();
	}

	// -- LabeledSpace methods --

	@Override
	public long[] getDims() {
		final long[] dims = new long[numDimensions()];
		getExtents().dimensions(dims);
		return dims;
	}

	@Override
	public AxisType[] getAxes() {
		final AxisType[] axes = new AxisType[numDimensions()];
		axes(axes);
		return axes;
	}

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		return axes.indexOf(axis);
	}

	@Override
	public AxisType axis(final int d) {
		if (d < 0 || d >= axes.size()) {
			throw new IllegalArgumentException("Index out of range: " + d);
		}
		return axes.get(d);
	}

	@Override
	public void axes(final AxisType[] axesToFill) {
		for (int i = 0; i < axesToFill.length && i < axes.size(); i++) {
			axesToFill[i] = axes.get(i);
		}
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		while (axes.size() <= d) {
			this.axes.add(null);
			this.calibrations.add(1.0);
		}
		this.axes.set(d, axis);
	}

	@Override
	public double calibration(final int d) {
		if (d >= calibrations.size()) return 1.0;
		return calibrations.get(d);
	}

	@Override
	public void calibration(final double[] cal) {
		for (int i = 0; (i < cal.length) && (i < this.calibrations.size()); i++) {
			cal[i] = this.calibrations.get(i);
		}
		if (cal.length > calibrations.size()) {
			Arrays.fill(cal, calibrations.size(), cal.length, 1.0);
		}
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		while (calibrations.size() <= d) {
			calibrations.add(1.0);
		}
		calibrations.set(d, cal);
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return axes.size();
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Data data) {
		return getName().compareTo(data.getName());
	}

	// -- Externalizable methods --

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(axes);
		out.writeObject(calibrations);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		final List<AxisType> axisList = (List<AxisType>) in.readObject();
		axes.clear();
		axes.addAll(axisList);
		final List<Double> calibrationList = (List<Double>) in.readObject();
		calibrations.clear();
		calibrations.addAll(calibrationList);
	}

}
