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

package imagej.ext.display.ui;

import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.plugin.IPlugin;

/**
 * A display viewer is a UI widget that shows a display to a user.
 * 
 * @author Lee Kamentsky
 */
public interface DisplayViewer<T> extends IPlugin {

	/**
	 * Returns true if an instance of this display viewer can view the given
	 * display.
	 */
	boolean canView(Display<?> d);

	/**
	 * Begins viewing the given display.
	 * 
	 * @param w The frame / window that will contain the GUI elements
	 * @param d the model for the display to show.
	 */
	void view(DisplayWindow w, Display<?> d);

	/** Gets the display being viewed. */
	Display<T> getDisplay();

	/** Gets the window in which the view is displayed. */
	DisplayWindow getWindow();

	/**
	 * Installs the display panel.
	 * 
	 * @param panel the panel used to host the gui
	 */
	void setPanel(DisplayPanel panel);

	/** Gets the display panel that hosts the gui elements. */
	DisplayPanel getPanel();

	/** Handles a display update event directed at this viewer's display. */
	void onDisplayUpdatedEvent(DisplayUpdatedEvent e);

	/** Handles a display deleted event directed at this viewer's display. */
	void onDisplayDeletedEvent(DisplayDeletedEvent e);

	/**
	 * Handles a display activated event directed at this viewer's display. Note
	 * that the event's display may not be the viewer's display, but the active
	 * display will always be the viewer's display.
	 */
	void onDisplayActivatedEvent(DisplayActivatedEvent e);

}
