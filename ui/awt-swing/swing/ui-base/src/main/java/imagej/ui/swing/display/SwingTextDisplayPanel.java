//
// SwingTextDisplayPanel.java
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

import imagej.display.Display;
import imagej.display.EventDispatcher;
import imagej.display.TextDisplay;
import imagej.display.TextDisplayPanel;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingTextDisplayPanel extends JPanel implements TextDisplayPanel {

	private final TextDisplay display;
	private final JTextArea textArea = new JTextArea();
	private final SwingTextDisplayWindow win;

	public SwingTextDisplayPanel(final TextDisplay display,
		final SwingTextDisplayWindow win)
	{
		this.display = display;
		this.win = win;
		textArea.setEditable(false);
		textArea.setRows(30);
		textArea.setColumns(80);
		final Font font = new Font("Monospaced", java.awt.Font.PLAIN, 12);
		textArea.setFont(font);
		// textArea.setPreferredSize(new Dimension(700,300));
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		win.setContent(this);
	}

	// -- OutputWindow methods --

	@Override
	public void append(final String text) {
		textArea.append(text);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	@Override
	public void clear() {
		textArea.setText("");
	}

	// -- DisplayPanel methods --

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void close() {

		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void makeActive() {
		win.requestFocus();
	}

	@Override
	public void redoLayout() {
		// no action needed
	}

	@Override
	public void setLabel(final String s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTitle(final String s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void update() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
