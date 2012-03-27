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

package imagej.core.plugins.app;

import imagej.data.Dataset;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Mysterious!
 * 
 * @author Curtis Rueden
 */
@Plugin(label = "| It's a secret to everyone |", headless = true)
public class EasterEgg implements ImageJPlugin {

	private static final String CHARS = "#O*o+-,. ";

	@Parameter
	public Dataset dataset;

	@Parameter(type = ItemIO.OUTPUT)
	public String ascii;

	@Override
	public void run() {
		final double min = dataset.getChannelMinimum(0);
		final double max = dataset.getChannelMaximum(0);
		if (min == max) return; // no range

		final ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		final int colCount = (int) imgPlus.dimension(0);
		final int rowCount = (int) imgPlus.dimension(1);

		final RandomAccess<? extends RealType<?>> access = imgPlus.randomAccess();
		final StringBuilder sb = new StringBuilder();
		for (int r = 0; r < rowCount; r++) {
			access.setPosition(r, 1);
			for (int c = 0; c < colCount; c++) {
				access.setPosition(c, 0);
				final double value = access.get().getRealDouble();
				sb.append(getChar(value, min, max));
			}
			sb.append("\n");
		}
		ascii = sb.toString();
	}

	// -- Helper methods --

	private char getChar(final double value, final double min, final double max) {
		final int len = CHARS.length();
		final double norm = (value - min) / (max - min); // normalized to [0, 1]
		final int index = (int) (len * norm);
		if (index < 0) return CHARS.charAt(0);
		if (index >= len) return CHARS.charAt(len - 1);
		return CHARS.charAt(index);
	}

}
