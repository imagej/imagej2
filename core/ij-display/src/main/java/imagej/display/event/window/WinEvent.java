package imagej.display.event.window;

import imagej.display.Display;
import imagej.display.event.DisplayEvent;

/**
 * An event indicating something has happened to a display window.
 *
 * @author Curtis Rueden
 */
public class WinEvent extends DisplayEvent {

	public WinEvent(final Display display) {
		super(display);
	}

}
