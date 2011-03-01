package imagej.tool;

import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import java.awt.Cursor;

import imagej.display.Display;

/**
 * An empty {@link ITool}, to simplify tool implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractTool implements ITool {

	protected Display display;

	@Override
	public void activate(final Display d) {
		display = d;
	}

	@Override
	public void deactivate() {
		// do nothing by default
	}

	@Override
	public int getCursor() {
		return Cursor.DEFAULT_CURSOR;
	}

	@Override
	public void onKeyDown(KyPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onKeyUp(KyReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDown(MsPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseUp(MsReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseClicked(MsClickedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(MsMovedEvent evt) {
		// do nothing by default
	}
}
