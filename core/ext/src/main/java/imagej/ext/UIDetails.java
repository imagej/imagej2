//
// UIDetails.java
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

package imagej.ext;

import java.util.List;

/**
 * An interface defining details useful for generating relevant user interface
 * elements.
 * 
 * @author Curtis Rueden
 */
public interface UIDetails extends BasicDetails, Comparable<UIDetails> {

	int FIRST_PRIORITY = 0;
	int HIGH_PRIORITY = 25;
	int NORMAL_PRIORITY = 50;
	int LOW_PRIORITY = 75;
	int LAST_PRIORITY = 100;

	/**
	 * Gets an appropriate title for the object, for use in a user interface. The
	 * result is prioritized as follows:
	 * <ol>
	 * <li>Item label</li>
	 * <li>Menu path's leaf entry name</li>
	 * <li>Item name</li>
	 * <li>Item's class name, without package prefix</li>
	 * </ol>
	 */
	String getTitle();

	/** Gets the path to the object's suggested position in the menu structure. */
	List<MenuEntry> getMenuPath();

	/** Gets the resource path to an icon representing the object. */
	String getIconPath();

	/** Gets the sort priority of the object. */
	int getPriority();

	/**
	 * Gets whether the object can be selected (e.g., checking and unchecking its
	 * menu item) in the user interface.
	 */
	boolean isSelectable();

	/**
	 * Gets the name of the selection group to which the object belongs. Only one
	 * object in a particular selection group can be selected at a time.
	 */
	String getSelectionGroup();

	/**
	 * Gets whether the object is selected (e.g., its menu item is checked) in the
	 * user interface.
	 */
	boolean isSelected();

	/** Gets whether the object should be enabled in the user interface. */
	boolean isEnabled();

	/** Sets the path to the object's suggested position in the menu structure. */
	void setMenuPath(List<MenuEntry> menuPath);

	/** Sets the resource path to an icon representing the object. */
	void setIconPath(String iconPath);

	/** Sets the sort priority of the object. */
	void setPriority(int priority);

	/** Sets whether the object should be enabled in the user interface. */
	void setEnabled(boolean enabled);

	/**
	 * Sets whether the object can be selected (e.g., checking and unchecking its
	 * menu item) in the user interface.
	 */
	void setSelectable(boolean selectable);

	/**
	 * Sets the name of the selection group to which the object belongs. Only one
	 * object in a particular selection group can be selected at a time.
	 */
	void setSelectionGroup(String selectionGroup);

	/**
	 * Sets whether the object is selected (e.g., its menu item is checked) in the
	 * user interface.
	 */
	void setSelected(boolean selected);

}
