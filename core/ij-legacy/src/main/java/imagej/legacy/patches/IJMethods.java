package imagej.legacy.patches;

import ij.IJ;
import imagej.Log;

/**
 * Overrides {@link IJ} methods.
 *
 * @author Curtis Rueden
 */
public class IJMethods {

	/** Appends {@link IJ#showProgress(double)}. */
	public static void showProgress(final double progress) {
		Log.debug("showProgress: " + progress);
		// TODO
	}

	/** Appends {@link IJ#showProgress(int, int)}. */
	public static void showProgress(final int currentIndex,
		final int finalIndex)
	{
		Log.debug("showProgress: " + currentIndex + "/" + finalIndex);
		// TODO
	}

	/** Appends {@link IJ#showStatus(String)}. */
	public static void showStatus(final String s) {
		Log.debug("showStatus: " + s);
		// TODO
	}

}
