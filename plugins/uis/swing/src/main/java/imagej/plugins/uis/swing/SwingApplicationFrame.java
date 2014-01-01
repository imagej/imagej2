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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.plugins.uis.swing;

import imagej.ui.ApplicationFrame;
import imagej.ui.common.awt.AWTInputEventDispatcher;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.HeadlessException;

import javax.swing.JFrame;

/**
 * Swing implementation of {@link ApplicationFrame}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class SwingApplicationFrame extends JFrame implements ApplicationFrame {

	public SwingApplicationFrame(final String title) throws HeadlessException {
		super(title);
	}

	// -- SwingApplicationFrame methods --

	public void addEventDispatcher(final AWTInputEventDispatcher dispatcher) {
		dispatcher.register(this, false, true);
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

	@Override
	public void activate() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				// NB: You might think calling requestFocus() would work, but no.
				// The following solution is from: http://bit.ly/zAXzd5
				toFront();
				repaint();
			}
		});
	}

	// -- Helper methods --

	/** Recursively listens for keyboard events on the given component. */
	private void addKeyDispatcher(final AWTInputEventDispatcher dispatcher,
		final Component comp)
	{
		comp.addKeyListener(dispatcher);
		if (!(comp instanceof Container)) return;
		final Container c = (Container) comp;
		final int childCount = c.getComponentCount();
		for (int i = 0; i < childCount; i++) {
			final Component child = c.getComponent(i);
			addKeyDispatcher(dispatcher, child);
		}
	}

}
