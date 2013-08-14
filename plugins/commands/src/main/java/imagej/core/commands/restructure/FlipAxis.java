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
import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
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

// TODO Whether we use Cursors or Views to make wholesale data changes I now
// realize that a flip data along axis needs to flip color tables as well. The
// metadata branch should allow color tables to be associated with axes and thus
// automatically remapped by Views. Waiting for that code. Until then color
// tables are not remapped.

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

	private int d;

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

	private String checkInput() {
		if (dataset == null) return "Dataset is null.";
		axisType = getAxis();
		if (axisType == null) return "Axis is null.";
		d = dataset.dimensionIndex(axisType);
		if (d < 0) return axisType + " axis is not present in dataset.";
		if (dataset.dimension(d) == 1) return axisType + " axis is a single plane.";
		return null;
	}

	// CTR has mentioned we can use Views.invertAxis() to get an inverted view and
	// then assign back the Img(View?) to the Dataset. No messing with data values
	// at all. There is an ImgView class in OPS that may be helpful.

	private void newSwapDataBroken() {
		long[] min = new long[dataset.numDimensions()];
		long[] max = new long[dataset.numDimensions()];
		for (int i = 0; i < max.length; i++) {
			max[i] = dataset.dimension(i) - 1;
		}
		max[d] = (dataset.dimension(d) / 2) - 1;
		// NB due to earlier error checking max[d] must be >= 0
		IntervalView<? extends RealType<?>> halfForw =
			Views.interval(dataset.getImgPlus(), min, max);
		IntervalView<? extends RealType<?>> invertedView =
			Views.invertAxis(dataset.getImgPlus(), d);
		IntervalView<? extends RealType<?>> halfBack =
			Views.interval(invertedView, min.clone(), max.clone());
		IterableInterval<? extends RealType<?>> dataForw = Views.iterable(halfForw);
		// IterableInterval<? extends RealType<?>> dataBack =
		// Views.iterable(halfBack);
		// Cursor<? extends RealType<?>> back = dataBack.cursor();
		Cursor<? extends RealType<?>> forw = dataForw.cursor();
		RandomAccess<? extends RealType<?>> back = halfBack.randomAccess();
		while (forw.hasNext()) {
			forw.fwd();
			back.setPosition(forw);
			double orig = forw.get().getRealDouble();
			forw.get().setReal(back.get().getRealDouble());
			back.get().setReal(orig);
		}
	}

	private void swapData() {
		long lo = 0;
		long hi = dataset.dimension(d) - 1;
		while (lo < hi) {
			swapChunk(lo, hi);
			lo++;
			hi--;
		}
		swapColorTables();
	}

	private void swapChunk(long pos1, long pos2) {
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

	// NB - this is one approach to swapping color tables. Ideally when the
	// metadata branch is merged Axes can be tagged with color tables and Views
	// will automatically return the right one. Then we can use the setImg() of
	// the inverted view of the axis and color tables will auto remap.

	private void swapColorTables() {
		if (axisType == Axes.X || axisType == Axes.Y) return;
		long numPlanes = numPlanes(dataset);
		if (dataset.getColorTableCount() != numPlanes) return;
		for (int i = 0; i < (numPlanes / 2) - 1; i++) {
			int partner = findPartner(i);
			if (partner != i) {
				ColorTable table = dataset.getColorTable(i);
				dataset.setColorTable(dataset.getColorTable(partner), i);
				dataset.setColorTable(table, partner);
			}
		}
	}

	private long numPlanes(Dataset ds) {
		long tot = 1;
		AxisType[] axes = ds.getAxes();
		for (int i = 0; i < axes.length; i++) {
			if (axes[i].equals(Axes.X)) continue;
			if (axes[i].equals(Axes.Y)) continue;
			tot *= ds.dimension(i);
		}
		return tot;
	}

	// NB Determine if a given color table number needs to be remapped. If no then
	// the input index is returned. If yes then the matching partner index is
	// returned.

	private int findPartner(int i) {
		// TODO
		// Right now no color table swapping takes place
		return i;
	}
}
