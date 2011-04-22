//
// DuplicateImage.java
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

package imagej.core.plugins.imglib;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.data.Metadata;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Fills an output Dataset with the contents of an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Duplicate...", accelerator="shift control D")})
public class DuplicateImage implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	@Parameter(output = true)
	private Dataset output;

	@Parameter(label="Output filename")
	private String outputFilename;

	// TODO - make a parameter field called file extension with predefined choices?
	
	// -- public interface --

	@Override
	public void run() {
		Img<? extends RealType<?>> image = cloneImage(input.getImage());
		Metadata metadata = new Metadata();
		metadata.copyFrom(input.getMetadata());
		metadata.setName(outputFilename);
		output = new Dataset(image, metadata);
	}
	
	// -- private interface --
	
	private Img<? extends RealType<?>> cloneImage(Img image) {
		// TODO - used to be able to call Image::clone()
		//  For now copy data by hand
		
		long[] dimensions = new long[image.numDimensions()];
		for (int i = 0; i < dimensions.length; i++)
			dimensions[i] = image.dimension(i);
		
		Img<? extends RealType<?>> copyOfImg =
			image.factory().create(dimensions, image.firstElement());
		
		long[] position = new long[dimensions.length];
		
		Cursor<? extends RealType<?>> cursor = image.cursor();

		RandomAccess<? extends RealType<?>> access = copyOfImg.randomAccess();
		
		while (cursor.hasNext()) {
			cursor.next();
			double currValue = cursor.get().getRealDouble();
			for (int i = 0; i < position.length; i++)
				position[i] = cursor.getLongPosition(i);
			access.setPosition(position);
			access.get().setReal(currValue);
		}
		
		return copyOfImg;
	}
}
