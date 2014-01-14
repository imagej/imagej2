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

package imagej.data.overlay;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.roi.CompositeRegionOfInterest;

import org.scijava.Context;

//TODO
//  The various query methods might need to be defined such that it gets
//  composited info. Axes? Calibration? mins/maxes/realMins/realMaxes probably
//  work already. One thing we could do is enforce consistency of axes defs
//  and such when adding overlays.

/**
 * A composite of one or more overlays.
 * 
 * @author Barry DeZonia
 * @author Lee Kamentsky
 */
public class CompositeOverlay extends
	AbstractROIOverlay<CompositeRegionOfInterest>
{
	// -- type declarations --
	
	public enum Operation
	{
		AND, OR, XOR, NOT
	}

	// -- instance variables --
	
	private List< Overlay > overlays = new ArrayList< Overlay >();
	private List< Operation > operations = new ArrayList< Operation >();
	

	// -- CompositeOverlay methods --
	
	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public CompositeOverlay() {
		super(new CompositeRegionOfInterest(2));
	}
	
	public CompositeOverlay(final Context context) {
		this(context, 2);
	}

	public CompositeOverlay(final Context context, final int numDimensions) {
		super(context, new CompositeRegionOfInterest(numDimensions));
	}

	@Override
	public void move(double[] deltas) {
		for (Overlay o : overlays)
			o.move(deltas);
		recalcRegionOfInterest();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(java.io.ObjectInput in)
		throws IOException ,ClassNotFoundException
	{
		super.readExternal(in);
		overlays = (List<Overlay>) in.readObject();
		operations = (List< Operation>) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(overlays);
		out.writeObject(operations);
	}
	
	public void startWith(Overlay o) {
		overlays.clear();
		operations.clear();
		overlays.add(o);
		operations.add(Operation.OR);
		CompositeRegionOfInterest newRoi =
				new CompositeRegionOfInterest(o.getRegionOfInterest());
		setRegionOfInterest(newRoi);
	}
	
	public void and(Overlay o) {
		if (overlays.size() == 0) {
			startWith(o);
			return;
		}
		overlays.add(o);
		operations.add(Operation.AND);
		getRegionOfInterest().and(o.getRegionOfInterest());
	}
	
	public void or(Overlay o) {
		if (overlays.size() == 0) {
			startWith(o);
			return;
		}
		overlays.add(o);
		operations.add(Operation.OR);
		getRegionOfInterest().or(o.getRegionOfInterest());
	}
	
	public void xor(Overlay o) {
		if (overlays.size() == 0) {
			startWith(o);
			return;
		}
		overlays.add(o);
		operations.add(Operation.XOR);
		getRegionOfInterest().xor(o.getRegionOfInterest());
	}

	public void not(Overlay o) {
		// NB - A CompositeOverlay is expected to be made of one or more Overlays.
		// Must avoid a situation where you define nothing but a NOT which inverts
		// all of space. In that case we cannot sensibly define its containing
		// region of interest
		if (overlays.size() == 0) {
			throw new IllegalArgumentException(
				"CompositeOverlay cannot start with a NOT operation");
		}
		overlays.add(o);
		operations.add(Operation.NOT);
		getRegionOfInterest().not(o.getRegionOfInterest());
	}

	public void remove(Overlay o) {
		int i = 0;
		while (i < overlays.size()) {
			Overlay ovr = overlays.get(i);
			if (ovr == o) {
				overlays.remove(i);
				operations.remove(i);
			}
			else
				i++;
		}
		if (overlays.size() == 0)
			throw new IllegalArgumentException(
				"Modified CompositeOverlay now consists of no overlays");
		if (operations.get(0) == Operation.NOT)
			throw new IllegalArgumentException(
				"Modified CompositeOverlay now starts with a NOT operation");
		recalcRegionOfInterest();
	}

	public void doOperation(Operation op, Overlay o) {
		switch (op) {
			case AND: and(o); break;
			case OR:  or(o);  break;
			case XOR: xor(o); break;
			case NOT: not(o); break;
			default: throw new IllegalStateException("Unknown operation: "+op);
		}
	}
	
	public List<Overlay> getSubcomponents() {
		return Collections.unmodifiableList(overlays);
	}
	
	// -- private helpers --
	
	private void recalcRegionOfInterest() {
		CompositeRegionOfInterest newRoi =
				new CompositeRegionOfInterest(overlays.get(0).getRegionOfInterest());
		for (int i = 1; i < overlays.size(); i++) {
			Overlay o = overlays.get(i);
			Operation op = operations.get(i);
			switch (op) {
				case AND : newRoi.and(o.getRegionOfInterest()); break;
				case OR  : newRoi.or(o.getRegionOfInterest()); break;
				case XOR : newRoi.xor(o.getRegionOfInterest()); break;
				case NOT : newRoi.not(o.getRegionOfInterest()); break;
				default: throw new IllegalArgumentException("Unknown operation "+ op);
			}
		}
		setRegionOfInterest(newRoi);
	}
}
