package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window has been activated.
 *
 * @author Curtis Rueden
 */
public class WinActivatedEvent extends WinEvent {

	public WinActivatedEvent(final Display display) {
		super(display);
	}

}
