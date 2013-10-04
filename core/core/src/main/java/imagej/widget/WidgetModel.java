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

package imagej.widget;

import imagej.module.MethodCallException;
import imagej.module.Module;
import imagej.module.ModuleItem;

import java.util.Arrays;
import java.util.List;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.util.ClassUtils;
import org.scijava.util.MiscUtils;
import org.scijava.util.NumberUtils;

/**
 * The backing data model for a particular {@link InputWidget}.
 * 
 * @author Curtis Rueden
 */
public class WidgetModel extends AbstractContextual {

	private final InputPanel<?, ?> inputPanel;
	private final Module module;
	private final ModuleItem<?> item;
	private final List<?> objectPool;

	@Parameter
	private ThreadService threadService;

	@Parameter(required = false)
	private LogService log;

	private boolean initialized;

	public WidgetModel(final Context context, final InputPanel<?, ?> inputPanel,
		final Module module, final ModuleItem<?> item, final List<?> objectPool)
	{
		setContext(context);
		this.inputPanel = inputPanel;
		this.module = module;
		this.item = item;
		this.objectPool = objectPool;
	}

	/** Gets the input panel intended to house the widget. */
	public InputPanel<?, ?> getPanel() {
		return inputPanel;
	}

	/** Gets the module's associated module instance. */
	public Module getModule() {
		return module;
	}

	/** Gets the module input's associated item descriptor. */
	public ModuleItem<?> getItem() {
		return item;
	}

	/**
	 * Gets the available objects for use with the widget. For example,
	 * {@link ObjectWidget}s typically display a dropdown combo box providing
	 * multiple choice selection between these objects.
	 * <p>
	 * Note that this list does not represent a constraint in allowed widget
	 * values, but rather provides a list of possibilities in cases where the
	 * realm of values is not defined by the type in some other way.
	 * </p>
	 * 
	 * @see ObjectWidget
	 */
	public List<?> getObjectPool() {
		return objectPool;
	}

