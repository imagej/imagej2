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

package imagej.ui.common.awt;

import imagej.event.EventService;
import imagej.ext.display.Display;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.display.event.window.WinClosedEvent;
import imagej.ext.display.event.window.WinClosingEvent;
import imagej.ext.display.event.window.WinDeactivatedEvent;
import imagej.ext.display.event.window.WinDeiconifiedEvent;
import imagej.ext.display.event.window.WinEvent;
import imagej.ext.display.event.window.WinIconifiedEvent;
import imagej.ext.display.event.window.WinOpenedEvent;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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

	/** Creates an AWT event dispatcher for the given display. */
	public AWTWindowEventDispatcher(final Display<?> display,
		final EventService eventService)
	{
		this.display = display;
		this.eventService = eventService;
	}

	// -- WindowListener methods --

	@Override
	public void windowActivated(final WindowEvent e) {
		eventService.publish(new WinActivatedEvent(display));
	}

	@Override
	public void windowClosed(final WindowEvent e) {
		eventService.publish(new WinClosedEvent(display));
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		eventService.publish(new WinClosingEvent(display));
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
		eventService.publish(new WinDeactivatedEvent(display));
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
		eventService.publish(new WinDeiconifiedEvent(display));
	}

	@Override
	public void windowIconified(final WindowEvent e) {
		eventService.publish(new WinIconifiedEvent(display));
	}

	@Override
	public void windowOpened(final WindowEvent e) {
		eventService.publish(new WinOpenedEvent(display));
	}

}
