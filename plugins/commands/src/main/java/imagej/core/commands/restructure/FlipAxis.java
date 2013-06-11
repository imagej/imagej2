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

package imagej.core.commands.restructure;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;
import imagej.module.DefaultMutableModuleItem;

import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

//TODO - add correct weight to @Plugin annotation.

// TODO - blacklist the StackReverser plugin. It is broken for our CellImgs.
// The plugin is referenced twice as Image Transform Flip Z and Image Stacks
// Tools Reverse.

/**
 * Flips the planes of a {@link Dataset} along a user specified axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "initAxes", menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Flip Axis", weight = 3) }, headless = true)
public class FlipAxis extends DynamicCommand {

	// -- constants --

	private static final String AXIS = "axis";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	// -- instance variables --

	private AxisType axisType = null;

	// -- Command methods --

	@Override
	public void run() {
		String err = checkInput();
		if (err != null) cancel(err);
		else swapData();
	}

	// -- accessors --

	public void setDataset(Dataset ds) {
		dataset = ds;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setAxis(AxisType axisType) {
		this.axisType = axisType;
	}

	public AxisType getAxis() {
		// allow API users to override dialog value
		if (axisType != null) return axisType;
		String name = (String) getInput(AXIS);
		AxisType[] axes = dataset.getAxes();
		for (int i = 0; i < axes.length; i++) {
			if (axes[i].getLabel().equals(name)) return axes[i];
		}
		return null;
	}

	// -- initializers --

	protected void initAxes() {
		AxisType[] axes = dataset.getAxes();
		ArrayList<String> choices = new ArrayList<String>();
		for (AxisType a : axes) {
			choices.add(a.getLabel());
		}
		final DefaultMutableModuleItem<String> axisItem =
			new DefaultMutableModuleItem<String>(this, AXIS, String.class);
		axisItem.setChoices(choices);
		axisItem.setPersisted(false);
		axisItem.setValue(this, axes[0].getLabel());
		addInput(axisItem);
	}

	// -- helpers --

	private void swapData() {
		int d = dataset.getAxisIndex(axisType);
		long lo = 0;
		long hi = dataset.dimension(d) - 1;
		while (lo < hi) {
			swapChunk(lo, hi);
			lo++;
			hi--;
		}
	}

	private void swapChunk(long pos1, long pos2) {
		int d = dataset.getAxisIndex(axisType);
		long[] min = new long[dataset.numDimensions()];
		long[] max = new long[dataset.numDimensions()];
		for (int i = 0; i < max.length; i++) {
			max[i] = dataset.dimension(i) - 1;
		}
		min[d] = pos1;
		max[d] = pos1;
		Img<? extends RealType<?>> img = dataset.getImgPlus();
		IntervalView<? extends RealType<?>> view = Views.interval(img, min, max);
		IterableInterval<? extends RealType<?>> data = Views.iterable(view);
		Cursor<? extends RealType<?>> cursor = data.cursor();
		RandomAccess<? extends RealType<?>> accessor = img.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			double orig = cursor.get().getRealDouble();
			accessor.setPosition(cursor);
			accessor.setPosition(pos2, d);
			cursor.get().setReal(accessor.get().getRealDouble());
			accessor.get().setReal(orig);
		}
	}

	private String checkInput() {
		if (dataset == null) return "Dataset is null.";
		axisType = getAxis();
		if (axisType == null) return "Axis is null.";
		if (dataset.getAxisIndex(axisType) < 0) {
			return axisType + " axis is not present in dataset.";
		}
		return null;
	}

}
