package imagej.ui.swing;

//: AWTKeyEventDispatcherGlobal.java
import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.ImageDisplay;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.event.Events;
import java.awt.*;
import java.awt.event.*;


/*
 * Intercept all keystrokes in the SystemEventQueue
 * and emit/publish new KyEvents on the EventBus...
 * using the currently selected ImageDisplay as the display in the KyEvent.

 * @author GBH
 */
public class AWTKeyEventDispatcherGlobal
		extends EventQueue {

	private static final boolean DEBUG = true; // BUG? what's that? ;-))
	private static final AWTKeyEventDispatcherGlobal instance = new AWTKeyEventDispatcherGlobal();

	static {
		// here we register ourselves as a new link in the chain of responsibility
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(instance);
	}

	private AWTKeyEventDispatcherGlobal() {
	} // One is enough - singleton

	public static AWTKeyEventDispatcherGlobal getInstance() {
		return instance;
	}

	@Override
	protected void dispatchEvent(AWTEvent event) {
		if (event instanceof KeyEvent) {
			// get the current display
			ImageDisplay display = ImageJ.get(DisplayService.class).getActiveImageDisplay();
			int eventId = event.getID();
			if (eventId == KeyEvent.KEY_TYPED) {
				keyTyped((KeyEvent) event, display);
			}
			if (eventId == KeyEvent.KEY_PRESSED) {
				keyPressed((KeyEvent) event, display);
			}
			if (eventId == KeyEvent.KEY_RELEASED) {
				keyReleased((KeyEvent) event, display);
			}
		}
		super.dispatchEvent(event); // let the next in chain handle event
	}

	public void keyTyped(final KeyEvent e, Display display) {
		Events.publish(new KyTypedEvent(display, e.getKeyChar(), e.getKeyCode(),
				e.getModifiers()));
	}

	public void keyPressed(final KeyEvent e, Display display) {
		Events.publish(new KyPressedEvent(display, e.getKeyChar(), e.getKeyCode(),
				e.getModifiers()));
	}

	public void keyReleased(final KeyEvent e, Display display) {
		Events.publish(new KyReleasedEvent(display, e.getKeyChar(),
				e.getKeyCode(), e.getModifiers()));
	}

}
