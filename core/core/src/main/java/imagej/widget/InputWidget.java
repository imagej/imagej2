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

import imagej.module.ModuleItem;
import imagej.plugin.ImageJPlugin;

import org.scijava.plugin.Plugin;

/**
 * Interface for input widgets. An input widget is intended to harvest user
 * input for a particular {@link ModuleItem}. They are used by the
 * {@link InputHarvester} preprocessor to collect module input values.
 * <p>
 * Widgets discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link InputWidget}.class. While it possible to create a widget merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractInputWidget}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <T> The input type of the widget.
 * @param <W> The type of UI component housing the widget.
 * @see Plugin
 * @see WidgetService
 * @see InputHarvester
 * @see InputPanel
 */
public interface InputWidget<T, W> extends ImageJPlugin, UIComponent<W> {

	/** Gets whether this widget would be appropriate for the given model. */
	boolean isCompatible(WidgetModel model);

	/**
	 * Initializes the widget to use the given widget model. Once initialized, the
	 * widget's UI pane will be accessible via {@link #getComponent()}.
	 */
	void initialize(WidgetModel model);

	/** Gets the model object backing this widget. */
	WidgetModel getModel();

	/** Updates the model to reflect the latest widget state. */
	void updateModel();

	/** Gets the current widget value. */
	T getValue();

	/** Refreshes the widget to reflect the latest model value(s). */
	void refreshWidget();

	/**
	 * Returns true iff the widget should be labeled with the parameter label.
	 * Most widgets are labeled this way, though some may not be; e.g.,
	 * {@link MessageWidget}s.
	 * 
	 * @see WidgetModel#getWidgetLabel()
	 */
	boolean isLabeled();

	/**
	 * Returns true iff the widget should be considered a read-only "message"
	 * rather than a bidirectional input widget. The
	 * {@link InputPanel#isMessageOnly()} method will return true iff this method
	 * returns true for all of its widgets.
	 */
	boolean isMessage();

}
