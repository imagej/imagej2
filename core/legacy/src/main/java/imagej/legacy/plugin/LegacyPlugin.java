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
import ij.gui.ImageWindow;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.DisplayWindow;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.legacy.DatasetHarmonizer;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyOutputTracker;
import imagej.legacy.LegacyService;
import imagej.legacy.LegacyUtils;
import imagej.object.ObjectService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

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

	private DisplayService displayService;
	
	// -- LegacyPlugin methods --

	/** Gets the list of output {@link Display}s. */
	public List<Display> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}

	// -- Runnable methods --

	@Override
	public void run() {
		displayService = ImageJ.get(DisplayService.class);
		final Display activeDisplay = displayService.getActiveDisplay();
		if (!isLegacyCompatible(activeDisplay)) {
			Log.warn("Active dataset is not compatible with IJ1");
			outputs = new ArrayList<Display>();
			return;
		}

		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		final LegacyImageMap map = legacyService.getImageMap();

		// sync legacy images to match existing modern displays
		final DatasetHarmonizer harmonizer =
			new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyOutputTracker.getOutputImps();
		final Set<ImagePlus> closedSet = LegacyOutputTracker.getClosedImps();
		
		harmonizer.resetTypeTracking();
		
		updateImagePlusesFromDisplays(map, harmonizer);

		outputSet.clear();
		closedSet.clear();  // must happen after prePluginHarmonization()

		// set ImageJ1's active image
		legacyService.syncActiveImage();

		try {
			// execute the legacy plugin
			IJ.runPlugIn(className, arg);

			// sync modern displays to match existing legacy images
			outputs = updateDisplaysFromImagePluses(map, harmonizer);
		}
		catch (final Exception e) {
			Log.warn("No outputs found - ImageJ 1.x plugin threw exception", e);
			// make sure our ImagePluses are in sync with original Datasets
			updateImagePlusesFromDisplays(map, harmonizer);
			// return no outputs
			outputs = new ArrayList<Display>();
		}

		for (ImagePlus imp : closedSet) {
			ImageWindow win = imp.getWindow();
			if ((win == null) || (win.isClosed())) {
				Display disp = map.lookupDisplay(imp);
				if (disp != null) {
					outputs.remove(disp);
					DisplayWindow dispWin = disp.getDisplayWindow();
					if (dispWin != null) dispWin.close();
				}
			}
		}
		
		harmonizer.resetTypeTracking();
		outputSet.clear();
		closedSet.clear();
	}

	// -- Helper methods --

	private void updateImagePlusesFromDisplays(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// TODO - track events and keep a dirty bit, then only harmonize those
		// displays that have changed. See ticket #546.
		final ObjectService objectService = ImageJ.get(ObjectService.class);
		for (final Display display : objectService.getObjects(Display.class)) {
			ImagePlus imp = map.lookupImagePlus(display);
			if (imp == null) {
				if (isLegacyCompatible(display)) {
					imp = map.registerDisplay(display);
					harmonizer.registerType(imp);
				}
			}
			else { // imp already exists : update it
				harmonizer.updateLegacyImage(display, imp);
				harmonizer.registerType(imp);
			}
		}
	}

	private List<Display> updateDisplaysFromImagePluses(final LegacyImageMap map,
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

		final Set<ImagePlus> imps = LegacyOutputTracker.getOutputImps();
		for (final ImagePlus imp : imps) {
			if (imp.getStack().getSize() == 0) {  // totally emptied by plugin
				// TODO - do we need to delete display or is it already done?
			}
			else { // image plus is not totally empty
				display = map.lookupDisplay(imp);
				if (display == null)
					display = map.registerLegacyImage(imp);
				else {
					if (imp == currImp) {
						// we harmonized this earlier
					}
					else harmonizer.updateDisplay(display, imp);
				}
				displays.add(display);
			}
		}

		return displays;
	}

	private boolean isLegacyCompatible(Display display) {
		if (display == null) return true;
		Dataset ds = displayService.getActiveDataset(display);
		Axis[] axes = ds.getAxes();
		if (LegacyUtils.hasNonIJ1Axes(axes)) return false;
		if (dimensionsIncompatible(ds)) return false;
		return true;
	}

	/**
	 * Assumes there are no incompatible IJ1 axes present in dataset
	 * @param dataset
	 */
	private boolean dimensionsIncompatible(Dataset dataset) {
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);

		long[] dims = dataset.getDims();
		
		final long xCount = xIndex < 0 ? 1 : dims[xIndex];
		final long yCount = yIndex < 0 ? 1 : dims[yIndex];
		final long cCount = cIndex < 0 ? 1 : dims[cIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];

		final long ij1ChannelCount =
			dataset.isRGBMerged() ? (cCount / 3) : cCount;
			
		// check width
		if ((xIndex < 0) || (xCount > Integer.MAX_VALUE)) return true;
		
		// check height
		if ((yIndex < 0) || (yCount > Integer.MAX_VALUE)) return true;

		// check plane size
		if ((xCount * yCount) > Integer.MAX_VALUE) return true;

		// check number of planes not too large
		if (ij1ChannelCount * zCount * tCount > Integer.MAX_VALUE)  return true;
		
		return false;
	}

}
