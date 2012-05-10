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

import imagej.data.display.ui.ImageDisplayViewer;
import imagej.event.EventService;
import imagej.ext.InputModifiers;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsEnteredEvent;
import imagej.ext.display.event.input.MsEvent;
import imagej.ext.display.event.input.MsExitedEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Rebroadcasts AWT {@link MouseEvent}s as ImageJ {@link MsEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTMouseEventDispatcher extends AWTInputEventDispatcher implements
	MouseListener, MouseMotionListener, MouseWheelListener
{

	private final ImageDisplayViewer displayViewer;
	private final EventService eventService;

	/** Creates an AWT input event dispatcher for the given display viewer. */
	public AWTMouseEventDispatcher(final ImageDisplayViewer displayViewer,
		final EventService eventService)
	{
		this.displayViewer = displayViewer;
		this.eventService = eventService;
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsClickedEvent evt =
			new MsClickedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY(), mouseButton(e), e.getClickCount(), e.isPopupTrigger());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsPressedEvent evt =
			new MsPressedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY(), mouseButton(e), e.getClickCount(), e.isPopupTrigger());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsReleasedEvent evt =
			new MsReleasedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY(), mouseButton(e), e.getClickCount(), e.isPopupTrigger());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsEnteredEvent evt =
			new MsEnteredEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsExitedEvent evt =
			new MsExitedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsDraggedEvent evt =
			new MsDraggedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY(), mouseButton(e), e.getClickCount(), e.isPopupTrigger());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsMovedEvent evt =
			new MsMovedEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final MsWheelEvent evt =
			new MsWheelEvent(displayViewer.getDisplay(), modifiers, e.getX(), e
				.getY(), e.getWheelRotation());
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- Helper methods --

	private int mouseButton(final MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				return MsButtonEvent.LEFT_BUTTON;
			case MouseEvent.BUTTON2:
				return MsButtonEvent.RIGHT_BUTTON;
			case MouseEvent.BUTTON3:
				return MsButtonEvent.MIDDLE_BUTTON;
			default:
				return -1;
		}
	}

}
