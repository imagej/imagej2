package imagej.tool;

import java.awt.Cursor;

import imagej.plugin.display.Display;

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
	public void onKeyDown(int keyCode, int shift) {
		// do nothing by default
	}

	@Override
	public void onKeyUp(int keyCode, int shift) {
		// do nothing by default
	}

	@Override
	public void onMouseDown(int button, int shift, int x, int y) {
		// do nothing by default
	}

	@Override
	public void onMouseUp(int button, int shift, int x, int y) {
		// do nothing by default
	}

	@Override
	public void onMouseDoubleClick(int button, int shift, int x, int y) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(int button, int shift, int x, int y) {
		// do nothing by default
	}

}
