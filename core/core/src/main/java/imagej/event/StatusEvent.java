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

package imagej.event;

/**
 * An event indicating a status update.
 * 
 * @author Curtis Rueden
 */
public class StatusEvent extends ImageJEvent {

	/** Current progress value. */
	private final int progress;

	/** Current progress maximum. */
	private final int maximum;

	/** Current status message. */
	private final String status;

	/** Whether or not this is a warning event. */
	private final boolean warning;

	public StatusEvent(final String message) {
		this(-1, -1, message);
	}

	public StatusEvent(final String message, final boolean warn) {
		this(-1, -1, message, warn);
	}

	public StatusEvent(final int progress, final int maximum) {
		this(progress, maximum, null);
	}

	public StatusEvent(final int progress, final int maximum,
		final String message)
	{
		this(progress, maximum, message, false);
	}

	public StatusEvent(final int progress, final int maximum,
		final String message, final boolean warn)
	{
		this.progress = progress;
		this.maximum = maximum;
		status = message;
		warning = warn;
	}

	// -- StatusEvent methods --

	/** Gets progress value. Returns -1 if progress is unknown. */
	public int getProgressValue() {
		return progress;
	}

	/** Gets progress maximum. Returns -1 if progress is unknown. */
	public int getProgressMaximum() {
		return maximum;
	}

	/** Gets status message. */
	public String getStatusMessage() {
		return status;
	}

	/** Returns whether or not this is a warning event. */
	public boolean isWarning() {
		return warning;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return super.toString() + "\n\tprogress = " + progress + "\n\tmaximum = " +
			maximum + "\n\tstatus = " + status + "\n\twarning = " + warning;
	}

}
