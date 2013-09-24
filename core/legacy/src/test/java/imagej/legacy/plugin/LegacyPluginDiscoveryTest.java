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

package imagej.legacy.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.legacy.LegacyService;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Tests discovery of legacy plugins within an ImageJ context containing a
 * {@link LegacyService}.
 * 
 * @author Curtis Rueden
 */
public class LegacyPluginDiscoveryTest {

	/**
	 * Tests legacy plugin discovery within an ImageJ application context
	 * containing a {@link LegacyService}.
	 */
	@Test
	public void testLegacyPluginDiscovery() {
		// discovery should work even when running headless!
		System.setProperty("java.awt.headless", "true");

		// create an ImageJ application context with a legacy service
		final Context context =
			new Context(LegacyService.class, PluginService.class,
				ModuleService.class, CommandService.class);
		final PluginService pluginService = context.getService(PluginService.class);

		// skip test if legacy service not available
		final LegacyService legacyService = context.getService(LegacyService.class);
		assumeTrue(legacyService != null);

		// verify that expected plugins were discovered
		final List<PluginInfo<LegacyCommand>> typedPlugins =
			pluginService.getPluginsOfType(LegacyCommand.class);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<?>> plugins = (List) typedPlugins;
		final HashMap<String, PluginInfo<?>> pluginMap =
			assertPlugins(plugins, 320);

		// verify the same plugins are available as modules
		final ModuleService moduleService = context.getService(ModuleService.class);
		final List<ModuleInfo> modules = moduleService.getModules();
		int moduleCount = 0;
		for (final ModuleInfo info : modules) {
			if (info.getDelegateClassName().equals(
				"imagej.legacy.plugin.LegacyCommand"))
			{
				moduleCount++;
				final String key = info.getMenuPath().getLeaf().getName();
				final PluginInfo<?> plugin = pluginMap.get(key);
				assertNotNull("No plugin for module: " + info, plugin);
			}
		}
		assertEquals(plugins.size(), moduleCount);

		// verify the same plugins are available as commands
		final CommandService commandService =
			context.getService(CommandService.class);
		final List<CommandInfo> commands =
			commandService.getCommandsOfType(LegacyCommand.class);
		assertEquals(plugins.size(), commands.size());
		for (final CommandInfo info : commands) {
			final String key = info.getMenuPath().getLeaf().getName();
			final PluginInfo<?> plugin = pluginMap.get(key);
			assertNotNull("No plugin for command: " + info, plugin);
		}
	}

	// -- Utility methods --

