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

import java.util.Random;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import imagej.data.Dataset;
import imagej.data.event.DatasetChangedEvent;
import imagej.event.Events;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Rect;

/**
 * Implements the same functionality as IJ1's Salt and Pepper plugin. Assigns
 * random pixels to 255 or 0. 0 and 255 assignments are each evenly balanced at
 * 2.5% of the image.
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
	
	private Rect selection;
	private Image<?> inputImage;
	private LocalizableByDimCursor<? extends RealType<?>> cursor;
	private int[] position;
	
	// -- public interface --

	@Override
	public void run() {
		checkInput();
		setupWorkingData();
		assignPixels();
		cleanup();
		Events.publish(new DatasetChangedEvent(input));
	}

	// -- private interface --

	private void checkInput() {
		if (input == null)
			throw new IllegalArgumentException("input Dataset is null");
		
		if (input.getImage() == null)
			throw new IllegalArgumentException("input Image is null");

		if (input.getImage().getNumDimensions() != 2)
			throw new IllegalArgumentException(
				"input image is not 2d but has " + 
				input.getImage().getNumDimensions() + " dimensions");
	}

	private void setupWorkingData() {
		inputImage = input.getImage();
		selection = input.getSelection();
		position = new int[inputImage.getNumDimensions()];
		cursor =
			(LocalizableByDimCursor<? extends RealType<?>>)
				inputImage.createLocalizableByDimCursor();
	}
	
	private void assignPixels() {
		Random rng = new Random();

		rng.setSeed(System.currentTimeMillis());

		double percentToChange = 0.05;

		long numPixels = (long) (inputImage.getNumPixels() * percentToChange);

		int ox = selection.x;
		int oy = selection.y;
		int w = selection.width;
		int h = selection.height;
		
		if (w <= 0) w = inputImage.getDimension(0);
		if (h <= 0) h = inputImage.getDimension(1);
		
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

	private void cleanup() {
		cursor.close(); // FINALLY close working cursor
	}
	
	/**
	 * sets a value at a specific (x,y) location in the image to a given value
	 */
	private void setPixel(int x, int y, double value) {
		position[0] = x;
		position[1] = y;

		cursor.setPosition(position);

		cursor.getType().setReal(value);
	}
}
