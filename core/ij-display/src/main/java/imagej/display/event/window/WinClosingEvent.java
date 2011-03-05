package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window is in the process of closing.
 *
 * @author Curtis Rueden
 */
public class WinClosingEvent extends WinEvent {

	public WinClosingEvent(final Display display) {
		super(display);
	}

}
