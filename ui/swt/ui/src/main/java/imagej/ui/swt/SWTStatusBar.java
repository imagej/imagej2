
package imagej.ui.swt;

import java.util.List;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;
import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * SWT implementation of {@link StatusBar}.
 *
 * @author Curtis Rueden
 */
public class SWTStatusBar extends Composite implements StatusBar {

	private final Label label;
	private final ProgressBar progressBar;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	public SWTStatusBar(final Composite parent, final EventService eventService) {
		super(parent, 0);
		setLayout(new MigLayout());
		label = new Label(this, 0);
		progressBar = new ProgressBar(this, 0);
		subscribers = eventService.subscribe(this);
	}

	@Override
	public void setStatus(final String message) {
		label.setText(message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		progressBar.setSelection(val);
		progressBar.setMaximum(max);
	}

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
