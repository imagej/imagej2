package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window was iconified.
 *
 * @author Curtis Rueden
 */
public class WinIconifiedEvent extends WinEvent {

	public WinIconifiedEvent(final Display display) {
		super(display);
	}

}
