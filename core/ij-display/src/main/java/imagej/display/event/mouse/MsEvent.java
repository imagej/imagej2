package imagej.display.event.mouse;

import imagej.display.Display;
import imagej.display.event.DisplayEvent;

/**
 * An event indicating mouse activity in a display.
 *
 * @author Curtis Rueden
 */
public class MsEvent extends DisplayEvent {

	private int x, y;

	public MsEvent(final Display display, final int x, final int y) {
		super(display);
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
