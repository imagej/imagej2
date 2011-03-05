package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse cursor entered a display.
 *
 * @author Curtis Rueden
 */
public class MsEnteredEvent extends MsEvent {

	public MsEnteredEvent(final Display display,
		final int x, final int y)
	{
		super(display, x, y);
	}

}
