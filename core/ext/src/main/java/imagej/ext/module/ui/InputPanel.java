//
// InputPanel.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.module.ui;

import imagej.ext.module.ModuleException;
import imagej.util.ColorRGB;

import java.io.File;

/**
 * Flexible panel-building interface, for use with UIs that prompt for input
 * values of various types.
 * 
 * @author Curtis Rueden
 */
public interface InputPanel {

	// TODO - groups of fields

	/** Adds a message consisting of one or more lines of text. */
	void addMessage(String text);

	/**
	 * Adds a numeric field.
	 * 
	 * @param model backing data model containing parameter details
	 * @param min minimum allowed value
	 * @param max maximum allowed value
	 * @param stepSize distance between steps when operating widget
	 */
	void addNumber(WidgetModel model,
		Number min, Number max, Number stepSize);

	/**
	 * Adds a checkbox field.
	 * 
	 * @param model backing data model containing parameter details
	 */
	void addToggle(WidgetModel model);

	/**
	 * Adds a text field.
	 * 
	 * @param model backing data model containing parameter details
	 * @param columns width of field in characters
	 */
	void addTextField(WidgetModel model, int columns);

	/**
	 * Adds a multiple choice text field.
	 * 
	 * @param model backing data model containing parameter details
	 * @param items the choices
	 */
	void addChoice(WidgetModel model, String[] items);

	/**
	 * Adds a file selector.
	 * 
	 * @param model backing data model containing parameter details
	 */
	void addFile(WidgetModel model);

	/**
	 * Adds a color chooser.
	 * 
	 * @param model backing data model containing parameter details
	 */
	void addColor(WidgetModel model);

	/**
	 * Adds an object selector.
	 * 
	 * @param model backing data model containing parameter details
	 * @throws ModuleException if the object cannot be added to the panel
	 */
	void addObject(WidgetModel model) throws ModuleException;

	/**
	 * Returns the contents of the given numeric field.
	 * 
	 * @param name unique name identifying this field
	 */
	Number getNumber(String name);

	/**
	 * Returns the state of the given checkbox.
	 * 
	 * @param name unique name identifying this field
	 */
	boolean getToggle(String name);

	/**
	 * Returns the contents of the given text field.
	 * 
	 * @param name unique name identifying this field
	 */
	String getTextField(String name);

	/**
	 * Returns the selected item in the given choice text field.
	 * 
	 * @param name unique name identifying this field
	 */
	String getChoice(String name);

	/**
	 * Returns the index of the selected item in the given choice text field.
	 * 
	 * @param name unique name identifying this field
	 */
	int getChoiceIndex(String name);

	/**
	 * Returns the value of the given file selector.
	 * 
	 * @param name unique name identifying this field
	 */
	File getFile(String name);

	/**
	 * Returns the value of the given color chooser.
	 * 
	 * @param name unique name identifying this field
	 */
	ColorRGB getColor(String name);

	/**
	 * Returns the value of the given object selector.
	 * 
	 * @param name unique name identifying this field
	 */
	Object getObject(String name);

	/** Gets the number of active widgets in the input panel. */
	int getWidgetCount();

	/** Gets whether the input panel has any active widgets. */
	boolean hasWidgets();

	/** Returns true if the input panel consists of only messages. */
	boolean isMessageOnly();

	/** Updates the widgets to reflect the most recent parameter value(s). */
	void refresh();

}
