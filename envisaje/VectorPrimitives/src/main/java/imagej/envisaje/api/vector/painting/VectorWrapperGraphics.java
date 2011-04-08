/*
 * WrapperGraphics.java
 *
 * Created on October 15, 2005, 8:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.painting;

import imagej.envisaje.api.vector.Primitive;
import imagej.envisaje.api.vector.elements.Arc;
import imagej.envisaje.api.vector.elements.CharacterIteratorWrapper;
import imagej.envisaje.api.vector.elements.Clear;
import imagej.envisaje.api.vector.elements.ImageWrapper;
import imagej.envisaje.api.vector.elements.Line;
import imagej.envisaje.api.vector.elements.Oval;
import imagej.envisaje.api.vector.elements.PathIteratorWrapper;
import imagej.envisaje.api.vector.elements.Polygon;
import imagej.envisaje.api.vector.elements.Polyline;
import imagej.envisaje.api.vector.elements.Rectangle;
import imagej.envisaje.api.vector.elements.RoundRect;
import imagej.envisaje.api.vector.elements.StringWrapper;
import imagej.envisaje.api.vector.graphics.AffineTransformWrapper;
import imagej.envisaje.api.vector.graphics.BasicStrokeWrapper;
import imagej.envisaje.api.vector.graphics.ColorWrapper;
import imagej.envisaje.api.vector.graphics.FontWrapper;
import imagej.envisaje.api.vector.graphics.GradientPaintWrapper;
import imagej.envisaje.api.vector.graphics.PaintWrapper;
import imagej.envisaje.api.vector.graphics.TexturePaintWrapper;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * A Graphics2D which wrappers another Graphics2D, and produces objects
 * representing primitive graphics elements for all drawing operations
 * performed upon it.
 *
 * @author Timothy Boudreau
 */
public class VectorWrapperGraphics extends Graphics2D {
    private final Graphics2D other;
    private final VectorRepaintHandle handle;
    private final Point location;
    private final int w;
    private final int h;
    /** Creates a new instance of WrapperGraphics */
    public VectorWrapperGraphics(VectorRepaintHandle handle, Graphics2D other, Point location, int w, int h) {
        this.w = w;
        this.h = h;
        this.other = (Graphics2D) other.create(0, 0, w, h); //so we can translate safely
        this.handle = handle;
        this.location = location;
        location.x = Math.min(0, location.x);
        location.y = Math.min(0, location.y);
        this.other.translate(-location.x, -location.y);
    }

    private BasicStrokeWrapper stroke() {
        Stroke s = getStroke();
        if (s instanceof BasicStroke) {
            return new BasicStrokeWrapper((BasicStroke) s);
        }
        return null;
    }

