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

package imagej.plugins.uis.swing;

import imagej.tool.IconDrawer;
import imagej.tool.IconService;
import imagej.tool.Tool;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import javax.swing.AbstractButton;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ColorRGB;

/**
 * Swing implementation of the {@link IconService}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class SwingIconService extends AbstractService implements IconService {

	// -- instance variables --

	private final HashMap<Tool, AbstractButton> buttonMap =
		new HashMap<Tool, AbstractButton>();

	// -- IconService methods --

	@Override
	public IconDrawer acquireDrawer(final Tool tool) {
		if (!buttonMap.containsKey(tool)) {
			// no button associated with the specified tool; no drawing needed
			return null;
		}
		return new SwingIconDrawer(tool);
	}

	// -- SwingIconService methods --

	public void registerButton(final Tool tool, final AbstractButton button) {
		buttonMap.put(tool, button);
	}

	// -- private helpers --

	private class SwingIconDrawer implements IconDrawer {

		private final AbstractButton button;
		private final Graphics graphics;

		public SwingIconDrawer(final Tool tool) {
			this.button = buttonMap.get(tool);
			// TODO - eliminate this ref. But experiment below doesn't draw correctly.
			graphics = button.getGraphics();
		}

		@Override
		public int getIconRectangleWidth() {
			return button.getWidth();
		}

		@Override
		public int getIconRectangleHeight() {
			return button.getHeight();
		}

		@Override
		public void setIconPixel(final int x, final int y, final ColorRGB color) {

			final Color awtColor =
				new Color(color.getRed(), color.getGreen(), color.getBlue());

			// TODO this would be nice but doesn't work
			// button.getGraphics().setColor(awtColor);
			// button.getGraphics().drawLine(x, y, x, y);

			// But using an old reference does work
			graphics.setColor(awtColor);
			graphics.drawLine(x, y, x, y);
		}

	}

}
