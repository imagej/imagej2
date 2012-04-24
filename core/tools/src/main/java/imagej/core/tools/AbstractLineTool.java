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

package imagej.core.tools;

import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsChannels;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * Abstract class that is used by PencilTool, PaintBrushTool, and their erase
 * modes to draw lines into a dataset using fg/bg values.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractLineTool extends AbstractTool {

	// -- instance variables --

	private DrawingTool drawingTool;
	private long lineWidth = 1;
	private boolean altKeyDown = false;

	// -- public interface --

	/** Sets the drawing width for lines (in pixels). */
	public void setLineWidth(final long w) {
		if (w < 1) lineWidth = 1;
		else lineWidth = w;
	}

	/** Gets the drawing width for lines (in pixels). */
	public long getLineWidth() {
		return lineWidth;
	}

	/** On mouse down the start point of a series of lines is established. */
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		if (!(evt.getDisplay() instanceof ImageDisplay)) return;
		initDrawingTool(evt);
		if (drawingTool != null) {
			// safe cast due to earlier test
			ImageDisplay disp = (ImageDisplay) evt.getDisplay();
			ImageCanvas canv = disp.getCanvas();
			IntCoords panelCoords = new IntCoords(evt.getX(), evt.getY());
			RealCoords realCoords = canv.panelToImageCoords(panelCoords);
			long modelX = realCoords.getLongX();
			long modelY = realCoords.getLongY();
			drawingTool.moveTo(modelX, modelY);
		}
		evt.consume();
	}

	/** On mouse up all resources are freed. */
	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		drawingTool = null;
		evt.consume();
	}

	/** On mouse drag a series of lines are drawn. */
	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (drawingTool == null) return;
		if (!(evt.getDisplay() instanceof ImageDisplay)) return;
		ImageDisplay disp = (ImageDisplay) evt.getDisplay();
		ImageCanvas canv = disp.getCanvas();
		IntCoords panelCoords = new IntCoords(evt.getX(), evt.getY());
		RealCoords realCoords = canv.panelToImageCoords(panelCoords);
		long modelX = realCoords.getLongX();
		long modelY = realCoords.getLongY();
		drawingTool.lineTo(modelX, modelY);
		evt.getDisplay().update();
		evt.consume();
	}

	/**
	 * Tracks the ALT key status. ALT key determines whether to draw in foreground
	 * or background color.
	 */
	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
	}

	/**
	 * Tracks the ALT key status. ALT key determines whether to draw in foreground
	 * or background color.
	 */
	@Override
	public void onKeyUp(final KyReleasedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
	}

	// -- private helpers --

	/** Allocates and initializes a DrawingTool if possible. */
	private void initDrawingTool(final MsPressedEvent evt) {

		// lookup display info where mouse down event happened
		final ImageJ context = getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final ImageDisplay imageDisplay = (ImageDisplay) evt.getDisplay();
		if (imageDisplay == null) return;

		final OptionsService oSrv = context.getService(OptionsService.class);
		OptionsChannels options = oSrv.getOptions(OptionsChannels.class);

		ChannelCollection channels;
		if (altKeyDown)
			channels = options.getBgValues();
		else
			channels = options.getFgValues();
		
		// get dataset associated with mouse down event
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		// allocate drawing tool
		drawingTool = new DrawingTool(dataset);
		drawingTool.setChannels(channels);

		// set the position of tool to current display's position
		// FIXME - this will break when the view axes are different than the
		// dataset's axes. this could happen from a display that combines multiple
		// datasets. Or perhaps a display that ignores some axes from a dataset.
		final long[] currPos = new long[imageDisplay.numDimensions()];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = imageDisplay.getLongPosition(i);
		drawingTool.setPosition(currPos);

		// TODO - change here to make this work on any two arbitrary axes
		drawingTool.setUAxis(0);
		drawingTool.setVAxis(1);

		drawingTool.setLineWidth(getLineWidth());
	}

}
