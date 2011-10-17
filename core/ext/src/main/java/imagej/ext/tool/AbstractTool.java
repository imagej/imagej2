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

import imagej.ext.display.MouseCursor;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;

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
public abstract class AbstractTool implements ITool {

	private ToolInfo info;

	@Override
	public ToolInfo getInfo() {
		return info;
	}

	@Override
	public void setInfo(final ToolInfo info) {
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

	/** Event Handlers return a boolean value indicating whether
	 * the event should be consumed or passed down the chain.
	 * Such methods return true for 'consume the event'
	 */
	
	@Override
	public boolean onKeyDown(final KyPressedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onKeyUp(final KyReleasedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseDown(final MsPressedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseUp(final MsReleasedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseClick(final MsClickedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseMove(final MsMovedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseDrag(final MsDraggedEvent evt) {
		return false; // pass event down chain by default
	}

	@Override
	public boolean onMouseWheel(final MsWheelEvent evt) {
		return false; // pass event down chain by default
	}

}
