package imagej.display;

//import imagej.gui.swing.display.NavigableImagePanel.ZoomDevice;
import java.awt.Point;

/**
 *
 * @author GBH
 */
public interface NavigableImageCanvas extends ImageCanvas, ToolEnabled{

	/*** This is an interface extracted from NavigableImagePanel
	 *  GBH, March 17, 2011
	 */

	// set EventDispatcher as event listener on this - esp. for key, mouse events
	void addEventDispatcher(EventDispatcher dispatcher);
	
	/**
	 * <p>Identifies a change to the zoom increment.</p>
	 */
	String ZOOM_INCREMENT_CHANGED_PROPERTY = "zoomIncrement";
	/**
	 * <p>Identifies a change to the zoom level.</p>
	 */
	String ZOOM_LEVEL_CHANGED_PROPERTY = "zoomLevel";

	/**
	 * <p>Gets the image origin.</p>
	 * <p>Image origin is defined as the upper, left corner of the image in
	 * the panel's coordinate system.</p>
	 * @return the point of the upper, left corner of the image in the panel's coordinates
	 * system.
	 */
	Point getImageOrigin();

	/**
	 * <p>Gets the current zoom level.</p>
	 *
	 * @return the current zoom level
	 */
	double getZoom();

	/**
	 * <p>Gets the current zoom device.</p>
	 */
	// ZoomDevice getZoomDevice();

	/**
	 * <p>Gets the current zoom increment.</p>
	 *
	 * @return the current zoom increment
	 */
	double getZoomIncrement();

	/**
	 * <p>Indicates whether the high quality rendering feature is enabled.</p>
	 *
	 * @return true if high quality rendering is enabled, false otherwise.
	 */
	boolean isHighQualityRenderingEnabled();

	/**
	 * <p>Indicates whether navigation image is enabled.<p>
	 *
	 * @return true when navigation image is enabled, false otherwise.
	 */
	boolean isNavigationImageEnabled();

	/**
	 * Pans the image by the given (X, Y) amount.
	 */
	void pan(int xDelta, int yDelta);

	/**
	 * <p>Enables/disables high quality rendering.</p>
	 *
	 * @param enabled enables/disables high quality rendering
	 */
	void setHighQualityRenderingEnabled(boolean enabled);

	/**
	 * <p>Sets the image origin.</p>
	 * <p>Image origin is defined as the upper, left corner of the image in
	 * the panel's coordinate system. After a new origin is set, the image is repainted.
	 * This method is used for programmatic image navigation.</p>
	 * @param x the x coordinate of the new image origin
	 * @param y the y coordinate of the new image origin
	 */
	void setImageOrigin(int x, int y);

	/**
	 * <p>Sets the image origin.</p>
	 * <p>Image origin is defined as the upper, left corner of the image in
	 * the panel's coordinate system. After a new origin is set, the image is repainted.
	 * This method is used for programmatic image navigation.</p>
	 * @param newOrigin the value of a new image origin
	 */
	void setImageOrigin(Point newOrigin);

	/**
	 * <p>Enables/disables navigation with the navigation image.</p>
	 * <p>Navigation image should be disabled when custom, programmatic navigation
	 * is implemented.</p>
	 *
	 * @param enabled true when navigation image is enabled, false otherwise.
	 */
	void setNavigationImageEnabled(boolean enabled);

	/**
	 * <p>Sets the zoom level used to display the image.</p>
	 * <p>This method is used in programmatic zooming. The zooming center is
	 * the point of the image closest to the center of the panel.
	 * After a new zoom level is set the image is repainted.</p>
	 *
	 * @param newZoom the zoom level used to display this panel's image.
	 */
	void setZoom(double newZoom);

	/**
	 * <p>Sets the zoom level used to display the image, and the zooming center,
	 * around which zooming is done.</p>
	 * <p>This method is used in programmatic zooming.
	 * After a new zoom level is set the image is repainted.</p>
	 *
	 * @param newZoom the zoom level used to display this panel's image.
	 */
	void setZoom(double newZoom, Point zoomingCenter);

	/**
	 * <p>Sets a new zoom device.</p>
	 *
	 * @param newZoomDevice specifies the type of a new zoom device.
	 */
	//void setZoomDevice(ZoomDevice newZoomDevice);

	/**
	 * <p>Sets a new zoom increment value.</p>
	 *
	 * @param newZoomIncrement new zoom increment value
	 */
	void setZoomIncrement(double newZoomIncrement);

	// Is this point in the image as displayed in the panel
	boolean isInImage(Point p);

	//Converts this panel's coordinates into the original image coordinates
	Coords panelToImageCoords(Point p);

	//Converts the original image coordinates into this panel's coordinates
	Coords imageToPanelCoords(Coords p);
}
