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

import imagej.ImageJ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.sampler.special.ConstantRandomAccessible;
import net.imglib2.type.logic.BitType;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
public class BinaryMaskOverlay extends AbstractOverlay {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> roi;

	// TODO: Decide whether to keep this noargs constructor.
	// It is currently present only so that TestBinaryMaskOverlay code passes
	// when deserializing. It points to a larger issue, though, which is that
	// Externalizable objects may need a noargs constructor to work properly.

	public BinaryMaskOverlay() {
		super(null);
	}

	public BinaryMaskOverlay(final ImageJ context) {
		super(context);
	}

	public BinaryMaskOverlay(
		final ImageJ context,
		final BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> roi)
	{
		super(context);
		this.roi = roi;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		final BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> theRoi =
			getRegionOfInterest();
		final BitType b = new BitType();
		b.set(true);
		final RandomAccessible<BitType> ra =
			new ConstantRandomAccessible<BitType>(b, theRoi.numDimensions());
		final IterableInterval<BitType> ii = theRoi.getIterableIntervalOverROI(ra);
		final Cursor<BitType> c = ii.localizingCursor();

		out.writeInt(theRoi.numDimensions());
		for (int i = 0; i < theRoi.numDimensions(); i++) {
			out.writeLong(ii.dimension(i));
		}
		/*
		 * This is a run-length encoding of the binary mask. The method is similar to PNG.
		 */
		final ByteArrayOutputStream s = new ByteArrayOutputStream();
		final DataOutputStream ds =
			new DataOutputStream(new DeflaterOutputStream(s));
		final long initial_position[] = new long[theRoi.numDimensions()];
		final long next_position[] = new long[theRoi.numDimensions()];
		Arrays.fill(initial_position, Long.MIN_VALUE);
		long run = 0;
		while (c.hasNext()) {
			c.next();
			next_position[0] = initial_position[0] + run;
			for (int i = 0; i < theRoi.numDimensions(); i++) {
				if (next_position[i] != c.getLongPosition(i)) {
					if (run > 0) {
						ds.writeLong(run);
						for (int j = 0; j < theRoi.numDimensions(); j++) {
							ds.writeLong(initial_position[j]);
						}
					}
					run = 0;
					c.localize(initial_position);
					c.localize(next_position);
					break;
				}
			}
			run++;
		}
		if (run > 0) {
			ds.writeLong(run);
			for (int j = 0; j < theRoi.numDimensions(); j++) {
				ds.writeLong(initial_position[j]);
			}
		}
		/*
		 * The end is signaled by a run of length 0
		 */
		ds.writeLong(0);
		ds.close();
		final byte[] buffer = s.toByteArray();
		out.writeInt(buffer.length);
		out.write(buffer);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		super.readExternal(in);
		final int nDimensions = in.readInt();
		final long[] dimensions = new long[nDimensions];
		for (int i = 0; i < nDimensions; i++) {
			dimensions[i] = in.readLong();
		}
		final NativeImg<BitType, BitAccess> img =
			new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		final BitType t = new BitType(img);
		img.setLinkedType(t);
		final RandomAccess<BitType> ra = img.randomAccess();
		final byte[] buffer = new byte[in.readInt()];
		in.read(buffer);
		final ByteArrayInputStream s = new ByteArrayInputStream(buffer);
		final DataInputStream ds = new DataInputStream(new InflaterInputStream(s));
		final long position[] = new long[nDimensions];
		while (true) {
			final long run = ds.readLong();
			if (run == 0) break;
			for (int i = 0; i < nDimensions; i++) {
				position[i] = ds.readLong();
			}
			for (int i = 0; i < run; i++) {
				ra.setPosition(position);
				position[0]++;
				ra.get().set(true);
			}
		}
		roi = new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(img);
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#getRegionOfInterest()
	 */
	@Override
	public BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>>
		getRegionOfInterest()
	{
		return roi;
	}

}
