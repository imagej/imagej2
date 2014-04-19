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
 * #L%
 */

package imagej.plugins.tools;

import org.scijava.display.event.input.KyPressedEvent;
import org.scijava.input.KeyCode;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.AbstractTool;
import org.scijava.tool.Tool;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;

/**
 * Brings the main application window into focus when ENTER is pressed.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Window Focus")
public class FocusHandler extends AbstractTool {

	@Parameter(required = false)
	private UIService uiService;

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		if (evt.getCode() != KeyCode.ENTER) return;
		if (uiService == null) return;
		final UserInterface ui = uiService.getDefaultUI();
		if (ui == null) return;
		final ApplicationFrame appFrame = ui.getApplicationFrame();
		if (appFrame == null) return;
		appFrame.activate();
	}

}
