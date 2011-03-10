//
// BaseTool.java
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

package imagej.tool;

import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;

import java.awt.Cursor;

/**
 * Base class for ImageJ tools. A tool is a collection of rules binding
 * user input (e.g., keyboard and mouse events) to display and data
 * manipulation in a coherent way. 
 *
 * <p>For example, a <code>PanTool</code> might pan a display when the mouse is
 * dragged or arrow key is pressed, while a <code>PencilTool</code> could draw
 * hard lines on the data within a display.</p>
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public abstract class BaseTool implements ITool {

	private ToolEntry entry;

	@Override
	public ToolEntry getToolEntry() {
		return entry;
	}

	@Override
	public void setToolEntry(final ToolEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public String getLabel() {
		return entry.getLabel();
	}

	@Override
	public String getDescription() {
		return entry.getDescription();
	}

	@Override
	public int getCursor() {
		return Cursor.DEFAULT_CURSOR;
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
	public void onKeyDown(KyPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onKeyUp(KyReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDown(MsPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseUp(MsReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseClick(MsClickedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(MsMovedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDrag(MsDraggedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseWheel(MsWheelEvent evt) {
		// do nothing by default
	}

}
