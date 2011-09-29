//
// AWTKeyEventDispatcher.java
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

import imagej.ImageJ;
import imagej.data.display.ImageDisplay;
import imagej.event.EventService;
import imagej.event.ImageJEvent;
import imagej.ext.display.Display;
import imagej.ext.display.EventDispatcher;
import imagej.ext.display.KeyCode;
import imagej.ext.display.event.key.KyPressedEvent;
import imagej.ext.display.event.key.KyReleasedEvent;
import imagej.ext.display.event.key.KyTypedEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Rebroadcasts AWT {@link KeyEvent}s as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTKeyEventDispatcher implements EventDispatcher, KeyListener {

	private final Display<?> display;

	final protected EventService eventService;

	/** Creates an AWT key event dispatcher for the given display. */
	public AWTKeyEventDispatcher(final Display<?> display, final EventService eventService) {
		this.display = display;
		this.eventService = eventService;
	}

	// -- KeyListener methods --

	@Override
	public void keyTyped(final KeyEvent e) {
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		eventService.publish(new KyTypedEvent(display, e.getKeyChar(), keyCode, e
			.getModifiers()));
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		eventService.publish(new KyPressedEvent(display, e.getKeyChar(), keyCode, e
			.getModifiers()));
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final KeyCode keyCode = KeyCode.get(e.getKeyCode());
		eventService.publish(new KyReleasedEvent(display, e.getKeyChar(), keyCode, e
			.getModifiers()));
	}

}
