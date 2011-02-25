package imagej.legacy.patches;

import ij.ImagePlus;
import imagej.Log;
import imagej.legacy.LegacyManager;

/** Overrides {@link ImagePlus} methods. */
public final class ImagePlusMethods {

	private ImagePlusMethods() {
		// prevent instantiation of utility class
	}

	/** Replaces {@link ImagePlus#updateAndDraw()). */
	public static void updateAndDraw(ImagePlus obj) {
		Log.debug("ImagePlus.updateAndDraw(): " + obj);
		LegacyManager.legacyImageChanged(obj);
	}

}
