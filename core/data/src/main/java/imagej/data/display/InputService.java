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

package imagej.data.display;

import imagej.display.Display;
import imagej.display.event.input.MsButtonEvent;
import imagej.service.IJService;

import org.scijava.event.EventService;
import org.scijava.input.InputModifiers;
import org.scijava.input.KeyCode;

/**
 * Interface for service that tracks the current status of input devices
 * (keyboard and mouse in particular).
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public interface InputService extends IJService {

	EventService getEventService();

	InputModifiers getModifiers();

	boolean isAltDown();

	boolean isAltGrDown();

	boolean isCtrlDown();

	boolean isMetaDown();

	boolean isShiftDown();

	boolean isKeyDown(KeyCode code);

	/**
	 * Gets the display associated with the last observed mouse cursor.
	 * 
	 * @return The display in question, or null if the display has been deleted,
	 *         or the mouse cursor is outside all known displays, or no mouse
	 *         events have ever been observed.
	 */
	Display<?> getDisplay();

	/**
	 * Gets the last observed X coordinate of the mouse cursor, relative to a
	 * specific display.
	 * 
	 * @see #getDisplay()
	 */
	int getX();

	/**
	 * Gets the last observed Y coordinate of the mouse cursor, relative to a
	 * specific display.
	 * 
	 * @see #getDisplay()
	 */
	int getY();

	/**
	 * Gets whether the given mouse button is currently pressed.
	 * 
	 * @param button One of:
	 *          <ul>
	 *          <li>{@link MsButtonEvent#LEFT_BUTTON}</li>
	 *          <li>{@link MsButtonEvent#MIDDLE_BUTTON}</li>
	 *          <li>{@link MsButtonEvent#RIGHT_BUTTON}</li>
	 *          </ul>
	 */
	boolean isButtonDown(int button);

}
