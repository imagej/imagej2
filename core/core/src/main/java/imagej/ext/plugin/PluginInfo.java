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

import imagej.ext.AbstractUIDetails;
import imagej.ext.Accelerator;
import imagej.ext.Instantiable;
import imagej.ext.InstantiableException;
import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.util.StringMaker;

import java.net.URL;

/**
 * A collection of metadata about a particular plugin. For performance reasons,
 * the metadata is populated without actually loading the plugin class, by
 * reading from an efficient binary cache (see {@link PluginService} for
 * details). As such, ImageJ can very quickly build a complex menu structure
 * containing all available {@link ImageJPlugin}s without waiting for the Java
 * class loader.
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 * @see Plugin
 * @see PluginService
 */
public class PluginInfo<P extends IPlugin> extends AbstractUIDetails implements
	Instantiable<P>
{

	/** Fully qualified class name of this plugin. */
	private String className;

	/** Class object for this plugin. Lazily loaded. */
	private Class<P> pluginClass;

	/** Type of this entry's plugin; e.g., {@link ImageJPlugin}. */
	private Class<P> pluginType;

	/** Annotation describing the plugin. */
	protected Plugin plugin;

	/** TODO */
	public PluginInfo(final String className, final Class<P> pluginType) {
		setClassName(className);
		setPluginType(pluginType);
		setMenuPath(null);
		setMenuRoot(Plugin.APPLICATION_MENU_ROOT);
	}

	/** TODO */
	public PluginInfo(final String className, final Class<P> pluginType,
		final Plugin plugin)
	{
		this(className, pluginType);
		this.plugin = plugin;
		populateValues();
	}

	// -- PluginInfo methods --

	/** Sets the fully qualified name of the {@link Class} of the item objects. */
	public void setClassName(final String className) {
		this.className = className;
	}

	/** TODO */
	public void setPluginType(final Class<P> pluginType) {
		this.pluginType = pluginType;
	}

	/** TODO */
	public Class<P> getPluginType() {
		return pluginType;
	}

	/**
	 * Gets the URL corresponding to the icon resource path.
	 * 
	 * @see #getIconPath()
	 */
	public URL getIconURL() throws InstantiableException {
		// See also: imagej.ext.menu.ShadowMenu#getIconURL()
		final String iconPath = getIconPath();
		if (iconPath == null || iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	/** Gets whether tool is always active, rather than part of the toolbar. */
	public boolean isAlwaysActive() {
		return plugin.alwaysActive();
	}

	/**
	 * Gets whether the tool receives input events when the main application frame
	 * has the focus.
	 */
	public boolean isActiveInAppFrame() {
		return plugin.activeInAppFrame();
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("class", className);
		sm.append(", ", super.toString());
		sm.append(", pluginType", pluginType);
		return sm.toString();
	}

	// -- Instantiable methods --

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<P> loadClass() throws InstantiableException {
		if (pluginClass == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (final ClassNotFoundException e) {
				throw new InstantiableException("Class not found: " + className, e);
			}
			pluginClass = (Class<P>) c;
		}
		return pluginClass;
	}

	@Override
	public P createInstance() throws InstantiableException {
		final Class<P> c = loadClass();

		// instantiate plugin
		final P instance;
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
		setName(plugin.name());
		setLabel(plugin.label());
		setDescription(plugin.description());

		final MenuPath menuPath;
		final Menu[] menu = plugin.menu();
		if (menu.length > 0) {
			menuPath = parseMenuPath(menu);
		}
		else {
			// parse menuPath attribute
			menuPath = new MenuPath(plugin.menuPath());
		}
		setMenuPath(menuPath);

		setMenuRoot(plugin.menuRoot());

		final String iconPath = plugin.iconPath();
		setIconPath(iconPath);
		setPriority(plugin.priority());
		setEnabled(plugin.enabled());
		setSelectable(plugin.selectable());
		setSelectionGroup(plugin.selectionGroup());

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
