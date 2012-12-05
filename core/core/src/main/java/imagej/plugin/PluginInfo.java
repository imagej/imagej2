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

package imagej.plugin;

import imagej.AbstractUIDetails;
import imagej.Instantiable;
import imagej.InstantiableException;
import imagej.MenuEntry;
import imagej.MenuPath;
import imagej.command.Command;
import imagej.input.Accelerator;
import imagej.menu.ShadowMenu;
import imagej.util.StringMaker;

import java.net.URL;

/**
 * A collection of metadata about a particular plugin.
 * <p>
 * For performance reasons, the metadata is populated without actually loading
 * the plugin class, by reading from an efficient binary cache (see
 * {@link imagej.plugin.DefaultPluginService} for details). As such, ImageJ can
 * very quickly build a complex menu structure containing all available
 * {@link Command}s without waiting for the Java class loader.
 * </p>
 * 
 * @param <PT> The <em>type</em> of plugin described by this metadata. See
 *          {@link ImageJPlugin} for a list of common plugin types.
 * @author Curtis Rueden
 * @see Command
 * @see Plugin
 * @see PluginService
 */
public class PluginInfo<PT extends ImageJPlugin> extends AbstractUIDetails
	implements Instantiable<PT>
{

	/** Fully qualified class name of this plugin. */
	private String className;

	/** Class object for this plugin. Lazily loaded. */
	private Class<? extends PT> pluginClass;

	/** Type of this entry's plugin; e.g., {@link Command}. */
	private Class<PT> pluginType;

	/** Annotation describing the plugin. */
	private Plugin annotation;

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param className The name of the class, which must implement
	 *          {@link ImageJPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link ImageJPlugin} for a list of common plugin types.
	 */
	public PluginInfo(final String className, final Class<PT> pluginType) {
		this(className, null, pluginType, null);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param className The name of the class, which must implement
	 *          {@link ImageJPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link ImageJPlugin} for a list of common plugin types.
	 * @param annotation The @{@link Plugin} annotation to associate with this
	 *          metadata object.
	 */
	public PluginInfo(final String className, final Class<PT> pluginType,
		final Plugin annotation)
	{
		this(className, null, pluginType, annotation);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param pluginClass The plugin class, which must implement
	 *          {@link ImageJPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link ImageJPlugin} for a list of common plugin types.
	 */
	public PluginInfo(final Class<? extends PT> pluginClass,
		final Class<PT> pluginType)
	{
		this(null, pluginClass, pluginType, null);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param pluginClass The plugin class, which must implement
	 *          {@link ImageJPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link ImageJPlugin} for a list of common plugin types.
	 * @param annotation The @{@link Plugin} annotation to associate with this
	 *          metadata object.
	 */
	public PluginInfo(final Class<? extends PT> pluginClass,
		final Class<PT> pluginType, final Plugin annotation)
	{
		this(null, pluginClass, pluginType, annotation);
	}

	protected PluginInfo(final String className,
		final Class<? extends PT> pluginClass, final Class<PT> pluginType,
		final Plugin annotation)
	{
		if (pluginClass != null) {
			if (className != null) {
				throw new IllegalArgumentException(
					"className and pluginClass are mutually exclusive");
			}
			setPluginClass(pluginClass);
		}
		else {
			this.className = className;
		}
		setPluginType(pluginType);
		setMenuPath(null);
		setMenuRoot(Plugin.APPLICATION_MENU_ROOT);
		if (annotation != null) {
			this.annotation = annotation;
			populateValues();
		}
	}

	// -- PluginInfo methods --

	/**
	 * Explicitly sets the {@link Class} of the item objects.
	 * <p>
	 * This is useful if your class is produced by something other than the system
	 * classloader.
	 * </p>
	 */
	public void setPluginClass(final Class<? extends PT> pluginClass) {
		this.pluginClass = pluginClass;
	}

	/**
	 * Obtains the {@link Class} of the item objects, if that class has already
	 * been loaded.
	 * 
	 * @return The {@link Class}, or null if it has not yet been loaded by
	 *         {@link #loadClass}.
	 */
	public Class<? extends PT> getPluginClass() {
		return pluginClass;
	}

	/**
	 * Sets the <em>type</em> of plugin described by the metadata.
	 * @see ImageJPlugin for a list of common plugin types.
	 */
	public void setPluginType(final Class<PT> pluginType) {
		this.pluginType = pluginType;
	}

	/**
	 * Gets the <em>type</em> of plugin described by the metadata.
	 * @see ImageJPlugin for a list of common plugin types.
	 */
	public Class<PT> getPluginType() {
		return pluginType;
	}

	/** Gets the associated @{@link Plugin} annotation. */
	public Plugin getAnnotation() {
		return annotation;
	}

	/**
	 * Gets the URL corresponding to the icon resource path.
	 * 
	 * @see #getIconPath()
	 * @see ShadowMenu#getIconURL()
	 */
	public URL getIconURL() throws InstantiableException {
		final String iconPath = getIconPath();
		if (iconPath == null || iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	/** Gets whether tool is always active, rather than part of the toolbar. */
	public boolean isAlwaysActive() {
		return getAnnotation().alwaysActive();
	}

	/**
	 * Gets whether the tool receives input events when the main application frame
	 * has the focus.
	 */
	public boolean isActiveInAppFrame() {
		return getAnnotation().activeInAppFrame();
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("class", className);
		sm.append(super.toString());
		sm.append("pluginType", pluginType);
		return sm.toString();
	}

	// -- Instantiable methods --

	@Override
	public String getClassName() {
		if (pluginClass != null) return pluginClass.getName();
		return className;
	}

	@Override
	public Class<? extends PT> loadClass() throws InstantiableException {
		if (pluginClass == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (final ClassNotFoundException e) {
				throw new InstantiableException("Class not found: " + className, e);
			}
			@SuppressWarnings("unchecked")
			final Class<? extends PT> typedClass = (Class<? extends PT>) c;
			pluginClass = typedClass;
		}

		return pluginClass;
	}

	@Override
	public PT createInstance() throws InstantiableException {
		final Class<? extends PT> c = loadClass();

		// instantiate plugin
		final PT instance;
		try {
			instance = c.newInstance();
		}
		catch (final InstantiationException e) {
			throw new InstantiableException(e);
		}
		catch (final IllegalAccessException e) {
			throw new InstantiableException(e);
		}
		return instance;
	}

	// -- Helper methods --

	/** Populates the entry to match the associated @{@link Plugin} annotation. */
	private void populateValues() {
		final Plugin ann = getAnnotation();
		if (ann == null) return;
		setName(ann.name());
		setLabel(ann.label());
		setDescription(ann.description());

		final MenuPath menuPath;
		final Menu[] menu = ann.menu();
		if (menu.length > 0) {
			menuPath = parseMenuPath(menu);
		}
		else {
			// parse menuPath attribute
			menuPath = new MenuPath(ann.menuPath());
		}
		setMenuPath(menuPath);

		setMenuRoot(ann.menuRoot());

		final String iconPath = ann.iconPath();
		setIconPath(iconPath);
		setPriority(ann.priority());
		setEnabled(ann.enabled());
		setSelectable(ann.selectable());
		setSelectionGroup(ann.selectionGroup());

		// add default icon if none attached to leaf
		final MenuEntry menuLeaf = menuPath.getLeaf();
		if (menuLeaf != null && !iconPath.isEmpty()) {
			final String menuIconPath = menuLeaf.getIconPath();
			if (menuIconPath == null || menuIconPath.isEmpty()) {
				menuLeaf.setIconPath(iconPath);
			}
		}
	}

	private MenuPath parseMenuPath(final Menu[] menu) {
		final MenuPath menuPath = new MenuPath();
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final Accelerator acc = Accelerator.create(menu[i].accelerator());
			final String iconPath = menu[i].iconPath();
			menuPath.add(new MenuEntry(name, weight, mnemonic, acc, iconPath));
		}
		return menuPath;
	}

}
