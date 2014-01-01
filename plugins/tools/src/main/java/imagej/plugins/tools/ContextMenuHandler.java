/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.plugins.tools;

import imagej.display.Display;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsClickedEvent;
import imagej.display.event.input.MsPressedEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import imagej.ui.UIService;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handles display of general-purpose context menu (e.g., on right mouse click).
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Context Menus",
	menuRoot = Plugin.CONTEXT_MENU_ROOT)
public class ContextMenuHandler extends AbstractTool {

	@Parameter
	private UIService uiService;

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		doPopupMenu(evt);
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		doPopupMenu(evt);
	}

	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		doPopupMenu(evt);
	}

	// -- Helper methods --

	private void doPopupMenu(final MsButtonEvent evt) {
		if (!evt.isPopupTrigger()) return;

		final String menuRoot = getInfo().getMenuRoot();
		final Display<?> display = evt.getDisplay();
		uiService.showContextMenu(menuRoot, display, evt.getX(), evt.getY());

		// consume event, so that nothing else tries to handle it
		evt.consume();
	}

}
