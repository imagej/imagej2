package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window has been opened.
 *
 * @author Curtis Rueden
 */
public class WinOpenedEvent extends WinEvent {

	public WinOpenedEvent(final Display display) {
		super(display);
	}

}
