package imagej.legacy;

import ij.IJ;
import ij.ImageJ;

public final class LegacyManager {

	/** Mapping between datasets and legacy image objects. */
	private static LegacyImageMap imageMap;

	private LegacyManager() {
		// prevent instantiation of utility class
	}

	static {
		// NB: Override class behavior before class loading gets too far along.
		CodeHacker hacker = new CodeHacker();

		// override ImageWindow behavior
		hacker.overrideMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.overrideMethod("ij.gui.ImageWindow", "public void show()");
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

}
