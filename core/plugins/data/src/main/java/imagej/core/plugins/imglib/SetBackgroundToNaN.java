//
// SetBackgroundToNaN.java
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
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Fills an output Dataset with the values of an input Dataset. All the values
 * in the input Dataset that are outside user defined thresholds are assigned
 * NaN.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "NaN Background", weight = 17) })
public class SetBackgroundToNaN implements ImageJPlugin {

	// -- instance variables --

	@Parameter
	private Dataset input;

	@Parameter(label =
		"TODO - should use current threshold - for now ask - Low threshold")
	private double loThreshold;

	@Parameter(label =
		"TODO - should use current threshold - for now ask - High threshold")
	private double hiThreshold;

	private Img<? extends RealType<?>> inputImage;
	
	// -- public interface --

	@Override
	public void run() {
		if (input.isInteger()) return; // FIXME: show error message
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
			
		if (loThreshold > hiThreshold)
			throw new IllegalArgumentException(
				"threshold values incorrectly specified (min > max)");
	}
	
	private void setupWorkingData() {
		inputImage = input.getImgPlus();
	}
	
	private void assignPixels() {
		Cursor<? extends RealType<?>> cursor = inputImage.cursor();

		while (cursor.hasNext()) {
			cursor.fwd();
			
			double inputValue = cursor.get().getRealDouble();

			if ((inputValue < loThreshold) || (inputValue > hiThreshold))
				cursor.get().setReal(Double.NaN);
		}
	}
	
	private void cleanup() {
		// nothing to do
	}
}
