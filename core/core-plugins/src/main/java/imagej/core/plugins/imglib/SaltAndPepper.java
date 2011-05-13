//
// SaltAndPepper.java
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

import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Index;
import imagej.util.IntRect;

import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Implements the same functionality as IJ1's Salt and Pepper plugin. Assigns
 * random pixels to 255 or 0. 0 and 255 assignments are each evenly balanced at
 * 2.5% of the image. Currently only works on 2d images.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Noise", mnemonic = 'n'),
	@Menu(label = "Salt and Pepper", weight = 3) })
public class SaltAndPepper implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	// -- other instance variables --
	
	private IntRect selection;
	private Img<? extends RealType<?>> inputImage;
	private RandomAccess<? extends RealType<?>> accessor;
	private long[] position;
	
	// -- public interface --

	@Override
	public void run() {
		checkInput();
		setupWorkingData();
		assignPixels();
		cleanup();
		input.update();
	}

	// -- private interface --

	private void checkInput() {
		if (input == null)
			throw new IllegalArgumentException("input Dataset is null");
		
		if (input.getImgPlus() == null)
			throw new IllegalArgumentException("input Image is null");
	}

	private void setupWorkingData() {
		inputImage = input.getImgPlus();
		selection = input.getSelection();
		position = new long[inputImage.numDimensions()];
		accessor = inputImage.randomAccess();
	}
	
	private void assignPixels() {
		Random rng = new Random();

		rng.setSeed(System.currentTimeMillis());

		long[] planeDims = new long[inputImage.numDimensions() - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.dimension(i+2);
		long[] planeIndex = new long[planeDims.length];
		long totalPlanes = Index.getTotalLength(planeDims);
		for (long plane = 0; plane < totalPlanes; plane++) {
			Index.index1DtoND(planeDims, plane, planeIndex);
			assignPixelsInXYPlane(planeIndex, rng);
		}
	}

	private void assignPixelsInXYPlane(long[] planeIndex, Random rng) {
		for (int i = 2; i < position.length; i++)
			position[i] = planeIndex[i-2];

		double percentToChange = 0.05;

		long[] dimensions = new long[inputImage.numDimensions()];
		inputImage.dimensions(dimensions);
		
		long numPixels = (long) (Index.getTotalLength(dimensions) * percentToChange);

		int ox = selection.x;
		int oy = selection.y;
		int w = selection.width;
		int h = selection.height;
		
		if (w <= 0) w = (int) dimensions[0];
		if (h <= 0) h = (int) dimensions[1];
		
		for (long p = 0; p < numPixels / 2; p++) {
			int randomX, randomY;

			randomX = ox + rng.nextInt(w);
			randomY = oy + rng.nextInt(h);
			setPixel(randomX, randomY, 255);

			randomX = ox + rng.nextInt(w);
			randomY = oy + rng.nextInt(h);
			setPixel(randomX, randomY, 0);
		}
	}
	
	/**
	 * sets a value at a specific (x,y) location in the image to a given value
	 */
	private void setPixel(int x, int y, double value) {
		position[0] = x;
		position[1] = y;

		accessor.setPosition(position);

		accessor.get().setReal(value);
	}

	private void cleanup() {
		// nothing to do
	}
}