    public void draw(Shape s) {
        if (!receiving) {
            push(new PathIteratorWrapper(s.getPathIterator(
                    AffineTransform.getTranslateInstance(0,0)),
                    false));
        }
        other.draw(s);
        Stroke stroke = getStroke();
        java.awt.Rectangle r = s.getBounds();
        if (stroke == null || stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke) stroke;
            if (bs == null || bs.getLineWidth() != 1) {
                r = s.getBounds();
                if (bs != null) {
                    int width = Math.round(bs.getLineWidth());
                    r.x -= width / 2;
                    r.y -= width / 2;
                    r.width += width;
                    r.height += width;
                }
            }
        }
        changed(r);
    }

    public void clear() {
        if (!receiving) {
            push(new Clear(0, 0, w, h));
        }
        clearRect(0, 0, w, h);
        changed(0, 0, w, h);
    }

    public void draw(Clear clear) {
        receiving = true;
        clear();
        receiving = false;
    }

    private void changed(int[] xPoints, int[] yPoints, int nPoints) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i=0; i < nPoints; i++) {
            minX = Math.min(minX, xPoints[i]);
            minY = Math.min(minY, yPoints[i]);
            maxX = Math.max(maxX, xPoints[i]);
            maxY = Math.max(maxY, yPoints[i]);
        }
        changed(minX, minY, maxX - minX, maxY - minY);
    }

    private void changed(int x, int y, int w, int h) {
        if (strokeWidth > 1) {
            float half = strokeWidth / 2;
            x -= half;
            y -= half;
            w += strokeWidth;
            h += strokeWidth;
        }
        handle.repaintArea(x, y, w, h);
    }

    private void changed() {
        changed(-1, -1, -1, -1);
    }

    private void changed(java.awt.Rectangle r) {
        changed(r.x, r.y, r.width, r.height);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean result = other.drawImage(img, xform, obs);
        if (result) {
            if (!receiving) {
                push(new ImageWrapper(xform.getTranslateX(), xform.getTranslateY(), img));
            }
            changed();
        }
        return result;
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        if (!receiving) {
            push(new ImageWrapper(img, x, y));
        }
        other.drawImage(img, op, x, y);
        changed(x, y, img.getWidth(), img.getHeight());
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        if (!receiving) {
            push(new ImageWrapper(img, xform.getTranslateX(), xform.getTranslateY()));
        }
        other.drawRenderedImage(img, xform);
        changed(0, 0, img.getWidth(), img.getHeight()); //XXX won't work on scale xform
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        if (!receiving) {
            push(new ImageWrapper(img, xform.getTranslateX(), xform.getTranslateY()));
        }
        other.drawRenderableImage(img, xform);
        changed(0, 0, (int) img.getWidth(), (int) img.getHeight()); //XXX won't work on scale xform
    }

    public void draw(ImageWrapper img) {
        receiving = true;
        drawRenderedImage(img.img, AffineTransform.getTranslateInstance(img.x, img.y));
        changed();
        receiving = false;
    }

    public void draw(StringWrapper sw) {
        receiving = true;
//        drawString(sw.string, sw.x, sw.y);
        sw.paint(this);
        receiving = false;
    }

    private FontWrapper font() {
        return new FontWrapper(getFont());
    }

    public void drawString(String str, int x, int y) {
        if (!receiving) {
            push(new StringWrapper(str, x, y));
        }
        other.drawString(str, x, y);
        changed();
    }

    public void drawString(String s, float x, float y) {
        if (!receiving) {
            push(new StringWrapper(s, x, y));
        }
        other.drawString(s, x, y);
        changed();
    }

    public void draw(CharacterIteratorWrapper w) {
        receiving = true;
//        drawString(w.it, w.x, w.y);
        w.paint(this);
        receiving = false;
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        if (!receiving) {
            push(new CharacterIteratorWrapper(iterator, x, y));
        }
        other.drawString(iterator, x, y);
        changed();
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        if (!receiving) {
            push(new CharacterIteratorWrapper(iterator, x, y));
        }
        other.drawString(iterator, x, y);
        changed();
    }

    public void drawGlyphVector(GlyphVector gv, float x, float y) {
        if (!receiving) {
            push (new PathIteratorWrapper (gv, x, y));
        }
        other.drawGlyphVector(gv, x, y);
        changed();
    }

    public void fill(Shape s) {
        if (!receiving) {
            push(new PathIteratorWrapper(
                    s.getPathIterator(
                    AffineTransform.getTranslateInstance(0, 0)),
                    true));
        }
        other.fill(s);
        changed(s.getBounds());
    }

    public boolean hit(java.awt.Rectangle rect, Shape s, boolean onStroke) {
        return other.hit(rect, s, onStroke);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return other.getDeviceConfiguration();
    }

    public void setComposite(Composite comp) {
        other.setComposite(comp);
    }

    public void setPaint(Paint paint) {
        if (!receiving) {
            if (paint == null) {
                return;
            }
            if (paint.getClass() == GradientPaint.class) {
                push(new GradientPaintWrapper((GradientPaint) paint));
            } else if (paint instanceof TexturePaint) {
                push(new TexturePaintWrapper((TexturePaint) paint));
            }
        }
        other.setPaint(paint);
    }

    float strokeWidth = 1;
    public void setStroke(Stroke s) {
        if (!receiving) {
            if (s instanceof Primitive) {
                push((Primitive) s);
            } else if (s instanceof BasicStroke) {
                push(new BasicStrokeWrapper((BasicStroke) s));
            }
        }
        if (s instanceof BasicStroke) {
            strokeWidth = ((BasicStroke) s).getLineWidth();
        }
        other.setStroke(s);
    }

    public void setRenderingHint(Key hintKey, Object hintValue) {
        other.setRenderingHint(hintKey, hintValue);
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
        other.translate(x, y);
        push(new AffineTransformWrapper(x, y,
                AffineTransformWrapper.TRANSLATE));
    }

    public void translate(double tx, double ty) {
        other.translate(tx, ty);
        push(new AffineTransformWrapper(tx, ty, AffineTransformWrapper.TRANSLATE));
    }

    public void rotate(double theta) {
        other.rotate(theta);
        push(new AffineTransformWrapper(theta));
    }

    public void rotate(double theta, double x, double y) {
        other.rotate(theta, x, y);
        push(new AffineTransformWrapper(theta, x, y));
    }

    public void scale(double sx, double sy) {
        other.scale(sx, sy);
        push(new AffineTransformWrapper(sx, sy, AffineTransformWrapper.SCALE));
    }

    public void shear(double shx, double shy) {
        other.shear(shx, shy);
        push(new AffineTransformWrapper(shx, shy, AffineTransformWrapper.SHEAR));
    }

    public void transform(AffineTransform tx) {
        other.transform(tx);
        push(new AffineTransformWrapper(tx));
    }

    public void setTransform(AffineTransform tx) {
        other.setTransform(tx);
        push(new AffineTransformWrapper(tx));
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
        other.clip(s);
    }

    public FontRenderContext getFontRenderContext() {
        return other.getFontRenderContext();
    }

    public Graphics create() {
        return new VectorWrapperGraphics(handle, (Graphics2D) other.create(),
                new Point(location), w, h);
    }

    public Color getColor() {
        return other.getColor();
    }

    public void setColor(Color c) {
        if (!receiving) {
            push(new ColorWrapper(c));
        }
        other.setColor(c);
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
        if (!receiving) {
            push(new FontWrapper(font));
        }
        other.setFont(font);
    }

    public void setFont(FontWrapper w) {
        receiving = true;
        setFont(w.toFont());
        receiving = false;
    }

    public FontMetrics getFontMetrics(Font f) {
        return other.getFontMetrics();
    }

    public java.awt.Rectangle getClipBounds() {
        return other.getClipBounds();
    }

    public void clipRect(int x, int y, int width, int height) {
        other.clipRect(x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height) {
        other.setClip(x, y, width, height);
    }

    public Shape getClip() {
        return other.getClip();
    }

    public void setClip(Shape clip) {
        other.setClip(clip);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        other.copyArea(x, y, width, height, dx, dy);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        if (!receiving) {
            push(new Line(x1, y1, x2, y2));
        }
        other.drawLine(x1, y1, x2, y2);
        int wid = Math.abs(x1 - x2);
        int ht = Math.abs(y1 - y2);
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        changed(x, y, wid, ht);
    }

    public void draw(Line line) {
        receiving = true;
//        drawLine(line.x1, line.y1, line.x2, line.y2);
        line.draw (this);
        receiving = false;
    }

    public void fillRect(int x, int y, int width, int height) {
        if (!receiving) {
            push(new imagej.envisaje.api.vector.elements.Rectangle(x, y, width, height, true));
        }
        other.fillRect(x, y, width, height);
        changed(x, y, width, height);
    }

    public void clearRect(int x, int y, int width, int height) {
        other.clearRect(x, y, width, height);
        changed(x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (!receiving) {
            push(new RoundRect(x, y, width, height, arcWidth, arcHeight, false));
        }
        other.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        changed(x, y, width, height);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (!receiving) {
            push(new RoundRect(x, y, width, height, arcWidth, arcHeight, true));
        }
        other.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        changed(x, y, width, height);
    }

    public void draw(RoundRect rr) {
        receiving = true;
        rr.draw(this);
        receiving = false;
    }

    public void drawOval(int x, int y, int width, int height) {
        if (!receiving) {
            push(new Oval(x, y, width, height, false));
        }
        other.drawOval(x, y, width, height);
        changed(x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height) {
        if (!receiving) {
            push(new Oval(x, y, width, height, true));
        }
        other.fillOval(x, y, width, height);
        changed(x, y, width, height);
    }

    public void draw(Oval o) {
        receiving = true;
        o.draw (this);
        receiving = false;
    }

    public void draw(Arc arc) {
        receiving = true;
        arc.draw (this);
        receiving = false;
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (!receiving) {
            push(new Arc(x, y, width, height, startAngle, arcAngle, false));
        }
        other.drawArc(x, y, width, height, startAngle, arcAngle);
        changed(x, y, width, height);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (!receiving) {
            push(new Arc(x, y, width, height, startAngle, arcAngle, true));
        }
        other.fillArc(x, y, width, height, startAngle, arcAngle);
        changed(x, y, width, height);
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        if (!receiving) {
            push(new Polyline(xPoints, yPoints, nPoints, false));
        }
        other.drawPolyline(xPoints, yPoints, nPoints);
        changed(xPoints, yPoints, nPoints);
    }

    private void push(Primitive serializable) {
        handle.drawn(serializable);
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        if (!receiving) {
            push(new Polygon(xPoints, yPoints, nPoints, false));
        }
        other.drawPolygon(xPoints, yPoints, nPoints);
        changed(xPoints, yPoints, nPoints);
    }

    private boolean receiving = false;
    public void draw(Polygon polygon) {
        receiving = true;
        if (polygon.fill) {
            fillPolygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
        } else {
            drawPolygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
        }
        receiving = false;
    }

    public void draw(Polyline polyline) {
        receiving = true;
        drawPolyline(polyline.xpoints, polyline.ypoints, polyline.npoints);
        receiving = false;
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        if (!receiving) {
            push(new Polygon(xPoints, yPoints, nPoints, true));
        }
        other.fillPolygon(xPoints, yPoints, nPoints);
        changed(xPoints, yPoints, nPoints);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        if (!receiving) {
            push(new ImageWrapper(x, y, img));
        }
        boolean result = other.drawImage(img, x, y, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        boolean result = other.drawImage(img, x, y, width, height, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage(img, x, y, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage(img, x, y, width, height, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean result = other.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        if (result) changed();
        return result;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        boolean result = other.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        if (result) changed();
        return result;
    }

    public void dispose() {
        other.dispose();
    }

    public void draw(java.awt.Rectangle rect) {
        receiving = true;
        drawRect(rect.x, rect.y, rect.width, rect.height);
        receiving = false;
    }

    public void drawRect(int x, int y, int width, int height) {
        if (width != 0 && height != 0) {
            if (!receiving) {
                push(new imagej.envisaje.api.vector.elements.Rectangle(x, y, width, height, false));
            }
            other.drawRect(x, y, width, height);
            changed(x, y, width + 1, height + 1);
        }
    }

    public void draw(Rectangle r) {
        receiving = true;
        r.draw (this);
        receiving = false;
    }

    public void receive(Primitive s) {
        other.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        other.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        other.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        other.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        other.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (s instanceof ColorWrapper) {
            receiving = true;
            setColor(((ColorWrapper) s).toColor());
            receiving = false;
        } else if (s instanceof Arc) {
            draw((Arc) s);
        } else if (s instanceof Line) {
            draw((Line) s);
        } else if (s instanceof PaintWrapper) {
            receiving = true;
            setPaint(((PaintWrapper) s).toPaint());
            receiving = false;
        } else if (s instanceof PathIteratorWrapper) {
            receiving = true;
            draw((PathIteratorWrapper) s);
            receiving = false;
        } else if (s instanceof CharacterIteratorWrapper) {
            receiving = true;
            draw((CharacterIteratorWrapper) s);
            receiving = false;
        } else if (s instanceof Oval) {
            receiving = true;
            draw((Oval)s);
            receiving = false;
        } else if (s instanceof Polygon) {
            receiving = true;
            draw((Polygon) s);
            receiving = false;
        } else if (s instanceof Polyline) {
            receiving = true;
            draw((Polyline) s);
            receiving = false;
        } else if (s instanceof RoundRect) {
            receiving = true;
            draw((RoundRect) s);
            receiving = false;
        } else if (s instanceof StringWrapper) {
            receiving = true;
            draw((StringWrapper) s);
            receiving = false;
        } else if (s instanceof BasicStrokeWrapper) {
            receiving = true;
            setStroke(((BasicStrokeWrapper) s).toStroke());
            receiving = false;
        } else if (s instanceof GradientPaintWrapper) {
            receiving = true;
            setPaint(((GradientPaintWrapper) s).toGradientPaint());
            receiving = false;
        } else if (s instanceof Line) {
            receiving = true;
            draw((Line) s);
            receiving = false;
        } else if (s instanceof Rectangle) {
            receiving = true;
            draw((Rectangle) s);
            receiving = false;
        } else if (s instanceof Clear) {
            receiving = true;
            draw((Clear) s);
            receiving = false;
        } else if (s instanceof ImageWrapper) {
            receiving = true;
            draw((ImageWrapper) s);
            receiving = false;
        }
    }

    public void setFontThrough(Font f) {
        other.setFont(f);
    }

    private void draw(PathIteratorWrapper pathIteratorWrapper) {
        pathIteratorWrapper.paint(this);
    }
}
