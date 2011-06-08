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

package imagej.plugin.ui.pivot;

import imagej.plugin.ui.AbstractInputPanel;
import imagej.plugin.ui.ParamModel;

import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class PivotInputPanel extends AbstractInputPanel {

	private final TablePane pane;

	public PivotInputPanel() {
		pane = new TablePane();
	}

	public Container getPanel() {
		return pane;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		pane.add(new Label(text));
		messageCount++;
	}

	@Override
	public void addNumber(final ParamModel model,
		final Number min, final Number max, final Number stepSize)
	{
		final PivotNumberWidget numberWidget =
			PivotNumberWidget.create(model, min, max, stepSize,
				model.getStyle());
		addField(model.getLabel(), numberWidget);
		numberWidgets.put(model.getName(), numberWidget);
	}

	@Override
	public void addToggle(final ParamModel model) {
		final PivotToggleWidget toggleWidget = new PivotToggleWidget(model);
		addField(model.getLabel(), toggleWidget);
		toggleWidgets.put(model.getName(), toggleWidget);
	}

	@Override
	public void addTextField(final ParamModel model, final int columns) {
		final PivotTextFieldWidget textFieldWidget =
			new PivotTextFieldWidget(model);
		addField(model.getLabel(), textFieldWidget);
		textFieldWidgets.put(model.getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final ParamModel model, final String[] items) {
		final PivotChoiceWidget choiceWidget =
			new PivotChoiceWidget(model, items);
		addField(model.getLabel(), choiceWidget);
		choiceWidgets.put(model.getName(), choiceWidget);
	}

	@Override
	public void addFile(final ParamModel model) {
		final PivotFileWidget fileWidget = new PivotFileWidget(model);
		addField(model.getLabel(), fileWidget);
		fileWidgets.put(model.getName(), fileWidget);
	}

	@Override
	public void addColor(final ParamModel model) {
		// TODO create PivotColorWidget and add here
	}

	@Override
	public void addObject(final ParamModel model) {
		// TODO create PivotObjectWidget and add here
	}

	@Override
	public int getWidgetCount() {
		return pane.getRows().getLength();
	}

	// -- Helper methods --

	private void addField(final String label, final Container component) {
		pane.add(new Label(label == null ? "" : label));
		pane.add(component);
	}

}
