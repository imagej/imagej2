//
// SwingMdiUI.java
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

package imagej.ui.swing.mdi;

import imagej.ui.Desktop;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UI;
import imagej.ui.swing.AbstractSwingUI;
import imagej.ui.swing.SwingApplicationFrame;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JScrollPane;

/**
 * Swing-based MDI user interface for ImageJ.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@UI
public class SwingMdiUI extends AbstractSwingUI {

	private JMDIDesktopPane desktopPane;

	private JScrollPane scrollPane;

	// -- UserInterface methods --

	@Override
	public Desktop getDesktop() {
		return desktopPane; 
	}

	@Override
	public SwingMdiDialogPrompt dialogPrompt(final String message,
		final String title, final MessageType msg, final OptionType option)
	{
		return new SwingMdiDialogPrompt(message, title, msg, option);
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		desktopPane = new JMDIDesktopPane();
		// TODO desktopPane.setTransferHandler(new DropFileTransferHandler());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(desktopPane);
		desktopPane.setBackground(new Color(200, 200, 255));
		appFrame.getContentPane().add(scrollPane);
		appFrame.setBounds(getWorkSpaceBounds());
	}

	// -- Helper methods --

	private Rectangle getWorkSpaceBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
	}

}
