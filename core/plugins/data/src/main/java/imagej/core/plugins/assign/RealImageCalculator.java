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

import imagej.core.plugins.restructure.RestructureUtils;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.UIService;

import java.util.HashMap;

import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.BinaryOperation;
import net.imglib2.ops.Function;
import net.imglib2.ops.function.general.GeneralBinaryFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.image.ImageAssignment;
import net.imglib2.ops.operation.binary.real.RealAdd;
import net.imglib2.ops.operation.binary.real.RealAnd;
import net.imglib2.ops.operation.binary.real.RealAvg;
import net.imglib2.ops.operation.binary.real.RealBinaryOperation;
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
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Fills an output Dataset with a combination of two input Datasets. The
 * combination is specified by the user (such as Add, Min, Average, etc.).
 * 
 * @author Barry DeZonia
 */
@Plugin(iconPath = "/icons/plugins/calculator.png", menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Image Calculator...", weight = 22) }, headless = true)
public class RealImageCalculator<T extends RealType<T>> implements ImageJPlugin
{

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	private DatasetService datasetService;

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(persist = false)
	private Dataset input1;

	@Parameter(persist = false)
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

	private final HashMap<String, RealBinaryOperation<DoubleType, DoubleType, DoubleType>> operators;

	// -- constructor --

	/**
	 * Constructs the ImageMath object by initializing which binary operations are
	 * available.
	 */
	public RealImageCalculator() {
		operators =
			new HashMap<String, RealBinaryOperation<DoubleType, DoubleType, DoubleType>>();

		operators.put("Add", new RealAdd<DoubleType, DoubleType, DoubleType>());
		operators.put("Subtract",
			new RealSubtract<DoubleType, DoubleType, DoubleType>());
		operators.put("Multiply",
			new RealMultiply<DoubleType, DoubleType, DoubleType>());
		operators.put("Divide",
			new RealDivide<DoubleType, DoubleType, DoubleType>());
		operators.put("AND", new RealAnd<DoubleType, DoubleType, DoubleType>());
		operators.put("OR", new RealOr<DoubleType, DoubleType, DoubleType>());
		operators.put("XOR", new RealXor<DoubleType, DoubleType, DoubleType>());
		operators.put("Min", new RealMin<DoubleType, DoubleType, DoubleType>());
		operators.put("Max", new RealMax<DoubleType, DoubleType, DoubleType>());
		operators.put("Average", new RealAvg<DoubleType, DoubleType, DoubleType>());
		operators.put("Difference",
			new RealDifference<DoubleType, DoubleType, DoubleType>());
		operators.put("Copy",
			new RealCopyRight<DoubleType, DoubleType, DoubleType>());
		operators.put("Transparent-zero",
			new RealCopyZeroTransparent<DoubleType, DoubleType, DoubleType>());
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
		output =
			datasetService.create(span, "Result of operation", input1.getAxes(),
				bits, signed, floating);
		// This is what I'd like to do
		// output = datasetService.create(input1.getType(), span,
		// "Result of operation", input1.getAxes());

		assignPixelValues(span);

		// replace original data if desired by user
		if (!overrideType && !newWindow) {
			copyDataIntoInput1(span);
			output = null;
		}
	}

	public Dataset getInput1() {
		return input1;
	}

	public void setInput1(final Dataset input1) {
		this.input1 = input1;
	}

	public Dataset getInput2() {
		return input2;
	}

	public void setInput2(final Dataset input2) {
		this.input2 = input2;
	}

	public Dataset getOutput() {
		return output;
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(final String opName) {
		this.opName = opName;
	}

	public boolean isNewWindow() {
		return newWindow;
	}

	public void setNewWindow(final boolean newWindow) {
		this.newWindow = newWindow;
	}

	public boolean isOverrideType() {
		return overrideType;
	}

	public void setOverrideType(final boolean overrideType) {
		this.overrideType = overrideType;
	}

	// -- private helpers --

	private long[] calcOverlappedSpan(final long[] dimsA, final long[] dimsB) {
		if (dimsA.length != dimsB.length) return null;

		final long[] overlap = new long[dimsA.length];

		for (int i = 0; i < overlap.length; i++)
			overlap[i] = Math.min(dimsA[i], dimsB[i]);

		return overlap;
	}

	private void warnBadSpan() {
		uiService.showDialog("Input images have different number of dimensions",
			"Image Calculator");
	}

	private void assignPixelValues(final long[] span) {
		final long[] origin = new long[span.length];
		final BinaryOperation<DoubleType, DoubleType, DoubleType> binOp =
			operators.get(opName);
		final Function<long[], DoubleType> f1 =
			new RealImageFunction<T, DoubleType>((Img<T>) input1.getImgPlus(),
				new DoubleType());
		final Function<long[], DoubleType> f2 =
			new RealImageFunction<T, DoubleType>((Img<T>) input2.getImgPlus(),
				new DoubleType());
		final GeneralBinaryFunction<long[], DoubleType, DoubleType, DoubleType> binFunc =
			new GeneralBinaryFunction<long[], DoubleType, DoubleType, DoubleType>(f1,
				f2, binOp, new DoubleType());
		final ImageAssignment<T, DoubleType> assigner =
			new ImageAssignment<T, DoubleType>((Img<T>) output.getImgPlus(), origin,
				span, binFunc, null);
		assigner.assign();
	}

	private void copyDataIntoInput1(final long[] span) {
		final ImgPlus<? extends RealType<?>> srcImgPlus = output.getImgPlus();
		final ImgPlus<? extends RealType<?>> dstImgPlus = input1.getImgPlus();
		RestructureUtils.copyHyperVolume(srcImgPlus, new long[span.length], span,
			dstImgPlus, new long[span.length], span);
		input1.update();
	}

}
