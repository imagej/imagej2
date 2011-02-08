package imagej.ij1bridge.patches;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import imagej.Log;
import imagej.dataset.Dataset;
import imagej.ij1bridge.LegacyManager;
import imagej.ij1bridge.plugin.LegacyPlugin;

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
		final Dataset dataset = LegacyManager.getImageMap().registerLegacyImage(imp);

		// record resultant dataset as a legacy plugin output
		LegacyPlugin.getOutputList().add(dataset);
	}

	/** Replaces {@link ImageWindow#show(). */
	public static void show(ImageWindow obj) {
		setVisible(obj, true);
	}

}
