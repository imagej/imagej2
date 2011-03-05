package imagej.display.event.window;

import imagej.display.Display;

/**
 * An event indicating a display window was deiconified.
 *
 * @author Curtis Rueden
 */
public class WinDeiconifiedEvent extends WinEvent {

	public WinDeiconifiedEvent(final Display display) {
		super(display);
	}

}
