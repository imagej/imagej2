//
// AWTMouseEventDispatcher.java
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

package imagej.ui.common.awt;

import imagej.data.display.ImageDisplay;
import imagej.event.EventService;
import imagej.event.ImageJEvent;
import imagej.ext.display.EventDispatcher;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsEnteredEvent;
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
 * Rebroadcasts AWT events as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTMouseEventDispatcher implements EventDispatcher, MouseListener,
	MouseMotionListener, MouseWheelListener
{

	private final ImageDisplay display;
	private final boolean relative;
	private final EventService eventService;

	/**
	 * Creates an AWT event dispatcher for the given display, which assumes
	 * viewport mouse coordinates.
	 */
	public AWTMouseEventDispatcher(final ImageDisplay display,
		final EventService eventService)
	{
		this(display, eventService, true);
	}

	/**
	 * Creates an AWT event dispatcher for the given display, with mouse
	 * coordinates interpreted according to the relative flag.
	 * 
	 * @param relative If true, coordinates are relative to the entire image
	 *          canvas rather than just the viewport; hence, the pan offset is
	 *          already factored in.
	 */
	public AWTMouseEventDispatcher(final ImageDisplay display,
		final EventService eventService, final boolean relative)
	{
		this.display = display;
		this.relative = relative;
		this.eventService = eventService;
	}

	// -- AWTEventDispatcher methods --

	/**
	 * Gets whether mouse coordinates are provided relative to the unpanned image
	 * canvas. If true, the coordinates are measured from the top left corner of
	 * the image canvas, regardless of the current pan. Hence, the coordinate
	 * values will equal the pan offset plus the viewport coordinate values. If
	 * false, the coordinates are relative to the canvas's viewport, meaning that
	 * the pan offset is not lumped into the coordinate values.
	 */
	public boolean isRelative() {
		return relative;
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(final MouseEvent e) {
		eventService.publish(new MsClickedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		eventService.publish(new MsPressedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		eventService.publish(new MsReleasedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(final MouseEvent e) {
		eventService.publish(new MsEnteredEvent(display, getX(e), getY(e)));
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		eventService.publish(new MsExitedEvent(display, getX(e), getY(e)));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		eventService.publish(new MsDraggedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		eventService.publish(new MsMovedEvent(display, getX(e), getY(e)));
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		eventService.publish(new MsWheelEvent(display, getX(e), getY(e), e
			.getWheelRotation()));
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

	private int getX(final MouseEvent e) {
		final int x = e.getX();
		if (relative) return x;
		return x - display.getCanvas().getPanOrigin().x;
	}

	private int getY(final MouseEvent e) {
		final int y = e.getY();
		if (relative) return y;
		return y - display.getCanvas().getPanOrigin().y;
	}

}
