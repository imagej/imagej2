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

package imagej.plugin.gui;

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
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue value to be initially displayed
	 * @param style preferred widget style for the numeric field
	 * @param min minimum allowed value
	 * @param max maximum allowed value
	 * @param stepSize distance between steps when operating widget
	 */
	void addNumber(String name, String label, Number initialValue,
		WidgetStyle style, Number min, Number max, Number stepSize);

	/**
	 * Adds a checkbox field.
	 * 
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue the initial state
	 * @param style preferred widget style for the checkbox field
	 */
	void addToggle(String name, String label, boolean initialValue,
		WidgetStyle style);

	/**
	 * Adds a text field.
	 * 
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue the text initially displayed
	 * @param style preferred widget style for the text field
	 * @param columns width of field in characters
	 */
	void addTextField(String name, String label, String initialValue,
		WidgetStyle style, int columns);

	/**
	 * Adds a multiple choice text field.
	 * 
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue the initially selected item
	 * @param style preferred widget style for the text field
	 * @param items the choices
	 */
	void addChoice(String name, String label, String initialValue,
		WidgetStyle style, String[] items);

	/**
	 * Adds a file selector.
	 * 
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue the initially specified file
	 * @param style preferred widget style for the file selector
	 */
	void
		addFile(String name, String label, File initialValue, WidgetStyle style);

	/**
	 * Adds an object selector.
	 * 
	 * @param name unique name identifying this field
	 * @param label the label
	 * @param initialValue the initially specified dataset
	 * @param style preferred widget style for the object selector
	 */
	void addObject(String name, String label, Object initialValue,
		WidgetStyle style);

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

}
