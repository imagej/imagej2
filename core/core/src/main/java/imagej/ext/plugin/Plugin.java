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

package imagej.ext.plugin;

import imagej.ext.Priority;
import imagej.ext.display.Display;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

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

	String APPLICATION_MENU_ROOT = "app";
	String CONTEXT_MENU_ROOT = "context";

	/**
	 * The type of plugin; e.g., {@link ImageJPlugin}, {@link PreprocessorPlugin},
	 * {@link PostprocessorPlugin} or {@link Display}.
	 */
	Class<?> type() default ImageJPlugin.class;

	/** The name of the plugin. */
	String name() default "";

	/** The human-readable label to use (e.g., in the menu structure). */
	String label() default "";

	/** A longer description of the plugin (e.g., for use a tool tip). */
	String description() default "";

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

	/**
	 * String identifier naming the menu to which this plugin belongs, or in the
	 * case of a tool, the context menu that should be displayed while the tool is
	 * active. The default value of {@link #APPLICATION_MENU_ROOT} references the
	 * menu structure of the primary application window.
	 */
	String menuRoot() default APPLICATION_MENU_ROOT;

	/** Path to the plugin's icon (e.g., shown in the menu structure). */
	String iconPath() default "";

	/**
	 * The plugin index returns plugins sorted by priority. This is useful for
	 * {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s to control the
	 * order of their execution.
	 * <p>
	 * Any double value is allowed, but for convenience, there are some presets:
	 * </p>
	 * <ul>
	 * <li>{@link Priority#FIRST_PRIORITY}</li>
	 * <li>{@link Priority#VERY_HIGH_PRIORITY}</li>
	 * <li>{@link Priority#HIGH_PRIORITY}</li>
	 * <li>{@link Priority#NORMAL_PRIORITY}</li>
	 * <li>{@link Priority#LOW_PRIORITY}</li>
	 * <li>{@link Priority#VERY_LOW_PRIORITY}</li>
	 * <li>{@link Priority#LAST_PRIORITY}</li>
	 * </ul>
	 */
	double priority() default Priority.NORMAL_PRIORITY;

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

	/** When false, grays out the plugin in the user interface. */
	boolean enabled() default true;

	/** When false, the user interface will not provide a cancel button. */
	boolean cancelable() default true;

	/**
	 * Provides a "hint" as to whether the plugin would execute correctly in a
	 * headless context.
	 * <p>
	 * Plugin developers should not specify "headless = true" unless the plugin
	 * refrains from using any UI-specific features (e.g., AWT or Swing calls).
	 * </p>
	 * <p>
	 * Of course, merely setting this flag does not guarantee that the plugin will
	 * not invoke any headless-incompatible functionality, but it provides an
	 * extra safety net for downstream headless code that wishes to be
	 * conservative in which plugins it allows to execute.
	 * </p>
	 */
	boolean headless() default false;

	/**
	 * Defines a function that is called during preprocessing to assign the
	 * plugin's initial input values. This initializer is called before the
	 * individual @{@link Parameter#initializer()} (i.e.,
	 * {@link ModuleItem#getInitializer()}) methods.
	 * 
	 * @see InitPreprocessor
	 */
	String initializer() default "";

	/** When true, tool has no button but rather is active all the time. */
	boolean alwaysActive() default false;

	/**
	 * When true, tool receives events when the main ImageJ application frame is
	 * active. When false, tool only receives events when a display window is
	 * active.
	 */
	boolean activeInAppFrame() default false;

}
