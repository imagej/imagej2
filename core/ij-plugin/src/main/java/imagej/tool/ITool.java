package imagej.tool;

// Credit http://edndoc.esri.com/arcobjects/9.2/java/api/arcobjects/com/esri/arcgis/systemUI/ITool.html

public interface ITool {

	//Causes the tool to be the active tool.
	boolean activate();

	//Causes the tool to no longer be the active tool.
	boolean deactivate();

	// The mouse pointer for this tool.
	int getCursor();

	// Context menu event occured at the given xy location.
	boolean onContextMenu(int x, int y);

	// Occurs when a mouse button is double clicked when this tool is active.
	void onDblClick();

	// Occurs when a key on the keyboard is pressed when this tool is active.
	void onKeyDown(int keyCode, int shift);

	// Occurs when a key on the keyboard is released when this tool is active.
	void onKeyUp(int keyCode, int shift);

	// Occurs when a mouse button is pressed when this tool is active.
	void onMouseDown(int button, int shift, int x, int y);

	// Occurs when the mouse is moved when this tool is active.
	void onMouseMove(int button, int shift, int x, int y);

	// Occurs when a mouse button is released when this tool is active.
	void onMouseUp(int button, int shift, int x, int y);
}
