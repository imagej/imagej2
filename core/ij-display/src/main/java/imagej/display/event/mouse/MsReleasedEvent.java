package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse button was released in a display.
 *
 * @author Curtis Rueden
 */
public class MsReleasedEvent extends MsButtonEvent {

	public MsReleasedEvent(final Display display,
		final int x, final int y, final int button, int numClicks)
	{
		super(display, x, y, button, numClicks);
	}

}
