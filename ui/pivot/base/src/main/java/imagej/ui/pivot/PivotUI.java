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

package imagej.ui.pivot;

import imagej.ext.display.Display;
import imagej.ext.plugin.Plugin;
import imagej.ui.AbstractUserInterface;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.OutputWindow;
import imagej.ui.SystemClipboard;
import imagej.ui.UserInterface;
import imagej.ui.viewer.DisplayWindow;

import java.util.concurrent.Callable;

import org.apache.pivot.wtk.DesktopApplicationContext;

/**
 * Apache Pivot-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
public class PivotUI extends AbstractUserInterface implements Callable<Object> {

	// -- UserInterface methods --

	@Override
	public PivotApplicationFrame getApplicationFrame() {
		// TODO
		return null;
	}

	@Override
	public PivotToolBar getToolBar() {
		// TODO
		return null;
	}

	@Override
	public PivotStatusBar getStatusBar() {
		// TODO
		return null;
	}

	@Override
	public SystemClipboard getSystemClipboard() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayWindow createDisplayWindow(final Display<?> display) {
		throw new UnsupportedOperationException("Not supported yet.");
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

	// -- Internal methods --

	@Override
	protected void createUI() {
		getUIService().getThreadService().run(this);
	}

}
