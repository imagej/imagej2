//
// AbstractTool.java
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

package imagej.ext.tool;

import imagej.ext.MouseCursor;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.plugin.PluginInfo;

/**
 * Abstract base class for ImageJ tools. A tool is a collection of rules binding
 * user input (e.g., keyboard and mouse events) to display and data manipulation
 * in a coherent way.
 * <p>
 * For example, a <code>PanTool</code> might pan a display when the mouse is
 * dragged or arrow key is pressed, while a <code>PencilTool</code> could draw
 * hard lines on the data within a display.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public abstract class AbstractTool implements Tool {

	private PluginInfo<Tool> info;

	@Override
	public PluginInfo<Tool> getInfo() {
		return info;
	}

	@Override
	public void setInfo(final PluginInfo<Tool> info) {
		this.info = info;
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.DEFAULT;
	}

	@Override
	public void activate() {
		// do nothing by default
	}

	@Override
	public void deactivate() {
		// do nothing by default
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onKeyUp(final KyReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		// do nothing by default
	}

}
