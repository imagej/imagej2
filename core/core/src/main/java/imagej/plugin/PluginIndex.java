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

import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.object.SortedObjectIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for managing registered plugins.
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
	 * returns only a <code>List&lt;PluginInfo&lt;?&gt;&gt;</code>, whereas this
	 * one is guaranteed to return a
	 * <code>List&lt;PluginInfo&lt;? extends P&gt;&gt;</code>.
	 * </p>
	 * 
	 * @return Read-only list of registered objects of the given type, or an empty
	 *         list if no such objects exist (this method never returns null).
	 */
	public <P extends IPlugin> List<PluginInfo<? extends P>> getPlugins(
		final Class<P> type)
	{
		final List<PluginInfo<?>> list = get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<? extends P>> result = (List) list;
		return result;
	}

	// -- Internal methods --

	@Override
	protected boolean add(final PluginInfo<?> info, final boolean batch) {
		return add(info, info.getPluginType(), batch);
	}

	@Override
	protected boolean remove(final Object o, final boolean batch) {
		if (!(o instanceof PluginInfo)) return false;
		final PluginInfo<?> info = (PluginInfo<?>) o;
		return remove(info, info.getPluginType(), batch);
	}

}
