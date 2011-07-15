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
import imagej.module.ModuleException;
import imagej.module.ui.AbstractInputPanel;
import imagej.module.ui.InputPanel;
import imagej.module.ui.WidgetModel;
import imagej.object.ObjectManager;

import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;

import net.miginfocom.swing.MigLayout;

/**
 * AWT implementation of {@link InputPanel}.
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
		messageCount++;
	}

	@Override
	public void addNumber(final WidgetModel model, final Number min,
		final Number max, final Number stepSize)
	{
		final AWTNumberWidget numberWidget =
			new AWTNumberWidget(model, min, max, stepSize);
		addField(model.getWidgetLabel(), numberWidget);
		numberWidgets.put(model.getItem().getName(), numberWidget);
	}

	@Override
	public void addToggle(final WidgetModel model) {
		final AWTToggleWidget toggleWidget = new AWTToggleWidget(model);
		addField(model.getWidgetLabel(), toggleWidget);
		toggleWidgets.put(model.getItem().getName(), toggleWidget);
	}

	@Override
	public void addTextField(final WidgetModel model, final int columns) {
		final AWTTextFieldWidget textFieldWidget =
			new AWTTextFieldWidget(model, columns);
		addField(model.getWidgetLabel(), textFieldWidget);
		textFieldWidgets.put(model.getItem().getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final WidgetModel model, final String[] items) {
		final AWTChoiceWidget choiceWidget = new AWTChoiceWidget(model, items);
		addField(model.getWidgetLabel(), choiceWidget);
		choiceWidgets.put(model.getItem().getName(), choiceWidget);
	}

	@Override
	public void addFile(final WidgetModel model) {
		final AWTFileWidget fileWidget = new AWTFileWidget(model);
		addField(model.getWidgetLabel(), fileWidget);
		fileWidgets.put(model.getItem().getName(), fileWidget);
	}

	@Override
	public void addColor(final WidgetModel model) {
		// TODO create AWTColorWidget and add here
	}

	@Override
	public void addObject(final WidgetModel model) throws ModuleException {
		// CTR FIXME - Rectify with identical logic in other implementations.
		// Should ij-object be merged with ij-core?
		final Class<?> type = model.getItem().getType();
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		final Object[] items = objectManager.getObjects(type).toArray();
		if (items.length == 0) {
			// no valid objects of the given type
			throw new ModuleException("No objects of type " + type.getName());
		}
		final AWTObjectWidget objectWidget = new AWTObjectWidget(model, items);
		addField(model.getWidgetLabel(), objectWidget);
		objectWidgets.put(model.getItem().getName(), objectWidget);
	}

	@Override
	public int getWidgetCount() {
		return panel.getComponentCount();
	}

	// -- Helper methods --

	private void addField(final String label, final Component component) {
		panel.add(new Label(label == null ? "" : label));
		panel.add(component);
	}

}
