package imagej.gui.swing;

import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Simple status bar with text area and progress bar, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class StatusBar extends JPanel implements EventSubscriber<StatusEvent> {

	private final JProgressBar progressBar;

	public StatusBar() {
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		setLayout(new BorderLayout());
		add(progressBar, BorderLayout.CENTER);
		Events.subscribe(StatusEvent.class, this);
	}

	public void setStatus(final String message) {
		progressBar.setString(message == null ? "" : message);
	}

	public void setProgress(final int val, final int max) {
		if (val >= 0 && val < max) {
			progressBar.setValue(val);
			progressBar.setMaximum(max);
		}
		else {
			progressBar.setValue(0);
			progressBar.setMaximum(1);
		}
	}

	@Override
	public void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
