package ijx.gui;

import ij.gui.Overlay;
import ij.gui.Roi;
import ijx.IjxImagePlus;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

/**
 * IjxImageCanvas: interface for a generalized Image Canvas in ImageJ
 *
 * Refactored from ImageJ by Grant B. Harris, November 2008, at ImageJ 2008, Luxembourg
 * and again, Sept 2010
 *
 * @author GBH <imagejdev.org>
 */
public interface IjxImageCanvas extends Cloneable, MouseListener, MouseMotionListener {

    Component getCanvas();
    /**
     * Disable/enable popup menu.
     */
    void disablePopupMenu(boolean status);

    void fitToWindow();

    public Rectangle getBounds();

    /**
     * Returns the current cursor location in image coordinates.
     */
    Point getCursorLoc();

    /**
     * @deprecated
     * replaced by IjxImagePlus.getOverlay()
     */
     Vector getDisplayList();

    /**
     * Returns the IjxImagePlus object that is associated with this ImageCanvas.
     */
    IjxImagePlus getImage();

    double getMagnification();

    /**
     * Returns the mouse event modifiers.
     */
    int getModifiers();

    /**
     * Use IjxImagePlus.getOverlay().
     */
    Overlay getOverlay();

    //Dimension getPreferredSize();

    /**
     * Return the ROI Manager "Show All" list as an overlay.
     */
    Overlay getShowAllList();

    /**
     * Returns the state of the ROI Manager "Show All" flag.
     */
    boolean getShowAllROIs();

    int getSliceNumber(String label);

    Rectangle getSrcRect();

    /**
     * Converts a screen x-coordinate to an offscreen x-coordinate.
     */
    int offScreenX(int sx);

    /**
     * Converts a screen x-coordinate to a floating-point offscreen x-coordinate.
     */
    double offScreenXD(int sx);

    /**
     * Converts a screen y-coordinate to an offscreen y-coordinate.
     */
    int offScreenY(int sy);

    /**
     * Converts a screen y-coordinate to a floating-point offscreen y-coordinate.
     */
    double offScreenYD(int sy);

    /**
     * Enlarge the canvas if the user enlarges the window.
     */
    void resizeCanvas(int width, int height);

    /**
     * Converts an offscreen x-coordinate to a screen x-coordinate.
     */
    int screenX(int ox);

    /**
     * Converts a floating-point offscreen x-coordinate to a screen x-coordinate.
     */
    int screenXD(double ox);

    /**
     * Converts an offscreen y-coordinate to a screen y-coordinate.
     */
    int screenY(int oy);

    /**
     * Converts a floating-point offscreen x-coordinate to a screen x-coordinate.
     */
    int screenYD(double oy);

    /**
     * Sets the cursor based on the current tool and cursor location.
     */
    void setCursor(int sx, int sy, int ox, int oy);

    /**
     * Allows plugins (e.g., Orthogonal_Views) to create a custom ROI using a display list.
     */
    void setCustomRoi(boolean customRoi);

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(ij.gui.Overlay)
     */
     void setDisplayList(Vector list);

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(Shape, Color, BasicStroke)
     */
     void setDisplayList(Shape shape, Color color, BasicStroke stroke);

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(Roi, Color, int, Color)
     */
    void setDisplayList(Roi roi, Color color);

    void setDrawingSize(int width, int height);

    /**
     * IjxImagePlus.updateAndDraw calls this method to get paint
     * to update the image from the ImageProcessor.
     */
    void setImageUpdated();

    void setMagnification(double magnification);

    void setMagnification2(double magnification);

    void setMaxBounds();

    /**
     * Use IjxImagePlus.setOverlay(ij.gui.Overlay).
     */
    void setOverlay(Overlay overlay);

    /**
     * Enables/disables the ROI Manager "Show All" mode.
     */
    void setShowAllROIs(boolean showAllROIs);

    /**
     * Called by IJ.showStatus() to prevent status bar text from
     * being overwritten until the cursor moves at least 12 pixels.
     */
    void setShowCursorStatus(boolean status);

    void setSourceRect(Rectangle r);

    void setSrcRect(Rectangle srcRect);

    /**
     * Update this ImageCanvas to have the same zoom and scale settings as the one specified.
     */
    void update(IjxImageCanvas icX);

   // void update(Graphics g);

    public void repaint();

    public void repaint(int x, int y, int width, int height);

    public void setCursor(Cursor cursor);

    //public Graphics getGraphics();

    void updateImage(IjxImagePlus imp);

    /**
     * Implements the Image/Zoom/Original Scale command.
     */
    void unzoom();

    /**
     * Implements the Image/Zoom/View 100% command.
     */
    void zoom100Percent();

    /**
     * Zooms in by making the window bigger. If it can't
     * be made bigger, then make the source rectangle
     * (srcRect) smaller and center it at (sx,sy). Note that
     * sx and sy are screen coordinates.
     */
    void zoomIn(int sx, int sy);

    /**
     * Zooms out by making the source rectangle (srcRect)
     * larger and centering it on (x,y). If we can't make it larger,
     * then make the window smaller.
     */
    void zoomOut(int x, int y);

  //  public void paint(Graphics g);
}
