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

import imagej.widget.ChoiceWidget;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.scijava.plugin.Plugin;

/**
 * Swing implementation of multiple choice selector widget using
 * {@link JRadioButton}s.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class, priority = SwingChoiceWidget.PRIORITY + 1)
public class SwingChoiceRadioWidget extends SwingInputWidget<String> implements
	ActionListener, ChoiceWidget<JPanel>
{

	private List<JRadioButton> radioButtons;

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public boolean isCompatible(final WidgetModel model) {
		return super.isCompatible(model) && model.isText() &&
			model.isMultipleChoice() && isRadioButtonStyle(model);
	}

	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		final String[] items = model.getChoices();

		final ButtonGroup buttonGroup = new ButtonGroup();
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, getBoxAxis(model)));
		radioButtons = new ArrayList<JRadioButton>(items.length);

		for (final String item : items) {
			final JRadioButton radioButton = new JRadioButton(item);
			setToolTip(radioButton);
			radioButton.addActionListener(this);

			buttonGroup.add(radioButton);
			buttonPanel.add(radioButton);
			radioButtons.add(radioButton);
		}
		getComponent().add(buttonPanel);

		refreshWidget();
	}

	@Override
	public String getValue() {
		final JRadioButton selectedButton = getSelectedButton();
		return selectedButton == null ? null : selectedButton.getText();
	}

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		final JRadioButton radioButton = getButton(value);
		if (radioButton.isSelected()) return; // no change
		radioButton.setSelected(true);
	}

	// -- Helper methods --

	private boolean isRadioButtonStyle(final WidgetModel model) {
		final String style = model.getItem().getWidgetStyle();
		return ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE.equals(style) ||
			ChoiceWidget.RADIO_BUTTON_VERTICAL_STYLE.equals(style);
	}

	private int getBoxAxis(final WidgetModel model) {
		final String style = model.getItem().getWidgetStyle();
		if (ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE.equals(style)) {
			return BoxLayout.X_AXIS;
		}
		if (ChoiceWidget.RADIO_BUTTON_VERTICAL_STYLE.equals(style)) {
			return BoxLayout.Y_AXIS;
		}
		throw new IllegalStateException("Invalid widget style: " + style);
	}

	private JRadioButton getSelectedButton() {
		for (final JRadioButton radioButton : radioButtons) {
			if (radioButton.isSelected()) return radioButton;
		}
		return null;
	}

	private JRadioButton getButton(final Object value) {
		for (final JRadioButton radioButton : radioButtons) {
			if (radioButton.getText().equals(value)) return radioButton;
		}
		return null;
	}

}
