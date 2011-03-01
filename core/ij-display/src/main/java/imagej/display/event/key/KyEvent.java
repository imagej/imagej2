package imagej.display.event.key;

import imagej.display.Display;
import imagej.display.event.DisplayEvent;

/**
 * An event indicating keyboard activity in a display.
 *
 * @author Curtis Rueden
 */
public class KyEvent extends DisplayEvent {

	private char character;
	private int code;
	private int modifiers;

	public KyEvent(final Display display, final char character,
		final int code, final int modifiers)
	{
		super(display);
		this.character = character;
		this.code = code;
		this.modifiers = modifiers;
	}

	public char getCharacter() {
		return character;
	}

	public int getCode() {
		return code;
	}

	public int getModifiers() {
		return modifiers;
	}

}
