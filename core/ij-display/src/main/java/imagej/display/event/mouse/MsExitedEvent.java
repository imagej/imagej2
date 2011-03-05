package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse cursor exited a display.
 *
 * @author Curtis Rueden
 */
public class MsExitedEvent extends MsEvent {

	public MsExitedEvent(final Display display,
		final int x, final int y)
	{
		super(display, x, y);
	}

}
