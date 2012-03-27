
package imagej.ui.swing.mdi;

import imagej.event.EventService;
import imagej.event.ImageJEvent;
import imagej.ext.display.Display;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.display.event.window.WinClosedEvent;
import imagej.ext.display.event.window.WinClosingEvent;
import imagej.ext.display.event.window.WinDeactivatedEvent;
import imagej.ext.display.event.window.WinDeiconifiedEvent;
import imagej.ext.display.event.window.WinIconifiedEvent;
import imagej.ext.display.event.window.WinOpenedEvent;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Rebroadcasts AWT internal frame events as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class InternalFrameEventDispatcher implements InternalFrameListener {

	private final Display<?> display;
	private final EventService eventService;

	/** Creates an AWT event dispatcher for the given display. */
	public InternalFrameEventDispatcher(final Display<?> display,
		final EventService eventService)
	{
		this.eventService = eventService;
		this.display = display;
	}

	// -- InternalFrameListener methods --

	@Override
	public void internalFrameActivated(final InternalFrameEvent e) {
		eventService.publish(new WinActivatedEvent(display));
	}

	@Override
	public void internalFrameClosed(final InternalFrameEvent e) {
		eventService.publish(new WinClosedEvent(display));
	}

	@Override
	public void internalFrameClosing(final InternalFrameEvent e) {
		eventService.publish(new WinClosingEvent(display));
	}

	@Override
	public void internalFrameDeactivated(final InternalFrameEvent e) {
		eventService.publish(new WinDeactivatedEvent(display));
	}

	@Override
	public void internalFrameDeiconified(final InternalFrameEvent e) {
		eventService.publish(new WinDeiconifiedEvent(display));
	}

	@Override
	public void internalFrameIconified(final InternalFrameEvent e) {
		eventService.publish(new WinIconifiedEvent(display));
	}

	@Override
	public void internalFrameOpened(final InternalFrameEvent e) {
		eventService.publish(new WinOpenedEvent(display));
	}

}
