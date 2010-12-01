package imagej.ij1bridge;

import imagej.ij1bridge.plugin.Ij1PluginFinder;
import imagej.plugin.PluginEntry;

import java.util.ArrayList;

import org.junit.Test;

public class Ij1PluginFinderTest {

/**
 * Test to see that at least one PluginEntry was added to the list.
 */
	@Test
	public void testIj1PluginFinder()
	{
		ArrayList<PluginEntry> plugins = new ArrayList<PluginEntry>();
		new Ij1PluginFinder().findPlugins(plugins);

		org.junit.Assert.assertEquals(true, plugins.size() > 0);
	}
}