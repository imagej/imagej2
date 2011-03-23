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
import imagej.plugin.gui.ParamDetails;

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

	private final Composite panel;

	public SWTInputPanel(final Composite parent) {
		panel = new Composite(parent, 0);
		panel.setLayout(new MigLayout("wrap 2"));
	}

	public Composite getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		final Label label = addLabel(text);
		label.setLayoutData("span");
	}

	@Override
	public void addNumber(final ParamDetails details, final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		addLabel(details.getLabel());
		final SWTNumberWidget numberWidget =
			new SWTNumberWidget(panel, initialValue, min, max, stepSize);
		numberWidgets.put(details.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamDetails details, final boolean initialValue)
	{
		addLabel(details.getLabel());
		final SWTToggleWidget toggleWidget =
			new SWTToggleWidget(panel, initialValue);
		toggleWidgets.put(details.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamDetails details,
		final String initialValue, final int columns)
	{
		addLabel(details.getLabel());
		final SWTTextFieldWidget textFieldWidget =
			new SWTTextFieldWidget(panel, initialValue, columns);
		textFieldWidgets.put(details.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamDetails details, final String initialValue,
		final String[] items)
	{
		addLabel(details.getLabel());
		final SWTChoiceWidget choiceWidget =
			new SWTChoiceWidget(panel, initialValue, items);
		choiceWidgets.put(details.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamDetails details, final File initialValue) {
		addLabel(details.getLabel());
		final SWTFileWidget fileWidget = new SWTFileWidget(panel, initialValue);
		fileWidgets.put(details.getName(), fileWidget);
	}

	@Override
	public void addObject(final ParamDetails details, final Object initialValue)
	{
		// TODO create ObjectWidget and add here
	}

	// -- Helper methods --

	private Label addLabel(final String text) {
		final Label label = new Label(panel, 0);
		label.setText(text == null ? "" : text);
		return label;
	}

}
