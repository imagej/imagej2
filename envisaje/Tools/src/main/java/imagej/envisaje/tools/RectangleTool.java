/*
 * RectangleTool.java
 *
 * Created on September 28, 2006, 6:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import static imagej.envisaje.tools.MutableRectangle.ANY;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.image.Layer;

import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.api.toolcustomizers.AggregateCustomizer;
import imagej.envisaje.api.toolcustomizers.Constants;
import imagej.envisaje.api.toolcustomizers.Customizers;
import imagej.envisaje.tools.fills.FillCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)
public class RectangleTool implements Tool, PaintParticipant, MouseMotionListener, MouseListener, KeyListener, CustomizerProvider {
    MutableRectangle rect;
    private int draggingCorner;

    public RectangleTool () {
    }

    public boolean canAttach (Layer layer) {
        return layer.getLookup().lookup (Surface.class) != null;
    }

    public Rectangle getRectangle() {
        return rect == null ? null : new Rectangle (rect);
    }

    private void setDraggingCorner(int draggingCorner) {
        this.draggingCorner = draggingCorner;
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiersEx() == 0) {
            clear();
        }
    }

    private void clear() {
        rect = null;
        draggingCorner = ANY;
        armed = false;
        if (repainter != null) {
            repainter.requestRepaint(null);
        }
    }

    @Override
    public String toString() {
        return NbBundle.getMessage (getClass(), "Rectangle");
    }

    public void paint(Graphics2D g2d) {
        Rectangle toPaint = rect == null ? TEMPLATE_RECTANGLE : rect;
        lastPaintedRectangle.setBounds (toPaint);
        if (armed || committing) {
            g2d.setStroke(new BasicStroke (this.strokeC.get()));
            boolean fill = fillC.get();
            draw (toPaint, g2d, fill);
        }
    }

    protected void draw (Rectangle toPaint, Graphics2D g2d, boolean fill) {
        g2d.setStroke(new BasicStroke(strokeC.get()));
        if (fill) {
            Paint paint = paintC.get().getPaint();
            g2d.setPaint (paint);
            g2d.fillRect(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
            g2d.setColor(this.outlineC.get());
            g2d.drawRect(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
        } else {
            g2d.setColor(this.outlineC.get());
            g2d.drawRect(toPaint.x, toPaint.y, toPaint.width, toPaint.height);
        }
    }

    public void mouseDragged(MouseEvent e) {
       mouseMoved (e);
    }

    public void mouseMoved(MouseEvent e) {
        armed = true;
        Point p = e.getPoint();
        TEMPLATE_RECTANGLE.setLocation(p);
        if (rect != null) {
            dragged (e.getPoint(), e.getModifiersEx());
            repaintWithRect();
        } else {
            repaintNoRect(p);
        }
    }

    private void dragged (Point p, int modifiers) {
        int currCorner = draggingCorner;
        int corner = rect.setPoint(p, currCorner);

        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            rect.makeSquare(currCorner);
        }
        if (corner == -2 || (corner != currCorner && corner != -1)) {
            if (corner != -2) {
                setDraggingCorner(corner);
            }
        }
    }

    private Rectangle lastPaintedRectangle = new Rectangle();

    int NO_ANCHOR_SIZE = 18;
    private Rectangle TEMPLATE_RECTANGLE = new Rectangle (0, 0, NO_ANCHOR_SIZE, NO_ANCHOR_SIZE);
    private void repaintNoRect(Point p) {
//        Rectangle old = new Rectangle (TEMPLATE_RECTANGLE);
//        TEMPLATE_RECTANGLE.setLocation(p);
//        Rectangle repaintRect = old.union(TEMPLATE_RECTANGLE);
//        c.repaint (repaintRect.x, repaintRect.y,  repaintRect.width, repaintRect.height);
        repainter.requestRepaint(null);
    }

    private void repaintWithRect() {
//        Rectangle repaintRect = lastPaintedRectangle.union(rect);
//        c.repaint (repaintRect.x, repaintRect.y,  repaintRect.width, repaintRect.height);
        repainter.requestRepaint(null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        TEMPLATE_RECTANGLE.setLocation(p);
        if (rect == null) {
            p.x ++;
            p.y ++;
            rect = new MutableRectangle (e.getPoint(), p);
            draggingCorner = rect.nearestCorner(p);
        }
    }

    private static final int CLICK_DIST = 7;
    public void mouseReleased(MouseEvent e) {
        Point p = e.getPoint();
//        boolean inBounds = c.contains(p);
//        if (rect != null && inBounds) {
        if (rect != null) {
            int nearestCorner = rect.nearestCorner(p);
            if (p.distance(rect.getLocation()) < CLICK_DIST) {
                setDraggingCorner (nearestCorner);
                rect.setLocation(p);
            } else {
                setDraggingCorner(nearestCorner);
                rect.setPoint(p, nearestCorner);
                armed = false;
                commit();
                clear();
            }
        }
    }

    boolean committing = false;
    private void commit() {
        repainter.requestCommit();
    }

    public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
        committing = commit;
        assert layer != null;
        assert layer.getSurface() != null;
        assert toString() != null;
        layer.getSurface().beginUndoableOperation(toString());
        try {
            paint (g2d);
        } finally {
            layer.getSurface().endUndoableOperation();
            committing = false;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    boolean armed;
    public void mouseExited(MouseEvent e) {
        armed = false;
        repaintNoRect(e.getPoint());
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (rect == null) {
            return;
        }
        Point p = rect.getLocation();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN :
                p.y ++;
                break;
            case KeyEvent.VK_UP :
                p.y--;
                break;
            case KeyEvent.VK_LEFT :
                p.x --;
                break;
            case KeyEvent.VK_RIGHT :
                p.x ++;
                break;
            case KeyEvent.VK_ENTER :
                commit();
                break;
        }
    }

    public String getInstructions() {
        return NbBundle.getMessage (getClass(), "Click_and_move_mouse_or_click-and-drag_to_draw");
    }

    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(DrawTool.class, "rect.png"));
    }

    public String getName() {
        return toString();
    }

    private Layer layer;
    public void activate(Layer layer) {
        this.layer = layer;
    }

    public void deactivate() {
        rect = null;
        TEMPLATE_RECTANGLE.setBounds (0, 0, NO_ANCHOR_SIZE, NO_ANCHOR_SIZE);
        this.layer = null;
        committing = false;
        armed = false;
    }

    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    public Lookup getLookup() {
        return Lookups.singleton (this);
    }

    private Repainter repainter;
    public void attachRepainter(PaintParticipant.Repainter repainter) {
        this.repainter = repainter;
    }
    
    protected final FillCustomizer paintC = FillCustomizer.getDefault();
    protected final Customizer<Color> outlineC = Customizers.getCustomizer(Color.class, Constants.FOREGROUND);
    protected final Customizer<Boolean> fillC = Customizers.getCustomizer(Boolean.class, Constants.FILL);
    protected final Customizer<Float> strokeC = Customizers.getCustomizer(Float.class, Constants.STROKE);

    public Customizer getCustomizer() {
        return new AggregateCustomizer ("foo", fillC, outlineC, strokeC, paintC);
    }
}
