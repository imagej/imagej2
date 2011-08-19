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

import imagej.display.ImageDisplay;
import imagej.display.EventDispatcher;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.event.Events;
import imagej.event.ImageJEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Rebroadcasts AWT events as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class AWTKeyEventDispatcher implements EventDispatcher, KeyListener
{

	private final ImageDisplay display;

	/**
	 * Creates an AWT event dispatcher for the given display, with mouse
	 * coordinates interpreted according to the relative flag.
	 * 
	 * @param relative If true, coordinates are relative to the entire image
	 *          canvas rather than just the viewport; hence, the pan offset is
	 *          already factored in.
	 */
	public AWTKeyEventDispatcher(final ImageDisplay display) {
		this.display = display;
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

}
