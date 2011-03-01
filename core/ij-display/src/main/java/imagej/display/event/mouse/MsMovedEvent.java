package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse was moved in a display.
 *
 * @author Curtis Rueden
 */
public class MsMovedEvent extends MsEvent {

	public MsMovedEvent(final Display display,
		final int x, final int y)
	{
		super(display, x, y);
	}

}
