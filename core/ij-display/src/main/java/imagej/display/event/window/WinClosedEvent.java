package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window has been closed.
 *
 * @author Curtis Rueden
 */
public class WinClosedEvent extends WinEvent {

	public WinClosedEvent(final Display display) {
		super(display);
	}

}
