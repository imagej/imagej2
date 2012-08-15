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

package imagej.ext.module.ui;

import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.IPlugin;

/**
 * An input widget is a particular type of {@link IPlugin} intended to harvest
 * user input for a particular {@link ModuleItem}. They are used by the
 * {@link InputHarvester} preprocessor to collect module input values.
 * 
 * @author Curtis Rueden
 * @see InputHarvester
 * @see InputPanel
 */
public interface InputWidget<T, U> extends IPlugin {

	/** Gets whether this widget would be appropriate for the given model. */
	boolean isCompatible(WidgetModel model);

	/**
	 * Initializes the widget to use the given widget model. Once initialized, the
	 * widget's UI pane will be accessible via {@link #getPane()}.
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
	 * Gets the user interface pane housing this widget.
	 * 
	 * @return the pane, or null if the widget has not yet been initialized.
	 */
	U getPane();

}
