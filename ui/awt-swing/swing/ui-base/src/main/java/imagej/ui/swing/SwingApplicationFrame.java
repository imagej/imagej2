//
// SwingApplicationFrame.java
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

package imagej.ui.swing;

import imagej.ui.ApplicationFrame;
import imagej.ui.common.awt.AWTKeyEventDispatcher;

import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;

import javax.swing.JFrame;

/**
 * Swing implementation of {@link ApplicationFrame}.
 * 
 * @author Grant Harris
 */
public class SwingApplicationFrame extends JFrame implements ApplicationFrame {

	public SwingApplicationFrame(final String title) throws HeadlessException {
		super(title);
	}
	
	// -- SwingApplicationFrame methods --
	
	public void addEventDispatcher(final AWTKeyEventDispatcher dispatcher) {
		addKeyListener(dispatcher);
		addKeyDispatcher(dispatcher, getContentPane());
	}

	// -- ApplicationFrame methods --

	@Override
	public int getLocationX() {
		return getLocation().x;
	}

	@Override
	public int getLocationY() {
		return getLocation().y;
	}

	// -- Helper methods --
		
	/** Recursively listens for keyboard events on the given component. */
	private void addKeyDispatcher(final AWTKeyEventDispatcher keyDispatcher,
		final Component comp)
	{
		comp.addKeyListener(keyDispatcher);
		if (!(comp instanceof Container)) return;
		final Container c = (Container) comp;
		final int childCount = c.getComponentCount();
		for (int i = 0; i < childCount; i++) {
			final Component child = c.getComponent(i);
			addKeyDispatcher(keyDispatcher, child);
		}
	}

}
