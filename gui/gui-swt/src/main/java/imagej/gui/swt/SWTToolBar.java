//
// SWTToolBar.java
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

package imagej.gui.swt;

import imagej.Log;
import imagej.manager.Managers;
import imagej.plugin.api.PluginException;
import imagej.tool.ITool;
import imagej.tool.ToolEntry;
import imagej.tool.ToolManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Button bar with selectable tools, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class SWTToolBar extends Composite {

	private final Display display;
	private final ToolManager toolManager;
	private final Map<String, Button> toolButtons;

	public SWTToolBar(final Display display, final Composite parent) {
		super(parent, 0);
		this.display = display;
		toolManager = Managers.get(ToolManager.class);
		toolButtons = new HashMap<String, Button>();
		setLayout(new MigLayout());
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
		final Button button = new Button(this, SWT.TOGGLE);
		if (iconImage != null) button.setImage(iconImage);
		if (iconURL == null) {
			button.setText(name);
			Log.warn("Invalid icon for tool: " + tool);
		}

		// TODO
//		button.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				getToolManager().setActiveTool(tool);
//			}
//		});

		return button;
	}

	private Image loadImage(final URL iconURL) {
		try {
			final InputStream iconStream = iconURL.openStream();
			final Image image = new Image(display, iconStream);
			iconStream.close();
			return image;
		}
		catch (IOException e) {
			return null;
		}
	}

}
