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

package imagej.core.commands.calculator;

import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.menu.MenuConstants;
import imagej.module.ItemIO;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.util.HashMap;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.img.ImageCombiner;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.real.binary.RealAdd;
import net.imglib2.ops.operation.real.binary.RealAnd;
import net.imglib2.ops.operation.real.binary.RealAvg;
import net.imglib2.ops.operation.real.binary.RealBinaryOperation;
import net.imglib2.ops.operation.real.binary.RealCopyRight;
import net.imglib2.ops.operation.real.binary.RealCopyZeroTransparent;
import net.imglib2.ops.operation.real.binary.RealDifference;
import net.imglib2.ops.operation.real.binary.RealDivide;
import net.imglib2.ops.operation.real.binary.RealMax;
import net.imglib2.ops.operation.real.binary.RealMin;
import net.imglib2.ops.operation.real.binary.RealMultiply;
import net.imglib2.ops.operation.real.binary.RealOr;
import net.imglib2.ops.operation.real.binary.RealSubtract;
import net.imglib2.ops.operation.real.binary.RealXor;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
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
public class ImageCalculator<U extends RealType<U>,V extends RealType<V>>
	extends ContextCommand
{
	// -- instance variables that are Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset input1;

	@Parameter
	private Dataset input2;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(label = "Operation to do between the two input images",
		choices = { "Add", "Subtract", "Multiply", "Divide", "AND", "OR", "XOR",
			"Min", "Max", "Average", "Difference", "Copy", "Transparent-zero" })
	private String opName = "Add";

	@Parameter(label = "Create new window")
	private boolean newWindow = true;

	@Parameter(label = "Floating point result")
	private boolean wantDoubles = false;

	// -- other instance variables --

	private final HashMap<String, RealBinaryOperation<U, V, DoubleType>> operators;
	
	private BinaryOperation<U,V,DoubleType> operator = null;

	// -- constructor --

	/**
	 * Constructs the RealImageCalculator object by initializing which binary
	 * operations are available.
	 */
	public ImageCalculator() {
		operators =
			new HashMap<String, RealBinaryOperation<U,V,DoubleType>>();

		operators.put("Add", new RealAdd<U,V,DoubleType>());
		operators.put("Subtract",
			new RealSubtract<U,V,DoubleType>());
		operators.put("Multiply",
			new RealMultiply<U,V,DoubleType>());
		operators.put("Divide",
			new RealDivide<U,V,DoubleType>());
		operators.put("AND", new RealAnd<U,V,DoubleType>());
		operators.put("OR", new RealOr<U,V,DoubleType>());
		operators.put("XOR", new RealXor<U,V,DoubleType>());
		operators.put("Min", new RealMin<U,V,DoubleType>());
		operators.put("Max", new RealMax<U,V,DoubleType>());
		operators.put("Average", new RealAvg<U,V,DoubleType>());
		operators.put("Difference",
			new RealDifference<U,V,DoubleType>());
		operators.put("Copy",
			new RealCopyRight<U,V,DoubleType>());
		operators.put("Transparent-zero",
			new RealCopyZeroTransparent<U,V,DoubleType>());
	}

	// -- public interface --

	/**
	 * Runs the plugin filling the output image with the user specified binary
	 * combination of the two input images.
	 */
	@Override
	public void run() {
		if (operator == null) operator = operators.get(opName);
		Img<DoubleType> img = null;
		try {
			@SuppressWarnings("unchecked")
			Img<U> img1 = (Img<U>) input1.getImgPlus();
			@SuppressWarnings("unchecked")
			Img<V> img2 = (Img<V>) input2.getImgPlus();
			// TODO - limited by ArrayImg size constraints
			img =
				ImageCombiner.applyOp(operator, img1, img2, 
													new ArrayImgFactory<DoubleType>(), new DoubleType());
		} catch (IllegalArgumentException e) {
			cancel(e.toString());
			return;
		}
		long[] span = new long[img.numDimensions()];
		img.dimensions(span);

		// replace original data if desired by user
		if (!wantDoubles && !newWindow) {
			output = null;
			copyDataInto(input1.getImgPlus(), img, span);
			input1.update();
		}
		else { // write into output
			int bits = input1.getType().getBitsPerPixel();
			boolean floating = !input1.isInteger();
			boolean signed = input1.isSigned();
			if (wantDoubles) {
				bits = 64;
				floating = true;
				signed = true;
			}
			// TODO : HACK - this next line works but always creates a PlanarImg
			output =
				datasetService.create(span, "Result of operation", input1.getAxes(),
					bits, signed, floating);
			copyDataInto(output.getImgPlus(), img, span);
			output.update(); // TODO - probably unecessary
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

	public BinaryOperation<U,V,DoubleType> getOperation() {
		return operator;
	}

	// TODO - due to generics is this too difficult to specify for real world use?
	
	public void setOperation(BinaryOperation<U,V,DoubleType> operation)
	{
		this.operator = operation;
	}

	public boolean isNewWindow() {
		return newWindow;
	}

	public void setNewWindow(final boolean newWindow) {
		this.newWindow = newWindow;
	}

	public boolean isDoubleOutput() {
		return wantDoubles;
	}

	public void setDoubleOutput(final boolean wantDoubles) {
		this.wantDoubles = wantDoubles;
	}

	// -- private helpers --

	private void copyDataInto(
		Img<? extends RealType<?>> out, Img<? extends RealType<?>> in, long[] span)
	{
		RandomAccess<? extends RealType<?>> src = in.randomAccess();
		RandomAccess<? extends RealType<?>> dst = out.randomAccess();
		HyperVolumePointSet ps = new HyperVolumePointSet(span);
		PointSetIterator iter = ps.iterator();
		long[] pos = null;
		while (iter.hasNext()) {
			pos = iter.next();
			src.setPosition(pos);
			dst.setPosition(pos);
			double value = src.get().getRealDouble();
			dst.get().setReal(value);
		}
	}

}
