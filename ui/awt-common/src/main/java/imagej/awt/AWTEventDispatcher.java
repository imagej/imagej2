//
// AWTEventDispatcher.java
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

package imagej.awt;

import imagej.display.Display;
import imagej.display.EventDispatcher;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.display.event.mouse.MsButtonEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsEnteredEvent;
import imagej.display.event.mouse.MsExitedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.display.event.window.WinClosingEvent;
import imagej.display.event.window.WinDeactivatedEvent;
import imagej.display.event.window.WinDeiconifiedEvent;
import imagej.display.event.window.WinIconifiedEvent;
import imagej.display.event.window.WinOpenedEvent;
import imagej.event.Events;
import imagej.event.ImageJEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Rebroadcasts AWT events as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTEventDispatcher implements EventDispatcher, KeyListener,
	MouseListener, MouseMotionListener, MouseWheelListener, WindowListener
{

	private final Display display;
	private final boolean relative;

	/**
	 * Creates an AWT event dispatcher for the given display, which assumes
	 * viewport mouse coordinates.
	 */
	public AWTEventDispatcher(final Display display) {
		this(display, true);
	}

	/**
	 * Creates an AWT event dispatcher for the given display, with mouse
	 * coordinates interpreted according to the relative flag.
	 * 
	 * @param relative If true, coordinates are relative to the entire image
	 *          canvas rather than just the viewport; hence, the pan offset is
	 *          already factored in.
	 */
	public AWTEventDispatcher(final Display display, final boolean relative) {
		this.display = display;
		this.relative = relative;
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

	// -- KeyListener methods --

	@Override
	public void keyTyped(final KeyEvent e) {
		Events.publish(new KyTypedEvent(display, e.getKeyChar(), e.getKeyCode(),
			e.getModifiers()));
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		Events.publish(new KyPressedEvent(display, e.getKeyChar(), e.getKeyCode(),
			e.getModifiers()));
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		Events.publish(new KyReleasedEvent(display, e.getKeyChar(),
			e.getKeyCode(), e.getModifiers()));
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(final MouseEvent e) {
		Events.publish(new MsClickedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		Events.publish(new MsPressedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		Events.publish(new MsReleasedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(final MouseEvent e) {
		Events.publish(new MsEnteredEvent(display, getX(e), getY(e)));
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		Events.publish(new MsExitedEvent(display, getX(e), getY(e)));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		Events.publish(new MsDraggedEvent(display, getX(e), getY(e),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		Events.publish(new MsMovedEvent(display, getX(e), getY(e)));
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		Events.publish(new MsWheelEvent(display, getX(e), getY(e),
			e.getWheelRotation()));
	}

	// -- WindowListener methods --

	@Override
	public void windowActivated(final WindowEvent e) {
		Events.publish(new WinActivatedEvent(display));
	}

	@Override
	public void windowClosed(final WindowEvent e) {
		Events.publish(new WinClosedEvent(display));
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		Events.publish(new WinClosingEvent(display));
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
		Events.publish(new WinDeactivatedEvent(display));
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
		Events.publish(new WinDeiconifiedEvent(display));
	}

	@Override
	public void windowIconified(final WindowEvent e) {
		Events.publish(new WinIconifiedEvent(display));
	}

	@Override
	public void windowOpened(final WindowEvent e) {
		Events.publish(new WinOpenedEvent(display));
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
		return x - display.getImageCanvas().getPanOrigin().x;
	}

	private int getY(final MouseEvent e) {
		final int y = e.getY();
		if (relative) return y;
		return y - display.getImageCanvas().getPanOrigin().y;
	}

}
