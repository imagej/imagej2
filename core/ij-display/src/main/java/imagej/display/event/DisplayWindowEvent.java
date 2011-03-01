package imagej.display.event;

import imagej.display.Display;

/**
 * An event indicating something has happened to a display window.
 *
 * @author Curtis Rueden
 */
public class DisplayWindowEvent extends DisplayEvent {

	public DisplayWindowEvent(final Display display) {
		super(display);
	}

}
