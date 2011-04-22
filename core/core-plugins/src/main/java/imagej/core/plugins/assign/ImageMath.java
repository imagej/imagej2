//
// ImageMath.java
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

import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.ops.function.p2.BinaryOperatorFunction;
import net.imglib2.ops.operator.BinaryOperator;
import net.imglib2.ops.operator.binary.Add;
import net.imglib2.ops.operator.binary.And;
import net.imglib2.ops.operator.binary.Average;
import net.imglib2.ops.operator.binary.Copy;
import net.imglib2.ops.operator.binary.CopyTransparentZero;
import net.imglib2.ops.operator.binary.Difference;
import net.imglib2.ops.operator.binary.Divide;
import net.imglib2.ops.operator.binary.Max;
import net.imglib2.ops.operator.binary.Min;
import net.imglib2.ops.operator.binary.Multiply;
import net.imglib2.ops.operator.binary.Or;
import net.imglib2.ops.operator.binary.Subtract;
import net.imglib2.ops.operator.binary.Xor;

import java.util.Arrays;
import java.util.HashMap;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;

// NOTE - attempted to use the ImageCalculator in Imglib using
// ImglibOutputAlgorithmRunner but could not solve compiler errors and
// warnings. That test implementation saved below commented out. For now
// I'll do this via imglib-ops and hatch binary ops as needed. Even with
// ImageCalculator attempt I'd need to hatch multiple Functions somewhere.

/**
 * Fills an output Dataset with a combination of two input Datasets. The
 * combination is specified by the user (such as Add, Min, Average, etc.).
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Image Calculator...", weight = 13) })
public class ImageMath implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input1;

	@Parameter
	private Dataset input2;

	@Parameter(output = true)
	private Dataset output;

	@Parameter(label = "Operation to do between the two input images",
		choices = { "Add", "Subtract", "Multiply", "Divide", "AND", "OR", "XOR",
			"Min", "Max", "Average", "Difference", "Copy", "Transparent-zero" })
	private String operatorName;

	// -- other instance variables --

	private HashMap<String, BinaryOperator> operators;

	// -- constructor --

	/**
	 * constructs the ImageMath object by initializing which binary operations are
	 * avaialable.
	 */
	public ImageMath() {
		operators = new HashMap<String, BinaryOperator>();

		operators.put("Add", new Add());
		operators.put("Subtract", new Subtract());
		operators.put("Multiply", new Multiply());
		operators.put("Divide", new Divide());
		operators.put("AND", new And());
		operators.put("OR", new Or());
		operators.put("XOR", new Xor());
		operators.put("Min", new Min());
		operators.put("Max", new Max());
		operators.put("Average", new Average());
		operators.put("Difference", new Difference());
		operators.put("Copy", new Copy());
		operators.put("Transparent-zero", new CopyTransparentZero());
	}

	// -- public interface --

	/**
	 * runs the plugin filling the output image with the user specified binary
	 * combination of the two input images.
	 */
	@Override
	public void run() {
		long[] img1Dims = new long[input1.getImage().numDimensions()];
		input1.getImage().dimensions(img1Dims);

		long[] img2Dims = new long[input2.getImage().numDimensions()];
		input2.getImage().dimensions(img2Dims);

		if (!Arrays.equals(img1Dims, img2Dims)) throw new IllegalArgumentException(
			"ImageMath requires the two input images to have the same dimensions");

		BinaryOperator binOp = operators.get(operatorName);

		BinaryOperatorFunction binaryFunction = new BinaryOperatorFunction(binOp);

		NAryOperation operation = new NAryOperation(input1, input2, binaryFunction);

		operation.setOutput(output);

		output = operation.run();
	}

}

/*
 * 
 * import net.imglib2.algorithm.OutputAlgorithm; import
 * net.imglib2.algorithm.math.ImageCalculator; import
 * net.imglib2.function.Function; import net.imglib2.type.numeric.RealType;
 * 
 * public class ImageMath extends ImglibOutputAlgorithmPlugin {
 * 
 * @Parameter(label="Operation to do between the two input images",
 * choices={"Add"
 * ,"Subtract","Multiply","Divide","AND","OR","XOR","Min","Max","Average"
 * ,"Difference","Copy","Transparent-zero"}) String operator;
 * 
 * @Parameter Dataset input1;
 * 
 * @Parameter Dataset input2; private ImageCalculator<? extends RealType<?>, ?
 * extends RealType<?>, ? extends RealType<?>> calculator;
 * 
 * @Override public void run() { // TODO - temp hack for testing purposes if (in
 * == null) { //TODO - set in to an arraylist of two datasets backed with same
 * size images of same type }
 * 
 * setupCalculator();
 * 
 * setAlgorithm(calculator);
 * 
 * super.run(); }
 * 
 * private void setupCalculator() { Function function;
 * 
 * if (operator.equals("Add")) function = new BinaryAddFunction(); else if
 * (operator.equals("Subtract")) function = new BinaryAddFunction(); else if
 * (operator.equals("Multiply")) function = new BinaryAddFunction(); else if
 * (operator.equals("Divide")) function = new BinaryAddFunction(); else if
 * (operator.equals("AND")) function = new BinaryAddFunction(); else if
 * (operator.equals("OR")) function = new BinaryAddFunction(); else if
 * (operator.equals("XOR")) function = new BinaryAddFunction(); else if
 * (operator.equals("Min")) function = new BinaryAddFunction(); else if
 * (operator.equals("Max")) function = new BinaryAddFunction(); else if
 * (operator.equals("Average")) function = new BinaryAddFunction(); else if
 * (operator.equals("Difference")) function = new BinaryAddFunction(); else if
 * (operator.equals("Copy")) function = new BinaryAddFunction(); else if
 * (operator.equals("Transparent-zero")) function = new BinaryAddFunction();
 * else throw new IllegalArgumentException("unknown operator type : "+operator);
 * 
 * calculator = new ImageCalculator(input1.getImage(), input2.getImage(),
 * output.getImage(), function); }
 * 
 * private class BinaryAddFunction implements Function<? extends RealType<?>,?
 * extends RealType<?>,? extends RealType<?>> {
 * 
 * @Override public void compute(RealType input1, RealType input2, RealType
 * output) { double value = input1.getRealDouble() + input2.getRealDouble();
 * 
 * output.setReal(value); }
 * 
 * } }
 */
