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

package imagej.plugin.ui.swt;

import imagej.ImageJ;
import imagej.object.ObjectManager;
import imagej.plugin.ui.AbstractInputPanel;
import imagej.plugin.ui.ParamModel;
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
	public void addNumber(final ParamModel model,
		final Number min, final Number max, final Number stepSize)
	{
		addLabel(model.getLabel());
		final SWTNumberWidget numberWidget =
			new SWTNumberWidget(panel, model, min, max, stepSize);
		numberWidgets.put(model.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamModel model) {
		addLabel(model.getLabel());
		final SWTToggleWidget toggleWidget =
			new SWTToggleWidget(panel, model);
		toggleWidgets.put(model.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamModel model, final int columns) {
		addLabel(model.getLabel());
		final SWTTextFieldWidget textFieldWidget =
			new SWTTextFieldWidget(panel, model, columns);
		textFieldWidgets.put(model.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamModel model, final String[] items) {
		addLabel(model.getLabel());
		final SWTChoiceWidget choiceWidget =
			new SWTChoiceWidget(panel, model, items);
		choiceWidgets.put(model.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamModel model) {
		addLabel(model.getLabel());
		final SWTFileWidget fileWidget = new SWTFileWidget(panel, model);
		fileWidgets.put(model.getName(), fileWidget);
	}

	@Override
	public void addColor(final ParamModel model) {
		// TODO create SWTColorWidget and add here
	}

	@Override
	public void addObject(final ParamModel model) {
		final Class<?> type = model.getType();
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		final Object[] items = objectManager.getObjects(type).toArray();
		final SWTObjectWidget objectWidget =
			new SWTObjectWidget(panel, model, items);
		objectWidgets.put(model.getName(), objectWidget);
	}

	@Override
	public boolean hasWidgets() {
		return panel.getChildren().length > 0;
	}

	// -- Helper methods --

	private Label addLabel(final String text) {
		final Label label = new Label(panel, 0);
		label.setText(text == null ? "" : text);
		return label;
	}

}
