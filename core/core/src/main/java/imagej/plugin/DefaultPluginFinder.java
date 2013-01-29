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

import imagej.util.Log;

import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Default ImageJ plugin discovery mechanism.
 * <p>
 * It works by using SezPoz to scan the classpath for {@link Plugin}
 * annotations.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultPluginFinder implements PluginFinder {

	/** Class loader to use when querying SezPoz. */
	private final ClassLoader classLoader;

	// -- Constructors --

	public DefaultPluginFinder() {
		this(null);
	}

	public DefaultPluginFinder(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	// -- PluginFinder methods --

	@Override
	public void findPlugins(final List<PluginInfo<?>> plugins) {
		final Index<Plugin, ImageJPlugin> pluginIndex;
		if (classLoader == null) {
			pluginIndex = Index.load(Plugin.class, ImageJPlugin.class);
		}
		else {
			pluginIndex = Index.load(Plugin.class, ImageJPlugin.class, classLoader);
		}

		final int oldSize = plugins.size();
		for (final IndexItem<Plugin, ImageJPlugin> item : pluginIndex) try {
			final PluginInfo<?> info = createInfo(item);
			plugins.add(info);
		} catch (Throwable t) {
			Log.debug(t);
		}
		final int newSize = plugins.size();

		Log.info("Found " + (newSize - oldSize) + " plugins.");
		if (Log.isDebug()) {
			for (int i = oldSize; i < newSize; i++) {
				Log.debug("- " + plugins.get(i));
			}
		}
	}

	// -- Helper methods --

	private PluginInfo<ImageJPlugin> createInfo(
		final IndexItem<Plugin, ImageJPlugin> item)
	{
		final String className = item.className();
		final Plugin plugin = item.annotation();

		@SuppressWarnings("unchecked")
		final Class<ImageJPlugin> pluginType = (Class<ImageJPlugin>) plugin.type();

		return new PluginInfo<ImageJPlugin>(className, pluginType, plugin);
	}

}
