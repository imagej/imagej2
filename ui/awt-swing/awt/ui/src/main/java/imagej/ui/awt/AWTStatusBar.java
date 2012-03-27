
package imagej.ui.awt;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;

import java.awt.Graphics;
import java.awt.Label;
import java.util.List;

/**
 * AWT implementation of {@link StatusBar}.
 *
 * @author Curtis Rueden
 */
public class AWTStatusBar extends Label implements StatusBar {

	private int value;
	private int maximum;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	public AWTStatusBar() {
		subscribers = ImageJ.get(EventService.class).subscribe(this);
	}

	// -- Component methods --

	@Override
	public void paint(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		final int pix = maximum > 0 ? value * width / maximum : 0;
		g.setColor(getForeground());
		g.fillRect(0, 0, pix, height);
		g.setColor(getBackground());
		g.fillRect(pix, 0, width, height);
		super.paint(g);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		setText(message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		value = val;
		maximum = max;
		repaint();
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
