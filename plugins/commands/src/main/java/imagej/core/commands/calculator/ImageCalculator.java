/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.operator.CalculatorOp;
import imagej.data.operator.CalculatorService;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Fills an output Dataset with a combination of two input Datasets. The
 * combination is specified by the user (such as Add, Min, Average, etc.).
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, iconPath = "/icons/commands/calculator.png",
	menu = {
		@Menu(label = MenuConstants.PROCESS_LABEL,
			weight = MenuConstants.PROCESS_WEIGHT,
			mnemonic = MenuConstants.PROCESS_MNEMONIC),
		@Menu(label = "Image Calculator...", weight = 22) }, headless = true,
	initializer = "initCalculator")
public class ImageCalculator<U extends RealType<U>, V extends RealType<V>>
	extends DynamicCommand
{

	// -- instance variables that are Parameters --

	@Parameter
	private CalculatorService calculatorService;

	@Parameter
	private DatasetService datasetService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset input1;

	@Parameter
	private Dataset input2;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(label = "Operation to do between the two input images")
	private String opName;

	@Parameter(label = "Create new window")
	private boolean newWindow = true;

	@Parameter(label = "Floating point result")
	private boolean wantDoubles = false;

	// -- other instance variables --

	private CalculatorOp<U, V> operator;

	// -- public interface --

	/**
	 * Runs the plugin filling the output image with the user specified binary
	 * combination of the two input images.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		if (operator == null) {
			operator = (CalculatorOp<U, V>) calculatorService.getOperator(opName);
		}
		Img<DoubleType> img = null;
		try {
			final Img<U> img1 = (Img<U>) input1.getImgPlus();
			final Img<V> img2 = (Img<V>) input2.getImgPlus();
			img = calculatorService.combine(img1, img2, operator);
		}
		catch (final IllegalArgumentException e) {
			cancel(e.toString());
			return;
		}
		final long[] span = new long[img.numDimensions()];
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
			output.update(); // TODO - probably unnecessary
		}
	}

	/**
	 * Returns the {@link Dataset} that is input 1 for this image calculation.
	 */
	public Dataset getInput1() {
		return input1;
	}

	/**
	 * Sets the {@link Dataset} that will be input 1 for this image calculation.
	 */
	public void setInput1(final Dataset input1) {
		this.input1 = input1;
	}

	/**
	 * Returns the {@link Dataset} that is input 2 for this image calculation.
	 */
	public Dataset getInput2() {
		return input2;
	}

	/**
	 * Sets the {@link Dataset} that will be input 2 for this image calculation.
	 */
	public void setInput2(final Dataset input2) {
		this.input2 = input2;
	}

	/**
	 * Returns the output {@link Dataset} of the image calculation.
	 */
	public Dataset getOutput() {
		return output;
	}

	/**
	 * Returns the {@link CalculatorOp} that was used in the image calculation.
	 */
	public CalculatorOp<U, V> getOperation() {
		return operator;
	}

	// TODO - due to generics is this too difficult to specify for real world use?

	/**
	 * Sets the {@link CalculatorOp} to be used in the image calculation.
	 */
	public void setOperation(final CalculatorOp<U, V> operation) {
		this.operator = operation;
	}

	/**
	 * Returns true if image calculation with create a new image window. Otherwise
	 * the existing image that is input 1 is changed.
	 */
	public boolean isNewWindow() {
		return newWindow;
	}

	/**
	 * Sets whether image calculation will create a new image window. If true then
	 * yes else the existing image that is input 1 is changed.
	 */
	public void setNewWindow(final boolean newWindow) {
		this.newWindow = newWindow;
	}

	/**
	 * Returns true if image calculation will return a double backed Img as
	 * output.
	 */
	public boolean isDoubleOutput() {
		return wantDoubles;
	}

	/**
	 * Sets whether image calculation will create double backed output. If true
	 * then yes else data is created that matches input image 1's data type.
	 */
	public void setDoubleOutput(final boolean wantDoubles) {
		this.wantDoubles = wantDoubles;
	}


	// -- initializer --

	protected void initCalculator() {
		List<String> opNames = calculatorService.getOperatorNames();
		@SuppressWarnings("unchecked")
		final MutableModuleItem<String> opNameInput =
			(MutableModuleItem<String>) getInfo().getInput("opName");
		opNameInput.setChoices(opNames);
	}

	// -- private helpers --

	private void copyDataInto(final Img<? extends RealType<?>> out,
		final Img<? extends RealType<?>> in, final long[] span)
	{
		final RandomAccess<? extends RealType<?>> src = in.randomAccess();
		final RandomAccess<? extends RealType<?>> dst = out.randomAccess();
		final HyperVolumePointSet ps = new HyperVolumePointSet(span);
		final PointSetIterator iter = ps.iterator();
		long[] pos = null;
		while (iter.hasNext()) {
			pos = iter.next();
			src.setPosition(pos);
			dst.setPosition(pos);
			final double value = src.get().getRealDouble();
			dst.get().setReal(value);
		}
	}

}
