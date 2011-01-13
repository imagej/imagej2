/*
 * WrapperGraphics.java
 *
 * Created on October 15, 2005, 8:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.rasterlayer;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import org.imagejdev.imagine.api.util.TrackingGraphics;
import org.imagejdev.imagine.spi.image.RepaintHandle;

/**
 * A Graphics2D which wrappers another Graphics2D and pushes repaint
 * requests for the modified area out to the component, whenever a 
 * drawing operation is performed.
 *
 * @author Timothy Boudreau
 */
class WrapperGraphics extends TrackingGraphics {
    private final Graphics2D other;
    private final RepaintHandle handle;
    private final Point location;
    private final int w;
    private final int h;
    /** Creates a new instance of WrapperGraphics */
    public WrapperGraphics(RepaintHandle handle, Graphics2D other, Point location, int w, int h) {
        this.w = w;
        this.h = h;
        this.other = (Graphics2D) other.create(0, 0, w, h); //so we can translate safely
        this.handle = handle;
	this.location = location;
        location.x = Math.min (0, location.x);
        location.y = Math.min (0, location.y);
        this.other.translate (-location.x, -location.y);
    }

    public void draw(Shape s) {
        other.draw (s);
        Rectangle r = s.getBounds();
        changed (r);
    }
    
    private void changed (int[] xPoints, int[] yPoints, int nPoints) {
	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int maxY = Integer.MIN_VALUE;
	for (int i=0; i < nPoints; i++) {
	    minX = Math.min (minX, xPoints[i]);
	    minY = Math.min (minY, yPoints[i]);
	    maxX = Math.max (maxX, xPoints[i]);
	    maxY = Math.max (maxY, yPoints[i]);
	}
	changed (minX, minY, maxX - minX, maxY - minY);
    }
    
    private void changed (int x, int y, int w, int h) {
//        System.err.println("CHANGED " + x  + "," + y  + "," + w  + "," + h);
        handle.repaintArea(x, y, w, h);
    }
    
    private void changed() {
        changed (-1, -1, -1, -1);
    }
    
    private void changed (Rectangle r) {
        changed (r.x, r.y, r.width, r.height);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean result = other.drawImage (img, xform, obs);
        if (result) {
            changed();
        }
        return result;
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        other.drawImage (img, op, x, y);
        changed (x, y, img.getWidth(), img.getHeight());
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        other.drawRenderedImage (img, xform);
        Point scratch = new Point (0, 0);
        Point topLeft = new Point();
        xform.transform(scratch, topLeft);
        Point bottomRight = new Point();
        scratch.x = img.getWidth();
        scratch.y = img.getHeight();
        xform.transform (scratch, bottomRight);
        changed (topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y); 
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        other.drawRenderableImage (img, xform);
        Point scratch = new Point (0, 0);
        Point topLeft = new Point();
        xform.transform(scratch, topLeft);
        Point bottomRight = new Point();
        scratch.x = (int) Math.ceil(img.getWidth());
        scratch.y = (int) Math.ceil(img.getHeight());
        xform.transform (scratch, bottomRight);
        changed (topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y); 
    }

    public void drawString(String str, int x, int y) {
        other.drawString (str, x, y);
        FontMetrics fm = getFontMetrics();
        Rectangle2D r = fm.getStringBounds(str, other);
        changed(x, y, (int) Math.round(r.getWidth()), (int) Math.round(r.getHeight()));
    }

