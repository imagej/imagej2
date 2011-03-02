package imagej.tool;

import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;

/**
 * Interface for ImageJ tools. While it possible to create a tool
 * merely by implementing this interface, it is encouraged to instead
 * extend {@link BaseTool}, for convenience.
 *
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ITool {

	/** Gets the tool entry associated with the tool. */
	ToolEntry getToolEntry();

	/** Sets the tool entry associated with the tool. */
	void setToolEntry(final ToolEntry entry);

	/** Gets the unique name of the tool. */
	String getName();

	/** Gets the human-readable label for the tool. */
	String getLabel();

	/** Gets a string describing the tool in detail. */
	String getDescription();

	/** The tool's mouse pointer. */
	int getCursor();

	/** Informs the tool that it is now active. */
	void activate();

	/** Informs the tool that it is no longer active. */
	void deactivate();

	/** Occurs when a key on the keyboard is pressed when the tool is active. */
	void onKeyDown(KyPressedEvent evt);

	/** Occurs when a key on the keyboard is released when the tool is active. */
	void onKeyUp(KyReleasedEvent evt);

	/** Occurs when a mouse button is pressed when the tool is active. */
	void onMouseDown(MsPressedEvent evt);

	/** Occurs when a mouse button is released when the tool is active. */
	void onMouseUp(MsReleasedEvent evt);

	/** Occurs when a mouse button is double clicked when the tool is active. */
	void onMouseClicked(MsClickedEvent evt);

	/** Occurs when the mouse is moved when the tool is active. */
	void onMouseMove(MsMovedEvent evt);

	/** Occurs when the mouse is dragged when the tool is active. */
	void onMouseDrag(MsDraggedEvent evt);

}
