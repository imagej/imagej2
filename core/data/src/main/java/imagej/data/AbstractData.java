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

package imagej.data;

import imagej.data.event.DataCreatedEvent;
import imagej.data.event.DataDeletedEvent;
import imagej.data.overlay.Overlay;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.meta.AxisType;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;

/**
 * Base implementation of {@link Data}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see Dataset
 * @see Overlay
 */
public abstract class AbstractData extends AbstractContextual implements Data,
	Comparable<Data>, Externalizable
{

	private String name;

	private int refs = 0;

	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public AbstractData() {
	}
	
	public AbstractData(final Context context) {
		setContext(context);
	}

	// -- AbstractData methods --

	/**
	 * Informs interested parties that the data object has become relevant and
	 * should be registered. Called the first time the reference count is
	 * incremented. Classes that extend this class may choose to override this
	 * method to publish more specific events.
	 */
	protected void register() {
		publish(new DataCreatedEvent(this));
	}

	/**
	 * Informs interested parties that the data object is no longer relevant and
	 * should be deleted. Called when the reference count is decremented to zero.
	 * Classes that extend this class may choose to override this method to
	 * publish more specific events.
	 */
	protected void delete() {
		publish(new DataDeletedEvent(this));
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

	// -- CalibratedInterval methods --

	@Override
	public AxisType[] getAxes() {
		final AxisType[] axes = new AxisType[numDimensions()];
		axes(axes);
		return axes;
	}

	@Override
	public Extents getExtents() {
		final long[] min = new long[numDimensions()];
		final long[] max = new long[numDimensions()];
		min(min);
		max(max);
		return new Extents(min, max);
	}

	@Override
	public long[] getDims() {
		final long[] dims = new long[numDimensions()];
		dimensions(dims);
		return dims;
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

	private final static String BOGUS_NAME = "NULL 42 PI E 8 GAMMA PHI WOOHOO!";
	
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		/* these seem like they should be handled by subclasses. so removing
		 * on 5-31-12 BDZ
		 *
		final AxisType[] axes = getAxes();
		final double[] cal = new double[axes.length];
		calibration(cal);
		out.writeObject(axes);
		out.writeObject(cal);
		*/
		if (name == null)
			out.writeUTF(BOGUS_NAME);
		else
			out.writeUTF(name);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		/* these seem like they should be handled by subclasses. so removing
		 * on 5-31-12 BDZ
		 *
		final AxisType[] axes = (AxisType[]) in.readObject();
		final double[] cal = (double[]) in.readObject();
		for (int d = 0; d < axes.length; d++) {
			setAxis(axes[d], d);
			setCalibration(cal[d], d);
		}
		*/
		name = in.readUTF();
		if (name.equals(BOGUS_NAME))
			name = null;
	}

	// -- Internal methods --

	protected void publish(final SciJavaEvent event) {
		final Context context = getContext();
		if (context == null) return;
		final EventService eventService = context.getService(EventService.class);
		if (eventService == null) return;
		eventService.publish(event);
	}

}
