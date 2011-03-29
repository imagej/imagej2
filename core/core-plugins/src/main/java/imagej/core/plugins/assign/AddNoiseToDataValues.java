//
// AddNoiseToDataValues.java
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

package imagej.core.plugins.assign;

import imagej.model.Dataset;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AddNoise;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * Fills an output Dataset by applying random noise to an input Dataset. This
 * class is used by AddDefaultNoiseToDataValues and
 * AddSpecificNoiseToDataValues. They each manipulate setStdDev(). This class
 * can be used to implement simple (1 pixel neighborhood) gaussian noise
 * addition without requiring a plugin.
 * 
 * @author Barry DeZonia
 */
public class AddNoiseToDataValues {

	// -- instance variables --

	/** the input Dataset that contains original values */
	private Dataset input;

	/** the output Dataset that will contain perturbed values */
	private Dataset output;

	/**
	 * the stand deviation of the gaussian random value used to create perturbed
	 * values
	 */
	private double rangeStdDev;

	/**
	 * maximum allowable values - varies by underlying data type. For instance
	 * (0,255) for 8 bit and (0,65535) for 16 bit. used to make sure that
	 * perturned values do not leave the allowable range for the underlying data
	 * type.
	 */
	private double rangeMin, rangeMax;

	// -- constructor --

	/**
	 * constructor - takes an input Dataset as the baseline data to compute
	 * perturbed values from.
	 */
	public AddNoiseToDataValues(Dataset input) {
		this.input = input;
	}

	// -- public interface --

	/**
	 * use this method to specify the output Dataset that will hold output data.
	 * if this method is not called then the add noise operation defaults to
	 * creating a new output Dataset and returning it from the run() method.
	 */
	public void setOutput(Dataset output) {
		this.output = output;
	}

	/**
	 * specify the standard deviation of the gaussian range desired. affects the
	 * distance of perturbation of each data value.
	 */
	protected void setStdDev(double stdDev) {
		this.rangeStdDev = stdDev;
	}

	/** runs the operation and returns the Dataset that contains the output data */
	public Dataset run() {
		calcTypeMinAndMax();

		UnaryOperator op = new AddNoise(rangeMin, rangeMax, rangeStdDev);

		UnaryTransformation transform = new UnaryTransformation(input, output, op);

		return transform.run();
	}

	// -- private interface --

	/**
	 * calculates the min and max allowable data range for the image : depends
	 * upon its underlying data type
	 */
	private void calcTypeMinAndMax() {
		Cursor<? extends RealType<?>> cursor =
			(Cursor<? extends RealType<?>>) input.getImage().createCursor();
		rangeMin = cursor.getType().getMinValue();
		rangeMax = cursor.getType().getMaxValue();
		cursor.close();
	}

}