	public static HashMap<String, PluginInfo<?>> assertPlugins(
		final List<PluginInfo<?>> plugins, final int minCount)
	{
		assertTrue(plugins.size() >= minCount);

		// index plugins by menu leaf
		final HashMap<String, PluginInfo<?>> pluginMap =
			new HashMap<String, PluginInfo<?>>();
		for (final PluginInfo<?> plugin : plugins) {
			final String key = plugin.getMenuPath().getLeaf().getName();
			pluginMap.put(key, plugin);
		}

		final String[] expectedPlugins = { "3D Project...", //
			"8-bit Color", //
			"AND...", //
			"AVI... ", //
			"AVI...", //
			"About This Submenu...", //
			"Add Specified Noise...", //
			"Add...", //
			"Analyze Line Graph", //
			"Analyze Particles...", //
			"Apply LUT", //
			"Area to Line", //
			"Arrange Channels...", //
			"AuPbSn 40 (56K)", //
			"BMP...", //
			"Bandpass Filter...", //
			"Bat Cochlea Renderings (449K)", //
			"Bat Cochlea Volume (19K)", //
			"Benchmark", //
			"Bin...", //
			"Blobs (25K)", //
			"Blue", //
			"Boats (356K)", //
			"Bridge (174K)", //
			"Brightness/Contrast...", //
			"CT (420K, 16-bit DICOM)", //
			"Calibrate...", //
			"Calibration Bar...", //
			"Canvas Size...", //
			"Capture Image", //
			"Capture Screen", //
			"Cardio (768K, RGB DICOM)", //
			"Cascade", //
			"Cell Colony (31K)", //
			"Channels Tool...", //
			"Clear Outside", //
			"Clear Results", //
			"Clear", //
			"Close All", //
			"Close", //
			"Close-", //
			"Clown (14K)", //
			"Color Balance...", //
			"Color Threshold...", //
			"Combine...", //
			"Compile and Run...", //
			"Concatenate...", //
			"Confocal Series (2.2MB)", //
			"Control Panel...", //
			"Convert to Mask", //
			"Convert...", //
			"Convex Hull", //
			"Convolve...", //
			"Copy", //
			"Create Mask", //
			"Create Selection", //
			"Crop", //
			"Curve Fitting...", //
			"Custom Filter...", //
			"Cut", //
			"Cyan", //
			"Despeckle", //
			"Dev. Resources...", //
			"Dilate", //
			"Display LUTs", //
			"Distance Map", //
			"Distribution...", //
			"Divide...", //
			"Documentation...", //
			"Dot Blot (7K)", //
			"Draw", //
			"East", //
			"Edit LUT...", //
			"Edit...", //
			"Embryos (42K)", //
			"Enhance Contrast...", //
			"Enlarge...", //
			"Erode", //
			"FD Math...", //
			"FFT Options...", //
			"FFT", //
			"FITS...", //
			"Fill Holes", //
			"Fill", //
			"Find Commands...", //
			"Find Edges", //
			"Find Maxima...", //
			"Fire", //
			"Fit Circle", //
			"Fit Ellipse", //
			"Fit Spline", //
			"Flatten", //
			"Flip Horizontally", //
			"Flip Vertically", //
			"Fluorescent Cells (400K)", //
			"Fly Brain (1MB)", //
			"Fractal Box Count...", //
			"Gamma...", //
			"Gaussian Blur 3D...", //
			"Gaussian Blur...", //
			"Gel (105K)", //
			"Gel Analyzer Options...", //
			"Gif...", //
			"Grays", //
			"Green", //
			"Grouped Z Project...", //
			"HSB Stack", //
			"HeLa Cells (1.3M, 48-bit RGB)", //
			"Histogram", //
			"Hyperstack...", //
			"Ice", //
			"Image Calculator...", //
			"Image Sequence... ", //
			"Image Sequence...", //
			"Image to Results", //
			"Image to Selection...", //
			"Image...", //
			"ImageJ News...", //
			"ImageJ Properties...", //
			"ImageJ Website...", //
			"Images to Stack", //
			"Insert...", //
			"Install Plugin...", //
			"Install... ", //
			"Install...", //
			"Installation...", //
			"Internal Clipboard", //
			"Interpolate", //
			"Inverse FFT", //
			"Invert LUT", //
			"JavaScript", //
			"Jpeg...", //
			"LUT... ", //
			"LUT...", //
			"Label Peaks", //
			"Label", //
			"Label...", //
			"Labels...", //
			"Leaf (36K)", //
			"Lena (68K)", //
			"Line Graph (21K)", //
			"Line Width... ", //
			"Line to Area", //
			"List Elements", //
			"List Shortcuts...", //
			"M51 Galaxy (177K, 16-bits)", //
			"MRI Stack (528K)", //
			"Macro Functions...", //
			"Macro Tool", //
			"Macro", //
			"Macro... ", //
			"Macros...", //
			"Magenta", //
			"Mailing List...", //
			"Make Band...", //
			"Make Binary", //
			"Make Inverse", //
			"Make Montage...", //
			"Make Substack...", //
			"Max...", //
			"Maximum 3D...", //
			"Maximum...", //
			"Mean 3D...", //
			"Mean...", //
			"Measure", //
			"Measure...", //
			"Median 3D...", //
			"Median...", //
			"Merge Channels...", //
			"Min...", //
			"Minimum 3D...", //
			"Minimum...", //
			"Mitosis (26MB, 5D stack)", //
			"Monitor Events...", //
			"Monitor Memory...", //
			"Montage to Stack...", //
			"Multiply...", //
			"NaN Background", //
			"Neuron (1.6M, 5 channels)", //
			"New Hyperstack...", //
			"Nile Bend (1.9M)", //
			"North", //
			"Northeast", //
			"Northwest", //
			"OR...", //
			"Open Next", //
			"Open", //
			"Open...", //
			"Options...", //
			"Organ of Corti (2.8M, 4D stack)", //
			"Original Scale", //
			"Orthogonal Views", //
			"Outline", //
			"PGM...", //
			"PNG...", //
			"Page Setup...", //
			"Particles (75K)", //
			"Paste Control...", //
			"Paste", //
			"Plot Lanes", //
			"Plot Profile", //
			"Plot Z-axis Profile", //
			"Plugin Filter", //
			"Plugin Frame", //
			"Plugin Tool", //
			"Plugin", //
			"Plugins...", //
			"Print...", //
			"Properties... ", //
			"Properties...", //
			"Put Behind [tab]", //
			"RGB Color", //
			"RGB Stack", //
			"Raw Data...", //
			"Raw...", //
			"Re-plot Lanes", //
			"Record...", //
			"Red", //
			"Redisplay Power Spectrum", //
			"Reduce Dimensionality...", //
			"Reduce...", //
			"Refresh Menus", //
			"Remove NaNs...", //
			"Remove Outliers...", //
			"Remove Overlay", //
			"Remove Slice Labels", //
			"Remove...", //
			"Rename...", //
			"Reset", //
			"Reset...", //
			"Reslice [/]...", //
			"Restore Selection", //
			"Results to Image", //
			"Results... ", //
			"Results...", //
			"Revert", //
			"Rotate 90 Degrees Left", //
			"Rotate 90 Degrees Right", //
			"Rotate... ", //
			"Rotate...", //
			"Run...", //
			"Salt and Pepper", //
			"Save XY Coordinates...", //
			"Save", //
			"Scale Bar...", //
			"Scale... ", //
			"Scale...", //
			"Search...", //
			"Select First Lane", //
			"Select Next Lane", //
			"Selection...", //
			"Set Label...", //
			"Set Measurements...", //
			"Set Scale...", //
			"Set...", //
			"Shadows Demo", //
			"Sharpen", //
			"Show All", //
			"Show Circular Masks...", //
			"Show Info...", //
			"Show LUT", //
			"Size...", //
			"Skeletonize", //
			"Smooth", //
			"South", //
			"Southeast", //
			"Southwest", //
			"Specify...", //
			"Spectrum", //
			"Split Channels", //
			"Stack From List...", //
			"Stack to Images", //
			"Stack to RGB", //
			"Startup Macros...", //
			"Statistics", //
			"Straighten...", //
			"Subtract Background...", //
			"Subtract...", //
			"Summarize", //
			"Surface Plot...", //
			"Swap Quadrants", //
			"Synchronize Windows", //
			"System Clipboard", //
			"T1 Head (2.4M, 16-bits)", //
			"T1 Head Renderings (736K)", //
			"TEM Filter (112K)", //
			"TIFF Virtual Stack...", //
			"Table... ", //
			"Table...", //
			"Text File... ", //
			"Text Image... ", //
			"Text Image...", //
			"Text Window", //
			"Text Window...", //
			"Text...", //
			"Threads...", //
			"Threshold...", //
			"Tiff...", //
			"Tile", //
			"To Bounding Box", //
			"To Selection", //
			"Tree Rings (48K)", //
			"URL...", //
			"Ultimate Points", //
			"Undo", //
			"Unsharp Mask...", //
			"Variance 3D...", //
			"Variance...", //
			"View 100%", //
			"Virtual Stack...", //
			"Voronoi", //
			"Watershed", //
			"West", //
			"Window/Level...", //
			"XOR...", //
			"XY Coordinates... ", //
			"XY Coordinates...", //
			"Yellow", //
			"Z Project...", //
			"ZIP...", //
		};
		for (final String expectedPlugin : expectedPlugins) {
			final boolean found = pluginMap.containsKey(expectedPlugin);
			assertTrue("Plugin not found: " + expectedPlugin, found);
		}
		assertFalse(pluginMap.containsKey("Bogus Plugin"));

		return pluginMap;
	}

}
