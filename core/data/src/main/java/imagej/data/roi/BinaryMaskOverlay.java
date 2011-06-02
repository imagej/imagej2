//
// BinaryMaskOverlay.java
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
package imagej.data.roi;
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
 * @author Lee Kamentsky
 *
 */
public class BinaryMaskOverlay extends AbstractOverlay {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> roi;

	public BinaryMaskOverlay() {
	}
	
	public BinaryMaskOverlay(BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> roi) {
		this.roi = roi;
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		final BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> roi = getRegionOfInterest();
		final BitType b = new BitType();
		b.set(true);
		final RandomAccessible<BitType> ra = new ConstantRandomAccessible<BitType>(b, roi.numDimensions());
		IterableInterval <BitType> ii = roi.getIterableIntervalOverROI(ra);
		Cursor<BitType> c = ii.localizingCursor();
		
		out.writeInt(roi.numDimensions());
		for (int i=0; i < roi.numDimensions(); i++) {
			out.writeLong(ii.dimension(i));
		}
		/*
		 * This is a run-length encoding of the binary mask. The method is similar to PNG.
		 */
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(new DeflaterOutputStream(s));
		long initial_position [] = new long [roi.numDimensions()];
		long next_position [] = new long [roi.numDimensions()];
		Arrays.fill(initial_position, Long.MIN_VALUE);
		long run = 0;
		while(c.hasNext()) {
			c.next();
			next_position[0] = initial_position[0] + run;
			for (int i=0; i < roi.numDimensions(); i++) {
				if (next_position[i] != c.getLongPosition(i)) {
					if (run > 0) {
						ds.writeLong(run);
						for (int j=0; j<roi.numDimensions(); j++) { 
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
			for (int j=0; j<roi.numDimensions(); j++) { 
				ds.writeLong(initial_position[j]);
			}
		}
		/*
		 * The end is signaled by a run of length 0
		 */
		ds.writeLong(0);
		ds.close();
		byte [] buffer = s.toByteArray();
		out.writeInt(buffer.length);
		out.write(buffer);
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		int nDimensions = in.readInt();
		long [] dimensions = new long [nDimensions];
		for (int i=0; i<nDimensions; i++) {
			dimensions[i] = in.readLong();
		}
		NativeImg<BitType, BitAccess> img = new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		BitType t = new BitType(img);
		img.setLinkedType(t);
		RandomAccess<BitType> ra = img.randomAccess();
		byte [] buffer = new byte[in.readInt()];
		in.read(buffer);
		ByteArrayInputStream s = new ByteArrayInputStream(buffer);
		DataInputStream ds = new DataInputStream(new InflaterInputStream(s));
		long position[] = new long[nDimensions];
		while(true) {
			long run = ds.readLong();
			if (run == 0) break;
			for (int i=0; i<nDimensions; i++) {
				position[i] = ds.readLong();
			}
			for (int i=0; i<run; i++) {
				ra.setPosition(position);
				position[0] ++;
				ra.get().set(true);
			}
		}
		roi = new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(img);
	}
	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#getRegionOfInterest()
	 */
	@Override
	public BinaryMaskRegionOfInterest<? extends BitType, ? extends Img<BitType>> getRegionOfInterest() {
		return roi;
	}

}
