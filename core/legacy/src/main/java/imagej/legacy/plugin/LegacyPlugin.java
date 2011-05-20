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
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.legacy.DatasetHarmonizer;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import java.util.ArrayList;
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

	@Parameter(output=true)
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
	
	// -- public interface --
	
	@Override
	public void run() {

		/*

		use current imp as input to harmonize : no
		
		
		a) harmonize all ds (from Obj man)
		b) set imp of active ds as current image
				set temp curr im or set curr image

		dirty flag for a ds 1 for harm and 1 for saved
		image map can track dirty harm and listen events
		ds can track dirty saved

		*/

		final LegacyImageMap map = ImageJ.get(LegacyManager.class).getImageMap();
		final DatasetHarmonizer harmonizer = new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyPlugin.getOutputs();
		outputSet.clear();
		harmonizeInputs(map, harmonizer);
		// set current temp image or curr image
		IJ.runPlugIn(className, arg);
		harmonizeCurrentImagePlus(map, harmonizer);
		outputs = harmonizeOutputs(map, harmonizer);
		outputSet.clear();
	}

	/**
	 * Gets a list for storing output parameter values.
	 * This method is thread-safe, because it uses a separate map per thread.
	 */
	public static Set<ImagePlus> getOutputs() {
		return outputImps.get();
	}

	// -- helpers --

	private void harmonizeInputs(LegacyImageMap map,
		DatasetHarmonizer harmonizer)
	{
	}

	private void harmonizeCurrentImagePlus(LegacyImageMap map,
		DatasetHarmonizer harmonizer)
	{
		// this plugin may not have any outputs but just changes current ImagePlus
		// make sure we catch any changes via harmonization
		ImagePlus currImp = IJ.getImage();
		Dataset ds = map.findDataset(currImp);
		if (ds != null)
			harmonizer.harmonize(ds, currImp);
	}
	
	private List<Dataset> harmonizeOutputs(LegacyImageMap map,
		DatasetHarmonizer harmonizer)
	{
		List<Dataset> datasets = new ArrayList<Dataset>();

		// also harmonize all outputs
		for (ImagePlus imp : getOutputs()) {
			Dataset ds = map.findDataset(imp);
			if (ds == null)
				ds = map.registerLegacyImage(imp);
			else
				harmonizer.harmonize(ds, imp);
			datasets.add(ds);
		}

		return datasets;
	}
}
