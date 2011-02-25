package imagej.legacy;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.model.Dataset;

public final class LegacyManager {

	/** Mapping between datasets and legacy image objects. */
	private static LegacyImageMap imageMap;

	private LegacyManager() {
		// prevent instantiation of utility class
	}

	static {
		// NB: Override class behavior before class loading gets too far along.
		final CodeHacker hacker = new CodeHacker();

		// override ImageWindow behavior
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override ImagePlus behavior
		hacker.insertAfterMethod("ij.ImagePlus", "public void updateAndDraw()");
		hacker.loadClass("ij.ImagePlus");
	}
	
	public static ImageJ initialize() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) return ij;

		imageMap = new LegacyImageMap();

		// initialize legacy ImageJ application
		return new ImageJ(ImageJ.NO_SHOW);
	}

	public static LegacyImageMap getImageMap() {
		return imageMap;
	}

	public static void legacyImageChanged(final ImagePlus imp) {
		// register image with legacy manager
		final Dataset dataset = imageMap.registerLegacyImage(imp);

		// record resultant dataset as a legacy plugin output
		LegacyPlugin.getOutputSet().add(dataset);
	}

}
