/*
 * $RCSfile: DisplayJAI.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:57:03 $
 * $State: Exp $
 */
package imagedisplay;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A Java<sup>TM</sup> Foundation Classes {@link JPanel} which is able
 * to contain an image.  The image and its container may have different
 * sizes. The image may also be positioned within the container.
 *
 * <p>This class extends <code>JPanel</code> in order to support layout
 * management. Image tiling is supported as of version 1.3 of the
 * Java<sup>TM</sup> 2 Platform via
 * {@link Graphics2D#drawRenderedImage(RenderedImage,AffineTransform)}.</p>
 *
 * <p><i>This class is not a committed part of the Java<sup>TM</sup>
 * Advanced Imaging API per se.  It might therefore not be supported by
 * JAI implementations other than that of Sun Microsystems, Inc.</i></p>
 *
 * @see java.awt.Graphics2D
 * @see java.awt.image.RenderedImage
 * @see javax.swing.JPanel
 * @see javax.swing.JComponent
 */
public class DisplayJAI extends JPanel
                        implements MouseListener,
                                   MouseMotionListener {

    /** The image to display. */
    protected RenderedImage source = null;

    /** Abscissa of image origin relative to panel origin. */
    protected int originX = 0;

    /** Ordinate of image origin relative to panel origin. */
    protected int originY = 0;

    /**
     * Constructs a <code>DisplayJAI</code> and sets its
     * the layout to <code>null</code>.
     */
    public DisplayJAI() {
        super();
        setLayout(null);
    }

    /**
     * Constructs a <code>DisplayJAI</code>, sets its layout to
     * <code>null</code>, and sets its displayed image.
     *
     * <p>The preferred size is set such that its width is the image
     * width plus the left and right insets and its height is the image
     * height plus the top and bottom insets.</p>
     *
     * @param image The image to display.
     * @throws IllegalArgumentException if <code>image</code> is
     * <code>null</code>.
     */
    public DisplayJAI(RenderedImage image) {
        super();
        setLayout(null);

        if(image == null) {
            throw new IllegalArgumentException("image == null!");
        }

        source = image;

        // Swing geometry
        int w = source.getWidth();
        int h = source.getHeight();
        Insets insets = getInsets();
        Dimension dim = new Dimension(w + insets.left + insets.right,
                                      h + insets.top + insets.bottom);


        setPreferredSize(dim);
    }

    /**
     * Moves the image within it's container. The instance variables
     * <code>originX</code> and <code>originY</code> are set to the
     * parameter valus <code>x</code> and <code>y</code>, respectively.
     * {@link #repaint()} is invoked after the origin values are changed.
     *
     * @param x image origin abscissa.
     * @param y image origin ordinate.
     */
    public void setOrigin(int x, int y) {
        originX = x;
        originY = y;
        repaint();
    }

    /** Retrieves the image origin. */
    public Point getOrigin() {
        return new Point(originX, originY);
    }

    /**
     * Sets a new image to display.
     *
     * <p>The preferred size is set such that its width is the image
     * width plus the left and right insets and its height is the image
     * height plus the top and bottom insets. {@link #revalidate()} and
     * {@link #repaint()} are invoked after all other changes.</p>
     *
     * @param im The image to display.
     * @throws IllegalArgumentException if <code>im</code> is
     * <code>null</code>.
     */
    public void set(RenderedImage im) {
        if(im == null) {
            throw new IllegalArgumentException("im == null!");
        }

        source = im;

        // Swing geometry
        int w = source.getWidth();
        int h = source.getHeight();
        Insets insets = getInsets();
        Dimension dim = new Dimension(w + insets.left + insets.right,
                                      h + insets.top + insets.bottom);


        setPreferredSize(dim);
        revalidate();
        repaint();
    }

    /**
     * Sets a new image to display and its coordinates within the container.
     *
     * <p>The preferred size is set such that its width is the image
     * width plus the left and right insets and its height is the image
     * height plus the top and bottom insets. {@link #revalidate()} and
     * {@link #repaint()} are invoked after all other changes.</p>
     *
     * @param im The image to display.
     * @param x image origin abscissa.
     * @param y image origin ordinate.
     * @throws IllegalArgumentException if <code>im</code> is
     * <code>null</code>.
     */
    public void set(RenderedImage im, int x, int y) {
        if(im == null) {
            throw new IllegalArgumentException("im == null!");
        }

        source = im;

        // Swing geometry
        int w = source.getWidth();
        int h = source.getHeight();
        Insets insets = getInsets();
        Dimension dim = new Dimension(w + insets.left + insets.right,
                                      h + insets.top + insets.bottom);

        setPreferredSize(dim);

        originX = x;
        originY = y;

        revalidate();
        repaint();
    }

    /**
     * Returns the image to be displayed by this panel.
     *
     * @return The value of the {@link #source} instance variable.
     */
    public RenderedImage getSource() {
        return source;
    }

    /**
     * Draws the image or fills the panel with a background color.
     *
     * <p>If the current image is <code>null</code>, the rectangle
     * <code>new Rectangle(0,0,{@link #getWidth()},{@link #getHeight()})</code>
     * is filled with the background color returned by
     * {@link #getBackground()}.</p>
     *
     * <p>If the current image is non-<code>null</code>, the rectangle
     * returned by {@link Graphics2D#getClipBounds()} is filled with the
     * background color and the image is drawn using
     * {@link Graphics2D#drawRenderedImage(RenderedImage,AffineTransform)}
     * at the location
     * <code>({@link #getInsets()}.left+originX,
     * {@link #getInsets()}.right+originY)</code>.
     *
     * @param g <code>Graphics</code> context in which to paint.
     */
    public synchronized void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        // empty component (no image)
        if ( source == null ) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        // clear damaged component area
        Rectangle clipBounds = g2d.getClipBounds();
        g2d.setColor(getBackground());
        g2d.fillRect(clipBounds.x,
                     clipBounds.y,
                     clipBounds.width,
                     clipBounds.height);

        // account for borders
        Insets insets = getInsets();
        int tx = insets.left + originX;
        int ty = insets.top  + originY;

        // Translation moves the entire image within the container
        try {
            g2d.drawRenderedImage(source,
                                  AffineTransform.getTranslateInstance(tx, ty));
        } catch( OutOfMemoryError e ) {
        }
    }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse presses.
     */
    public void mousePressed(MouseEvent e)  { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse releases.
     */
    public void mouseReleased(MouseEvent e) { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse movement.
     */
    public void mouseMoved(MouseEvent e)    { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse dragging.
     */
    public void mouseDragged(MouseEvent e)  { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse entrances.
     */
    public void mouseEntered(MouseEvent e)  { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse exits.
     */
    public void mouseExited(MouseEvent e)   { }

    /**
     * An empty method which should be overridden by subclasses which
     * respond to mouse clicks.
     */
    public void mouseClicked(MouseEvent e)  { }
}
