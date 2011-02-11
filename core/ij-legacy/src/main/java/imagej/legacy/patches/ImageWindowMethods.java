package imagej.legacy.patches;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import imagej.Log;
import imagej.dataset.Dataset;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyManager;
import imagej.legacy.plugin.LegacyPlugin;

/** Overrides {@link ImageWindow} methods. */
public final class ImageWindowMethods {

	private ImageWindowMethods() {
		// prevent instantiation of utility class
	}

	/** Replaces {@link ImageWindow#setVisible(boolean). */
	public static void setVisible(ImageWindow obj, boolean visible) {
		Log.debug("ImageWindow.setVisible(" + visible + "): " + obj);
		if (!visible) return;
		final ImagePlus imp = obj.getImagePlus();

		// register image with legacy manager
		final LegacyImageMap imageMap = LegacyManager.getImageMap();
		final Dataset dataset = imageMap.registerLegacyImage(imp);

		// record resultant dataset as a legacy plugin output
		LegacyPlugin.getOutputSet().add(dataset);
	}

	/** Replaces {@link ImageWindow#show(). */
	public static void show(ImageWindow obj) {
		setVisible(obj, true);
	}

}
