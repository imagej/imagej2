//
// SwingOutputWindow.java
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

package imagej.ui.swing;

import imagej.event.EventSubscriber;
import imagej.event.OutputEvent;
import imagej.ui.OutputWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Generalized textual output window. Can be subscribed to {@link OutputEvent}s
 * for global output, e.g. logging.
 * 
 * @author Grant Harris
 */
public class SwingOutputWindow extends JFrame implements
	EventSubscriber<OutputEvent>, OutputWindow
{

	private final JTextArea textArea = new JTextArea();

	// TODO: add tabular functionality

	public SwingOutputWindow(final String title) {
		this(title, 400, 400, 700, 300);
	}

	public SwingOutputWindow(final String title, final int x, final int y,
		final int w, final int h)
	{
		// Add a scrolling text area
		this.setTitle(title);
		textArea.setEditable(false);
		textArea.setRows(20);
		textArea.setColumns(50);
		final Font font = new Font("Monospaced", java.awt.Font.PLAIN, 12);
		textArea.setFont(font);
		getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		setBounds(new Rectangle(x, y, w, h));
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final OutputEvent event) {
		final String output = event.getOutput();
		final OutputEvent.Type type = event.getType();
		// LOG, INFO, RESULT, ERROR, DIAGNOSTIC
		if (type == OutputEvent.Type.ERROR) {
			textArea.setForeground(Color.RED);
		}
		else if (type == OutputEvent.Type.RESULT) {
			textArea.setForeground(Color.GREEN);
		}
		else if (type == OutputEvent.Type.INFO) {
			textArea.setForeground(Color.BLACK);
		}
		else if (type == OutputEvent.Type.LOG) {
			textArea.setForeground(Color.GRAY);
		}
		else if (type == OutputEvent.Type.DIAGNOSTIC) {
			textArea.setForeground(Color.MAGENTA);
		}
		else {
			textArea.setForeground(Color.BLACK);
		}
		append(output);
		textArea.setForeground(Color.BLACK);
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

}
