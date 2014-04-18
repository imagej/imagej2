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

package imagej.plugins.uis.swing.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.imagej.Dataset;
import net.imglib2.meta.TypedAxis;
import net.imglib2.meta.TypedSpace;

import org.scijava.command.CommandModuleItem;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Default Swing implementation of dimension selector widget.
 * 
 * @author Daniel Seebacher
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingDimSelectionWidget extends SwingInputWidget<TypedAxis[]>
	implements DimSelectionWidget<JPanel>
{

	@Parameter
	private LogService log;

	protected TypedAxis[] m_typedAxis;

	/**
	 * A list containing a JToggleButton for each input dimension.
	 */
	private List<JToggleButton> m_dimLabelButtonList;

	/**
	 * A queue containing all currently active {@link JToggleButton}. Limited to
	 * m_maxNumDims.
	 */
	private Queue<JToggleButton> m_activeToogleButtonsQueue;

	/**
	 * The maximum number of allowed dimensions.
	 */
	private int m_maxNumDims;

	/**
	 * The minimum number of allowed dimensions.
	 */
	private int m_minNumDims;

	@Override
	public TypedAxis[] getValue() {
		final List<TypedAxis> selectedAxis = new ArrayList<TypedAxis>();

		for (int i = 0; i < m_dimLabelButtonList.size(); i++) {
			final JToggleButton axisButton = m_dimLabelButtonList.get(i);
			if (axisButton.isSelected()) {
				selectedAxis.add(m_typedAxis[i]);
			}
		}

		return selectedAxis.toArray(new TypedAxis[selectedAxis.size()]);
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		setAxis();

		m_minNumDims = getMinDims();
		m_maxNumDims = getMaxDims();
		if (m_minNumDims > m_maxNumDims) {
			throw new IllegalArgumentException("Minimum can't be larger than maximum");
		}

		buildUI();
		refreshWidget();
	}

	/**
	 * Sets the axis which will be used by this dimension selection dialog.
	 */
	protected void setAxis() {
		m_typedAxis = extractAxis(get());

		if (m_typedAxis == null) {
			log.debug("Input model doesn't have a TypedSpace");
			throw new IllegalArgumentException("Model doesn't have a TypedSpace<?>");
		}

	}

	/**
	 * Builds the UI, i.e. create a JToggleButton for each dimension of the
	 * {@link Dataset}, add an ActionListener and add it to the {@link Component}
	 * Panel of the super class.
	 */
	private void buildUI() {

		m_dimLabelButtonList = new ArrayList<JToggleButton>(m_typedAxis.length);
		m_activeToogleButtonsQueue = new LinkedList<JToggleButton>();

		for (final TypedAxis m_typedAxi : m_typedAxis) {
			final JToggleButton button =
				new DimensionSelectionToggleButton(m_typedAxi.type().getLabel());

			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					toogleButtonChanged(e);
				}

			});

			m_dimLabelButtonList.add(button);
			getComponent().add(button);
		}

		// activate m_maxNumDims number of dimensions
		for (int i = 0; i < Math.min(m_dimLabelButtonList.size(), m_maxNumDims); i++)
		{
			final JToggleButton button = m_dimLabelButtonList.get(i);
			button.setSelected(true);
			m_activeToogleButtonsQueue.add(button);
		}
	}

	/**
	 * FIXME: wait for curtis to enable easier access.
	 * 
	 * @return The minimum number of dimensions.
	 */
	private int getMinDims() {
		for (final Attr attr : ((CommandModuleItem<?>) get().getItem())
			.getParameter().attrs())
		{
			if (attr.name().equals("min_dims")) {
				return Integer.parseInt(attr.value());
			}
		}

		return 0;
	}

	/**
	 * FIXME: wait for curtis to enable easier access.
	 * 
	 * @return The maximum number of dimensions.
	 */
	private int getMaxDims() {
		for (final Attr attr : ((CommandModuleItem<?>) get().getItem())
			.getParameter().attrs())
		{
			if (attr.name().equals("max_dims")) {
				return Integer.parseInt(attr.value());
			}
		}

		return Integer.MAX_VALUE;
	}

	/**
	 * If one of the {@link JToggleButton} was clicked, it is checked whether the
	 * button must be added or removed from the activeToggleButtonsQueue. Also it
	 * is ensured that no more than {@link #getMaxDims() getMaxDims} and no less
	 * than {@link #getMinDims() getMinsDims} are selected.
	 * 
	 * @param e the ActionEvent of the pressed ToggleButton.
	 */
	private void toogleButtonChanged(final ActionEvent e) {

		final JToggleButton sourceButton = (JToggleButton) e.getSource();

		// if the button is selected now, it wasn't before, so add the dimension
		// represented by this button.
		if (sourceButton.isSelected()) {
			if (m_activeToogleButtonsQueue.size() == m_maxNumDims) {
				m_activeToogleButtonsQueue.poll().setSelected(false);
			}

			m_activeToogleButtonsQueue.add(sourceButton);
		}
		else {

			// only remove the button if there are still more than m_minNumDims
			// buttons selected, otherwise reselect it.
			if (m_activeToogleButtonsQueue.size() > m_minNumDims) {
				m_activeToogleButtonsQueue.remove(e.getSource());
			}
			else {
				((JToggleButton) e.getSource()).setSelected(true);
			}
		}

		updateModel();
	}

	/**
	 * Extract the Axis from the {@link Dataset}.
	 * 
	 * @param model A {@link WidgetModel}
	 * @return The Axis of the {@link Dataset} or null if no {@link Dataset} was
	 *         found.
	 */
	private TypedAxis[] extractAxis(final WidgetModel model) {

		final Module module = model.getModule();

		for (final ModuleItem<?> moduleItem : module.getInfo().inputs()) {
			if (TypedSpace.class.isAssignableFrom(moduleItem.getType())) {
				final TypedSpace<?> space = (TypedSpace<?>) moduleItem.getValue(module);
				final TypedAxis[] axis = new TypedAxis[space.numDimensions()];
				for (int i = 0; i < space.numDimensions(); i++) {
					axis[i] = space.axis(i);
				}

				return axis;
			}
		}

		return null;
	}

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(TypedAxis[].class) &&
			extractAxis(model) != null;
	}

	@Override
	public void doRefresh() {
		get().setValue(getValue());
	}

	/**
	 * Better looking dimension selection button. Changes color on click.
	 * 
	 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
	 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
	 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
	 *         Zinsmaier</a>
	 */
	private class DimensionSelectionToggleButton extends JToggleButton {

		private static final long serialVersionUID = 1L;

		public DimensionSelectionToggleButton(final String text) {
			super(text);
		}

		@Override
		public void paint(final Graphics g) {
			super.paint(g);

			final Color oldC = g.getColor();
			if (isSelected()) {
				g.setColor(new Color(0, 109, 44));
			}
			else {
				g.setColor(new Color(204, 76, 2));
			}
			g.fillRect(getWidth() - 10, getHeight() - 10, 6, 6);
			g.setColor(oldC);
		}
	}

}
