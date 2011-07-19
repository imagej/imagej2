//
// ModuleItem.java
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

package imagej.ext.module;

import imagej.ext.BasicDetails;
import imagej.ext.module.ui.WidgetStyle;

import java.util.List;

/**
 * A ModuleItem represents metadata about one input or output of a module.
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface ModuleItem<T> extends BasicDetails {

	/** Type of the item. */
	Class<T> getType();

	/** The visibility of the item. */
	ItemVisibility getVisibility();

	/** Gets whether the item value must be specified (i.e., no default). */
	boolean isRequired();

	/** Gets whether to remember the most recent value of the parameter. */
	boolean isPersisted();

	/** Gets the key to use for saving the value persistently. */
	String getPersistKey();

	/**
	 * Gets the function that is called whenever this item changes.
	 * <p>
	 * This mechanism enables interdependent items of various types. For example,
	 * two int parameters "width" and "height" could update each other when
	 * another boolean "Preserve aspect ratio" flag is set.
	 * </p>
	 */
	String getCallback();

	/**
	 * Invokes this item's callback function, if any, on the given module.
	 * 
	 * @see #getCallback()
	 */
	void callback(Module module);

	/**
	 * Gets the preferred widget style to use when rendering the item in a user
	 * interface.
	 */
	WidgetStyle getWidgetStyle();

	/** Gets the minimum allowed value (if applicable). */
	T getMinimumValue();

	/** Gets the maximum allowed value (if applicable). */
	T getMaximumValue();

	/**
	 * Gets the preferred step size to use when rendering the item in a user
	 * interface (if applicable).
	 */
	Number getStepSize();

	/**
	 * Gets the preferred width of the input field in characters (if applicable).
	 */
	int getColumnCount();

	/** Gets the list of possible values. */
	List<T> getChoices();

	/** Gets the item's current value with respect to the given module. */
	T getValue(Module module);

}
