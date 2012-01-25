//
// AbstractLineTool.java
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

package imagej.core.tools;

import java.util.Arrays;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsColors;

/**
 * Abstract class that is used by PencilTool, PaintBrushTool, and their erase
 * modes to draw lines into a dataset using fg/bg values.
 * 
 * @author Barry DeZonia
 *
 */
public abstract class AbstractLineTool extends AbstractTool {

	private DrawingTool drawingTool;
	private long lineWidth = 1;
	private boolean altKeyDown = false;

	public void setLineWidth(long w) {
		if (w < 1)
			lineWidth = 1;
		else
			lineWidth = w;
	}
	
	public long getLineWidth() {
		return lineWidth;
	}
	
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		initDrawingTool(evt);
		if (drawingTool != null) {
			drawingTool.moveTo(evt.getX(), evt.getY());
		}
		super.onMouseDown(evt);
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		drawingTool = null;
		super.onMouseUp(evt);
	}

	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (drawingTool != null) {
			drawingTool.lineTo(evt.getX(), evt.getY());
			evt.getDisplay().getPanel().redraw();
			evt.getDisplay().update();
		}
		super.onMouseDrag(evt);
	}

	@Override
	public void onKeyDown(KyPressedEvent evt) {
		altKeyDown = evt.getModifiers().isAltDown() ||
				evt.getModifiers().isAltGrDown();
		super.onKeyDown(evt);
	}
	
	@Override
	public void onKeyUp(KyReleasedEvent evt) {
		altKeyDown = evt.getModifiers().isAltDown() ||
				evt.getModifiers().isAltGrDown();
		super.onKeyUp(evt);
	}

	// -- private helpers --
	
	private void initDrawingTool(MsPressedEvent evt) {
		
		// lookup display info where mouse down event happened
		final ImageJ context = evt.getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final ImageDisplay imageDisplay = (ImageDisplay)evt.getDisplay();
		if (imageDisplay == null) return;

		// get dataset associated with mouse down event
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		// try to avoid allocating objects when possible
		if ((drawingTool == null) || (drawingTool.getDataset() != dataset))
			drawingTool = new DrawingTool(dataset);

		// FIXME - this will break when the view axes are different than the
		// dataset's axes. this could happen from a view combining multiple datasets
		long[] currPos = new long[imageDisplay.numDimensions()];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = imageDisplay.getLongPosition(i);
		drawingTool.setPosition(currPos);
		
		// TODO - change here to make this work on any two axes
		drawingTool.setUAxis(0);
		drawingTool.setVAxis(1);

		initAttributes(dataset.isRGBMerged());
	}

	private void initAttributes(boolean isColorData) {

		// set line width of drawingTool
		
		drawingTool.setLineWidth(getLineWidth());

		// set color of drawingTool
		
		OptionsService oSrv = ImageJ.get(OptionsService.class);
		
		OptionsColors opts = oSrv.getOptions(OptionsColors.class);
		
		if (isColorData) {
			if (altKeyDown)
				drawingTool.setColorValue(opts.getBgColor());
			else
				drawingTool.setColorValue(opts.getFgColor());
		}
		else { // gray data
			if (altKeyDown)
				drawingTool.setGrayValue(opts.getBgGray());
			else
				drawingTool.setGrayValue(opts.getFgGray());
		}
	}
}
