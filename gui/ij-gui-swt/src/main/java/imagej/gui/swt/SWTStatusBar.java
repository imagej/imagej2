//
// SWTStatusBar.java
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

package imagej.gui.swt;

import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * Status bar with text area and progress bar, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class SWTStatusBar extends Composite
	implements EventSubscriber<StatusEvent>
{

	private final Label label;
	private final ProgressBar progressBar;

	public SWTStatusBar(final Composite parent) {
		super(parent, 0);
		setLayout(new MigLayout());
		label = new Label(this, 0);
		progressBar = new ProgressBar(this, 0);
		Events.subscribe(StatusEvent.class, this);
	}

	public void setStatus(final String message) {
		label.setText(message);
	}

	public void setProgress(final int val, final int max) {
		progressBar.setSelection(val);
		progressBar.setMaximum(max);
	}

	@Override
	public void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
