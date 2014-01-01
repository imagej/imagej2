/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Tests {@link OptionsService} and {@link OptionsPlugin}s.
 * 
 * @author Curtis Rueden
 */
public class OptionsTest {

	private OptionsService createOptionsService() {
		final Context context =
			new Context(PluginService.class, OptionsService.class);

		// add FooOptions to the list of available plugins
		final PluginService pluginService = context.getService(PluginService.class);
		final PluginInfo<OptionsPlugin> info =
			new PluginInfo<OptionsPlugin>(FooOptions.class, OptionsPlugin.class);
		pluginService.addPlugin(info);

		return context.getService(OptionsService.class);
	}

	/**
	 * Tests that the options service is available in a new context, and that it
	 * manages its OptionsPlugin instances as expected.
	 */
	@Test
	public void testBasic() {
		final OptionsService optionsService = createOptionsService();

		// verify that the options service is there
		assertNotNull(optionsService);

		// verify that the options service has (only) a FooOptions
		final List<OptionsPlugin> optionsList = optionsService.getInstances();
		assertEquals(1, optionsList.size());
		final FooOptions fooOptions = optionsService.getOptions(FooOptions.class);
		assertSame(optionsList.get(0), fooOptions);

		// verify that a 2nd request returns the exact same instance
		assertSame(fooOptions, optionsService.getOptions(FooOptions.class));
	}

	/** Tests that persistence of option values works as it should. */
	@Test
	public void testPersistence() {
		{
			final OptionsService optionsService = createOptionsService();
			final FooOptions fooOptions = optionsService.getOptions(FooOptions.class);

			// bar should initially be 0
			assertEquals(0, fooOptions.getBar());

			// verify that we can set bar to a desired value at all
			fooOptions.setBar(0xcafebabe);
			assertEquals(0xcafebabe, fooOptions.getBar());

			// verify that save and load work as expected in the same context
			fooOptions.save();
			fooOptions.setBar(0xdeadbeef);
			fooOptions.load();
			assertEquals(0xcafebabe, fooOptions.getBar());

			// throw away the 1st context
			optionsService.getContext().dispose();
		}
		{
			// create a new, 2nd context
			final OptionsService optionsService = createOptionsService();
			final FooOptions fooOptions = optionsService.getOptions(FooOptions.class);

			// verify that persisted values are loaded correctly in a new context
			assertEquals(0xcafebabe, fooOptions.getBar());

			// clean up for next time
			fooOptions.reset(); // FIXME: If this test fails, reset will not happen!
		}
	}

	// -- Helper classes --

	/** Sample options plugin, for unit testing. */
	public static class FooOptions extends OptionsPlugin {

		@Parameter
		private int bar;

		public int getBar() {
			return bar;
		}

		public void setBar(final int bar) {
			this.bar = bar;
		}

	}

}
