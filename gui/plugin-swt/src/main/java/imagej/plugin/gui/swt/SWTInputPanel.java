//
// SWTInputPanel.java
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

package imagej.plugin.gui.swt;

import imagej.plugin.gui.AbstractInputPanel;
import imagej.plugin.gui.WidgetStyle;

import java.io.File;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class SWTInputPanel extends AbstractInputPanel {

	private Composite panel;

	public SWTInputPanel(final Composite parent) {
		panel = new Composite(parent, 0);
		panel.setLayout(new MigLayout("wrap 2"));
	}

	public Composite getPanel() {
		return panel;
	}

	@Override
	public void addMessage(final String text) {
		final Label label = addLabel(text);
		label.setLayoutData("span");
	}

	@Override
	public void addNumber(final String name, final String label,
		final Number initialValue, final WidgetStyle style, final Number min,
		final Number max, final Number stepSize)
	{
		addLabel(label);
		final SWTNumberWidget numberWidget =
			new SWTNumberWidget(panel, initialValue, min, max, stepSize);
		numberWidgets.put(name, numberWidget);
	}

	@Override
	public void addToggle(final String name, final String label,
		final boolean initialValue, final WidgetStyle style)
	{
		addLabel(label);
		final SWTToggleWidget toggleWidget =
			new SWTToggleWidget(panel, initialValue);
		toggleWidgets.put(name, toggleWidget);
	}

	@Override
	public void addTextField(final String name, final String label,
		final String initialValue, final WidgetStyle style, final int columns)
	{
		addLabel(label);
		final SWTTextFieldWidget textFieldWidget =
			new SWTTextFieldWidget(panel, initialValue, columns);
		textFieldWidgets.put(name, textFieldWidget);
	}

	@Override
	public void addChoice(final String name, final String label,
		final String initialValue, final WidgetStyle style, final String[] items)
	{
		addLabel(label);
		final SWTChoiceWidget choiceWidget =
			new SWTChoiceWidget(panel, initialValue, items);
		choiceWidgets.put(name, choiceWidget);
	}

	@Override
	public void addFile(final String name, final String label,
		final File initialValue, final WidgetStyle style)
	{
		addLabel(label);
		final SWTFileWidget fileWidget = new SWTFileWidget(panel, initialValue);
		fileWidgets.put(name, fileWidget);
	}

	@Override
	public void addObject(final String name, final String label,
		final Object initialValue, final WidgetStyle style)
	{
		// TODO create ObjectWidget and add here
	}

	private Label addLabel(final String text) {
		final Label label = new Label(panel, 0);
		label.setText(text == null ? "" : text);
		return label;
	}

}
