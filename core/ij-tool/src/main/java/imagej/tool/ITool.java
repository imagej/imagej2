package imagej.tool;

import imagej.display.Display;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;

/**
 * Interface for ImageJ tools. A tool is a collection of rules binding
 * user input (e.g., keyboard and mouse events) to display and data
 * manipulation in a coherent way. 
 *
 * For example, a {@link PanTool} might pan a display when the mouse is dragged
 * or arrow key is pressed, while a {@link PencilTool} could draw hard lines
 * on the data within a display.
 *
 * Portions of this interface were inspired by the
 * <a href="http://edndoc.esri.com/arcobjects/9.2/java/api/arcobjects/com/esri/arcgis/systemUI/ITool.html">ArcGIS ArcObjects ITool interface</a>.
 *
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ITool {

	/** Informs the tool that it is now active for the given display. */
	void activate(Display display);

	/** Informs the tool that it is no longer active. */
	void deactivate();

	/** The tool's mouse pointer. */
	int getCursor();

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

}
