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

package imagej.ui.awt;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;

import java.awt.Graphics;
import java.awt.Label;
import java.util.List;

/**
 * AWT implementation of {@link StatusBar}.
 *
 * @author Curtis Rueden
 */
public class AWTStatusBar extends Label implements StatusBar {

	private int value;
	private int maximum;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	public AWTStatusBar() {
		subscribers = ImageJ.get(EventService.class).subscribe(this);
	}

	// -- Component methods --

	@Override
	public void paint(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		final int pix = maximum > 0 ? value * width / maximum : 0;
		g.setColor(getForeground());
		g.fillRect(0, 0, pix, height);
		g.setColor(getBackground());
		g.fillRect(pix, 0, width, height);
		super.paint(g);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		setText(message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		value = val;
		maximum = max;
		repaint();
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
