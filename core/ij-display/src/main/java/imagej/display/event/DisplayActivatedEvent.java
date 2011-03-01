package imagej.display.event;

import imagej.display.Display;

/**
 * A event indicating a display has been activated.
 *
 * @author Curtis Rueden
 */
public class DisplayActivatedEvent extends DisplayWindowEvent {

	public DisplayActivatedEvent(final Display display) {
		super(display);
	}

}
