package imagej.core.plugins.restructure;

//
//DeleteAxis.java
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
import imagej.core.plugins.axispos.AxisUtils;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;


/**
* Deletes an axis from an input Dataset
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Delete Axis") })
public class DeleteAxis implements ImageJPlugin {
	
	@Parameter(required = true)
	private Dataset input;

	// TODO - populate choices from Dataset somehow
	@Parameter(label="Axis to delete", choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	private String axisToDelete;
	
	@Parameter(label="Index of hyperplane to keep", min="1")
	private long oneBasedHyperplanePos;
	
	private long hyperPlaneToKeep;

	/** creates new ImgPlus data with one less axis. sets pixels of ImgPlus
	 * to user specified hyperplane within original ImgPlus data. Assigns the
	 * new ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		hyperPlaneToKeep = oneBasedHyperplanePos - 1;
		Axis axis = AxisUtils.getAxis(axisToDelete);
		if (inputBad(axis)) return;
		Axis[] newAxes = getNewAxes(input, axis);
		long[] newDimensions = getNewDimensions(input, axis);
		ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(input, newDimensions, newAxes);
		fillNewImgPlus(input.getImgPlus(), dstImgPlus);
		// TODO - colorTables, metadata, etc.?
		input.setImgPlus(dstImgPlus);
	}
	
	/** detects if user specified data is invalid */
	private boolean inputBad(Axis axis) {
		// axis not determined by dialog
		if (axis == null)
			return true;
		
	  // axis not already present in Dataset
		int axisIndex = input.getAxisIndex(axis);
		if (axisIndex < 0)
			return true;
		
		// hyperplane index out of range
		long axisSize = input.getImgPlus().dimension(axisIndex);
		if ((hyperPlaneToKeep < 0) ||
				(hyperPlaneToKeep >= axisSize))
			return true;
		
		return false;
	}

	/** creates an Axis[] that consists of all the axes from a Dataset minus
	 * a user specified axis
	 */
	private Axis[] getNewAxes(Dataset ds, Axis axis) {
		Axis[] origAxes = ds.getAxes();
		Axis[] newAxes = new Axis[origAxes.length-1];
		int index = 0;
		for (Axis a : origAxes)
			if (a != axis) 
			newAxes[index++] = a;
		return newAxes;
	}
	
	/** creates a long[] that consists of all the dimensions from a Dataset
	 * minus a user specified axis.
	 */
	private long[] getNewDimensions(Dataset ds, Axis axis) {
		long[] origDims = ds.getDims();
		Axis[] origAxes = ds.getAxes();
		long[] newDims = new long[origAxes.length-1];
		int index = 0;
		for (int i = 0; i < origAxes.length; i++) {
			Axis a = origAxes[i];
			if (a != axis) 
				newDims[index++] = origDims[i];
		}
		return newDims;
	}
	
	/** fills the data in the shrunken ImgPlus with the contents of the user
	 * specified hyperplane in the original image
	 */
	private void fillNewImgPlus(ImgPlus<? extends RealType<?>> srcImgPlus,
		ImgPlus<? extends RealType<?>> dstImgPlus)
	{
		long[] srcOrigin = new long[srcImgPlus.numDimensions()];
		long[] dstOrigin = new long[dstImgPlus.numDimensions()];
		
		long[] srcSpan = new long[srcOrigin.length];
		long[] dstSpan = new long[dstOrigin.length];

		srcImgPlus.dimensions(srcSpan);
		dstImgPlus.dimensions(dstSpan);

		Axis axis = AxisUtils.getAxis(axisToDelete);
		int axisIndex = srcImgPlus.getAxisIndex(axis);
		srcOrigin[axisIndex] = this.hyperPlaneToKeep;
		srcSpan[axisIndex] = 1;
		
		RestructureUtils.copyHyperVolume(srcImgPlus, srcOrigin, srcSpan, dstImgPlus, dstOrigin, dstSpan);
	}
}
