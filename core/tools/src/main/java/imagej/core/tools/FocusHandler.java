//
// FocusHandler.java
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

import imagej.ext.KeyCode;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.ui.ApplicationFrame;
import imagej.ui.UserInterface;
import imagej.ui.UIService;

/**
 * Brings the main application window into focus when ENTER is pressed.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Window Focus", alwaysActive = true)
public class FocusHandler extends AbstractTool {

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		if (evt.getCode() != KeyCode.ENTER) return;
		final UIService uiService = evt.getContext().getService(UIService.class);
		if (uiService == null) return;
		final UserInterface ui = uiService.getUI();
		if (ui == null) return;
		final ApplicationFrame appFrame = ui.getApplicationFrame();
		if (appFrame == null) return;
		appFrame.activate();
	}

}
