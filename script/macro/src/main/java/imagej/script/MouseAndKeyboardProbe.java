//
// MouseAndKeyboardProbe.java
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

package imagej.script;

import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.display.Display;
import imagej.display.event.input.InputEvent;
import imagej.display.event.input.KyPressedEvent;
import imagej.display.event.input.MsMovedEvent;
import imagej.display.event.input.MsPressedEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.input.InputModifiers;
import imagej.plugin.Plugin;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * Records mouse position and keyboard state for the macro language
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = Tool.class, name = "MouseAndKeyboardProbe", alwaysActive = true)
public class MouseAndKeyboardProbe extends AbstractTool {

	// -- private instance variables --

	private int screenX, screenY, mouseModifiers;
	private double imageX, imageY;
	private boolean altDown;
	private boolean ctrlDown;

	// -- constructor --

	public MouseAndKeyboardProbe() {}

	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final ImageCanvas canvas = imageDisplay.getCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		// mouse not in image ?
		if (!canvas.isInImage(mousePos)) {
			imageX = imageY = screenX = screenY = -1;
		}
		else { // mouse is over image
			screenX = mousePos.x;
			screenY = mousePos.y;
			final RealCoords coords = canvas.panelToDataCoords(mousePos);
			imageX = coords.x;
			imageY = coords.y;
		}
		updateModifiers(evt);
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		screenX = evt.getX();
		screenY = evt.getY();
		mouseModifiers |= (1 << evt.getButton());
		updateModifiers(evt);
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		screenX = evt.getX();
		screenY = evt.getY();
		mouseModifiers &= ~(1 << evt.getButton());
		updateModifiers(evt);
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		updateModifiers(evt);
	}

	public int getScreenX() {
		return screenX;
	}

	public int getScreenY() {
		return screenY;
	}

	public int getMouseModifiers() {
		return mouseModifiers;
	}

	public double getImageX() {
		return imageX;
	}

	public double getImageY() {
		return imageY;
	}

	public boolean getAltDown() {
		return altDown;
	}

	public boolean getCtrlDown() {
		return ctrlDown;
	}

	protected void updateModifiers(final InputEvent evt) {
		final InputModifiers modifiers = evt.getModifiers();
		altDown = modifiers.isAltDown();
		ctrlDown = modifiers.isCtrlDown();
	}

}
