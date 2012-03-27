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

package imagej.data;

import imagej.util.ColorRGB;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A ChannelCollection maintains a list of data values. The data is read only.
 * One can request any channel number from a ChannelCollection. If there is no
 * data associated with a channel number the value returned is zero.
 */
public class ChannelCollection {
	
	// -- instance variables --
	
	private List<Double> channelInfos;
	private boolean areInteger;
	
	// -- constructors --
	
	/**
	 * Constructs a new empty channel collection. Any subsequent calls to
	 * getChannelValue() will return 0 for all channels. 
	 */
	public ChannelCollection() {
		this(new LinkedList<Double>());
	}
	
	/**
	 * Constructs a new channel collection from a list of Double values.
	 */
	public ChannelCollection(List<Double> values) {
		this.channelInfos = new LinkedList<Double>();
		channelInfos.addAll(values);
		// NB - make sure its always populated with at least one channel. This
		// simplifies API elsewhere (by always having a positive channel count)
		if (channelInfos.size() == 0) channelInfos.add(0.0);
		areInteger = true;
		for (Double value : channelInfos) {
			areInteger &= (value == Math.floor(value));
		}
	}

	/**
	 * Constructs a ChannelCollection containing the three RGB values of a given
	 * color.
	 */
	public ChannelCollection(ColorRGB color) {
		this(
			Arrays.asList(
				new Double[]{
					(double) color.getRed(),
					(double) color.getGreen(),
					(double) color.getBlue()}));
	}

	// -- public interface --
	
	/**
	 * Returns the number of channels in the collection 
	 */
	public long getChannelCount() {
		return channelInfos.size();
	}
	
	/**
	 * Gets the value of a channel in the collection
	 */
	public double getChannelValue(long chan) {
		if (chan > Integer.MAX_VALUE)
			throw new IllegalArgumentException("too many channels: "+chan);
		if (chan >= channelInfos.size()) return 0;
		return channelInfos.get((int)chan);
	}

	/**
	 * Returns true if all channels in the collection can be exactly represented
	 * with integer types. This information can be useful to determine whether
	 * you should display values with trailing decimal information or not.
	 */
	public boolean areInteger() {
		return areInteger;
	}

}

