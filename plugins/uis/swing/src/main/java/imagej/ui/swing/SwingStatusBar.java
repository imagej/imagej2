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

package imagej.ui.swing;

import imagej.core.options.OptionsMemoryAndThreads;
import imagej.event.EventHandler;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.StatusBar;
import imagej.ui.UIService;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * Swing implementation of {@link StatusBar}.
 * 
 * @author Curtis Rueden
 */
public class SwingStatusBar extends JPanel implements StatusBar, MouseListener {

	private final UIService uiService;

	private final JLabel statusText;
	private final JProgressBar progressBar;

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public SwingStatusBar(final UIService uiService) {
		this.uiService = uiService;
		statusText = new JLabel(uiService.getContext().getInfo(false));
		statusText.setBorder(new BevelBorder(BevelBorder.LOWERED));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		setLayout(new BorderLayout());
		add(statusText, BorderLayout.CENTER);
		add(progressBar, BorderLayout.EAST);
		subscribers = uiService.getEventService().subscribe(this);
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
			uiService.getOptionsService().getOptions(OptionsMemoryAndThreads.class);
		if (options.isRunGcOnClick()) System.gc();
		uiService.getStatusService().showStatus(
			uiService.getContext().getInfo(true));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		// NB: no action needed
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		final boolean warning = event.isWarning();
		if (warning) {
			// report warning messages to the user in a dialog box
			uiService.showDialog(message, MessageType.WARNING_MESSAGE);
		}
		else {
			// report status updates in the status bar
			setStatus(message);
			setProgress(val, max);
		}
	}

}
