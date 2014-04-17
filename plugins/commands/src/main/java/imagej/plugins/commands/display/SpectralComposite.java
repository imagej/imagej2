/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.display;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.display.DatasetView;
import imagej.menu.MenuConstants;
import net.imglib2.display.ColorTable8;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * Creates a series of lookup tables according to the given range of
 * wavelengths, compositing them together to produce the final visualization.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Color"),
	@Menu(label = "Spectral Composite...") })
public class SpectralComposite extends ContextCommand {

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private DatasetView datasetView;

	@Parameter(label = "Starting wavelength", min = "1")
	private int startWave = 400;

	@Parameter(label = "Ending wavelength", min = "1")
	private int endWave = 700;

	// -- Runnable methods --

	@Override
	public void run() {
		spectralComposite();
	}

	public void setView(DatasetView view) {
		datasetView = view;
	}
	
	public DatasetView getView() {
		return datasetView;
	}

	public void setStartingWavelength(int waveLength) {
		startWave = waveLength;
	}
	
	public int getStartingWavelength() {
		return startWave;
	}
	
	public void setEndingWavelength(int waveLength) {
		endWave = waveLength;
	}
	
	public int getEndingWavelength() {
		return endWave;
	}
	
	// -- Utility methods --

	/**
	 * Converts a wavelength of light to an RGB color.
	 * <p>
	 * Thanks to <a href="http://miguelmoreno.net/sandbox/wavelengthtoRGB/">Miguel
	 * Moreno</a> for the code, based on <a
	 * href="http://www.physics.sfasu.edu/astro/color/spectra.html">Dan Bruton's
	 * algorithm</a>.
	 * </p>
	 */
	public static ColorRGB wavelengthToColor(final int wavelength) {
		final double blue;
		final double green;
		final double red;
		if (wavelength >= 350 && wavelength <= 439) {
			red = -(wavelength - 440d) / (440d - 350d);
			green = 0.0;
			blue = 1.0;
		}
		else if (wavelength >= 440 && wavelength <= 489) {
			red = 0.0;
			green = (wavelength - 440d) / (490d - 440d);
			blue = 1.0;
		}
		else if (wavelength >= 490 && wavelength <= 509) {
			red = 0.0;
			green = 1.0;
			blue = -(wavelength - 510d) / (510d - 490d);

		}
		else if (wavelength >= 510 && wavelength <= 579) {
			red = (wavelength - 510d) / (580d - 510d);
			green = 1.0;
			blue = 0.0;
		}
		else if (wavelength >= 580 && wavelength <= 644) {
			red = 1.0;
			green = -(wavelength - 645d) / (645d - 580d);
			blue = 0.0;
		}
		else if (wavelength >= 645 && wavelength <= 780) {
			red = 1.0;
			green = 0.0;
			blue = 0.0;
		}
		else {
			red = 0.0;
			green = 0.0;
			blue = 0.0;
		}

		final double factor;
		if (wavelength >= 350 && wavelength <= 419) {
			factor = 0.3 + 0.7 * (wavelength - 350d) / (420d - 350d);
		}
		else if (wavelength >= 420 && wavelength <= 700) {
			factor = 1.0;
		}
		else if (wavelength >= 701 && wavelength <= 780) {
			factor = 0.3 + 0.7 * (780d - wavelength) / (780d - 700d);
		}
		else {
			factor = 0.0;
		}

		final double gamma = 1.00;
		final int intensityMax = 255;
		final int r = factorAdjust(red, factor, intensityMax, gamma);
		final int g = factorAdjust(green, factor, intensityMax, gamma);
		final int b = factorAdjust(blue, factor, intensityMax, gamma);

		return new ColorRGB(r, g, b);
	}

	public static int factorAdjust(final double color, final double factor,
		final int intensityMax, final double gamma)
	{
		if (color == 0.0) return 0;
		return (int) Math.round(intensityMax * Math.pow(color * factor, gamma));
	}

	// -- Helper methods --

	private void spectralComposite() {
		final int cIndex = datasetView.getCompositeDimIndex();
		final int cSize =
			cIndex < 0 ? 1 : (int) datasetView.getData().dimension(cIndex);
		for (int c = 0; c < cSize; c++) {
			final int wavelength =
				startWave + (endWave - startWave) * c / (cSize - 1);
			final ColorRGB color = wavelengthToColor(wavelength);
			final int lutLength = 256;
			final byte[][] values = new byte[3][lutLength];
			for (int i = 0; i < lutLength; i++) {
				values[0][i] = (byte) (color.getRed() * i / (lutLength - 1));
				values[1][i] = (byte) (color.getGreen() * i / (lutLength - 1));
				values[2][i] = (byte) (color.getBlue() * i / (lutLength - 1));
			}
			final ColorTable8 colorTable = new ColorTable8(values);
			datasetView.setColorTable(colorTable, c);
		}
		datasetView.setComposite(true);
	}

}
