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

package imagej.ext.ui.swt;

import imagej.ext.module.ModuleException;
import imagej.ext.module.ui.AbstractInputPanel;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.module.ui.WidgetModel;
import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * SWT implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 */
public class SWTInputPanel extends AbstractInputPanel {

	private final Composite panel;

	public SWTInputPanel(final Composite parent) {
		panel = new Composite(parent, 0);
		panel.setLayout(new MigLayout("wrap 2"));
	}
	
	// -- SWTInputPanel methods --

	public Composite getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		final Label label = addLabel(text);
		label.setLayoutData("span");
		messageCount++;
	}

	@Override
	public void addNumber(final WidgetModel model, final Number min,
		final Number max, final Number stepSize)
	{
		addLabel(model.getWidgetLabel());
		final SWTNumberWidget numberWidget =
			new SWTNumberWidget(panel, model, min, max, stepSize);
		numberWidgets.put(model.getItem().getName(), numberWidget);
	}

	@Override
	public void addToggle(final WidgetModel model) {
		addLabel(model.getWidgetLabel());
		final SWTToggleWidget toggleWidget = new SWTToggleWidget(panel, model);
		toggleWidgets.put(model.getItem().getName(), toggleWidget);
	}

	@Override
	public void addTextField(final WidgetModel model, final int columns) {
		addLabel(model.getWidgetLabel());
		final SWTTextFieldWidget textFieldWidget =
			new SWTTextFieldWidget(panel, model, columns);
		textFieldWidgets.put(model.getItem().getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final WidgetModel model, final String[] items) {
		addLabel(model.getWidgetLabel());
		final SWTChoiceWidget choiceWidget =
			new SWTChoiceWidget(panel, model, items);
		choiceWidgets.put(model.getItem().getName(), choiceWidget);
	}

	@Override
	public void addFile(final WidgetModel model) {
		addLabel(model.getWidgetLabel());
		final SWTFileWidget fileWidget = new SWTFileWidget(panel, model);
		fileWidgets.put(model.getItem().getName(), fileWidget);
	}

	@Override
	public void addColor(final WidgetModel model) {
		// TODO create SWTColorWidget and add here
	}

	@Override
	public void addObject(final WidgetModel model) throws ModuleException {
		addLabel(model.getWidgetLabel());
		final Object[] items = getObjects(model);
		final SWTObjectWidget objectWidget =
			new SWTObjectWidget(panel, model, items);
		objectWidgets.put(model.getItem().getName(), objectWidget);
	}

	@Override
	public int getWidgetCount() {
		return panel.getChildren().length;
	}

	// -- Helper methods --

	private Label addLabel(final String text) {
		final Label label = new Label(panel, 0);
		label.setText(text == null ? "" : text);
		return label;
	}

}
