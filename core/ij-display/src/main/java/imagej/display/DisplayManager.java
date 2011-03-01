package imagej.display;

import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.ImageJEvent;

/**
 *
 * @author Grant Harris
 */


public class DisplayManager implements EventSubscriber<DisplayEvent> {
	private Display activeDisplay;

	public DisplayManager() {
		Events.subscribe(DisplayEvent.class, this);
	}

	@Override
	public void onEvent(DisplayEvent event) {
		if(event instanceof DisplayActivatedEvent) {
			activeDisplay = event.getDisplay();


		}
	}


}
