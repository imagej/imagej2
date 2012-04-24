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
/**
 * 
 */
package imagej.ui.swing;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import imagej.ext.display.Display;
import imagej.ext.display.DisplayWindow;
import imagej.ext.display.OutputPanel;
import imagej.util.ColorRGB;

/**
 * @author Lee Kamentsky
 * 
 * This is the DisplayPanel for Display<String>
 *
 */
public class SwingOutputPanel extends JScrollPane implements OutputPanel {
	final DisplayWindow window;
	final Display<String> display;
	final JTextArea textArea;
	/**
	 * Constructor takes a display and the parent window
	 * 
	 * @param display
	 * @param window
	 */
	public SwingOutputPanel(Display<String> display, DisplayWindow window) {
		this.display = display;
		this.window = window;
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setRows(25);
		textArea.setColumns(84);
		final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		textArea.setFont(font);
		setViewportView(textArea);
		window.setContent(this);
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#getDisplay()
	 */
	@Override
	public Display<?> getDisplay() {
		return display;
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#getWindow()
	 */
	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#redoLayout()
	 */
	@Override
	public void redoLayout() {
		// Nothing to layout

	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String s) {
		// The label is not shown.
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#setBorderColor(imagej.util.ColorRGB)
	 */
	@Override
	public void setBorderColor(ColorRGB color) {
		// The border color is not shown.
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.DisplayPanel#redraw()
	 */
	@Override
	public void redraw() {
		//
		// The strategy is to compare the lines in the text area against
		// those in the display. We clear the control if we find a mismatch.
		//
		String currentText = textArea.getText();
		StringBuffer targetText = new StringBuffer();
		for (String line:display) {
			targetText.append(line + "\n");
		}
		if (targetText.toString().startsWith(currentText)) {
			if (targetText.length() > currentText.length()) {
				textArea.append(targetText.substring(currentText.length()));
			}
		} else {
			textArea.setText(targetText.toString());
		}
		textArea.setCaretPosition(targetText.length());
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.OutputPanel#append(java.lang.String)
	 */
	@Override
	public void append(String text) {
		display.add(text);
	}

	/* (non-Javadoc)
	 * @see imagej.ext.display.OutputPanel#clear()
	 */
	@Override
	public void clear() {
		display.clear();

	}

}
