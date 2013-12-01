/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.plugins.uis.swt;

import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.ui.ToolBar;
import imagej.ui.UIService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;

/**
 * SWT implementation of {@link ToolBar}.
 * 
 * @author Curtis Rueden
 */
public class SWTToolBar extends Composite implements ToolBar {

	private final Display display;
	private final Map<String, Button> toolButtons;

	@Parameter
	private ToolService toolService;

	@Parameter
	private UIService uiService;

	@Parameter
	private LogService log;

	public SWTToolBar(final Display display, final Composite parent,
		final Context context)
	{
		super(parent, 0);
		context.inject(this);

		this.display = display;
		toolButtons = new HashMap<String, Button>();
		setLayout(new MigLayout());
		// CTR FIXME
		populateToolBar();
	}

	// -- Helper methods --

	private void populateToolBar() {
		final Tool activeTool = toolService.getActiveTool();
		for (final Tool tool : toolService.getTools()) {
			final PluginInfo<?> info = tool.getInfo();
			try {
				final Button button = createButton(tool, tool == activeTool);
				toolButtons.put(info.getName(), button);
			}
			catch (final InstantiableException e) {
				log.warn("Invalid tool: " + info, e);
			}
		}
	}

	private Button createButton(final Tool tool, boolean active) throws InstantiableException {
		final PluginInfo<?> info = tool.getInfo();
		final String name = info.getName();
		final URL iconURL = info.getIconURL();

		final Image iconImage = loadImage(iconURL);
		final Button button = new Button(this, SWT.TOGGLE);
		if (iconImage != null) button.setImage(iconImage);
		if (iconURL == null) {
			button.setText(name);
			log.warn("Invalid icon for tool: " + tool);
		}

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolService.setActiveTool(tool);
			}
		});

		if (active) button.setSelection(true);

		return button;
	}

	private Image loadImage(final URL iconURL) {
		try {
			final InputStream iconStream = iconURL.openStream();
			final Image image = new Image(display, iconStream);
			iconStream.close();
			return image;
		}
		catch (final IOException e) {
			return null;
		}
	}

}
