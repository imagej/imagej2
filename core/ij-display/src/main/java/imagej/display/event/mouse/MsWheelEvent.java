package imagej.display.event.mouse;

import imagej.display.Display;

/**
 * An event indicating a mouse wheel was moved in a display.
 *
 * @author Curtis Rueden
 */
public class MsWheelEvent extends MsEvent {

	private int wheelRotation;

	public MsWheelEvent(final Display display,
		final int x, final int y, final int wheelRotation)
	{
		super(display, x, y);
		this.wheelRotation = wheelRotation;
	}

	public int getWheelRotation() {
		return wheelRotation;
	}

}
