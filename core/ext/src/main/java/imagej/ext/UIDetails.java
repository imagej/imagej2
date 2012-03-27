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

package imagej.ext;

/**
 * An interface defining details useful for generating relevant user interface
 * elements.
 * 
 * @author Curtis Rueden
 */
public interface UIDetails extends BasicDetails, Comparable<UIDetails> {

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
	MenuPath getMenuPath();

	/** Gets the name of the menu structure to which the object belongs. */
	String getMenuRoot();

	/** Gets the resource path to an icon representing the object. */
	String getIconPath();

	/** Gets the sort priority of the object. */
	double getPriority();

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
	void setMenuPath(MenuPath menuPath);

	/** Sets the name of the menu structure to which the object belongs. */
	void setMenuRoot(String menuRoot);

	/** Sets the resource path to an icon representing the object. */
	void setIconPath(String iconPath);

	/** Sets the sort priority of the object. */
	void setPriority(double priority);

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
