package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse button was clicked in a display.
 *
 * @author Curtis Rueden
 */
public class MsClickedEvent extends MsButtonEvent {

	public MsClickedEvent(final Display display,
		final int x, final int y, final int button, int numClicks)
	{
		super(display, x, y, button, numClicks);
	}

}
