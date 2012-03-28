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

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsMemoryAndThreads;
import imagej.ui.StatusBar;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * Swing implementation of {@link StatusBar}.
 * 
 * @author Curtis Rueden
 */
public class SwingStatusBar extends JPanel implements StatusBar, MouseListener {

	private final EventService eventService;
	
	private final JLabel statusText;
	private final JProgressBar progressBar;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	public SwingStatusBar(final EventService eventService) {
		this.eventService = eventService;
		statusText = new JLabel(getInfoString(false));
		statusText.setBorder(new BevelBorder(BevelBorder.LOWERED));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		setLayout(new BorderLayout());
		add(statusText, BorderLayout.CENTER);
		add(progressBar, BorderLayout.EAST);
		subscribers = eventService.subscribe(this);
		statusText.addMouseListener(this);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		final String text;
		if (message == null || message.isEmpty()) text = " ";
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
		final OptionsService optionsService =
			eventService.getContext().getService(OptionsService.class);
		final OptionsMemoryAndThreads options =
				optionsService.getOptions(OptionsMemoryAndThreads.class);
		if (options.isRunGcOnClick())
			System.gc();
		eventService.publish(new StatusEvent(getInfoString(true)));
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
			JOptionPane.showMessageDialog(this, message, "ImageJ",
				JOptionPane.WARNING_MESSAGE);
		}
		else {
			// report status updates in the status bar
			setStatus(message);
			setProgress(val, max);
		}
	}

	// -- Helper methods --

	// TODO - refactor this code to a UI-agnostic location

	private String getInfoString(final boolean mem) {
		final String javaVersion = System.getProperty("java.version");
		final String osArch = System.getProperty("os.arch");
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long usedMB = usedMem / 1048576;
		final long maxMB = maxMem / 1048576;
		final StringBuilder sb = new StringBuilder();
		sb.append("ImageJ " + ImageJ.VERSION + "; Java " + javaVersion + " [" +
			osArch + "]");
		if (mem) {
			sb.append("; " + usedMB + "MB of " + maxMB + "MB");
		}
		return sb.toString();
	}

}
