package imagej.tool;

import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;

import java.awt.Cursor;

/**
 * Base class for ImageJ tools. A tool is a collection of rules binding
 * user input (e.g., keyboard and mouse events) to display and data
 * manipulation in a coherent way. 
 *
 * For example, a {@link PanTool} might pan a display when the mouse is dragged
 * or arrow key is pressed, while a {@link PencilTool} could draw hard lines
 * on the data within a display.
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public abstract class BaseTool implements ITool {

	private ToolEntry entry;

	@Override
	public ToolEntry getToolEntry() {
		return entry;
	}

	@Override
	public void setToolEntry(final ToolEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public String getLabel() {
		return entry.getLabel();
	}

	@Override
	public String getDescription() {
		return entry.getDescription();
	}

	@Override
	public int getCursor() {
		return Cursor.DEFAULT_CURSOR;
	}

	@Override
	public void activate() {
		// do nothing by default
	}

	@Override
	public void deactivate() {
		// do nothing by default
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
	public void onMouseClick(MsClickedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(MsMovedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDrag(MsDraggedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseWheel(MsWheelEvent evt) {
		// do nothing by default
	}

}
