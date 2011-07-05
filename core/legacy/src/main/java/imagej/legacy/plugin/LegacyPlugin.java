//
// LegacyPlugin.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.legacy.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.legacy.DatasetHarmonizer;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyManager;
import imagej.object.ObjectManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes an IJ1 plugin.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyPlugin implements ImageJPlugin {

	@Parameter
	private String className;

	@Parameter
	private String arg;

	@Parameter(output = true)
	private List<Display> outputs;

	/** Used to provide one list of {@link ImagePlus} per calling thread. */
	private static ThreadLocal<Set<ImagePlus>> outputImps =
		new ThreadLocal<Set<ImagePlus>>() {

			@Override
			protected synchronized Set<ImagePlus> initialValue() {
				return new HashSet<ImagePlus>();
			}
		};

	/**
	 * Gets a list for storing output parameter values. This method is
	 * thread-safe, because it uses a separate map per thread.
	 */
	public static Set<ImagePlus> getOutputImps() {
		return outputImps.get();
	}

	// -- LegacyPlugin methods --

	/** Gets the list of output {@link Display}s. */
	public List<Display> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}

	// -- Runnable methods --

	@Override
	public void run() {
		final LegacyManager legacyManager = ImageJ.get(LegacyManager.class);
		final LegacyImageMap map = legacyManager.getImageMap();

		// sync legacy images to match existing modern displays
		final DatasetHarmonizer harmonizer =
			new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyPlugin.getOutputImps();
		outputSet.clear();
		harmonizer.resetTypeTracking();
		prePluginHarmonization(map, harmonizer);

		// set ImageJ1's active image
		legacyManager.syncActiveImage();

		try {
			// execute the legacy plugin
			IJ.runPlugIn(className, arg);

			// sync modern displays to match existing legacy images
			outputs = postPluginHarmonization(map, harmonizer);
		}
		catch (final Exception e) {
			Log.warn("No outputs found - ImageJ 1.x plugin threw exception", e);
			// make sure our ImagePluses are in sync with original Datasets
			prePluginHarmonization(map, harmonizer);
			// return no outputs
			outputs = new ArrayList<Display>();
		}
		harmonizer.resetTypeTracking();
		outputSet.clear();
	}

	// -- Helper methods --

	private void prePluginHarmonization(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// TODO - track events and keep a dirty bit, then only harmonize those
		// displays that have changed. See ticket #546.
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		for (final Display display : objectManager.getObjects(Display.class)) {
			ImagePlus imp = map.lookupImagePlus(display);
			if (imp == null) imp = map.registerDisplay(display);
			else harmonizer.updateLegacyImage(display, imp);
			harmonizer.registerType(imp);
		}
	}

	private List<Display> postPluginHarmonization(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// TODO - check the changes flag for each ImagePlus that already has a
		// Display and only harmonize those that have changed. Maybe changes
		// flag does not track everything (such as metadata changes?) and thus
		// we might still have to do some minor harmonization. Investigate.

		// the IJ1 plugin may not have any outputs but just changes current
		// ImagePlus make sure we catch any changes via harmonization
		final ImagePlus currImp = IJ.getImage();
		Display display = map.lookupDisplay(currImp);
		if (display != null) harmonizer.updateDisplay(display, currImp);

		// also harmonize any outputs

		final List<Display> displays = new ArrayList<Display>();

		final Set<ImagePlus> imps = getOutputImps();
		for (final ImagePlus imp : imps) {
			display = map.lookupDisplay(imp);
			if (display == null) display = map.registerLegacyImage(imp);
			else {
				if (imp == currImp) {
					// we harmonized this earlier
				}
				else harmonizer.updateDisplay(display, imp);
			}
			displays.add(display);
		}

		return displays;
	}

}
