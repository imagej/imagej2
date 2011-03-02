package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse was dragged in a display.
 *
 * @author Curtis Rueden
 */
public class MsDraggedEvent extends MsButtonEvent {

	public MsDraggedEvent(final Display display, final int x, final int y,
		final int button, final int numClicks, final boolean isPopupTrigger)
	{
		super(display, x, y, button, numClicks, isPopupTrigger);
	}

}
