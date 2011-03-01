package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse button was pressed in a display.
 *
 * @author Curtis Rueden
 */
public class MsPressedEvent extends MsButtonEvent {

	public MsPressedEvent(final Display display,
		final int x, final int y, final int button, int numClicks)
	{
		super(display, x, y, button, numClicks);
	}

}
