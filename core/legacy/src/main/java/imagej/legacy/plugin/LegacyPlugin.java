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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.imglib2.img.Axes;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.numeric.RealType;

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
		dirty flag for a ds - special one for harmonizer
		image map can track dirty harm and listen events
		*/

		final Dataset activeDS = ImageJ.get(DisplayManager.class).getActiveDataset();
		final LegacyImageMap map = ImageJ.get(LegacyManager.class).getImageMap();
		final DatasetHarmonizer harmonizer = new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyPlugin.getOutputs();
		outputSet.clear();
		prePluginHarmonization(map, harmonizer);
		WindowManager.setTempCurrentImage(map.findImagePlus(activeDS));
		IJ.runPlugIn(className, arg);
		outputs = postPluginHarmonization(map, harmonizer);
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

	private void prePluginHarmonization(LegacyImageMap map,
		DatasetHarmonizer harmonizer)
	{
		// TODO - have LegacyImageMap track dataset events and keep a dirty bit.
		// then only harmonize those datasets that have changed. 
		ObjectManager objMgr = ImageJ.get(ObjectManager.class);
		for (Dataset ds : objMgr.getObjects(Dataset.class)) {
			// TODO : when we allow nonplanar images and other primitive types
			//   to go over to IJ1 world this will need updating
			if (isIJ1Compatible(ds)) {
				ImagePlus imp = map.findImagePlus(ds);
				if (imp == null)
					map.registerDataset(ds);
				else
					harmonizer.updateLegacyImage(ds, imp);
			}
		}
	}

	private List<Dataset> postPluginHarmonization(LegacyImageMap map,
		DatasetHarmonizer harmonizer)
	{
		// the IJ1 plugin may not have any outputs but just changes current
		// ImagePlus make sure we catch any changes via harmonization
		ImagePlus currImp = IJ.getImage();
		Dataset ds = map.findDataset(currImp);
		if (ds != null)
			harmonizer.updateDataset(ds, currImp);

		// also harmonize any outputs

		List<Dataset> datasets = new ArrayList<Dataset>();

		for (ImagePlus imp : getOutputs()) {
			ds = map.findDataset(imp);
			if (ds == null)
				ds = map.registerLegacyImage(imp);
			else {
				if (imp == currImp) {
					// we harmonized this earlier
				}
				else
					harmonizer.updateDataset(ds, imp);
			}
			datasets.add(ds);
		}

		return datasets;
	}
	
	private boolean isIJ1Compatible(Dataset ds) {
		// for now only allow Datasets that can be represented in a planar
		// fashion made up of sign compat bytes, shorts, ints, and float32s
		// TODO - relax these constraints later

		final Img<? extends RealType<?>> img = ds.getImgPlus().getImg();
		
		if (img instanceof PlanarAccess) {
			
			int bitsPerPixel = ds.getType().getBitsPerPixel();
			boolean signed = ds.isSigned();
			boolean integer = ds.isInteger();
			boolean color = ds.isRGBMerged();
			int cAxis = ds.getAxisIndex(Axes.CHANNEL);
			long channels = (cAxis == -1) ? 1 : ds.getImgPlus().dimension(cAxis); 

			// ByteProcessor compatible
			if (!signed && integer && bitsPerPixel == 8) return true;

			// ShortProcessor compatible
			if (!signed && integer && bitsPerPixel == 16) return true;

			// FloatProcessor compatible
			if (signed && !integer && bitsPerPixel == 32) return true;

			// ColorProcessor compatible
			if (color && integer && bitsPerPixel == 8 && channels == 3) return true;
		}
		return false;
	}
}
