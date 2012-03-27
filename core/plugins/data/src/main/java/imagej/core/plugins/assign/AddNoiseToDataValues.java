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

package imagej.core.plugins.assign;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import net.imglib2.ops.operation.unary.real.RealAddNoise;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Fills an output Dataset by applying random noise to an input Dataset. This
 * class is used by AddDefaultNoiseToDataValues and
 * AddSpecificNoiseToDataValues. They each manipulate setStdDev(). This class
 * can be used to implement simple (1 pixel neighborhood) gaussian noise
 * addition without requiring a plugin.
 * 
 * @author Barry DeZonia
 */
public class AddNoiseToDataValues<T extends RealType<T>> {

	// -- instance variables --

	private final ImageDisplay display;

	/**
	 * The stand deviation of the gaussian random value used to create perturbed
	 * values
	 */
	private double rangeStdDev;

	/**
	 * Maximum allowable values - varies by underlying data type. For instance
	 * (0,255) for 8 bit and (0,65535) for 16 bit. Used to make sure that returned
	 * values do not leave the allowable range for the underlying data type.
	 */
	private double rangeMin, rangeMax;

	// -- constructor --

	/**
	 * Constructor - takes an input Dataset as the baseline data to compute
	 * perturbed values from.
	 */
	public AddNoiseToDataValues(final ImageDisplay display) {
		this.display = display;
	}

	// -- public interface --

	/**
	 * Specify the standard deviation of the gaussian range desired. affects the
	 * distance of perturbation of each data value.
	 */
	protected void setStdDev(final double stdDev) {
		this.rangeStdDev = stdDev;
	}

	/**
	 * Runs the operation and returns the Dataset that contains the output data
	 */
	public void run() {
		calcTypeMinAndMax();

		final RealAddNoise<DoubleType, DoubleType> op =
			new RealAddNoise<DoubleType,DoubleType>(rangeMin, rangeMax, rangeStdDev);

		final InplaceUnaryTransform<T,DoubleType> transform =
			new InplaceUnaryTransform<T,DoubleType>(display, op, new DoubleType());

		transform.run();
	}

	// -- private interface --

	/**
	 * Calculates the min and max allowable data range for the image : depends
	 * upon its underlying data type
	 */
	private void calcTypeMinAndMax() {
		final Dataset input =
			ImageJ.get(ImageDisplayService.class).getActiveDataset(display);
		rangeMin = input.getType().getMinValue();
		rangeMax = input.getType().getMaxValue();
	}

}
