//
// AWTKeyEventDispatcherGlobal.java
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
import imagej.data.display.ImageDisplayService;
import imagej.event.Events;
import imagej.ext.display.Display;
import imagej.ext.display.event.key.KyPressedEvent;
import imagej.ext.display.event.key.KyReleasedEvent;
import imagej.ext.display.event.key.KyTypedEvent;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

/**
 * Intercept all keystrokes in the SystemEventQueue and emit/publish new
 * KyEvents on the EventBus, using the currently selected ImageDisplay as the
 * display in the KyEvent.
 * 
 * @author Grant Harris
 */
public class AWTKeyEventDispatcherGlobal extends EventQueue {

	private static final AWTKeyEventDispatcherGlobal instance =
		new AWTKeyEventDispatcherGlobal();

	static {
		// here we register ourselves as a new link in the chain of responsibility
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(instance);
	}

	private AWTKeyEventDispatcherGlobal() {} // One is enough - singleton

	public static AWTKeyEventDispatcherGlobal getInstance() {
		return instance;
	}

	@Override
	protected void dispatchEvent(final AWTEvent event) {
		if (event instanceof KeyEvent) {
			// get the current image display
			final ImageDisplay display =
				ImageJ.get(ImageDisplayService.class).getActiveImageDisplay();
			final int eventId = event.getID();
			if (eventId == KeyEvent.KEY_TYPED) {
				keyTyped((KeyEvent) event, display);
			}
			if (eventId == KeyEvent.KEY_PRESSED) {
				keyPressed((KeyEvent) event, display);
			}
			if (eventId == KeyEvent.KEY_RELEASED) {
				keyReleased((KeyEvent) event, display);
			}
		}
		super.dispatchEvent(event); // let the next in chain handle event
	}

	public void keyTyped(final KeyEvent e, final Display<?> display) {
		Events.publish(new KyTypedEvent(display, e.getKeyChar(), e.getKeyCode(), e
			.getModifiers()));
	}

	public void keyPressed(final KeyEvent e, final Display<?> display) {
		Events.publish(new KyPressedEvent(display, e.getKeyChar(), e.getKeyCode(),
			e.getModifiers()));
	}

	public void keyReleased(final KeyEvent e, final Display<?> display) {
		Events.publish(new KyReleasedEvent(display, e.getKeyChar(), e.getKeyCode(),
			e.getModifiers()));
	}

}
