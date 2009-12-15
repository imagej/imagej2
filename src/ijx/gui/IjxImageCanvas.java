// IJX: ImageJ Interface
package ijx.gui;

import ijx.IjxImagePlus;

import java.awt.BasicStroke;
import java.awt.Color;
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
 **/
public interface IjxImageCanvas extends Cloneable, MouseListener, MouseMotionListener {

    public Graphics getGraphics();

    /**
     * Disable/enable popup menu.
     */
    void disablePopupMenu(boolean status);

    void fitToWindow();

    Rectangle getBounds();

    /**
     * Returns the current cursor location in image coordinates.
     */
    Point getCursorLoc();

    Vector getDisplayList();

    double getMagnification();

    /**
     * Returns the mouse event modifiers.
     */
    int getModifiers();

    Dimension getPreferredSize();

    boolean getShowAllROIs();

    int getSliceNumber(String label);

    Rectangle getSrcRect();

//    void mouseClicked(MouseEvent e);
//
//    void mouseDragged(MouseEvent e);
//
//    void mouseEntered(MouseEvent e);
//
//    void mouseExited(MouseEvent e);
//
//    void mouseMoved(MouseEvent e);
//
//    void mousePressed(MouseEvent e);
//
//    void mouseReleased(MouseEvent e);

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

    void paint(Graphics g);

    void repaint();

    void repaint(int x, int y, int w, int h);

    void resizeCanvas(int w, int h);
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

    void setCursor(java.awt.Cursor cursor);

    void setDisplayList(Vector list);

    void setDisplayList(Shape shape, Color color, BasicStroke stroke);

    void setDrawingSize(int width, int height);

    /**
     * ImagePlus.updateAndDraw calls this method to get paint
     * to update the image from the ImageProcessor.
     */
    void setImageUpdated();
    
    void setMaxBounds();

    void setSrcRect(Rectangle rect);

    void setMagnification(double magnification);

    void setMagnification2(double magnification);

    void setShowAllROIs(boolean showAllROIs);

    /**
     * Called by IJ.showStatus() to prevent status bar text from
     * being overwritten until the cursor moves at least 12 pixels.
     */
    void setShowCursorStatus(boolean status);

    /**
     * Implements the Image/Zoom/Original Scale command.
     */
    void unzoom();

    void update(Graphics g);
    
    void update(IjxImageCanvas ic);

    void updateImage(IjxImagePlus imp);

    /**
     * Implements the Image/Zoom/View 100% command.
     */
    void zoom100Percent();

    /**
     * Zooms in by making the window bigger. If it can't
     * be made bigger, then make the source rectangle
     * (srcRect) smaller and center it at (x,y).
     */
    void zoomIn(int x, int y);

    /**
     * Zooms out by making the source rectangle (srcRect)
     * larger and centering it on (x,y). If we can't make it larger,
     * then make the window smaller.
     */
    void zoomOut(int x, int y);

}
