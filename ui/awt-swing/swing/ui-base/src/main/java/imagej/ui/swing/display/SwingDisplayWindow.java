//
// SwingDisplayWindow.java
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

package imagej.ui.swing.display;

import imagej.data.display.DisplayWindow;
import imagej.ext.display.DisplayPanel;
import imagej.ext.display.EventDispatcher;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTWindowEventDispatcher;
import imagej.ui.swing.StaticSwingUtils;

import java.awt.Dimension;
import java.awt.HeadlessException;

import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingDisplayWindow extends JFrame implements DisplayWindow {

	SwingDisplayPanel panel;

	public SwingDisplayWindow() throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocation(StaticSwingUtils.nextFramePosition());
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		if (dispatcher instanceof AWTWindowEventDispatcher) {
			addWindowListener((AWTWindowEventDispatcher) dispatcher);
		}
		if (dispatcher instanceof AWTKeyEventDispatcher) {
			addKeyListener((AWTKeyEventDispatcher) dispatcher);
		}
	}

	@Override
	public void setContent(final DisplayPanel panel) {
		this.panel = (SwingDisplayPanel) panel;
		setContentPane(this.panel); 
	}

	@Override
	public void showDisplay(final boolean visible) {
		// place on desktop with appropriate sizing
		sizeAppropriately();
		pack();
		setVisible(visible);
	}
	
	private boolean initial=true;

	void sizeAppropriately() {
		JHotDrawImageCanvas canvas = (JHotDrawImageCanvas) panel.getDisplay().getImageCanvas();
		Rectangle deskBounds = StaticSwingUtils.getWorkSpaceBounds();
		Dimension canvasSize = canvas.getPreferredSize();
		int scaling = 1;
		while (canvasSize.height > deskBounds.height - 32 || canvasSize.width > deskBounds.width - 32) {
			canvas.setZoom(1.0 / scaling++);
			canvasSize = canvas.getPreferredSize();
		}
		if(initial) {
			canvas.setInitialScale(canvas.getZoomFactor());
			initial = false;
		}
	}


	@Override
	public void close() {
		setVisible(false);
		dispose();
	}
	
	

}
