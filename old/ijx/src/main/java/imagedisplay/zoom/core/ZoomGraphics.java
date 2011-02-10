package imagedisplay.zoom.core;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

/**
 * Wrapper of Java Graphics class
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public class ZoomGraphics {

    Graphics2D g2d = null; //the actual Java Graphics object
    private double zfx = 1.0,  zfy = 1.0; //zoom factor x & y
    private Rectangle2D.Double efficientArea = null; // the area needed to be shown within the user space
    private Rectangle2D.Double viewArea = null; //the clip area
    private boolean validEA = false;

    /**
     * set the Graphics object
     *
     * @param g the graphics object
     */
    public void setGraphics(Graphics g) {
        this.g2d = (Graphics2D) g;
    }

    /**
     * get the current Graphics object used by ZoomGraphics
     *
     * @return current graphics object
     */
    public Graphics getGraphics() {
        return (Graphics) this.g2d;
    }

    RenderingHints hints;

    public void setHints(RenderingHints hints) {
        this.hints = hints;
        g2d.setRenderingHints(hints);
    }

    /**
     * return the current zoom factor X
     *
     * @return zoom factor X
     */
    public double getZoomFactorX() {
        return zfx;
    }

    /**
     * set the current zoom factor X
     *
     * @param zfx zoom factor x
     */
    public void setZoomFactorX(double zfx) {
        this.zfx = zfx;
    }

    /**
     * return the current zoom factor y
     *
     * @return zoom factor y
     */
    public double getZoomFactorY() {
        return zfy;
    }

    /**
     * set the current zoom factor y
     *
     * @param zfy zoom factor y
     */
    public void setZoomFactorY(double zfy) {
        this.zfy = zfy;
    }

    /**
     * Get the efficient area. Efficient area is the minimum area covering
     * all shapes drawn. The efficient area is used by ZoomJPane, ZoonScrollPane and others
     * to determine the canvas size
     *
     * @return the efficient area
     */
    protected Rectangle2D.Double getEfficientArea() {
        if (efficientArea == null) {
            return null;
        }
        Rectangle2D.Double r = new Rectangle2D.Double();
        r.setRect(efficientArea);
        return r;
    }

    /**
     * clear the efficient area
     */
    protected void clearEfficientArea() {
        efficientArea = null;
    }

    /**
     * Set the rectangle zoom clip area. Shapes not within this area will not be drawn.
     *
     * @param x      top left x of the area
     * @param y      top left y of the area
     * @param width  width of the area
     * @param height height of the area
     */
    public void setZoomClipArea(double x, double y, double width,
                                double height) {
        viewArea = new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Set the validity of the efficient area. If the efficient area
     * is invalid, ZoomGraphics will recalculate it while repainting
     *
     * @param value false to invalidate the efficient area
     */
    public void setEAValidity(boolean value) {
        if (!value) {
            this.efficientArea = null;
        }
        validEA = value;
    }

    /**
     * Convert the point from zoom space to user space
     * (space before zooming)
     *
     * @param p a point in the zoom space
     * @return its corresponding point in the user space
     */
    public Point2D.Double toUserSpace(Point2D.Double p) {
        p.x = p.x / zfx;
        p.y = p.y / zfy;
        return p;
    }

    /**
     * Convert the point from user space to zoomed space
     *
     * @param p apoint in the user space
     * @return its corresponding point in the zoom space
     */
    public Point2D.Double toZoomedSpace(Point2D.Double p) {
        p.x = p.x * zfx;
        p.y = p.y * zfy;
        return p;
    }

    /**
     * Convert a rectangle area defined in user space to its corresponding
     * area in the zoomed space
     *
     * @param x      the top left x of the area
     * @param y      the top left y of the area
     * @param width  the width of the area
     * @param height the height of the area
     */
    public Rectangle2D.Double getZoomedSpace(double x, double y,
                                             double width, double height) {
        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;
        return new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Convert a rectangle area defined in zoom space to its corresponding
     * area in the user space (unzoomed space)
     *
     * @param x      the top left x of the area
     * @param y      the top left y of the area
     * @param width  the width of the area
     * @param height the height of the area
     */
    public Rectangle2D.Double getUnzoomedSpace(double x, double y,
                                               double width, double height) {

        x = x / zfx;
        y = y / zfy;
        width = width / zfx;
        height = height / zfy;

        return new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Dispose the Graphics
     */
    public void dispose() {
        if (g2d != null) {
            g2d.dispose();

            g2d = null;
        }
    }

    /**
     * Draw a shape
     *
     * @param s shape to draw
     */
    public void draw(Shape s) {
        if (g2d == null) {
            return;
        }
        Rectangle2D.Double bounds = new Rectangle2D.Double();
        bounds.setRect(s.getBounds2D());
        bounds.width++;
        bounds.height++;
        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(bounds.x, bounds.y,
                bounds.width, bounds.height)
                : efficientArea.createUnion(new Rectangle2D.Double(bounds.x, bounds.y,
                bounds.width,
                bounds.height)));
        }
        if ((viewArea != null) && !viewArea.intersects(bounds.x, bounds.y, bounds.width,
            bounds.height)) {
            return;
        }
        AffineTransform af = new AffineTransform();
        af.scale(zfx, zfy);
        Shape ns = af.createTransformedShape(s);
        g2d.draw(ns);
    }

    /**
     * Draw Arc
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param endAngle
     */
    public void drawArc(double x, double y, double width, double height,
                        double startAngle, double endAngle) {
        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.drawArc((int) x, (int) y, (int) width, (int) height,
            (int) startAngle, (int) endAngle);
    }

    /**
     * Draw a line
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void drawLine(double x1, double y1, double x2, double y2) {
        if (g2d == null) {
            return;
        }

        double x = Math.min(x1, x2);
        double y = Math.min(y1, y2);
        double width = Math.abs(x2 - x1);
        double height = Math.abs(y2 - y1);

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, (width == 0)
                ? 1
                : width, (height == 0)
                ? 1
                : height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, (width == 0)
                ? 1
                : width, (height == 0)
                ? 1
                : height)));
        }

        if ((viewArea != null) && !viewArea.intersects(x, y, (width == 0)
            ? 1
            : width, (height == 0)
            ? 1
            : height)) {
            return;
        }

        x1 = x1 * zfx;
        y1 = y1 * zfy;
        x2 = x2 * zfx;
        y2 = y2 * zfy;

        g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    /**
     * Draw oval
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void drawOval(double x, double y, double width, double height) {
        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.drawOval((int) x, (int) y, (int) width, (int) height);
    }

    /**
     * Draw polygon
     * @param p
     */
    public void drawPolygon(Polygon p) {
        draw(p);
    }

    /**
     * Draw polygon
     * @param xPoints
     * @param yPoints
     * @param nPoints
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        draw(p);
    }

    /**
     * Draw rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void drawRect(double x, double y, double width, double height) {
        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, (width == 0)
                ? 1
                : width, (height == 0)
                ? 1
                : height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, (width == 0)
                ? 1
                : width, (height == 0)
                ? 1
                : height)));
        }

        if ((viewArea != null) && !viewArea.intersects(x, y, (width == 0)
            ? 1
            : width, (height == 0)
            ? 1
            : height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.drawRect((int) x, (int) y, (int) width, (int) height);
    }

    /**
     * Draw round rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     */
    public void drawRoundRect(double x, double y, double width,
                              double height, double arcWidth,
                              double arcHeight) {

        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;
        arcWidth = arcWidth * zfx;
        arcHeight = arcHeight * zfy;

        g2d.drawRoundRect((int) x, (int) y, (int) width, (int) height,
            (int) arcWidth, (int) arcHeight);
    }

    /**
     * Draw string
     * @param str
     * @param x
     * @param y
     */
    public void drawString(String str, double x, double y) {
        if (g2d == null) {
            return;
        }
        Object hinter =
            g2d.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font font = g2d.getFont();
        FontMetrics fm = g2d.getFontMetrics(font);
        Rectangle2D.Double bounds = new Rectangle2D.Double();
        bounds.setRect(fm.getStringBounds(str, this.g2d));
        bounds.x = x;
        bounds.y = y - fm.getAscent();
        bounds.height = fm.getAscent() + fm.getDescent();
        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? bounds
                : efficientArea.createUnion(bounds));
        }
        if ((viewArea != null) && !viewArea.intersects(bounds)) {
            return;
        }
        float size = (float) (font.getSize2D() * zfx);
        Font newFont = font.deriveFont(size);
        g2d.setFont(newFont);
        x = x * zfx;
        y = y * zfy;
        g2d.drawString(str, (int) x, (int) y);
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, hinter);
    }

    /**
     * Draw image
     * @param img
     * @param op
     * @param x
     * @param y
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, double x,
                          double y) {
        if (g2d == null || img == null) {
            return;
        }
        double width = img.getWidth();
        double height = img.getHeight();
        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null) ? new Rectangle2D.Double(x,
                y,
                (width == 0) ? 1 : width,
                (height == 0) ? 1 : height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y,
                (width == 0) ? 1 : width,
                (height == 0) ? 1 : height)));
        }
        if ((viewArea != null) && !viewArea.intersects(x, y,
            (width == 0) ? 1 : width, (height == 0) ? 1 : height)) {
            return;
        }
        x = x * zfx;
        y = y * zfy;
        AffineTransform af = new AffineTransform();
        //af.scale(zfx, zfy);
        af.scale(zfx, zfy);
        AffineTransformOp afop = new AffineTransformOp(af, null);
        if (null != op) {
            img = op.createCompatibleDestImage(img, null);
            //System.out.println("createCompatibleDestImage");
        }
//      System.out.println("img.getColorModel().getColorSpace().getType():" +
//              img.getColorModel().getColorSpace().getType());
        g2d.drawImage(img, afop, (int) x, (int) y);
    }

    /**
     * Fill a shape
     * @param s
     */
    public void fill(Shape s) {
        if (g2d == null) {
            return;
        }
        Rectangle2D.Double bounds = new Rectangle2D.Double();
        bounds.setRect(s.getBounds2D());
        bounds.width++;
        bounds.height++;
        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height)
                : efficientArea.createUnion(new Rectangle2D.Double(bounds.x, bounds.y,
                bounds.width,
                bounds.height)));
        }
        if ((viewArea != null) && !viewArea.intersects(bounds.x, bounds.y, bounds.width,
            bounds.height)) {
            return;
        }
        AffineTransform af = new AffineTransform();
        af.scale(zfx, zfy);
        Shape ns = af.createTransformedShape(s);
        g2d.fill(ns);
    }

    /**
     * Fill arc
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param endAngle
     */
    public void fillArc(double x, double y, double width, double height,
                        double startAngle, double endAngle) {

        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.fillArc((int) x, (int) y, (int) width, (int) height,
            (int) startAngle, (int) endAngle);
    }

    /**
     * Fill oval
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void fillOval(double x, double y, double width, double height) {

        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.fillOval((int) x, (int) y, (int) width, (int) height);
    }

    /**
     * Fill polygon
     * @param p
     */
    public void fillPolygon(Polygon p) {
        fill(p);
    }

    /**
     * Fill polygon
     * @param xPoints
     * @param yPoints
     * @param nPoints
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {

        Polygon p = new Polygon(xPoints, yPoints, nPoints);

        fill(p);
    }

    /**
     * Fill rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void fillRect(double x, double y, double width, double height) {

        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;

        g2d.fillRect((int) x, (int) y, (int) width, (int) height);
    }

    /**
     * Fill round rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     */
    public void fillRoundRect(double x, double y, double width,
                              double height, double arcWidth,
                              double arcHeight) {

        if (g2d == null) {
            return;
        }

        if (!validEA) {
            efficientArea = (Rectangle2D.Double) ((efficientArea == null)
                ? new Rectangle2D.Double(x, y, width, height)
                : efficientArea.createUnion(new Rectangle2D.Double(x, y, width,
                height)));

        }

        if ((viewArea != null) && !viewArea.intersects(x, y, width, height)) {
            return;
        }

        x = x * zfx;
        y = y * zfy;
        width = width * zfx;
        height = height * zfy;
        arcWidth = arcWidth * zfx;
        arcHeight = arcHeight * zfy;

        g2d.fillRoundRect((int) x, (int) y, (int) width, (int) height,
            (int) arcWidth, (int) arcHeight);
    }

    /**
     * Finalize
     */
    public void finalize() {

        if (g2d != null) {
            g2d.finalize();

            g2d = null;
        }
    }

    /**
     * Get background
     * @return current background
     */
    public Color getBackground() {

        if (g2d == null) {
            return null;
        }

        return g2d.getBackground();
    }

    /**
     * get color
     * @return current color
     */
    public Color getColor() {

        if (g2d == null) {
            return null;
        }

        return g2d.getColor();
    }

    /**
     * Get composite
     * @return composite
     */
    public Composite getComposite() {

        if (g2d == null) {
            return null;
        }

        return g2d.getComposite();
    }

    /**
     * Get font
     * @return current font
     */
    public Font getFont() {
        if (g2d == null) {
            return null;
        }

        return g2d.getFont();
    }

    /**
     * Get font metrics
     * @return font metrics
     */
    public FontMetrics getFontMetrics() {
        if (g2d == null) {
            return null;
        }

        return g2d.getFontMetrics();
    }

    /**
     * Get font metrics of the specified font
     * @param f
     * @return font metrics of the specified font
     */
    public FontMetrics getFontMetrics(Font f) {
        if (g2d == null) {
            return null;
        }

        return g2d.getFontMetrics(f);
    }

    /**
     * Get paint
     * @return  paint
     */
    public Paint getPaint() {
        if (g2d == null) {
            return null;
        }

        return g2d.getPaint();
    }

    /**
     * Get stroke
     * @return   stroke
     */
    public Stroke getStroke() {
        if (g2d == null) {
            return null;
        }

        return g2d.getStroke();
    }

    /**
     * Rotate
     * @param theta
     */
    public void rotate(double theta) {
        if (g2d == null) {
            return;
        }

        g2d.rotate(theta);
    }

    /**
     * Set background
     * @param c
     */
    public void setBackground(Color c) {
        if (g2d != null) {
            g2d.setBackground(c);
        }
    }

    /**
     * Set color
     * @param c
     */
    public void setColor(Color c) {
        if (g2d != null) {
            g2d.setColor(c);
        }
    }

    /**
     * Set composite
     * @param c
     */
    public void setComposite(Composite c) {
        if (g2d != null) {
            g2d.setComposite(c);
        }
    }

    /**
     *  Set font
     * @param f
     */
    public void setFont(Font f) {
        if (g2d != null) {
            g2d.setFont(f);
        }
    }

    /**
     * Set font
     * @param p
     */
    public void setPaint(Paint p) {

        if (g2d != null) {
            g2d.setPaint(p);
        }
    }

    /**
     * Set paint mode
     */
    public void setPaintMode() {

        if (g2d != null) {
            g2d.setPaintMode();
        }
    }

    /**
     * Set stroke
     * @param s
     */
    public void setStroke(Stroke s) {

        if (g2d == null) {
            return;
        }

        g2d.setStroke(s);
    }

    /**
     * Set XOR mode
     * @param c
     */
    public void setXORMode(Color c) {

        if (g2d != null) {
            g2d.setXORMode(c);
        }
    }

    /**
     * Translate
     * @param x
     * @param y
     */
    public void translate(double x, double y) {

        if (g2d == null) {
            return;
        }

        g2d.translate(x * zfx, y * zfy);
    }

}
