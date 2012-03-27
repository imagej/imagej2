
package imagej.ui.pivot;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;

import java.util.List;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;

/**
 * Status bar with text area and progress bar, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public final class PivotStatusBar extends BoxPane implements StatusBar {

	private final Label label;
	private final Meter meter;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	public PivotStatusBar(final EventService eventService) {
		label = new Label();
		add(label);
		meter = new Meter();
		add(meter);
		subscribers = eventService.subscribe(this);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		label.setText(message == null ? "" : message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		if (val >= 0 && val < max) {
			meter.setPercentage((double) val / max);
		}
		else {
			meter.setPercentage(0);
		}
	}
	
	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
