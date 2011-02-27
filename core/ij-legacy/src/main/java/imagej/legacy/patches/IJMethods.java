package imagej.legacy.patches;

import ij.IJ;
import imagej.Log;
import imagej.event.Events;
import imagej.event.StatusEvent;

/**
 * Overrides {@link IJ} methods.
 *
 * @author Curtis Rueden
 */
public class IJMethods {

	/** Resolution to use when converting double progress to int ratio. */
	private static final int PROGRESS_GRANULARITY = 1000;

	/** Appends {@link IJ#showProgress(double)}. */
	public static void showProgress(final double progress) {
		// approximate progress as int ratio
		final int currentIndex = (int) (PROGRESS_GRANULARITY * progress);
		final int finalIndex = PROGRESS_GRANULARITY;
		showProgress(currentIndex, finalIndex);
	}

	/** Appends {@link IJ#showProgress(int, int)}. */
	public static void showProgress(final int currentIndex,
		final int finalIndex)
	{
		Log.debug("showProgress: " + currentIndex + "/" + finalIndex);
		// report progress through global event mechanism
		Events.publish(new StatusEvent(currentIndex, finalIndex));
	}

	/** Appends {@link IJ#showStatus(String)}. */
	public static void showStatus(final String s) {
		Log.debug("showStatus: " + s);
		// report status through global event mechanism
		Events.publish(new StatusEvent(s));
	}

}
