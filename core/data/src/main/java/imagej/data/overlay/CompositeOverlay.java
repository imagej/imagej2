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

package imagej.data.overlay;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imagej.ImageJ;
import net.imglib2.roi.CompositeRegionOfInterest;

/**
 * A composite of several overlays.
 * 
 * @author Barry DeZonia
 * @author Lee Kamentsky
 */
public class CompositeOverlay extends
	AbstractROIOverlay<CompositeRegionOfInterest>
{
	private enum Operation
	{
		AND, OR, XOR, NOT
	}

	private List< Overlay > overlays = new ArrayList< Overlay >();
	private List< Operation > operations = new ArrayList< Operation >();
	

	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public CompositeOverlay() {
		super(new CompositeRegionOfInterest(2));
	}
	
	public CompositeOverlay(final ImageJ context) {
		this(context, 2);
	}

	public CompositeOverlay(final ImageJ context, final int numDimensions) {
		super(context, new CompositeRegionOfInterest(numDimensions));
	}

	public CompositeOverlay(ImageJ context, CompositeRegionOfInterest croi) {
		super(context,croi);
		throw new UnsupportedOperationException("this constructor now obseolete");
	}

	@Override
	public void move(double[] deltas) {
		for (Overlay o : overlays)
			o.move(deltas);
		getRegionOfInterest().move(deltas);
	}

	/*
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
	*/
	
	public void startWith(Overlay o) {
		overlays.clear();
		operations.clear();
		overlays.add(o);
		CompositeRegionOfInterest newRoi =
				new CompositeRegionOfInterest(o.getRegionOfInterest());
		setRegionOfInterest(newRoi);
	}
	
	public void and(Overlay o) {
		overlays.add(o);
		operations.add(Operation.AND);
		getRegionOfInterest().and(o.getRegionOfInterest());
	}
	
	public void or(Overlay o) {
		overlays.add(o);
		operations.add(Operation.OR);
		getRegionOfInterest().or(o.getRegionOfInterest());
	}
	
	public void xor(Overlay o) {
		overlays.add(o);
		operations.add(Operation.XOR);
		getRegionOfInterest().xor(o.getRegionOfInterest());
	}

	/*
	public void not() {
		operations.add(Operation.NOT);
		getRegionOfInterest().not();
	}
	*/
}
