/*
 * DummyGraphics.java
 *
 * Created on October 25, 2006, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.vectorlayers;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
final class DummyGraphics extends Graphics2D {

    public void draw(Shape s) {
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return true;
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    }

    public void drawString(String str, int x, int y) {
    }

    public void drawString(String str, float x, float y) {
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
    }

    public void fill(Shape s) {
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return true;
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    private Composite composite;
    public void setComposite(Composite comp) {
        composite = comp;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    private Stroke stroke;
    public void setStroke(Stroke s) {
        this.stroke = s;
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        if (hintValue == null || hintKey == null) return;
        hints.put (hintKey, hintValue);
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return hints.get (hintKey);
    }

    public void setRenderingHints(Map hints) {
        hints.clear();
        addRenderingHints (hints);
    }

    @SuppressWarnings("unchecked")
    public void addRenderingHints(Map hints) {
        hints.putAll(hints);
    }

    @SuppressWarnings("unchecked")
    private RenderingHints hints = new RenderingHints(new HashMap ());
    public RenderingHints getRenderingHints() {
        return hints;
    }

    public void translate(int x, int y) {
    }

    public void translate(double tx, double ty) {
    }

    public void rotate(double theta) {
    }

    public void rotate(double theta, double x, double y) {
    }

    public void scale(double sx, double sy) {
    }

    public void shear(double shx, double shy) {
    }

    AffineTransform xform = AffineTransform.getTranslateInstance(0d, 0d);
    public void transform(AffineTransform Tx) {
        xform.concatenate (Tx);
    }

    public void setTransform(AffineTransform Tx) {
        xform = Tx;
    }

    public AffineTransform getTransform() {
        return xform;
    }

    public Paint getPaint() {
        return paint;
    }

    public Composite getComposite() {
        return composite;
    }

    Color background;
    public void setBackground(Color color) {
        background = color;
    }

    public Color getBackground() {
        return background;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void clip(Shape s) {
    }

    public FontRenderContext getFontRenderContext() {
        return null;
    }

    public Graphics create() {
        return new DummyGraphics();
    }

    public Color getColor() {
        return paint instanceof Color ? (Color) paint : null;
    }

    private Paint paint;
    public void setColor(Color c) {
        paint = c;
    }

    public void setPaintMode() {
        xor = null;
    }

    Color xor = null;
    public void setXORMode(Color c1) {
        xor = c1;
    }

    public Font getFont() {
        return font;
    }

    private Font font;
    public void setFont(Font font) {
        this.font = font;
    }

    public FontMetrics getFontMetrics(Font f) {
        //XXX safe to return null?
        Graphics g = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB));
        FontMetrics result = g.getFontMetrics (f);
        return result;
    }

    public Rectangle getClipBounds() {
        return clip == null ? null : clip.getBounds();
    }

    public void clipRect(int x, int y, int width, int height) {
        clip = new Rectangle (x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height) {
        clip = new Rectangle (x, y, width, height);
    }

    public Shape getClip() {
        return clip;
    }

    private Shape clip;
    public void setClip(Shape clip) {
        clip = clip;
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
    }

    public void fillRect(int x, int y, int width, int height) {
    }

    public void clearRect(int x, int y, int width, int height) {
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    public void drawOval(int x, int y, int width, int height) {
    }

    public void fillOval(int x, int y, int width, int height) {
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return true;
    }

    public void dispose() {
    }

    public Graphics create(int x, int y, int width, int height) {
        return new DummyGraphics();
    }

    public FontMetrics getFontMetrics() {
        return font == null ? null : getFontMetrics (font);
    }

    public boolean hitClip(int x, int y, int width, int height) {
        return clip == null ? true : getClipBounds().intersects(
                new Rectangle (x, y, width, height));
    }

    public Rectangle getClipBounds(Rectangle r) {
        if (clip != null) {
            r.setBounds (getClipBounds());
        }
        return r;
    }

    public boolean equals(Object obj) {
        return obj instanceof DummyGraphics;
    }

    public String toString() {
        return "Dummy Graphics"; //NOI18N
    }
}
