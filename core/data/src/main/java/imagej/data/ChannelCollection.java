//
// ChannelCollection.java
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

import java.util.LinkedList;
import java.util.List;

/**
 * A ChannelCollection maintains a list of data values.
 */
public class ChannelCollection {
	
	// -- instance variables --
	
	private List<Double> channelInfos;
	private boolean areInteger;
	
	// -- constructors --
	
	/**
	 * Constructs a new channel collection. Until modified any subsequent calls to
	 * getChannelValue() will return 0 for all channels. 
	 */
	public ChannelCollection() {
		this.channelInfos = new LinkedList<Double>();
		init();
	}
	
	/**
	 * Constructs a new channel collection from a list of Double values.
	 */
	public ChannelCollection(List<Double> values) {
		this();
		for (int i = 0; i < values.size(); i++)
			setChannelValue(i, values.get(i));
	}

	/**
	 * Constructs a new channel collection from another ChannelCollection. Data
	 * values are copied rather than shared by reference.
	 */
	public ChannelCollection(ChannelCollection channels) {
		this();
		assignChannels(channels);
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
	 * Sets the value of a channel in the collection
	 * @param i - channel number
	 * @param val - channel value
	 */
	public void setChannelValue(long i, double val) {
		if (i > Integer.MAX_VALUE)
			throw new IllegalArgumentException("too many channels: "+i);
		while (i >= channelInfos.size()) {
			channelInfos.add(0.0);
		}
		channelInfos.set((int)i, val);
		areInteger &= (val == Math.floor(val));
	}

	/**
	 * Resets channel collection such that it will return 0 for any channel
	 * value query.
	 */
	public void resetChannels() {
		channelInfos.clear();
		init();
	}

	/**
	 * Resets channel collection values to a set of provided channels. Data is
	 * copied rather than referenced.
	 */
	public void resetChannels(ChannelCollection channels) {
		resetChannels();
		assignChannels(channels);
	}
	
	/**
	 * Returns true if all channels in the collection can be exactly represented
	 * with integer types. This information can be useful to determine whether
	 * you should display values with trailing decimal information or not.
	 */
	public boolean areInteger() {
		return areInteger;
	}

	// -- private helpers --

	/**
	 * Always populate with a dummy channel. This simplifies API elsewhere.
	 */
	private void init() {
		channelInfos.add(0.0);
		areInteger = true;
	}

	/**
	 * Assigns channels from another channel collection. Does not reset current
	 * number of channels.
	 */
	private void assignChannels(ChannelCollection values) {
		for (long i = 0; i < values.getChannelCount(); i++) {
			double val = values.getChannelValue(i);
			setChannelValue(i, val);
		}
	}
}

