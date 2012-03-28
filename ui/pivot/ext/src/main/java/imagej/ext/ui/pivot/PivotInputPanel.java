/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.ext.ui.pivot;

import imagej.ext.module.ModuleItem;
import imagej.ext.module.ui.AbstractInputPanel;
import imagej.ext.module.ui.WidgetModel;

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
	public void addNumber(final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		final ModuleItem<?> item = model.getItem();
		final PivotNumberWidget numberWidget =
			PivotNumberWidget.create(model, min, max, stepSize,
				item.getWidgetStyle());
		addField(model.getWidgetLabel(), numberWidget);
		numberWidgets.put(item.getName(), numberWidget);
	}

	@Override
	public void addToggle(final WidgetModel model) {
		final PivotToggleWidget toggleWidget = new PivotToggleWidget(model);
		addField(model.getWidgetLabel(), toggleWidget);
		toggleWidgets.put(model.getItem().getName(), toggleWidget);
	}

	@Override
	public void addTextField(final WidgetModel model, final int columns) {
		final PivotTextFieldWidget textFieldWidget =
			new PivotTextFieldWidget(model);
		addField(model.getWidgetLabel(), textFieldWidget);
		textFieldWidgets.put(model.getItem().getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final WidgetModel model, final String[] items) {
		final PivotChoiceWidget choiceWidget =
			new PivotChoiceWidget(model, items);
		addField(model.getWidgetLabel(), choiceWidget);
		choiceWidgets.put(model.getItem().getName(), choiceWidget);
	}

	@Override
	public void addFile(final WidgetModel model) {
		final PivotFileWidget fileWidget = new PivotFileWidget(model);
		addField(model.getWidgetLabel(), fileWidget);
		fileWidgets.put(model.getItem().getName(), fileWidget);
	}

	@Override
	public void addColor(final WidgetModel model) {
		// TODO create PivotColorWidget and add here
	}

	@Override
	public void addObject(final WidgetModel model) {
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
