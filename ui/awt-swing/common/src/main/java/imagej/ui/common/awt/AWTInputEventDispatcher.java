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

package imagej.ui.common.awt;

import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.display.Display;
import imagej.display.event.input.KyEvent;
import imagej.display.event.input.KyPressedEvent;
import imagej.display.event.input.KyReleasedEvent;
import imagej.display.event.input.KyTypedEvent;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsClickedEvent;
import imagej.display.event.input.MsDraggedEvent;
import imagej.display.event.input.MsEnteredEvent;
import imagej.display.event.input.MsEvent;
import imagej.display.event.input.MsExitedEvent;
import imagej.display.event.input.MsMovedEvent;
import imagej.display.event.input.MsPressedEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.display.event.input.MsWheelEvent;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.scijava.event.EventService;
import org.scijava.input.InputModifiers;
import org.scijava.input.KeyCode;
import org.scijava.util.IntCoords;

/**
 * Rebroadcasts AWT {@link InputEvent}s as ImageJ
 * {@link imagej.display.event.input.InputEvent}s, translating
 * {@link KeyEvent}s into {@link KyEvent}s, and {@link MouseEvent}s into
 * {@link MsEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTInputEventDispatcher implements KeyListener, MouseListener,
	MouseMotionListener, MouseWheelListener
{

	/** Display associated with the dispatched events. */
	private final Display<?> display;

	/** The canvas associated with the dispatched events, if any. */
	private final ImageCanvas imageCanvas;

	/** Event service to use when dispatching events. */
	private final EventService eventService;

	/** Last known mouse X coordinate. */
	private int x = -1;

	/** Last known mouse Y coordinate. */
	private int y = -1;

	/** Creates an AWT input event dispatcher for the given display. */
	public AWTInputEventDispatcher(final Display<?> display) {
		this(display, display.getContext().getService(EventService.class));
	}

	/** Creates an AWT input event dispatcher for the given display. */
	public AWTInputEventDispatcher(final Display<?> display,
		final EventService eventService)
	{
		this.display = display;
		this.eventService = eventService;
		if (display instanceof ImageDisplay) {
			final ImageDisplay imageDisplay = (ImageDisplay) display;
			imageCanvas = imageDisplay.getCanvas();
		}
		else imageCanvas = null;
	}

	// -- AWTInputEventDispatcher methods --

	/**
	 * Attaches the event dispatcher to the given component as a listener.
	 * 
	 * @param c The component from which to rebroadcast events.
	 * @param keyEvents True if key events should be dispatched.
	 * @param mouseEvents True if mouse events should be dispatched.
	 */
	public void register(final Component c, final boolean keyEvents,
		final boolean mouseEvents)
	{
		if (keyEvents) c.addKeyListener(this);
		if (mouseEvents) {
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
			c.addMouseWheelListener(this);
		}
	}

	/** Gets the last known mouse X coordinate. */
	public int getLastX() {
		return x;
	}

	/** Gets the last known mouse Y coordinate. */
	public int getLastY() {
		return y;
	}

	// -- KeyListener methods --

	@Override
	public void keyTyped(final KeyEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final char keyChar = e.getKeyChar();
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		final KyTypedEvent evt =
			new KyTypedEvent(display, modifiers, x, y, keyChar, keyCode);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final char keyChar = e.getKeyChar();
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		final KyPressedEvent evt =
			new KyPressedEvent(display, modifiers, x, y, keyChar, keyCode);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		final char keyChar = e.getKeyChar();
		final KyReleasedEvent evt =
			new KyReleasedEvent(display, modifiers, x, y, keyChar, keyCode);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final int clickCount = e.getClickCount();
		final boolean isPopupTrigger = e.isPopupTrigger();
		final MsClickedEvent evt =
			new MsClickedEvent(display, modifiers, x, y, mouseButton(e), clickCount,
				isPopupTrigger);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final int clickCount = e.getClickCount();
		final boolean isPopupTrigger = e.isPopupTrigger();
		final MsPressedEvent evt =
			new MsPressedEvent(display, modifiers, x, y, mouseButton(e), clickCount,
				isPopupTrigger);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final int clickCount = e.getClickCount();
		final boolean isPopupTrigger = e.isPopupTrigger();
		final MsReleasedEvent evt =
			new MsReleasedEvent(display, modifiers, x, y, mouseButton(e), clickCount,
				isPopupTrigger);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final MsEnteredEvent evt = new MsEnteredEvent(display, modifiers, x, y);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final MsExitedEvent evt = new MsExitedEvent(display, modifiers, x, y);
		clearMouseCoords();
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final int clickCount = e.getClickCount();
		final boolean isPopupTrigger = e.isPopupTrigger();
		final MsDraggedEvent evt =
			new MsDraggedEvent(display, modifiers, x, y, mouseButton(e), clickCount,
				isPopupTrigger);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final MsMovedEvent evt = new MsMovedEvent(display, modifiers, x, y);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final InputModifiers modifiers = createModifiers(e.getModifiersEx());
		updateMouseCoords(e);
		final int wheelRotation = e.getWheelRotation();
		final MsWheelEvent evt =
			new MsWheelEvent(display, modifiers, x, y, wheelRotation);
		eventService.publish(evt);
		if (evt.isConsumed()) e.consume();
	}

	// -- Helper methods --

	private InputModifiers createModifiers(final int modsEx) {
		final boolean altDown = isOn(modsEx, InputEvent.ALT_DOWN_MASK);
		final boolean altGrDown = isOn(modsEx, InputEvent.ALT_GRAPH_DOWN_MASK);
		final boolean ctrlDown = isOn(modsEx, InputEvent.CTRL_DOWN_MASK);
		final boolean metaDown = isOn(modsEx, InputEvent.META_DOWN_MASK);
		final boolean shiftDown = isOn(modsEx, InputEvent.SHIFT_DOWN_MASK);
		final boolean leftButtonDown = isOn(modsEx, InputEvent.BUTTON1_DOWN_MASK);
		final boolean middleButtonDown = isOn(modsEx, InputEvent.BUTTON3_DOWN_MASK);
		final boolean rightButtonDown = isOn(modsEx, InputEvent.BUTTON2_DOWN_MASK);
		return new InputModifiers(altDown, altGrDown, ctrlDown, metaDown,
			shiftDown, leftButtonDown, middleButtonDown, rightButtonDown);
	}

	private boolean isOn(final int modsEx, final int mask) {
		return (modsEx & mask) != 0;
	}

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

	/** Updates last known mouse coordinates. */
	private void updateMouseCoords(final MouseEvent e) {
		x = e.getX();
		y = e.getY();
		if (imageCanvas != null) {
			final IntCoords panOffset = imageCanvas.getPanOffset();
			x -= panOffset.x;
			y -= panOffset.y;
		}
	}

	/** Invalidates last known mouse coordinates. */
	private void clearMouseCoords() {
		x = y = -1;
	}

}
