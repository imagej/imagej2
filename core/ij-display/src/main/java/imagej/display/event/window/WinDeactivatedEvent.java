package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window has been deactivated.
 *
 * @author Curtis Rueden
 */
public class WinDeactivatedEvent extends WinEvent {

	public WinDeactivatedEvent(final Display display) {
		super(display);
	}

}
