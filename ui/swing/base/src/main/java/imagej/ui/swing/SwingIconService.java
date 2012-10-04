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

package imagej.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.tool.IconDrawer;
import imagej.tool.IconService;
import imagej.tool.Tool;
import imagej.util.ColorRGB;

import javax.swing.AbstractButton;

/**
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Service.class)
public class SwingIconService extends AbstractService implements IconService {

	private HashMap<Tool,AbstractButton> buttonMap = new HashMap<Tool, AbstractButton>();
	
	@Override
	public IconDrawer acquireDrawer(Tool tool) {
		return new SwingIconDrawer(tool);
	}
	
	public void registerButton(Tool tool, AbstractButton button) {
		buttonMap.put(tool,button);
	}
	
	private class SwingIconDrawer implements IconDrawer {
		private Tool tool;
		private AbstractButton button;
		private Graphics graphics;
		
		public SwingIconDrawer(Tool tool) {
			this.tool = tool;
			this.button = buttonMap.get(tool);
			this.graphics = button.getGraphics();
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
		public void setIconPixel(int x, int y, ColorRGB color) {
			Color awtColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
			graphics.setColor(awtColor);
			graphics.drawLine(x, y, x, y);
		}
		
	}

}
