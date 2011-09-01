//
// SwingMdiTextDisplayWindow.java
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

package imagej.ui.swing.mdi.display;

import imagej.display.DisplayPanel;
import imagej.display.DisplayWindow;
import imagej.display.EventDispatcher;
import imagej.event.OutputEvent;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.display.SwingTextDisplayPanel;
import imagej.ui.swing.mdi.InternalFrameEventDispatcher;

import java.awt.HeadlessException;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

/**
 * Generalized textual output window. Can be subscribed to {@link OutputEvent}s
 * for global output, e.g. logging.
 * 
 * @author Grant Harris
 */
public class SwingMdiTextDisplayWindow extends JInternalFrame implements
	DisplayWindow
{

	// TODO: add tabular functionality

	public SwingMdiTextDisplayWindow() throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(700, 300);
		setLocation(StaticSwingUtils.nextFramePosition());

	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		if (dispatcher instanceof InternalFrameEventDispatcher) {
			addInternalFrameListener((InternalFrameEventDispatcher) dispatcher);
		}
	}

	@Override
	public void setContent(final DisplayPanel panel) {
		setContentPane((SwingTextDisplayPanel) panel);
	}

	@Override
	public void showDisplay(final boolean visible) {
		this.setVisible(visible);
	}

}
