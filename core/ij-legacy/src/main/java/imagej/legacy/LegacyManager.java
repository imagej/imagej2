package imagej.legacy;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.model.Dataset;

/**
 * Utility class for managing the linkage to legacy ImageJ 1.x.
 *
 * The legacy manager overrides the behavior of various IJ1 methods,
 * inserting seams so that (e.g.) the modern GUI is aware of IJ1 events
 * as they occur.
 *
 * It also maintains an image map between IJ1 {@link ImagePlus} objects
 * and IJ2 {@link Dataset}s.
 *
 * In this fashion, when a legacy plugin is executed on a {@link Dataset},
 * the manager transparently translates it into an {@link ImagePlus}, and
 * vice versa, enabling backward compatibility with legacy plugins.
 *
 * @author Curtis Rueden
 */
public final class LegacyManager {

	/** Mapping between datasets and legacy image objects. */
	private static LegacyImageMap imageMap;

	private LegacyManager() {
		// prevent instantiation of utility class
	}

	static {
		// NB: Override class behavior before class loading gets too far along.
		final CodeHacker hacker = new CodeHacker();

		// override behavior of ij.IJ
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(double progress)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showStatus(java.lang.String s)");
		hacker.loadClass("ij.IJ");

		// override behavior of ij.gui.ImageWindow
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override behavior of ij.ImagePlus
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
