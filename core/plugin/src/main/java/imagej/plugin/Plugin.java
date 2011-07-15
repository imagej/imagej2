//
// Plugin.java
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

package imagej.plugin;

import imagej.module.process.ModulePreprocessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * Annotation identifying a plugin, which gets loaded by ImageJ's dynamic
 * discovery mechanism.
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see PluginService
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type = IPlugin.class)
public @interface Plugin {

	int FIRST_PRIORITY = 0;
	int HIGH_PRIORITY = 25;
	int NORMAL_PRIORITY = 50;
	int LOW_PRIORITY = 75;
	int LAST_PRIORITY = 100;

	/**
	 * The type of plugin; e.g., {@link ImageJPlugin} or
	 * {@link ModulePreprocessor}.
	 */
	Class<?> type() default ImageJPlugin.class;

	/** The name of the plugin. */
	String name() default "";

	/** The human-readable label to use (e.g., in the menu structure). */
	String label() default "";

	/** A longer description of the plugin (e.g., for use a tool tip). */
	String description() default "";

	/** Path to the plugin's icon (e.g., shown in the menu structure). */
	String iconPath() default "";

	/**
	 * Whether the plugin can be selected in the user interface. A plugin's
	 * selection state (if any) is typically rendered in the menu structure using
	 * a checkbox or radio button menu item (see {@link #selectionGroup}).
	 */
	boolean selectable() default false;

	/**
	 * For selectable plugins, specifies a name defining a group of linked
	 * plugins, only one of which is selected at any given time. Typically this is
	 * rendered in the menu structure as a group of radio button menu items. If no
	 * group is given, the plugin is assumed to be a standalone toggle, and
	 * typically rendered as as checkbox menu item.
	 */
	String selectionGroup() default "";

	/**
	 * The plugin index returns plugins sorted by priority. This is useful for
	 * {@link ModulePreprocessor}s to control the order of their execution.
	 */
	int priority() default NORMAL_PRIORITY;

	/** When false, grays out the plugin in the user interface. */
	boolean enabled() default true;

	/**
	 * Abbreviated menu path defining where the plugin is shown in the menu
	 * structure. Uses greater than signs (>) as a separator; e.g.:
	 * "Image > Overlay > Properties..." defines a "Properties..." menu item
	 * within the "Overlay" submenu of the "Image" menu. Use either
	 * {@link #menuPath} or {@link #menu} but not both.
	 */
	String menuPath() default "";

	/**
	 * Full menu path defining where the plugin is shown in the menu structure.
	 * This construction allows menus to be fully specified including mnemonics,
	 * accelerators and icons. Use either {@link #menuPath} or {@link #menu} but
	 * not both.
	 */
	Menu[] menu() default {};

}
