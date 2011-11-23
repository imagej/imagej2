//
// AxisAccelerator.java
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

import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.InputModifiers;
import imagej.ext.KeyCode;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.IntCoords;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;


/**
 * Based on AcceleratorHandler...
 * Handles keyboard and mousewheel accelerator combinations for 
 * changing the axis positions....
 * <p>
 * Specifically, we want to handle key presses even if the current UI's built-in
 * event handler would pass them up. For example, with the Swing UI, the menu
 * infrastructure fires a menu item if the associated accelerator is pressed,
 * but we need to fire the linked module regardless of which window is active;
 * i.e., on Windows and Linux platforms, image windows do not have a menu bar
 * attached, so the Swing menu infrastructure does not handle key presses when
 * an image window is active.
 * </p>
 * 
 * @author Grant Harris
 */
@Tool(name = "Axis Accelerator Keyboard Shortcuts", alwaysActive = true,
	activeInAppFrame = true, priority = Integer.MIN_VALUE)
public class AxisAccelerator extends AbstractTool {

	/* Keyboard accelerators for changing the displayed axes.
	 * This mimics ImageJ, using forward and backward with '<' and '>' with
		No-modifier for channels
		Ctrl-modifier for slices
		Alt-modifier to frames 
	 */
	@Override
	public void onKeyDown(KyPressedEvent evt) {
		final ImageDisplayService imageDisplayService =
			evt.getContext().getService(ImageDisplayService.class);

		ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) {
			return;
		}
		if (display.getAxes().length < 3) {
			return;
		}
		//		
		KeyCode keyCode = evt.getCode();
		InputModifiers mods = evt.getModifiers();

		// Set Axis based on modifier
		AxisType axis = Axes.CHANNEL;
		if (display.getAxes().length == 3) {
			// default to only non-XY axis
			axis = display.getAxes()[2];
		} else {
			//if(mods.isShiftDown()) {axis = Axes.__;} 
			if (mods.isAltDown()) {
				axis = Axes.TIME;
			}
			if (mods.isCtrlDown()) {
				axis = Axes.Z;
			}
		}
		int increment = 0;
		// Move up or down based on KeyCode
		if (keyCode == KeyCode.PERIOD || keyCode == KeyCode.GREATER
				|| keyCode == KeyCode.KP_RIGHT || keyCode == KeyCode.RIGHT) {
			increment = 1;
		}
		if (keyCode == KeyCode.COMMA || keyCode == KeyCode.LESS
				|| keyCode == KeyCode.KP_LEFT || keyCode == KeyCode.LEFT) {
			increment = -1;
		}
		if (increment != 0 && display.getAxisIndex(axis) > -1) {
			display.setAxisPosition(axis, display.getAxisPosition(axis) + increment);
			// consume event, so that nothing else tries to handle it
			//  ???? +++ evt.consume();
		}
	}

	/*
	 * MouseWheel behavior:
	 * Stepping through C, if multiple channels.
	 * Stepping through Z when control held, if multiple focal planes.
	 * Stepping through T when alt held, if multiple time points.
	 * For single channel data, panning the display up and down when zoomed in. 
	 */
	
	private static final int PAN_AMOUNT = 10;

	@Override
	public void onMouseWheel(MsWheelEvent evt) {
		final ImageDisplayService imageDisplayService =
			evt.getContext().getService(ImageDisplayService.class);

		ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) {
			return;
		}
		int rotation = evt.getWheelRotation();
		if (display.getAxes().length < 3) {
			// pan up or down
			display.getCanvas().pan(new IntCoords(0, rotation * PAN_AMOUNT));
			return;
		}
		InputModifiers mods = evt.getModifiers();
		// Set Axis based on modifier
		AxisType axis = Axes.CHANNEL;
		if (display.getAxes().length == 3) {
			// default to only non-XY axis
			axis = display.getAxes()[2];
		} else {
			//if(mods.isShiftDown()) {axis = Axes.__;} 
			if (mods.isAltDown()) {
				axis = Axes.TIME;
			}
			if (mods.isCtrlDown()) {
				axis = Axes.Z;
			}
		}
		if (display.getAxisIndex(axis) > -1) {
			display.setAxisPosition(axis, display.getAxisPosition(axis) + rotation);
		}
	}

}
