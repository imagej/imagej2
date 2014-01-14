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

package imagej.plugins.uis.pivot;

import imagej.ui.StatusBar;
import imagej.ui.UIService;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.scijava.Context;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 * Status bar with text area and progress bar, similar to ImageJ 1.x.
 * 
 * @author Curtis Rueden
 */
public final class PivotStatusBar extends BoxPane implements StatusBar {

	@Parameter
	private UIService uiService;

	private final Label label;
	private final Meter meter;

	public PivotStatusBar(final Context context) {
		context.inject(this);

		label = new Label();
		add(label);
		meter = new Meter();
		add(meter);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		label.setText(message == null ? "" : message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		if (val >= 0 && val < max) {
			meter.setPercentage((double) val / max);
		}
		else {
			meter.setPercentage(0);
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		final String message = uiService.getStatusMessage(event);
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
