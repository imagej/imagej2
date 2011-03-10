//
// AWTToolBar.java
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

package imagej.gui.awt;

import imagej.Log;
import imagej.plugin.api.PluginException;
import imagej.tool.ITool;
import imagej.tool.ToolEntry;
import imagej.tool.ToolManager;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Button bar with selectable tools, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class AWTToolBar extends Panel {

	private ToolManager toolManager;

	private Map<String, Button> toolButtons;

	public AWTToolBar(final ToolManager toolManager) {
		this.toolManager = toolManager;
		toolButtons = new HashMap<String, Button>();
		setLayout(new FlowLayout());
		populateToolBar();
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	private void populateToolBar() {
		for (final ToolEntry entry : toolManager.getToolEntries()) {
			try {
				final Button button = createButton(entry);
				toolButtons.put(entry.getName(), button);
				add(button);
			}
			catch (PluginException e) {
				Log.warn("Invalid tool: " + entry, e);
			}
		}
	}

	private Button createButton(final ToolEntry entry) throws PluginException {
		final ITool tool = entry.createInstance();
		// TODO - consider alternatives to assigning the entry manually
		tool.setToolEntry(entry);
		final String name = entry.getName();
		final URL iconURL = entry.getIconURL();

		final Image iconImage = loadImage(iconURL);
		final Button button = new Button() {
			@Override
			public void paint(final Graphics g) {
				super.paint(g);
				if (iconImage == null) return;
				final int buttonWidth = getWidth();
				final int buttonHeight = getHeight();
				final int iconWidth = iconImage.getWidth(this);
				final int iconHeight = iconImage.getHeight(this);
				g.drawImage(iconImage, (buttonWidth - iconWidth) / 2,
					(buttonHeight - iconHeight) / 2, this);
			}
		};
		if (iconURL == null) {
			button.setLabel(name);
			Log.warn("Invalid icon for tool: " + tool);
		}

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getToolManager().setActiveTool(tool);
			}
		});

		return button;
	}

	private Image loadImage(final URL iconURL) {
		return Toolkit.getDefaultToolkit().createImage(iconURL);
	}

}
