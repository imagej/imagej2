/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.object.SortedObjectIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for managing registered plugins.
 * <p>
 * The plugin index is a special type of {@link imagej.object.ObjectIndex} that
 * classifies each {@link PluginInfo} object into a type hierarchy compatible
 * with its associated <em>plugin</em> type (i.e.,
 * {@link PluginInfo#getPluginType()}), rather than {@link PluginInfo}'s type
 * hierarchy (i.e., {@link PluginInfo}, {@link imagej.UIDetails},
 * {@link imagej.Instantiable}, etc.).
 * </p>
 * <p>
 * NB: This type hierarchy will typically <em>not</em> include the plugin class
 * itself; for example, the {@code imagej.core.plugins.app.AboutImageJ} command
 * has a plugin type of {@link imagej.command.Command}, and hence will be
 * categorized beneath {@code Command.class}, not {@code AboutImageJ.class}. The
 * rationale is that to fully classify each plugin including its own class, said
 * class would need to be loaded, which ImageJ makes an effort not to do until
 * the plugin is actually executed for the first time.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class PluginIndex extends SortedObjectIndex<PluginInfo<?>> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PluginIndex() {
		// NB: See: http://stackoverflow.com/questions/4765520/
		super((Class) PluginInfo.class);
	}

	// -- PluginIndex methods --

	/** Discovers plugins available on the classpath. */
	public void discover() {
		discover(new DefaultPluginFinder());
	}

	/**
	 * Discovers available plugins, using the given {@link PluginFinder} instance.
	 */
	public void discover(final PluginFinder pluginFinder) {
		final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
		pluginFinder.findPlugins(plugins);
		addAll(plugins);
	}

	/**
	 * Gets a list of registered plugins compatible with the given type.
	 * <p>
	 * This method is more specific than {@link #get(Class)} since that method
	 * returns only a {@code List<PluginInfo<?>>}, whereas this one is guaranteed
	 * to return a {@code List<PluginInfo<P>>}.
	 * </p>
	 * 
	 * @return Read-only list of registered objects of the given type, or an empty
	 *         list if no such objects exist (this method never returns null).
	 */
	public <PT extends ImageJPlugin> List<PluginInfo<PT>> getPlugins(
		final Class<PT> type)
	{
		final List<PluginInfo<?>> list = get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<PT>> result = (List) list;
		return result;
	}

	// -- Internal methods --

	/**
	 * Adds the plugin to all type lists compatible with its plugin type.
	 * <p>
	 * NB: This behavior differs from the default
	 * {@link imagej.object.ObjectIndex} behavior in that the {@code info}
	 * object's actual type hierarchy is not used for classification, but rather
	 * the object is classified according to {@link PluginInfo#getPluginType()}.
	 * </p>
	 * 
	 * @see PluginInfo#getPluginType()
	 */
	@Override
	protected boolean add(final PluginInfo<?> info, final boolean batch) {
		return add(info, info.getPluginType(), batch);
	}

	/**
	 * Removes the plugin from all type lists compatible with its plugin type.
	 * <p>
	 * NB: This behavior differs from the default
	 * {@link imagej.object.ObjectIndex} behavior in that the {@code info}
	 * object's actual type hierarchy is not used for classification, but rather
	 * the object is classified according to {@link PluginInfo#getPluginType()}.
	 * </p>
	 * 
	 * @see PluginInfo#getPluginType()
	 */
	@Override
	protected boolean remove(final Object o, final boolean batch) {
		if (!(o instanceof PluginInfo)) return false;
		final PluginInfo<?> info = (PluginInfo<?>) o;
		return remove(info, info.getPluginType(), batch);
	}

}
