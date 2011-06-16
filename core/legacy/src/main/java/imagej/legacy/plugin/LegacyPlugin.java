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
	private List<Dataset> outputs;

	/** Used to provide one list of datasets per calling thread. */
	private static ThreadLocal<Set<ImagePlus>> outputImps =
		new ThreadLocal<Set<ImagePlus>>()
	{
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

	/** Gets the list of output {@link Dataset}s. */
	public List<Dataset> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}

	// -- Runnable methods --

	@Override
	public void run() {
		/*
		dirty flag for a ds - special one for harmonizer
		image map can track dirty harm and listen events
		*/

		final Dataset activeDS =
			ImageJ.get(DisplayManager.class).getActiveDataset();
		final LegacyImageMap map = ImageJ.get(LegacyManager.class).getImageMap();
		final DatasetHarmonizer harmonizer =
			new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyPlugin.getOutputImps();
		outputSet.clear();
		harmonizer.resetTypeTracking();
		prePluginHarmonization(map, harmonizer);
		ImagePlus parallelImp = map.findImagePlus(activeDS);
		WindowManager.setTempCurrentImage(parallelImp);
		try {
			IJ.runPlugIn(className, arg);
			outputs = postPluginHarmonization(map, harmonizer);
		} catch (Exception e) {
			Log.warn("No outputs found - ImageJ 1.x plugin threw exception: "+e.getMessage());
			// make sure our ImagePluses are in sync with original Datasets
			prePluginHarmonization(map, harmonizer);
			// return no outputs
			outputs = new ArrayList<Dataset>();
		}
		harmonizer.resetTypeTracking();
		outputSet.clear();
	}

	// -- Helper methods --

	private void prePluginHarmonization(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// TODO - have LegacyImageMap track dataset events and keep a dirty bit.
		// then only harmonize those datasets that have changed. See ticket #546.
		final ObjectManager objMgr = ImageJ.get(ObjectManager.class);
		for (final Dataset ds : objMgr.getObjects(Dataset.class)) {
			ImagePlus imp = map.findImagePlus(ds);
			if (imp == null)
				imp = map.registerDataset(ds);
			else
				harmonizer.updateLegacyImage(ds, imp);
			harmonizer.registerType(imp);
		}
	}

	private List<Dataset> postPluginHarmonization(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// the IJ1 plugin may not have any outputs but just changes current
		// ImagePlus make sure we catch any changes via harmonization
		final ImagePlus currImp = IJ.getImage();
		Dataset ds = map.findDataset(currImp);
		if (ds != null) harmonizer.updateDataset(ds, currImp);

		// also harmonize any outputs

		final List<Dataset> datasets = new ArrayList<Dataset>();

		Set<ImagePlus> imps = getOutputImps();
		for (final ImagePlus imp : imps) {
			ds = map.findDataset(imp);
			if (ds == null) ds = map.registerLegacyImage(imp);
			else {
				if (imp == currImp) {
					// we harmonized this earlier
				}
				else harmonizer.updateDataset(ds, imp);
			}
			datasets.add(ds);
		}

		return datasets;
	}

}
