package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating mouse button activity in a display.
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class MsButtonEvent extends MsEvent {

	public static final int LEFT_BUTTON = 0;
	public static final int MIDDLE_BUTTON = 1;
	public static final int RIGHT_BUTTON = 2;
	private final int button;
	private final int numClicks;

	public MsButtonEvent(final Display display,
			final int x, final int y, final int button, int numClicks) {
		super(display, x, y);
		this.button = button;
		this.numClicks = numClicks;
	}

	public int getButton() {
		return button;
	}

	public int getNumClicks() {
		return numClicks;
	}
}
