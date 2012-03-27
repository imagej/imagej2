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

import imagej.data.display.ImageDisplay;
import imagej.ext.MouseCursor;
import imagej.ext.display.Display;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.IntCoords;

/**
 * Tool for panning the display.
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Pan",
	description = "Scrolling tool (or press space bar and drag)",
	iconPath = "/icons/tools/pan.png", priority = PanTool.PRIORITY)
public class PanTool extends AbstractTool {

	public static final int PRIORITY = ZoomTool.PRIORITY - 1;

	private static final int PAN_AMOUNT = 10;

	// TODO - Add customization to set pan amount

	private int lastX, lastY;

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final int panX, panY;
		switch (evt.getCode()) {
			case UP:
				panX = 0;
				panY = -PAN_AMOUNT;
				break;
			case DOWN:
				panX = 0;
				panY = PAN_AMOUNT;
				break;
			case LEFT:
				panX = -PAN_AMOUNT;
				panY = 0;
				break;
			case RIGHT:
				panX = PAN_AMOUNT;
				panY = 0;
				break;
			default:
				panX = panY = 0;
		}
		if (panX == 0 && panY == 0) return;

		imageDisplay.getCanvas().pan(new IntCoords(panX, panY));
		evt.consume();
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		lastX = evt.getX();
		lastY = evt.getY();
		evt.consume();
	}

	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final int xDelta = lastX - evt.getX();
		final int yDelta = lastY - evt.getY();
		imageDisplay.getCanvas().pan(new IntCoords(xDelta, yDelta));
		lastX = evt.getX();
		lastY = evt.getY();
		evt.consume();
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		// pan up or down
		final int rotation = evt.getWheelRotation();
		imageDisplay.getCanvas().pan(new IntCoords(0, rotation * PAN_AMOUNT));
		evt.consume();
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.HAND;
	}

}
