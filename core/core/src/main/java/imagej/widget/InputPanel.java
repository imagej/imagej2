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

package imagej.widget;

/**
 * Flexible panel-building interface, for use with UIs that prompt for input
 * values of various types.
 * 
 * @author Curtis Rueden
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public interface InputPanel<P, W> extends UIComponent<P> {

	// TODO - groups of fields

	/** Gets whether the given widget would be appropriate for the given panel. */
	boolean supports(InputWidget<?, ?> widget);

	/** Adds a widget to the panel. */
	void addWidget(InputWidget<?, W> widget);

	/**
	 * Returns the value of the given widget's input.
	 * 
	 * @param name unique name identifying this field
	 */
	Object getValue(String name);

	/** Gets the number of active widgets in the input panel. */
	int getWidgetCount();

	/** Gets whether the input panel has any active widgets. */
	boolean hasWidgets();

	/** Returns true if the input panel consists of only messages. */
	boolean isMessageOnly();

	/** Updates the widgets to reflect the most recent parameter value(s). */
	void refresh();

	/** Gets the type of the UI component housing the panel's widgets. */
	Class<W> getWidgetComponentType();

}
