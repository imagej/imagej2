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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import imagej.ImageJ;
import imagej.command.Command;

import java.util.List;

import org.junit.Test;

/**
 * Tests {@link PluginIndex}.
 * 
 * @author Curtis Rueden
 */
public class PluginIndexTest {

	/**
	 * Tests {@link PluginService#getPluginsOfClass(Class)}. In particular, it
	 * verifies that the plugin service can retrieve a plugin that does not have
	 * an associated @{@link Plugin} annotation.
	 */
	@Test
	public void testGetPluginsOfClass() {
		// create a minimal ImageJ context
		final ImageJ context = ImageJ.createContext(PluginService.class);
		final PluginIndex pluginIndex = context.getPluginIndex();

		// add a plugin to the index
		final PluginInfo<Command> testPlugin =
			new PluginInfo<Command>(FooBar.class.getName(), Command.class);
		pluginIndex.add(testPlugin);

		// retrieve the plugin from the index, by class
		final PluginService pluginService = context.getService(PluginService.class);
		final List<PluginInfo<ImageJPlugin>> plugins =
			pluginService.getPluginsOfClass(FooBar.class);

		assertEquals(1, plugins.size());
		assertSame(testPlugin, plugins.get(0));

		final PluginInfo<ImageJPlugin> plugin =
			pluginService.getPlugin(FooBar.class);
		assertSame(testPlugin, plugin);
	}

	/**
	 * Tests {@link PluginService#getPluginsOfClass(String)}. In particular, it
	 * verifies that the plugin service can retrieve a plugin that does not have
	 * an associated @{@link Plugin} annotation.
	 */
	@Test
	public void testGetPluginsOfClassString() {
		// create a minimal ImageJ context
		final ImageJ context = ImageJ.createContext(PluginService.class);
		final PluginIndex pluginIndex = context.getPluginIndex();

		// add a fake plugin to the index
		final String fakeClass = "foo.bar.FooBar";
		final PluginInfo<Command> testPlugin =
			new PluginInfo<Command>(fakeClass, Command.class);
		pluginIndex.add(testPlugin);

		// retrieve the fake plugin from the index, by class name
		final PluginService pluginService = context.getService(PluginService.class);
		final List<PluginInfo<ImageJPlugin>> plugins =
			pluginService.getPluginsOfClass(fakeClass);

		assertEquals(1, plugins.size());
		assertSame(testPlugin, plugins.get(0));

		final PluginInfo<ImageJPlugin> plugin = pluginService.getPlugin(fakeClass);
		assertSame(testPlugin, plugin);
	}

	/** A dummy plugin for testing the plugin service. */
	public static class FooBar implements Command {

		@Override
		public void run() {
			// This method intentionally left blank.
		}
	}

}
