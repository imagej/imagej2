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

package imagej.plugin.ui.swing;

import imagej.ImageJ;
import imagej.object.ObjectManager;
import imagej.plugin.PluginException;
import imagej.plugin.ui.AbstractInputPanel;
import imagej.plugin.ui.InputPanel;
import imagej.plugin.ui.ParamModel;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Swing implementation of {@link InputPanel}.
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
		messageCount++;
	}

	@Override
	public void addNumber(final ParamModel model, final Number min,
		final Number max, final Number stepSize)
	{
		final SwingNumberWidget numberWidget =
			new SwingNumberWidget(model, min, max, stepSize);
		addField(model, numberWidget);
		numberWidgets.put(model.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamModel model) {
		final SwingToggleWidget toggleWidget = new SwingToggleWidget(model);
		addField(model, toggleWidget);
		toggleWidgets.put(model.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamModel model, final int columns) {
		final SwingTextFieldWidget textFieldWidget =
			new SwingTextFieldWidget(model, columns);
		addField(model, textFieldWidget);
		textFieldWidgets.put(model.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamModel model, final String[] items) {
		final SwingChoiceWidget choiceWidget = new SwingChoiceWidget(model, items);
		addField(model, choiceWidget);
		choiceWidgets.put(model.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamModel model) {
		final SwingFileWidget fileWidget = new SwingFileWidget(model);
		addField(model, fileWidget);
		fileWidgets.put(model.getName(), fileWidget);
	}

	@Override
	public void addColor(final ParamModel model) {
		final SwingColorWidget colorWidget = new SwingColorWidget(model);
		addField(model, colorWidget);
		colorWidgets.put(model.getName(), colorWidget);
	}

	@Override
	public void addObject(final ParamModel model) throws PluginException {
		// TODO - Rectify with identical logic in other UI plugin implementations.
		// Should the ij-object dependency just be part of ij-plugin?
		final Class<?> type = model.getType();
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		final Object[] items = objectManager.getObjects(type).toArray();
		if (items.length == 0) {
			// no valid objects of the given type
			throw new PluginException("No objects of type " + type.getName());
		}
		final SwingObjectWidget objectWidget = new SwingObjectWidget(model, items);
		addField(model, objectWidget);
		objectWidgets.put(model.getName(), objectWidget);
	}

	@Override
	public int getWidgetCount() {
		return panel.getComponentCount();
	}

	// -- Helper methods --

	private void addField(final ParamModel model, final JComponent component) {
		final String label = model.getLabel();
		final String desc = model.getDescription();
		final JLabel l = new JLabel(label == null ? "" : label);
		if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
		panel.add(l);
		panel.add(component);
	}

}
