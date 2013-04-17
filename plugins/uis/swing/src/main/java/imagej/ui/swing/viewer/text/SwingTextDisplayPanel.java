/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.swing.viewer.text;

import imagej.display.TextDisplay;
import imagej.ui.viewer.DisplayWindow;
import imagej.ui.viewer.text.TextDisplayPanel;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * This is the display panel for {@link String}s.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class SwingTextDisplayPanel extends JScrollPane implements
	TextDisplayPanel
{

	private final DisplayWindow window;
	private final TextDisplay display;
	private final JEditorPane textArea;

	public SwingTextDisplayPanel(final TextDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		textArea = new JEditorPane();
		textArea.setPreferredSize(new Dimension(600, 500));
		textArea.setEditable(false);
		final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		textArea.setFont(font);
		setViewportView(textArea);
		window.setContent(this);
	}

	// -- TextDisplayPanel methods --

	@Override
	public void append(final String text) {
		display.add(text);
	}

	@Override
	public void clear() {
		display.clear();
	}

	// -- DisplayPanel methods --

	@Override
	public TextDisplay getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() {
		// Nothing to layout
	}

	@Override
	public void setLabel(final String s) {
		// The label is not shown.
	}

	@Override
	public void redraw() {
		// The strategy is to compare the lines in the text area against
		// those in the display. We clear the control if we find a mismatch.

		final StringBuffer targetText = new StringBuffer();
		for (final String line : display) {
			targetText.append(line + "\n");
		}
		final String text = targetText.toString();
		final boolean html = text.startsWith("<html>");
		textArea.setContentType(html ? "text/html" : "text/plain");
		textArea.setText(text);
	}

}
