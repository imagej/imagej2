//
// AddHyperplanes.java
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

package imagej.core.plugins.restructure;

import imagej.ImageJ;
import imagej.core.plugins.axispos.AxisUtils;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Adds hyperplanes of data to an input Dataset along a user specified axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Add Data...") })
public class AddHyperplanes extends DynamicPlugin {

	private static final String NAME_KEY = "Axis to modify";
	private static final String POSITION_KEY = "Insertion position";
	private static final String QUANTITY_KEY = "Insertion quantity";

	private Dataset dataset;
	private String axisToModify;
	private long oneBasedInsPos;
	private long numAdding;

	private long insertPosition;

	public AddHyperplanes() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final Display display = displayService.getActiveDisplay();
		if (display == null) return;
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(display);

		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		final List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis candidateAxis : AxisUtils.AXES) {
			if (datasetAxes.contains(candidateAxis)) choices.add(AxisUtils
				.getAxisName(candidateAxis));
		}
		name.setChoices(choices);
		addInput(name);

		final DefaultModuleItem<Long> pos =
			new DefaultModuleItem<Long>(this, POSITION_KEY, Long.class);
		pos.setMinimumValue(1L);
		// TODO - figure a way to set max val based on chosen Dataset's curr values
		addInput(pos);

		final DefaultModuleItem<Long> quantity =
			new DefaultModuleItem<Long>(this, QUANTITY_KEY, Long.class);
		quantity.setMinimumValue(1L);
		// TODO - figure a way to set max val based on chosen Dataset's curr values
		addInput(quantity);
	}

	/**
	 * Creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		final Map<String, Object> inputs = getInputs();
		axisToModify = (String) inputs.get(NAME_KEY);
		oneBasedInsPos = (Long) inputs.get(POSITION_KEY);
		numAdding = (Long) inputs.get(QUANTITY_KEY);

		insertPosition = oneBasedInsPos - 1;
		final Axis axis = AxisUtils.getAxis(axisToModify);
		if (inputBad(axis)) return;
		final Axis[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, numAdding);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		dataset.setImgPlus(dstImgPlus);
	}

	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final Axis axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// setup some working variables
		final int axisIndex = dataset.getAxisIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) return true;

		// bad value for startPosition
		if ((insertPosition < 0) || (insertPosition > axisSize)) return true;

		// bad value for numAdding
		if ((numAdding <= 0) || ((Long.MAX_VALUE - numAdding) < axisSize)) return true;

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a smaller source
	 * image. Copies data from existing hyperplanes.
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus, final Axis modifiedAxis)
	{
		final long[] dimensions = dataset.getDims();
		final int axisIndex = dataset.getAxisIndex(modifiedAxis);
		final long axisSize = dimensions[axisIndex];
		final long numBeforeInsert = insertPosition;
		final long numInInsertion = numAdding;
		final long numAfterInsertion = axisSize - numBeforeInsert;

		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis, 0, 0,
			numBeforeInsert);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeInsert, numBeforeInsert + numInInsertion, numAfterInsertion);
	}

}
