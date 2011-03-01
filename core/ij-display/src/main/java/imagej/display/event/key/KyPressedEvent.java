package imagej.display.event.key;

import imagej.display.Display;

/**
 * An event indicating keyboard activity in a display.
 *
 * @author Curtis Rueden
 */
public class KyPressedEvent extends KyEvent {

	public KyPressedEvent(final Display display, final char character,
		final int code, final int modifiers)
	{
		super(display, character, code, modifiers);
	}

}
