package imagedisplay.zoom.core;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 *
 * @author GBH
 */
public interface ZoomableView {

	/**
	 * enable/disable the back buffer image
	 * @param flag true the back buffer image is disabled. flase, enabled
	 */
	void alwaysDrawBackground(boolean flag);

	/**
	 * disable the pin function
	 */
	void disablePin();

	/**
	 * if a user wants to move a point from P to _P by setting zoom factors as
	 * (zfx_, zfy_) where should the user set the zoom position?
	 * @param p the original pos
	 * @param p_ the destination
	 * @param zfx_ zoom factor x
	 * @param zfy_ zoom factor y
	 */
	Point2D.Double getExpectedZoomCenter(Point p, Point p_, double zfx_, double zfy_);

	/**
	 * get the origin of the zoomjpanel on the zoomed space
	 * @return the origin position on the zoomed space
	 */
	Point getOriginOnZoomedSpace();

	Rectangle getROI();

	/**
	 * @return the zoom center
	 */
	Point2D.Double getZoomCenter();

	/**
	 * @return the zoom factor x
	 */
	double getZoomFactorX();

	/**
	 * @return the zoom factor y
	 */
	double getZoomFactorY();

	Rectangle getZoomedSpace();

	/**
	 * making the background invalid. by doing so, the zoomed space (the canvas size) will be recalculated
	 * and the back buffered image (if any) will be regenerated
	 */
	void invalidateBackground();

	/**
	 * paint
	 * @param g
	 */
	void paint(Graphics g);

	/**
	 * abatract function paint the background
	 *
	 */
	void paintBackground(ZoomGraphics zg);

	/**
	 * abstrcat function paint the front
	 * @param zg
	 */
	void paintFront(ZoomGraphics zg);

	/**
	 * restore the zoomed space to its original status (zoom factor == 1) and
	 * origin of the device space is on (0, 0) of the zoomed space
	 */
	void restore();

	/**
	 * set the orgin of the zoomjpanel on the zoomed space
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	void setOriginOnZoomedSpace(int x, int y);

	/**
	 * set the pin position. when zooming happens, by default, the position of the zoom center
	 * will be fixed on the screen. if you want another position instead of the zoom center to be
	 * fixed, you can set it as a pin postition.
	 *
	 * @param x x coordinate of the pin
	 * @param y y coordinate of the pin
	 */
	void setPin(int x, int y);

	void setROI(Rectangle roi);

	/**
	 * set zoom paramemters
	 * @param zoomcx zoom center x
	 * @param zoomcy zoom center y
	 * @param zfx       zoom factor x
	 * @param zfy       zoom factor y
	 * @return true if repainting the canvas required, otherwise false
	 */
	boolean setZoomParameters(double zoomcx, double zoomcy, double zfx, double zfy);

	/**
	 * change a position from user space to device space
	 * @param p the point
	 * @return the corresponding point within the device space
	 */
	Point toDeviceSpace(Point2D.Double p);

	/**
	 * change a position from device space (canvas space) to user space
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return the corresponding point within the user space
	 */
	Point2D.Double toUserSpace(int x, int y);

	/**
	 * change a position from device space (canvas space) to user space
	 * @param p the point
	 * @return the corresponding point within the user space
	 */
	Point2D.Double toUserSpace(Point p);

	/**
	 * zoom the specified rectangle area so that it fits zoomjpanel
	 * @param r the rectangle
	 */
	boolean zoomRectangleToWholeWindow(Rectangle r);

}
