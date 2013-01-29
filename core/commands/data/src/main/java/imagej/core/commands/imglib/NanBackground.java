/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.imglib;

import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;
import imagej.module.ItemIO;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
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
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "NaN Background", weight = 18) }, headless = true)
public class NanBackground extends ContextCommand {

	// -- instance variables --

	@Parameter(type = ItemIO.BOTH)
	private Dataset input;

	@Parameter(
		label = "TODO - should use current threshold - for now ask - Low threshold")
	private double loThreshold;

	@Parameter(
		label = "TODO - should use current threshold - for now ask - High threshold")
	private double hiThreshold;

	private Img<? extends RealType<?>> inputImage;

	// -- public interface --

	@Override
	public void run() {
		if (inputBad()) return;
		setupWorkingData();
		assignPixels();
		input.update();
	}

	public void setDataset(Dataset data) {
		this.input = data;
	}
	
	public Dataset getDataset() {
		return input;
	}

	// -- private interface --

	private boolean inputBad() {
		if (input.isInteger()) {
			cancel("This plugin requires a floating point dataset");
			return true;
		}
		if (input == null) {
			cancel("Input dataset is null");
			return true;
		}
		
		if (input.getImgPlus() == null) {
			cancel("Input ImgPlus is null");
			return true;
		}

		if (loThreshold > hiThreshold) {
			cancel("Threshold values incorrectly specified (min > max)");
			return true;
		}
		return false;
	}

	private void setupWorkingData() {
		inputImage = input.getImgPlus();
	}

	private void assignPixels() {
		final Cursor<? extends RealType<?>> cursor = inputImage.cursor();

		while (cursor.hasNext()) {
			cursor.fwd();

			final double inputValue = cursor.get().getRealDouble();

			if ((inputValue < loThreshold) || (inputValue > hiThreshold))
				cursor.get().setReal(Double.NaN);
		}
	}

}
