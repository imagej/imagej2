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

package imagej.io;

import imagej.event.EventService;
import imagej.event.StatusEvent;
import loci.common.StatusListener;

/**
 * Rebroadcasts {@link loci.common.StatusEvent}s as ImageJ {@link StatusEvent}s.
 * 
 * @author Curtis Rueden
 */
public class StatusDispatcher implements StatusListener {

	private final EventService eventService;

	private long lastTime;

	public StatusDispatcher(final EventService eventService) {
		this.eventService = eventService;
	}

	// -- StatusListener methods --

	@Override
	public void statusUpdated(final loci.common.StatusEvent e) {
		final long time = System.currentTimeMillis();
		final int progress = e.getProgressValue();
		final int maximum = e.getProgressMaximum();
		final String message = e.getStatusMessage();
		final boolean warn = e.isWarning();

		// don't update more than 20 times/sec
		if (time - lastTime < 50 && progress > 0 && progress < maximum && !warn) {
			return;
		}
		lastTime = time;

		eventService.publish(new StatusEvent(progress, maximum, message, warn));
	}

}
