package imagej.display.event;

import imagej.display.Display;
import imagej.event.ImageJEvent;

/**
 * A event indicating something has happened to a display.
 *
 * @author Curtis Rueden
 */
public class DisplayEvent extends ImageJEvent {

	private Display display;

	public DisplayEvent(final Display display) {
		this.display = display;
	}

	public Display getDisplay() {
		return display;
	}

}
