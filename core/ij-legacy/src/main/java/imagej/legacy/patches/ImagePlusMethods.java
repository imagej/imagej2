package imagej.legacy.patches;

import ij.ImagePlus;
import imagej.Log;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyManager;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.model.Dataset;

/** Overrides {@link ImagePlus} methods. */
public final class ImagePlusMethods {

	private ImagePlusMethods() {
		// prevent instantiation of utility class
	}

	/** Replaces {@link ImagePlus#updateAndDraw()). */
	public static void updateAndDraw(ImagePlus obj) {
		Log.debug("ImagePlus.updateAndDraw(): " + obj);

		
		// register image with legacy manager
		final LegacyImageMap imageMap = LegacyManager.getImageMap();
		final Dataset dataset = imageMap.registerLegacyImage(obj);

		// record resultant dataset as a legacy plugin output
		LegacyPlugin.getOutputSet().add(dataset);
	}

}
