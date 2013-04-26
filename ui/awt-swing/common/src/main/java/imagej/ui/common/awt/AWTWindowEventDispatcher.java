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

package imagej.ui.common.awt;

import imagej.display.Display;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.display.event.window.WinClosingEvent;
import imagej.display.event.window.WinDeactivatedEvent;
import imagej.display.event.window.WinDeiconifiedEvent;
import imagej.display.event.window.WinEvent;
import imagej.display.event.window.WinIconifiedEvent;
import imagej.display.event.window.WinOpenedEvent;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.scijava.event.EventService;

/**
 * Rebroadcasts AWT {@link WindowEvent}s as ImageJ {@link WinEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTWindowEventDispatcher implements WindowListener {

	// TODO: Use WindowAdapter ? Currently,
	// not implementing WindowStateListener windowStateChanged(WindowEvent) and
	// not implementing WindowFocusListener windowFocusGained/Lost(WindowEvent)

	private final Display<?> display;
	private final EventService eventService;

	/** Creates an AWT window event dispatcher for the given display. */
	public AWTWindowEventDispatcher(final Display<?> display) {
		this.display = display;
		eventService = display.getContext().getService(EventService.class);
	}

	/**
	 * Creates an AWT window event dispatcher for a null display, using the given
	 * event service.
	 */
	public AWTWindowEventDispatcher(final EventService eventService) {
		display = null;
		this.eventService = eventService;
	}

	// -- AWTWindowEventDispatcher methods --

	public void register(final Window w) {
		w.addWindowListener(this);
	}

	// -- WindowListener methods --

	@Override
	public void windowActivated(final WindowEvent e) {
		eventService.publish(new WinActivatedEvent(display, e.getWindow()));
	}

	@Override
	public void windowClosed(final WindowEvent e) {
		eventService.publish(new WinClosedEvent(display, e.getWindow()));
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		eventService.publish(new WinClosingEvent(display, e.getWindow()));
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
		eventService.publish(new WinDeactivatedEvent(display, e.getWindow()));
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
		eventService.publish(new WinDeiconifiedEvent(display, e.getWindow()));
	}

	@Override
	public void windowIconified(final WindowEvent e) {
		eventService.publish(new WinIconifiedEvent(display, e.getWindow()));
	}

	@Override
	public void windowOpened(final WindowEvent e) {
		eventService.publish(new WinOpenedEvent(display, e.getWindow()));
	}

}
