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
	private final boolean isPopupTrigger;

	public MsButtonEvent(final Display display, final int x, final int y,
		final int button, final int numClicks, final boolean isPopupTrigger)
	{
		super(display, x, y);
		this.button = button;
		this.numClicks = numClicks;
		this.isPopupTrigger = isPopupTrigger;
	}

	public int getButton() {
		return button;
	}

	public int getNumClicks() {
		return numClicks;
	}

	public boolean isPopupTrigger() {
		return isPopupTrigger;
	}

}
