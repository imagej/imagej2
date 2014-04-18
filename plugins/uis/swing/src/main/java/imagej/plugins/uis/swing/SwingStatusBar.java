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

package imagej.plugins.uis.swing;

import imagej.core.options.OptionsMemoryAndThreads;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.StatusBar;
import imagej.ui.UIService;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventHandler;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;

/**
 * Swing implementation of {@link StatusBar}.
 * 
 * @author Curtis Rueden
 */
public class SwingStatusBar extends JPanel implements StatusBar, MouseListener {

	private final JLabel statusText;
	private final JProgressBar progressBar;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private UIService uiService;

	public SwingStatusBar(final Context context) {
		context.inject(this);

		statusText = new JLabel(uiService.getApp().getInfo(false));
		statusText.setBorder(new BevelBorder(BevelBorder.LOWERED));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		setLayout(new BorderLayout());
		add(statusText, BorderLayout.CENTER);
		add(progressBar, BorderLayout.EAST);
		statusText.addMouseListener(this);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		if (message == null) return; // no change
		final String text;
		if (message.isEmpty()) text = " ";
		else text = message;
		statusText.setText(text);
	}

	@Override
	public void setProgress(final int val, final int max) {
		if (max < 0) {
			progressBar.setVisible(false);
			return;
		}

		if (val >= 0 && val < max) {
			progressBar.setValue(val);
			progressBar.setMaximum(max);
			progressBar.setVisible(true);
		}
		else {
			progressBar.setVisible(false);
		}
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(final MouseEvent e) {
		// NB: no action needed
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		// NB: no action needed
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		// NB: no action needed
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		final OptionsMemoryAndThreads options =
			optionsService.getOptions(OptionsMemoryAndThreads.class);
		if (options.isRunGcOnClick()) System.gc();
		statusService.showStatus(uiService.getApp().getInfo(true));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		// NB: no action needed
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		if (event.isWarning()) {
			// report warning messages to the user in a dialog box
			final String message = event.getStatusMessage();
			if (message != null && !message.isEmpty()) {
				uiService.showDialog(message, MessageType.WARNING_MESSAGE);
			}
		}
		else {
			// report status updates in the status bar
			final int val = event.getProgressValue();
			final int max = event.getProgressMaximum();
			final String message = uiService.getStatusMessage(event);
			setStatus(message);
			setProgress(val, max);
		}
	}

}
