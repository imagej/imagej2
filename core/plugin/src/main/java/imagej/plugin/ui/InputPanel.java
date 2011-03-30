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

package imagej.plugin.ui;

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
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue value to be initially displayed
	 * @param min minimum allowed value
	 * @param max maximum allowed value
	 * @param stepSize distance between steps when operating widget
	 */
	void addNumber(ParamDetails details, Number initialValue,
		Number min, Number max, Number stepSize);

	/**
	 * Adds a checkbox field.
	 * 
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue the initial state
	 */
	void addToggle(ParamDetails details, boolean initialValue);

	/**
	 * Adds a text field.
	 * 
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue the text initially displayed
	 * @param columns width of field in characters
	 */
	void addTextField(ParamDetails details, String initialValue, int columns);

	/**
	 * Adds a multiple choice text field.
	 * 
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue the initially selected item
	 * @param items the choices
	 */
	void addChoice(ParamDetails details, String initialValue, String[] items);

	/**
	 * Adds a file selector.
	 * 
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue the initially specified file
	 */
	void addFile(ParamDetails details, File initialValue);

	/**
	 * Adds an object selector.
	 * 
	 * @param details basic parameter details (e.g., name, label, description)
	 * @param initialValue the initially specified dataset
	 */
	void addObject(ParamDetails details, Object initialValue);

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
	 * Returns the value of the given object selector.
	 * 
	 * @param name unique name identifying this field
	 */
	Object getObject(String name);

	/** Updates the widgets to reflect the most recent parameter value(s). */
	void refresh();

}
