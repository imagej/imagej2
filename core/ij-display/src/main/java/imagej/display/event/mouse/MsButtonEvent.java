package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating mouse button activity in a display.
 *
 * @author Curtis Rueden
 */
public class MsButtonEvent extends MsEvent {

	public static final int LEFT_BUTTON = 0;
	public static final int MIDDLE_BUTTON = 1;
	public static final int RIGHT_BUTTON = 2;

	private int button;

	public MsButtonEvent(final Display display,
		final int x, final int y, final int button)
	{
		super(display, x, y);
		this.button = button;
	}

	public int getButton() {
		return button;
	}

}
