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
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.util.HashMap;

import net.imglib2.ops.BinaryOperation;
import net.imglib2.ops.DiscreteNeigh;
import net.imglib2.ops.Function;
import net.imglib2.ops.Real;
import net.imglib2.ops.function.general.GeneralBinaryFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.image.RealImageAssignment;
import net.imglib2.ops.operation.binary.real.RealAdd;
import net.imglib2.ops.operation.binary.real.RealAnd;
import net.imglib2.ops.operation.binary.real.RealAvg;
import net.imglib2.ops.operation.binary.real.RealCopyRight;
import net.imglib2.ops.operation.binary.real.RealCopyZeroTransparent;
import net.imglib2.ops.operation.binary.real.RealDifference;
import net.imglib2.ops.operation.binary.real.RealDivide;
import net.imglib2.ops.operation.binary.real.RealMax;
import net.imglib2.ops.operation.binary.real.RealMin;
import net.imglib2.ops.operation.binary.real.RealMultiply;
import net.imglib2.ops.operation.binary.real.RealOr;
import net.imglib2.ops.operation.binary.real.RealSubtract;
import net.imglib2.ops.operation.binary.real.RealXor;

/**
 * Fills an output Dataset with a combination of two input Datasets. The
 * combination is specified by the user (such as Add, Min, Average, etc.).
 * 
 * @author Barry DeZonia
 */
@Plugin(iconPath = "/icons/plugins/calculator.png", menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Image Calculator...", weight = 22) })
public class ImageMath implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(required = true)
	private Dataset input1;

	@Parameter(required = true)
	private Dataset input2;

	@Parameter(output = true)
	private Dataset output;

	@Parameter(label = "Operation to do between the two input images",
		choices = { "Add", "Subtract", "Multiply", "Divide", "AND", "OR", "XOR",
			"Min", "Max", "Average", "Difference", "Copy", "Transparent-zero" })
	private String operatorName;

	// -- other instance variables --

	private final HashMap<String, BinaryOperation<Real>> operators;

	// -- constructor --

	/**
	 * Constructs the ImageMath object by initializing which binary operations are
	 * avaialable.
	 */
	public ImageMath() {
		operators = new HashMap<String, BinaryOperation<Real>>();

		operators.put("Add", new RealAdd());
		operators.put("Subtract", new RealSubtract());
		operators.put("Multiply", new RealMultiply());
		operators.put("Divide", new RealDivide());
		operators.put("AND", new RealAnd());
		operators.put("OR", new RealOr());
		operators.put("XOR", new RealXor());
		operators.put("Min", new RealMin());
		operators.put("Max", new RealMax());
		operators.put("Average", new RealAvg());
		operators.put("Difference", new RealDifference());
		operators.put("Copy", new RealCopyRight());
		operators.put("Transparent-zero", new RealCopyZeroTransparent());
	}

	// -- public interface --

	/**
	 * Runs the plugin filling the output image with the user specified binary
	 * combination of the two input images.
	 */
	@Override
	public void run() {
		final BinaryOperation<Real> binOp = operators.get(operatorName);
		final Function<long[],Real> f1 =
			new RealImageFunction(input1.getImgPlus().getImg());
		final Function<long[],Real> f2 =
			new RealImageFunction(input2.getImgPlus().getImg());
		final GeneralBinaryFunction<long[],Real> binFunc =
			new GeneralBinaryFunction<long[],Real>(f1, f2, binOp);
		output = input1.duplicateBlank();
		int numDims = output.getImgPlus().numDimensions();
		long[] posOffs = new long[numDims];
		for (int i = 0; i < numDims; i++) {
			posOffs[i] = input1.getImgPlus().dimension(i) - 1;
		}
		DiscreteNeigh neigh = new DiscreteNeigh(new long[numDims], new long[numDims], posOffs);
		final RealImageAssignment assigner =
			new RealImageAssignment(output.getImgPlus().getImg(), neigh, binFunc);
		assigner.assign();
	}

}
