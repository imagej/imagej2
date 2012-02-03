//
// PivotUI.java
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

package imagej.ui.pivot;

import imagej.ext.display.Display;
import imagej.ext.plugin.Plugin;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;

import java.util.concurrent.Callable;

import org.apache.pivot.wtk.DesktopApplicationContext;

/**
 * Apache Pivot-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
public class PivotUI implements UserInterface, Callable<Object> {

	private UIService uiService;

	// -- IUserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		uiService.getThreadService().run(this);
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public void createMenus() {
		//
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public PivotApplicationFrame getApplicationFrame() {
		return null;
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public PivotToolBar getToolBar() {
		return null;
	}

	@Override
	public PivotStatusBar getStatusBar() {
		return null;
	}

	@Override
	public OutputWindow newOutputWindow(final String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// -- Callable methods --

	@Override
	public Object call() {
		final String[] args = { PivotApplication.class.getName() };
		DesktopApplicationContext.main(args);
		return null;
	}

}
