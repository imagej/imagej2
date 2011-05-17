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
 */
public class LegacyPlugin implements ImageJPlugin {

	@Parameter
	private String className;

	@Parameter
	private String arg;

	@Parameter(output=true)
	private List<Dataset> outputs;

	@Override
	public void run() {
		final LegacyImageMap map = ImageJ.get(LegacyManager.class).getImageMap();
		final DatasetHarmonizer harmonizer = new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyPlugin.getOutputs();
		outputSet.clear();
		IJ.runPlugIn(className, arg);
		outputs = new ArrayList<Dataset>();
		for (ImagePlus imp : outputSet) {
			Dataset ds = map.findDataset(imp);
			if (ds == null)
				ds = map.registerLegacyImage(imp);
			else
				harmonizer.harmonize(ds, imp);
			outputs.add(ds);
		}
		outputSet.clear();
	}

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
	 * Gets a list for storing output parameter values.
	 * This method is thread-safe, because it uses a separate map per thread.
	 */
	public static Set<ImagePlus> getOutputs() {
		return outputImps.get();
	}

}
