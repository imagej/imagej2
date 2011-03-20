//
// PivotInputPanel.java
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

package imagej.plugin.gui.pivot;

import imagej.plugin.gui.AbstractInputPanel;
import imagej.plugin.gui.WidgetStyle;

import java.io.File;

import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class PivotInputPanel extends AbstractInputPanel {

	private TablePane pane;

	public PivotInputPanel() {
		pane = new TablePane();
	}

	public Container getPanel() {
		return pane;
	}

	@Override
	public void addMessage(final String text) {
		pane.add(new Label(text));
	}

	@Override
	public void addNumber(final String name, final String label,
		final Number initialValue, final WidgetStyle style, final Number min,
		final Number max, final Number stepSize)
	{
		final PivotNumberWidget numberWidget =
			PivotNumberWidget.create(initialValue, min, max, stepSize, style);
		addField(label, numberWidget);
		numberWidgets.put(name, numberWidget);
	}

	@Override
	public void addToggle(final String name, final String label,
		final boolean initialValue, final WidgetStyle style)
	{
		final PivotToggleWidget toggleWidget = new PivotToggleWidget(initialValue);
		addField(label, toggleWidget);
		toggleWidgets.put(name, toggleWidget);
	}

	@Override
	public void addTextField(final String name, final String label,
		final String initialValue, final WidgetStyle style, final int columns)
	{
		final PivotTextFieldWidget textFieldWidget =
			new PivotTextFieldWidget(initialValue, columns);
		addField(label, textFieldWidget);
		textFieldWidgets.put(name, textFieldWidget);
	}

	@Override
	public void addChoice(final String name, final String label,
		final String initialValue, final WidgetStyle style, final String[] items)
	{
		final PivotChoiceWidget choiceWidget =
			new PivotChoiceWidget(initialValue, items);
		addField(label, choiceWidget);
		choiceWidgets.put(name, choiceWidget);
	}

	@Override
	public void addFile(final String name, final String label,
		final File initialValue, final WidgetStyle style)
	{
		final PivotFileWidget fileWidget = new PivotFileWidget(initialValue);
		addField(label, fileWidget);
		fileWidgets.put(name, fileWidget);
	}

	@Override
	public void addObject(final String name, final String label,
		final Object initialValue, final WidgetStyle style)
	{
		// TODO create ObjectWidget and add here
	}

	private void addField(final String label, final Container component) {
		pane.add(new Label(label == null ? "" : label));
		pane.add(component);
	}

}
