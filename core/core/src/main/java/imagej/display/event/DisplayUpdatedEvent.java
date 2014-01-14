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

package imagej.display.event;

import imagej.display.Display;

/**
 * An event indicating a display has updated; e.g., an object has been added or
 * removed.
 * 
 * @author Grant Harris
 * @author Lee Kamentsky
 */
public class DisplayUpdatedEvent extends DisplayEvent {

	/**
	 * The display update level gives a hint about how much work needs to be done
	 * during the update.
	 */
	public enum DisplayUpdateLevel {

		/**
		 * The data has changed, but the extents and decorations have not.<br>
		 * An example: changing the intensity of a single pixel.
		 */
		UPDATE,

		/**
		 * The extents and decorations have changed.<br>
		 * An example: stretching an image so it's height changes.
		 */
		REBUILD

	}

	private final DisplayUpdateLevel level;

	public DisplayUpdatedEvent(final Display<?> display,
		final DisplayUpdateLevel level)
	{
		super(display);
		this.level = level;
	}

	public DisplayUpdateLevel getLevel() {
		return level;
	}

}
