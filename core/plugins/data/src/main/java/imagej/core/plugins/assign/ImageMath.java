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

import imagej.ImageJ;
import imagej.core.plugins.restructure.RestructureUtils;
import imagej.data.Dataset;
import imagej.data.DatasetFactory;
import imagej.ext.MenuEntry;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.DialogPrompt;
import imagej.ui.IUserInterface;
import imagej.ui.UIService;

import java.util.HashMap;

import net.imglib2.img.ImgPlus;
import net.imglib2.ops.BinaryOperation;
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
import net.imglib2.type.numeric.RealType;

/**
 * Fills an output Dataset with a combination of two input Datasets. The
 * combination is specified by the user (such as Add, Min, Average, etc.).
 * 
 * @author Barry DeZonia
 */
@Plugin(iconPath = "/icons/plugins/calculator.png", menu = {
	@Menu(label = "Process", weight = MenuEntry.PROCESS_WEIGHT, mnemonic = 'p'),
	@Menu(label = "Image Calculator...", weight = 22) })
public class ImageMath implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(required = true, persist=false)
	private Dataset input1;

	@Parameter(required = true, persist=false)
	private Dataset input2;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(label = "Operation to do between the two input images",
		choices = { "Add", "Subtract", "Multiply", "Divide", "AND", "OR", "XOR",
			"Min", "Max", "Average", "Difference", "Copy", "Transparent-zero" })
	private String opName;

	@Parameter(label = "Create new window")
	private boolean newWindow = true;
	
	@Parameter(label = "Floating point result")
	private boolean overrideType;
	
	// -- other instance variables --

	private final HashMap<String, BinaryOperation<Real, Real, Real>> operators;

	// -- constructor --

	/**
	 * Constructs the ImageMath object by initializing which binary operations
	 * are available.
	 */
	public ImageMath() {
		operators = new HashMap<String, BinaryOperation<Real, Real, Real>>();

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
		final long[] span = calcOverlappedSpan(input1.getDims(), input2.getDims());
		if (span == null) {
			warnBadSpan();
			return;
		}
		int bits = input1.getType().getBitsPerPixel();
		boolean floating = !input1.isInteger();
		boolean signed = input1.isSigned();
		if (overrideType) {
			bits = 64;
			floating = true;
			signed = true;
		}
		// TODO : HACK - this next line works but always creates a PlanarImg
		output = DatasetFactory.create(span, "Result of operation",
			input1.getAxes(), bits, signed,	floating);
		// This is what I'd like to do
		// output = DatasetFactory.create(input1.getType(), span,
		//   "Result of operation", input1.getAxes());
		
		assignPixelValues(span);

		// replace original data if desired by user
		if (!overrideType && !newWindow) {
			copyDataIntoInput1(span);
			output = null;
		}
	}

	// -- private helpers --
	
	private long[] calcOverlappedSpan(long[] dimsA, long[] dimsB) {
		if (dimsA.length != dimsB.length)
			return null;
		
		final long[] overlap = new long[dimsA.length];
		
		for (int i = 0; i < overlap.length; i++)
			overlap[i] = Math.min(dimsA[i], dimsB[i]);
		
		return overlap;
	}
	
	private void warnBadSpan() {
		final IUserInterface ui = ImageJ.get(UIService.class).getUI();
		final DialogPrompt dialog =
			ui.dialogPrompt("Input images have different number of dimensions",
				"Image Calculator",
				DialogPrompt.MessageType.INFORMATION_MESSAGE,
				DialogPrompt.OptionType.DEFAULT_OPTION);
		dialog.prompt();
	}

	private void assignPixelValues(long[] span) {
		final long[] origin = new long[span.length];
		final BinaryOperation<Real, Real, Real> binOp = operators.get(opName);
		final Function<long[], Real> f1 =
			new RealImageFunction(input1.getImgPlus().getImg());
		final Function<long[], Real> f2 =
			new RealImageFunction(input2.getImgPlus().getImg());
		final GeneralBinaryFunction<long[], Real, Real, Real> binFunc =
			new GeneralBinaryFunction<long[], Real, Real, Real>(f1, f2, binOp);
		final RealImageAssignment assigner = new
			RealImageAssignment(output.getImgPlus().getImg(), origin, span, binFunc);
		assigner.assign();
	}

	private void copyDataIntoInput1(long[] span) {
		final ImgPlus<? extends RealType<?>> srcImgPlus = output.getImgPlus();
		final ImgPlus<? extends RealType<?>> dstImgPlus = input1.getImgPlus();
		RestructureUtils.copyHyperVolume(
			srcImgPlus, new long[span.length], span,
			dstImgPlus, new long[span.length], span);
		input1.update();
	}
}
