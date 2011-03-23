//
// SwingInputPanel.java
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

package imagej.plugin.gui.swing;

import imagej.plugin.gui.AbstractInputPanel;
import imagej.plugin.gui.ParamDetails;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class SwingInputPanel extends AbstractInputPanel {

	private final JPanel panel;

	public SwingInputPanel() {
		panel = new JPanel();
		panel.setLayout(new MigLayout("wrap 2"));
	}

	public JPanel getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		panel.add(new JLabel(text), "span");
	}

	@Override
	public void addNumber(final ParamDetails details, final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		final SwingNumberWidget numberWidget =
			SwingNumberWidget.create(details, initialValue, min, max, stepSize);
		addField(details.getLabel(), numberWidget);
		numberWidgets.put(details.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamDetails details, final boolean initialValue)
	{
		final SwingToggleWidget toggleWidget =
			new SwingToggleWidget(details, initialValue);
		addField(details.getLabel(), toggleWidget);
		toggleWidgets.put(details.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamDetails details,
		final String initialValue, final int columns)
	{
		final SwingTextFieldWidget textFieldWidget =
			new SwingTextFieldWidget(details, initialValue, columns);
		addField(details.getLabel(), textFieldWidget);
		textFieldWidgets.put(details.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamDetails details, final String initialValue,
		final String[] items)
	{
		final SwingChoiceWidget choiceWidget =
			new SwingChoiceWidget(details, initialValue, items);
		addField(details.getLabel(), choiceWidget);
		choiceWidgets.put(details.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamDetails details, final File initialValue) {
		final SwingFileWidget fileWidget =
			new SwingFileWidget(details, initialValue);
		addField(details.getLabel(), fileWidget);
		fileWidgets.put(details.getName(), fileWidget);
	}

	@Override
	public void addObject(final ParamDetails details, final Object initialValue)
	{
		// TODO create ObjectWidget and add here
	}

	// -- Helper methods --

	private void addField(final String label, final JComponent component) {
		panel.add(new JLabel(label == null ? "" : label));
		panel.add(component);
	}

}
