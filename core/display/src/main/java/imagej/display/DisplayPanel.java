/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.display;

/**
 *
 * @author GBH
 */
public interface DisplayPanel {
	/**
	 * 
	 * @return
	 */
	Display getDisplay();
	/**
	 * Handle keyboard, mouse and window events.
	 * @param dispatcher
	 */
	void addEventDispatcher(EventDispatcher dispatcher);

	/**
	 *
	 */
	void close();

	void makeActive();

	/**
	 * Rebuilds the display window to reflect the display's current views,
	 * dimensional lengths, etc. The window may change size, and hence may repack
	 * itself.
	 */
	void redoLayout();

	/**
	 *
	 * @param s
	 */
	void setLabel(String s);

	/**
	 * Sets the title of the  top-level component containing this panel.
	 *
	 * @param s
	 */
	void setTitle(String s);

	/**
	 * Updates the display window to reflect the display's current position.
	 */
	void update();
	
}