    public void drawString(String s, float x, float y) {
        other.drawString (s, x, y);
        FontMetrics fm = getFontMetrics();
        Rectangle2D r = fm.getStringBounds(s, other);
        changed((int)Math.floor(x), (int) Math.floor(y), 
                (int) Math.ceil(r.getWidth()), (int) Math.ceil(r.getHeight()));
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        other.drawString (iterator, x, y);
        FontMetrics fm = getFontMetrics();
        Rectangle2D r = fm.getStringBounds(iterator, 0, iterator.last(), other);
        changed(x, y, (int) Math.ceil(r.getWidth()), (int) Math.ceil(r.getHeight()));
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        other.drawString (iterator, x, y);
        FontMetrics fm = getFontMetrics();
        Rectangle2D r = fm.getStringBounds(iterator, 0, iterator.last(), other);
        changed((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(r.getWidth()), (int) Math.ceil(r.getHeight()));
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        other.drawGlyphVector(g, x, y);
        Rectangle2D r = g.getLogicalBounds();
        changed((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(r.getWidth()), (int) Math.ceil(r.getHeight()));
    }

    public void fill(Shape s) {
        other.fill (s);
        changed (s.getBounds());
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return other.hit (rect, s, onStroke);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return other.getDeviceConfiguration();
    }

    public void setComposite(Composite comp) {
        other.setComposite (comp);
    }

    public void setPaint(Paint paint) {
        other.setPaint (paint);
    }

    public void setStroke(Stroke s) {
        other.setStroke (s);
    }

    public void setRenderingHint(Key hintKey, Object hintValue) {
        other.setRenderingHint (hintKey, hintValue);
    }

    public Object getRenderingHint(Key hintKey) {
        return other.getRenderingHint(hintKey);
    }

    public void setRenderingHints(Map hints) {
        other.setRenderingHints(hints);
    }

    public void addRenderingHints(Map hints) {
        other.addRenderingHints(hints);
    }

    public RenderingHints getRenderingHints() {
        return other.getRenderingHints();
    }

    public void translate(int x, int y) {
        other.translate (x, y);
    }

    public void translate(double tx, double ty) {
        other.translate (tx, ty);
    }

    public void rotate(double theta) {
        other.rotate (theta);
    }

    public void rotate(double theta, double x, double y) {
        other.rotate (theta, x, y);
    }

    public void scale(double sx, double sy) {
        other.scale (sx, sy);
    }

    public void shear(double shx, double shy) {
        other.shear (shx, shy);
    }

    public void transform(AffineTransform tx) {
        other.transform (tx);
    }

    public void setTransform(AffineTransform tx) {
        other.setTransform(tx);
    }

    public AffineTransform getTransform() {
        return other.getTransform();
    }

    public Paint getPaint() {
        return other.getPaint();
    }

    public Composite getComposite() {
        return other.getComposite();
    }

    public void setBackground(Color color) {
        other.setBackground(color);
    }

    public Color getBackground() {
        return other.getBackground();
    }

    public Stroke getStroke() {
        return other.getStroke();
    }

    public void clip(Shape s) {
        other.clip (s);
    }

    public FontRenderContext getFontRenderContext() {
        return other.getFontRenderContext();
    }

    public Graphics create() {
        return new WrapperGraphics (handle, (Graphics2D) other.create(),
		new Point (location), w, h);
    }

    public Color getColor() {
        return other.getColor();
    }

    public void setColor(Color c) {
        other.setColor (c);
    }

    public void setPaintMode() {
        other.setPaintMode();
    }

    public void setXORMode(Color c1) {
        other.setXORMode(c1);
    }

    public Font getFont() {
        return other.getFont();
    }

    public void setFont(Font font) {
        other.setFont (font);
    }

    public FontMetrics getFontMetrics(Font f) {
        return other.getFontMetrics();
    }

    public Rectangle getClipBounds() {
        return other.getClipBounds();
    }

    public void clipRect(int x, int y, int width, int height) {
        other.clipRect (x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height) {
        other.setClip (x, y, width, height);
    }

    public Shape getClip() {
        return other.getClip();
    }

    public void setClip(Shape clip) {
        other.setClip (clip);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        other.copyArea (x, y, width, height, dx, dy);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        other.drawLine(x1, y1, x2, y2);
        int wid = Math.abs (x1 - x2);
        int ht = Math.abs (y1 - y2);
        int x = Math.min (x1, x2);
        int y = Math.min (y1, y2);
        changed (x, y, wid, ht);
    }

    public void fillRect(int x, int y, int width, int height) {
        other.fillRect (x, y, width, height);
        changed (x, y, width, height);
    }

    public void clearRect(int x, int y, int width, int height) {
        other.clearRect(x, y, width, height);
        changed (x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        other.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        changed (x, y, width, height);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        other.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        changed (x, y, width, height);
    }

    public void drawOval(int x, int y, int width, int height) {
        other.drawOval (x, y, width, height);
        changed (x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height) {
        other.fillOval(x, y, width, height);
        changed (x, y, width, height);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        other.drawArc(x, y, width, height, startAngle, arcAngle);
        changed (x, y, width, height);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        other.fillArc (x, y, width, height, startAngle, arcAngle);
        changed (x, y, width, height);
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        other.drawPolyline (xPoints, yPoints, nPoints);
        changed(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        other.drawPolygon (xPoints, yPoints, nPoints);
        changed(xPoints, yPoints, nPoints);
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        other.fillPolygon (xPoints, yPoints, nPoints);
	changed (xPoints, yPoints, nPoints);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        boolean result = other.drawImage (img, x, y, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        boolean result = other.drawImage (img, x, y, width, height, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage (img, x, y, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage (img, x, y, width, height, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean result = other.drawImage (img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage (img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public void dispose() {
        other.dispose();
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        if (width != 0 || height != 0) {
            other.drawRect (x, y, width, height);
            changed (x, y, width + 1, height + 1);
        }
    }

    @Override
    public void areaModified(int x, int y, int w, int h) {
        changed (x, y, w, h);
    }
}
