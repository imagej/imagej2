/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.uis.pivot.widget;

import imagej.widget.AbstractInputPanel;
import imagej.widget.InputPanel;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;

/**
 * Pivot implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 */
public class PivotInputPanel extends AbstractInputPanel<TablePane, BoxPane> {

	private TablePane uiComponent;

	// -- InputPanel methods --

	@Override
	public void addWidget(final InputWidget<?, BoxPane> widget) {
		super.addWidget(widget);
		final BoxPane widgetPane = widget.getComponent();
		final WidgetModel model = widget.get();

		final TablePane.Row row = new TablePane.Row();
		if (widget.isLabeled()) {
			// widget is prefixed by a label
			row.add(new Label(model.getWidgetLabel()));
		}
		row.add(widgetPane);
		getComponent().getRows().add(row);
	}

	@Override
	public Class<BoxPane> getWidgetComponentType() {
		return BoxPane.class;
	}

	// -- UIComponent methods --

	@Override
	public TablePane getComponent() {
		if (uiComponent == null) {
			uiComponent = new TablePane();
			final TablePane.Column labelColumn = new TablePane.Column();
			labelColumn.setWidth("1*");
			uiComponent.getColumns().add(labelColumn);
			final TablePane.Column widgetColumn = new TablePane.Column();
			labelColumn.setWidth("-1");
			uiComponent.getColumns().add(widgetColumn);
		}
		return uiComponent;
	}

	@Override
	public Class<TablePane> getComponentType() {
		return TablePane.class;
	}

}
