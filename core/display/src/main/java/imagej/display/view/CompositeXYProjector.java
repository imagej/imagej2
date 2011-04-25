//
// CompositeXYProjector.java
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

package imagej.display.view;

import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.converter.Converter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.Img;

/**
 * Creates a composite image from multi-channel source. Takes a list of
 * {@link Converter}s to apply a LUT for each channel.
 * 
 * @author Grant Harris
 * @see XYProjector for the code upon which this class was based.
 */
public class CompositeXYProjector<A, B> extends XYProjector<A, B> {

	final protected ArrayList<Converter<A, B>> converters;
	private final int channelDimIndex;
	private final int numChannels;

	public CompositeXYProjector(final Img<A> source,
		final IterableInterval<B> target,
		final ArrayList<Converter<A, B>> converters, final int channelDimIndex)
	{
		super(source, target, null);
		this.converters = converters;
		this.channelDimIndex = channelDimIndex;
		// check that there is a converter for each channel
		numChannels = converters.size();
		System.out.println("source.dimension(channelDimIndex)=" +
			source.dimension(channelDimIndex));
		if (numChannels != source.dimension(channelDimIndex)) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void map() {
		final Cursor<B> targetCursor = target.cursor();
		final RandomAccess<A> sourceRandomAccess = source.randomAccess();
		for (int i = 0; i < numChannels; i++) {
			System.out.println("channel: " + i);
			position[channelDimIndex] = i;
			sourceRandomAccess.setPosition(position);
			targetCursor.reset();
			while (targetCursor.hasNext()) {
				final B b = targetCursor.next();
				sourceRandomAccess.setPosition(targetCursor.getLongPosition(0), 0);
				sourceRandomAccess.setPosition(targetCursor.getLongPosition(1), 1);
				converters.get(i).convert(sourceRandomAccess.get(), b);
			}
		}
	}

}
