//
// AWTInputPanel.java
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

package imagej.plugin.ui.awt;

import imagej.ImageJ;
import imagej.object.ObjectManager;
import imagej.plugin.ui.AbstractInputPanel;
import imagej.plugin.ui.ParamDetails;

import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;

import net.miginfocom.swing.MigLayout;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class AWTInputPanel extends AbstractInputPanel {

	private final Panel panel;

	public AWTInputPanel() {
		panel = new Panel();
		panel.setLayout(new MigLayout("wrap 2"));
	}

	public Panel getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		panel.add(new Label(text), "span");
	}

	@Override
	public void addNumber(final ParamDetails details,
		final Number min, final Number max, final Number stepSize)
	{
		final AWTNumberWidget numberWidget =
			new AWTNumberWidget(details, min, max, stepSize);
		addField(details.getLabel(), numberWidget);
		numberWidgets.put(details.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamDetails details) {
		final AWTToggleWidget toggleWidget = new AWTToggleWidget(details);
		addField(details.getLabel(), toggleWidget);
		toggleWidgets.put(details.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamDetails details, final int columns) {
		final AWTTextFieldWidget textFieldWidget =
			new AWTTextFieldWidget(details, columns);
		addField(details.getLabel(), textFieldWidget);
		textFieldWidgets.put(details.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamDetails details, final String[] items) {
		final AWTChoiceWidget choiceWidget =
			new AWTChoiceWidget(details, items);
		addField(details.getLabel(), choiceWidget);
		choiceWidgets.put(details.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamDetails details) {
		final AWTFileWidget fileWidget = new AWTFileWidget(details);
		addField(details.getLabel(), fileWidget);
		fileWidgets.put(details.getName(), fileWidget);
	}

	@Override
	public void addObject(final ParamDetails details) {
		final Class<?> type = details.getType();
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		final Object[] items = objectManager.getObjects(type).toArray();
		final AWTObjectWidget objectWidget = new AWTObjectWidget(details, items);
		addField(details.getLabel(), objectWidget);
		objectWidgets.put(details.getName(), objectWidget);
	}

	@Override
	public boolean hasWidgets() {
		return panel.getComponentCount() > 0;
	}

	// -- Helper methods --

	private void addField(final String label, final Component component) {
		panel.add(new Label(label == null ? "" : label));
		panel.add(component);
	}

}