	/**
	 * Gets the text to use when labeling this widget. The linked item's label
	 * will be given if available (i.e., {@link ModuleItem#getLabel()}).
	 * Otherwise, a capitalized version of the item's name is given (i.e.,
	 * {@link ModuleItem#getName()}).
	 */
	public String getWidgetLabel() {
		// Do this dynamically. Don't cache this result. Some controls change their
		// labels at runtime.
		final String label = item.getLabel();
		if (label != null && !label.isEmpty()) return label;

		final String name = item.getName();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	/**
	 * Gets the current value of the module input.
	 * <p>
	 * In the case of inputs with a limited set of choices (i.e.,
	 * {@link ChoiceWidget}s and {@link ObjectWidget}s), this method ensures the
	 * value is in the set; if not, it returns the first item of the set.
	 * </p>
	 */
	public Object getValue() {
		final Object value = item.getValue(module);

		if (isMultipleChoice()) return ensureValidChoice(value);
		if (getObjectPool().size() > 0) return ensureValidObject(value);
		return value;
	}

	/** Sets the current value of the module input. */
	public void setValue(final Object value) {
		final String name = item.getName();
		if (MiscUtils.equal(item.getValue(module), value)) return; // no change
		module.setInput(name, value);
		if (initialized) {
			threadService.run(new Runnable() {

				@Override
				public void run() {
					callback();
					inputPanel.refresh(); // must be on AWT thread?
					module.preview();
				}
			});
		}
	}

	/** Executes the callback associated with this widget's associated input. */
	public void callback() {
		try {
			item.callback(module);
		}
		catch (final MethodCallException exc) {
			if (log != null) log.error(exc);
		}
	}

	/**
	 * Gets the minimum value for the module input.
	 * 
	 * @return The minimum value, or null if the type is unbounded.
	 */
	public Number getMin() {
		final Number min = toNumber(item.getMinimumValue());
		if (min != null) return min;
		return NumberUtils.getMinimumNumber(item.getType());
	}

	/**
	 * Gets the maximum value for the module input.
	 * 
	 * @return The maximum value, or null if the type is unbounded.
	 */
	public Number getMax() {
		final Number max = toNumber(item.getMaximumValue());
		if (max != null) return max;
		return NumberUtils.getMaximumNumber(item.getType());
	}

	/**
	 * Gets the "soft" minimum value for the module input.
	 * 
	 * @return The "soft" minimum value, or {@link #getMin()} if none.
	 * @see ModuleItem#getSoftMinimum()
	 */
	public Number getSoftMin() {
		final Number softMin = toNumber(item.getSoftMinimum());
		if (softMin != null) return softMin;
		return getMin();
	}

	/**
	 * Gets the "soft" maximum value for the module input.
	 * 
	 * @return The "soft" maximum value, or {@link #getMax()} if none.
	 * @see ModuleItem#getSoftMaximum()
	 */
	public Number getSoftMax() {
		final Number softMax = toNumber(item.getSoftMaximum());
		if (softMax != null) return softMax;
		return getMax();
	}

	/**
	 * Gets the step size between values for the module input.
	 * 
	 * @return The step size, or 1 by default.
	 */
	public Number getStepSize() {
		final Number stepSize = toNumber(item.getStepSize());
		if (stepSize != null) return stepSize;
		return NumberUtils.toNumber("1", item.getType());
	}

	/**
	 * Gets the multiple choice list for the module input.
	 * 
	 * @return The available choices, or an empty list if not multiple choice.
	 * @see ChoiceWidget
	 */
	public String[] getChoices() {
		final List<?> choicesList = item.getChoices();
		final String[] choices = new String[choicesList.size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = choicesList.get(i).toString();
		}
		return choices;
	}

	/**
	 * Gets the input's value rendered as a string.
	 * 
	 * @return String representation of the input value, or the empty string if
	 *         the value is null or the null character ('\0').
	 */
	public String getText() {
		final Object value = getValue();
		if (value == null) return "";
		final String text = value.toString();
		if (text.equals("\0")) return ""; // render null character as empty
		return text;
	}

	/**
	 * Gets whether the input is a message.
	 * 
	 * @see ItemVisibility#MESSAGE
	 */
	public boolean isMessage() {
		return getItem().getVisibility() == ItemVisibility.MESSAGE;
	}

	/**
	 * Gets whether the input is a text type (i.e., {@link String},
	 * {@link Character} or {@code char}.
	 */
	public boolean isText() {
		return ClassUtils.isText(getItem().getType());
	}

	/**
	 * Gets whether the input is a character type (i.e., {@link Character} or
	 * {@code char}).
	 */
	public boolean isCharacter() {
		return ClassUtils.isCharacter(getItem().getType());
	}

	/**
	 * Gets whether the input is a number type (e.g., {@code int}, {@code float}
	 * or any {@link Number} implementation.
	 */
	public boolean isNumber() {
		return ClassUtils.isNumber(getItem().getType());
	}

	/**
	 * Gets whether the input is a boolean type (i.e., {@link Boolean} or
	 * {@code boolean}).
	 */
	public boolean isBoolean() {
		return ClassUtils.isBoolean(getItem().getType());
	}

	/** Gets whether the input provides a restricted set of choices. */
	public boolean isMultipleChoice() {
		final List<?> choices = item.getChoices();
		return choices != null && !choices.isEmpty();
	}

	/** Gets whether the input is compatible with the given type. */
	public boolean isType(final Class<?> type) {
		return type.isAssignableFrom(getItem().getType());
	}

	/**
	 * Toggles the widget's initialization state. An initialized widget can be
	 * assumed to be an active part of a container {@link InputPanel}.
	 */
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Gets the widget's initialization state. An initialized widget can be
	 * assumed to be an active part of a container {@link InputPanel}.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	// -- Helper methods --

	/**
	 * For multiple choice widgets, ensure the value is a valid choice.
	 * 
	 * @see #getChoices()
	 * @see ChoiceWidget
	 */
	private Object ensureValidChoice(final Object value) {
		return ensureValid(value, Arrays.asList(getChoices()));
	}

	/**
	 * For object widgets, ensure the value is a valid object.
	 * 
	 * @see #getObjectPool()
	 * @see ObjectWidget
	 */
	private Object ensureValidObject(final Object value) {
		return ensureValid(value, getObjectPool());
	}

	/** Ensures the value is on the given list. */
	private Object ensureValid(final Object value, final List<?> list) {
		for (final Object o : list) {
			if (o.equals(value)) return value; // value is valid
		}

		// value is not valid; override with the first item on the list instead
		final Object validValue = list.get(0);
		// CTR TODO: Mutating the model in a getter is dirty. Find a better way?
		setValue(validValue);
		return validValue;
	}

	/** Converts the given object to a number matching the input type. */
	private Number toNumber(final Object value) {
		final Class<?> type = item.getType();
		final Class<?> saneType = ClassUtils.getNonprimitiveType(type);
		return NumberUtils.toNumber(value, saneType);
	}

}
