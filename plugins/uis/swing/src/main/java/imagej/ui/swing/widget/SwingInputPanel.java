/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.swing.widget;

import imagej.widget.AbstractInputPanel;
import imagej.widget.InputPanel;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Swing implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 */
public class SwingInputPanel extends AbstractInputPanel<JPanel, JPanel> {

	private JPanel uiComponent;

	// -- InputPanel methods --

	@Override
	public void addWidget(final InputWidget<?, JPanel> widget) {
		super.addWidget(widget);
		final JPanel widgetPane = widget.getComponent();
		final WidgetModel model = widget.get();

		// add widget to panel
		if (widget.isLabeled()) {
			// widget is prefixed by a label
			final JLabel l = new JLabel(model.getWidgetLabel());
			final String desc = model.getItem().getDescription();
			if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
			getComponent().add(l);
			getComponent().add(widgetPane);
		}
		else {
			// widget occupies entire row
			getComponent().add(widgetPane, "span");
		}
	}

	@Override
	public Class<JPanel> getWidgetComponentType() {
		return JPanel.class;
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		if (uiComponent == null) {
			uiComponent = new JPanel();
			final MigLayout layout =
					new MigLayout("fillx,wrap 2", "[right]10[fill,grow]");
			uiComponent.setLayout(layout);
		}
		return uiComponent;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

}
