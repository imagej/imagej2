package imagej.core.plugins.restructure;

//
//AddHyperplanes.java
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

import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;

/**
* Adds hyperplanes of data to an input Dataset along a user specified axis
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Add Data") })
public class AddHyperplanes implements ImageJPlugin {

	@Parameter(required=true)
	private Dataset input;
	
	// TODO - populate choices from Dataset somehow
	@Parameter(label="Axis to modify",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	private String axisToModify;
	
	// TODO - populate max from Dataset somehow
	@Parameter(label="Insertion position",min="0")
	private long insertPosition;
	
	// TODO - populate max from Dataset somehow
	@Parameter(label="Insertion quantity",min="1")
	private long numAdding;

	/** creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		Axis axis = RestructureUtils.getAxis(axisToModify);
		if (inputBad(axis)) return;
		Axis[] axes = input.getAxes();
		long[] newDimensions =
			RestructureUtils.getDimensions(input,axis,numAdding);
		ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(input,newDimensions, axes);
		fillNewImgPlus(input.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		input.setImgPlus(dstImgPlus);
	}

	/** detects if user specified data is invalid */
	private boolean inputBad(Axis axis) {
		// axis not determined by dialog
		if (axis == null)
			return true;
		
		// setup some working variables
		int axisIndex = input.getAxisIndex(axis);
		long axisSize = input.getImgPlus().dimension(axisIndex);

	  // axis not present in Dataset
		if (axisIndex < 0)
			return true;
		
		// bad value for startPosition
		if ((insertPosition < 0)  || (insertPosition >= axisSize))
			return true;
		
		// bad value for numAdding
		if (numAdding <= 0)
			return true;
		
		// if here everything is okay
		return false;
	}

	/** fills the newly created ImgPlus with data values from a smaller source
	 * image. Copies data from existing hyperplanes.
	 */
	private void fillNewImgPlus(ImgPlus<? extends RealType<?>> srcImgPlus,
		ImgPlus<? extends RealType<?>> dstImgPlus, Axis modifiedAxis)
	{
		long[] dimensions = input.getDims();
		int axisIndex = input.getAxisIndex(modifiedAxis);
		long axisSize = dimensions[axisIndex];
		long numBeforeInsert = insertPosition;
		long numInInsertion = numAdding;
		long numAfterInsertion = axisSize - numBeforeInsert;
		
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			0, 0, numBeforeInsert);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeInsert, numBeforeInsert+numInInsertion, numAfterInsertion);
	}

}
