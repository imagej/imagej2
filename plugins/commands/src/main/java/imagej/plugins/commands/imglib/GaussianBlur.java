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

package imagej.plugins.commands.imglib;

import imagej.data.Dataset;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Does an in place Gaussian blur noise reduction operation on a {@link Dataset}
 * .
 * 
 * @author Barry DeZonia
 * @param <T>
 */
@Plugin(
	type = Command.class,
	menu = {
		@Menu(label = MenuConstants.PROCESS_LABEL,
			weight = MenuConstants.PROCESS_WEIGHT,
			mnemonic = MenuConstants.PROCESS_MNEMONIC),
		@Menu(label = "Filters", mnemonic = 'f'), @Menu(label = "Gaussian Blur...") },
	headless = true)
public class GaussianBlur<T extends RealType<T>> extends
	ContextCommand
{

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;
	
	@Parameter(label = "Sigma (radius)", min = "0.0001")
	private double sigma = 2;
	
	@Parameter(label = "Use units")
	private boolean useUnits = false;

	// -- Command methods --

	@Override
	public void run() {
		double[] sigmas = sigmas();
		@SuppressWarnings("unchecked")
		Img<T> target = (Img<T>) dataset.getImgPlus();
		Img<T> input = target.copy();
		ExtendedRandomAccessibleInterval<T, ?> paddedInput =
			Views.extendMirrorSingle(input);
		try {
			Gauss3.gauss(sigmas, paddedInput, target);
		}
		catch (Exception e) {
			cancel(e.getMessage());
		}
	}

	// -- helpers --

	private double[] sigmas() {
		double[] sigmas = new double[dataset.numDimensions()];
		for (int d = 0; d < sigmas.length; d++) {
			if (useUnits) {
				CalibratedAxis axis = dataset.axis(d);
				sigmas[d] = axis.rawValue(sigma) - axis.rawValue(0);
			}
			else {
				sigmas[d] = sigma;
			}
		}
		return sigmas;
	}

}
