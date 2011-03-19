package imagedisplay.zoom.core;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

/**
 * ZoomJPanel is the key class of the zoom widget. Whenever zooming happens, ZoomJPanel updates
 * canvas graphical properties (canvase size, zoomcenter pos and so on). With these properties,
 * ZoomScrollPane tracks and fixes the zoom center position on the screen.
 *
 * user space, zoomed space and device/space are defined in zoomjpanel
 * userspace is the abstract space defined by user (zoomed space with zoomfacor == 1)
 * zoomed space is the zoomed user space
 * device/canvas space is the windowed jpanel used to draw things
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public class ZoomJPanel extends JPanel implements ZoomableView {

    private float MAX_ZOOM_FACTOR_X = 16f;
    private float MIN_ZOOM_FACTOR_X = 0.25f;
    private float MAX_ZOOM_FACTOR_Y = 16f;
    private float MIN_ZOOM_FACTOR_Y = 0.25f;
    ZoomGraphics zg = new ZoomGraphics();
    double originX_zsp = 0;
    double originY_zsp = 0;
    double zcx = 0;
    double zcy = 0;
    Rectangle2D.Double zoomedSpace = null;
    boolean invalidBackground = true;

    public ZoomJPanel() {
        init();
    }

    /**
     * private initialization function
     */
    private void init() {
        originX_zsp = 0;
        originY_zsp = 0;
        zcx = 0;
        zcy = 0;
        zoomedSpace = null;
    }

    /**
     * paint
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        _paintBackground(g);
        _paintForeground(g);
    }

    /**
     * abatract function paint the background
     *
     */
    public void paintBackground(ZoomGraphics zg) {
    }

    /**
     * abstrcat function paint the front
     * @param zg
     */
    public void paintFront(ZoomGraphics zg) {
    }

    /**
     * making the background invalid. by doing so, the zoomed space (the canvas size) will be recalculated
     * and the back buffered image (if any) will be regenerated
     */
    public void invalidateBackground() {
        zoomedSpace = null;
        invalidBackground = true;
    }

    /**
     * paint the background
     */
    private void _paintBackground(Graphics g) {
        if (invalidBackground) { //background is invalid
            zg.setEAValidity(false);
            invalidBackBuff = true;
        }
        if (!this.backBufferEnabled) {
            paintOnPanelDirectly(g);
        } else {
            paintOnPanelIndirectly(g);
        }
        zg.setEAValidity(true);
        invalidBackground = false;
    }

    /**
     * back buffer image is not enabled and we draw directly on the canvas panel
     */
    private void paintOnPanelDirectly(Graphics g) {
        zg.setGraphics(g);
        Dimension size = this.getSize();
        zg.setZoomClipArea(
                originX_zsp / zg.getZoomFactorX(),
                originY_zsp / zg.getZoomFactorX(),
                (size.width) / zg.getZoomFactorX(),
                (size.height) / zg.getZoomFactorX());
        g.translate((int) (-originX_zsp), (int) (-originY_zsp));
        paintBackground(zg);
        zg.setGraphics(null);
    }

    /**
     * paint the fore ground
     * @param g
     */
    private void _paintForeground(Graphics g) {
        zg.setGraphics(g);

        if (this.backBufferEnabled) {
            g.translate((int) (-originX_zsp), (int) (-originY_zsp));
        }

        paintFront(zg);

        zg.setGraphics(null);
    }

    /**
     * set the orgin of the zoomjpanel on the zoomed space
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setOriginOnZoomedSpace(int x, int y) {
        piix = piix + (int) (x - originX_zsp);
        piiy = piiy + (int) (y - originY_zsp);
        originX_zsp = x;
        originY_zsp = y;
    }

    /**
     * get the origin of the zoomjpanel on the zoomed space
     * @return the origin position on the zoomed space
     */
    public Point getOriginOnZoomedSpace() {
        return new Point((int) originX_zsp, (int) originY_zsp);
    }
    private boolean pinSet = false;
    private double pinX = 0;
    private double pinY = 0;

    /**
     * set the pin position. when zooming happens, by default, the position of the zoom center
     * will be fixed on the screen. if you want another position instead of the zoom center to be
     * fixed, you can set it as a pin postition.
     *
     * @param x x coordinate of the pin
     * @param y y coordinate of the pin
     */
    public void setPin(int x, int y) {
        pinSet = true;
        pinX = x;
        pinY = y;
    }

    /**
     * get the current pin position
     * @return the current pin position
     */
    public Point2D.Double getPin() {
        return new Point2D.Double(pinX, pinY);
    }

    /**
     * disable the pin function
     */
    public void disablePin() {
        pinSet = false;
    }

    /**
     * set zoom paramemters
     * @param zoomcx zoom center x
     * @param zoomcy zoom center y
     * @param zfx       zoom factor x
     * @param zfy       zoom factor y
     * @return true if repainting the canvas required, otherwise false
     */
    public boolean setZoomParameters(double zoomcx, double zoomcy, double zfx, double zfy) {
        double oldzfx = zg.getZoomFactorX();
        double oldzfy = zg.getZoomFactorY();
        double newzcx = 0;
        double newzcy = 0;
        boolean zpChanged = _setZoomParameters(zfx, zfy);

        if (!pinSet) {
            newzcx = zoomcx;
            newzcy = zoomcy;
        } else {
            newzcx = pinX;
            newzcy = pinY;
        }

        if ((zcx != newzcx) || (zcy != newzcy)) {
            zpChanged = true;
        }

        zcx = newzcx;
        zcy = newzcy;

        if (zpChanged) {
            originX_zsp = (zcx * (zg.getZoomFactorX() - oldzfx)) + originX_zsp;
            originY_zsp = (zcy * (zg.getZoomFactorY() - oldzfy)) + originY_zsp;

            updateZoomedSpace();

            invalidBackBuff = true;
        }

        return zpChanged;
    }

    /**
     * update the zoomed space
     */
    private void updateZoomedSpace() {
        zoomedSpace = null;
        Rectangle2D.Double ea = zg.getEfficientArea();

        if (ea != null) {
            zoomedSpace = zg.getZoomedSpace(ea.x, ea.y, ea.width, ea.height);
        // System.out.println("zoomedSpace: " + zoomedSpace);
        } else {
            // System.out.println("ea is null");
        }
    }

    /**
     * is the zoom factor x valid?
     * @param zf zoom factor x
     * @return true if valid otherwise false
     */
    private double checkZoomFactorX(double zf) {
        if (zf < MIN_ZOOM_FACTOR_X) {
            zf = MIN_ZOOM_FACTOR_X;
        } else if (zf > MAX_ZOOM_FACTOR_X) {
            zf = MAX_ZOOM_FACTOR_X;
        }
        return zf;
    }

    /**
     * is the zoom factor y valid?
     * @param zf zoom factor y
     * @return true if valid otherwise false
     */
    private double checkZoomFactorY(double zf) {
        if (zf < MIN_ZOOM_FACTOR_Y) {
            zf = MIN_ZOOM_FACTOR_Y;
        } else if (zf > MAX_ZOOM_FACTOR_Y) {
            zf = MAX_ZOOM_FACTOR_Y;
        }
        return zf;
    }

    /**
     * set Zoom Parameters, private version, called by the
     * public one
     * @param zfx zoom factor x
     * @param zfy zoom facotr y
     */
    private boolean _setZoomParameters(double zfx, double zfy) {
        boolean zfChanged = false;
        double zzfx = zg.getZoomFactorX();
        double zzfy = zg.getZoomFactorY();

        zfx = checkZoomFactorX(zfx);
        zfy = checkZoomFactorY(zfy);
        zfChanged = ((zfx != zzfx) || (zfy != zzfy));

        zg.setZoomFactorX(zfx);
        zg.setZoomFactorY(zfy);

        return zfChanged;
    }

    /**
     * @return the zoom factor x
     */
    public double getZoomFactorX() {
        return zg.getZoomFactorX();
    }

    /**
     * @return the zoom factor y
     */
    public double getZoomFactorY() {
        return zg.getZoomFactorY();
    }

    /**
     * @return the zoom center
     */
    public Point2D.Double getZoomCenter() {
        return new Point2D.Double(zcx, zcy);
    }
    /**
     * @return the zoom space
     */    // GBH: Added margins constants
    static final int horizMargin = 0;
    static final int vertMargin = 0;

    public Rectangle getZoomedSpace() {
        if (zoomedSpace == null) {
            updateZoomedSpace();
        }
        if (zoomedSpace == null) {
            return null;
        } else {
            return new Rectangle(
                    (int) zoomedSpace.x - horizMargin,
                    (int) zoomedSpace.y - vertMargin,
                    (int) zoomedSpace.width + (horizMargin * 2),
                    (int) zoomedSpace.height + (vertMargin * 2));
        }
    }

    /**
     * change a position from device space (canvas space) to user space
     * @param x x coordinate
     * @param y y coordinate
     * @return the corresponding point within the user space
     */
    public Point2D.Double toUserSpace(int x, int y) {
        Point2D.Double p = new Point2D.Double(x, y);

        p.x += originX_zsp;
        p.y += originY_zsp;
        p = zg.toUserSpace(p);

        return p;
    }

    /**
     * change a position from device space (canvas space) to user space
     * @param p the point
     * @return the corresponding point within the user space
     */
    public Point2D.Double toUserSpace(Point p) {
        return toUserSpace(p.x, p.y);
    }

    /**
     * change a position from user space to device space
     * @param p the point
     * @return the corresponding point within the device space
     */
    public Point toDeviceSpace(Point2D.Double p) {
        p = zg.toZoomedSpace(p);
        return new Point((int) (p.x - originX_zsp), (int) (p.y - originY_zsp));
    }

    /**
     * zoom the specified rectangle area so that it fits zoomjpanel
     * @param r the rectangle
     */
    public boolean zoomRectangleToWholeWindow(Rectangle r) {
        double zf =
                Math.min(
                (getSize().width * getZoomFactorX()) / r.width,
                (getSize().height * getZoomFactorY()) / r.height);
        Point2D.Double p =
                getExpectedZoomCenter(
                new Point((int) r.getCenterX(), (int) r.getCenterY()),
                new Point(getSize().width / 2, getSize().height / 2),
                zf,
                zf);

        if (p != null) {
            return setZoomParameters(p.x, p.y, zf, zf);
        } else {
            return false;
        }
    }

    public Rectangle getROI() {
        return null;
    }

    public void setROI(Rectangle roi) {
        return;
    }

    /**
     * zoom the user space and makes it fits the zoomjpanel
     * @return true if repainting reuqired, otherwise false
     */
    public boolean fitToScreen() {
        restore();
        Rectangle2D.Double ea = zg.getEfficientArea();
        if (ea != null) {
            Rectangle r = new Rectangle((int) ea.x, (int) ea.y, (int) ea.width, (int) ea.height);
            return zoomRectangleToWholeWindow(r);
        } else {
            return false;
        }
    }

    /**
     * restore the zoomed space to its original status (zoom factor == 1) and
     * origin of the device space is on (0, 0) of the zoomed space
     */
    public void restore() {
        disablePin();
        init();
        zg.setZoomFactorX(1.0f);
        zg.setZoomFactorY(1.0f);
        invalidBackBuff = true;
    }
    //sorry,,,,,mathematics
    //x   initialPosInDeviceSpace
    //x_  zoomedPosInDeviceSpace

    /**
     * if a user wants to move a point from P to _P by setting zoom factors as
     * (zfx_, zfy_) where should the user set the zoom position?
     * @param p the original pos
     * @param p_ the destination
     * @param zfx_ zoom factor x
     * @param zfy_ zoom factor y
     */
    public Point2D.Double getExpectedZoomCenter(Point p, Point p_, double zfx_, double zfy_) {
        zfx_ = checkZoomFactorX(zfx_);
        zfy_ = checkZoomFactorY(zfy_);

        double zfx = getZoomFactorX();
        double zfy = getZoomFactorY();

        if ((zfx_ == zfx) || (zfy_ == zfy)) {
            return null;
        }

//        double zcx = 0;
//        double zcy = 0;

        zcx = (((p.x + originX_zsp) * zfx_) - (zfx * (p_.x + originX_zsp))) / zfx / (zfx_ - zfx);
        zcy = (((p.y + originY_zsp) * zfy_) - (zfy * (p_.y + originY_zsp))) / zfy / (zfy_ - zfy);

        return new Point2D.Double(zcx, zcy);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //// Back Buffer Image Support
    //// if back buffer image is ebaled. the user-defined paintBackground(ZoomGraphics zg) will
    //// be called only once and the background will be saved as a image. Next time,
    //// repainting happens, the paintBackground(..) will not be called. Instead, the saved
    //// image will be pasted on the canvas. By doing so, a lot of drawing time saved
    //// if back buffer image is disable, the paintBackground function will be called whenever
    //// repainting happens.
    ///  the user-defined paintFront function will be called everytime repainting happens no matter
    ///the back buffer image is enabled or not
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private int widthExpand = 100;
    private int heightExpand = 100;
    private int piix = 50; //jpanel offset in the saved image
    private int piiy = 50; //jpanel offset in the saved image
    private Image savedImage = null;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private boolean backBufferEnabled = false;
    private boolean invalidBackBuff = true;    //private Image vimage = null;
    private int expandDirectionX = 0;
    private int expandDirectionY = 0;

    /**
     * enable/disable the back buffer image
     * @param flag true the back buffer image is disabled. flase, enabled
     */
    public void alwaysDrawBackground(boolean flag) {
        backBufferEnabled = !flag;
    }

    /**
     * reset origin of the canvas on the "image space"
     */
    private void resetPIIXY() {
        switch (expandDirectionX) {
            case 0:
                piix = widthExpand / 2;
                //piiy = heightExpand / 2;
                break;
            case 1:
                piix = 0;
                //piiy = 0;
                break;
            case -1:
                piix = widthExpand;
                //piiy = heightExpand;
                break;
        }

        switch (expandDirectionY) {
            case 0:
                //piix = widthExpand / 2;
                piiy = heightExpand / 2;
                break;
            case 1:
                //piix = 0;
                piiy = 0;
                break;
            case -1:
                //piix = widthExpand;
                piiy = heightExpand;
                break;
        }
    }

    /**
     * do we need to re-generate the image?
     * @return true yes. false, no, the image is still usable
     */
    private boolean isImageValid() {
        return (!invalidBackBuff) && ((piix >= 0) && (piiy >= 0)) && (((piix + getWidth()) <= imageWidth) && ((piiy + getHeight()) <= imageHeight));
    }

    /**
     * reset the back buffer image
     */
    private void resetSavedImage() {
        if (savedImage != null) {
            savedImage.flush();
        }
        savedImage = null;
        imageWidth = getWidth() + widthExpand;
        imageHeight = getHeight() + heightExpand;
        savedImage = createImage(imageWidth, imageHeight);
    }

    /**
     * draw on the image.
     */
    private void drawOnImage() {
        Graphics2D g2 = (Graphics2D) savedImage.getGraphics();
        Color oldColor = g2.getColor();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, imageWidth, imageHeight);
        g2.setColor(oldColor);
        zg.setZoomClipArea(
                (originX_zsp - piix) / zg.getZoomFactorX(),
                (originY_zsp - piiy) / zg.getZoomFactorX(),
                (imageWidth) / zg.getZoomFactorX(),
                (imageHeight) / zg.getZoomFactorX());
        g2.translate(piix - originX_zsp, piiy - originY_zsp);
        zg.setGraphics(g2);
        paintBackground(zg);
        zg.setGraphics(null);
        g2.dispose();
    }

    /**
     * draw on the image and then, paste the image on the canvas
     */
    private void paintOnPanelIndirectly(Graphics g) {
        if ((savedImage != null) && isImageValid()) { //the image is still valid, draw it directly
            g.drawImage(savedImage, -piix, -piiy, this);
        } else {
            resetPIIXY();
            resetSavedImage();
            drawOnImage();
            invalidBackBuff = false;
            g.drawImage(savedImage, -piix, -piiy, this);
        }
    }
}
