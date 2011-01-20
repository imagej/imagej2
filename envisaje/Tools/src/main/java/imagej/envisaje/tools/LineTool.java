/*
 * LineTool.java
 *
 * Created on September 29, 2006, 4:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import imagej.envisaje.api.toolcustomizers.AggregateCustomizer;
import imagej.envisaje.api.toolcustomizers.Customizers;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.api.toolcustomizers.Constants;
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

public class LineTool implements Tool, PaintParticipant, MouseMotionListener, MouseListener, KeyListener, CustomizerProvider {
    private PaintParticipant.Repainter repainter;
    public boolean canAttach (Layer layer) {
        return layer.getLookup().lookup (Surface.class) != null;
    }

    private BasicStroke stroke = new BasicStroke (2.5F);
    public void setStroke (float val) {
        stroke = new BasicStroke (val, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public float getStroke() {
        return stroke.getEndCap();
    }

    public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
        if (commit) {
            layer.getSurface().beginUndoableOperation(toString());
        }
        paint (g2d);
        if (commit) {
            try {
                layer.getSurface().endUndoableOperation();
            } finally {
                clear();
            }
        }
    }

    private void paint(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setStroke (stroke);
        g2d.setPaint(fc.get().getPaint());
        Point p = null;
        int max = points.size();
        int[] xpoints = new int[max];
        int[] ypoints = new int[max];
        int ix = 0;
        for (Iterator i=points.iterator(); i.hasNext();) {
            p = (Point) i.next();
            xpoints[ix] = p.x;
            ypoints[ix] = p.y;
            ix++;

        }
        if (max > 0) {
            g2d.drawPolyline(xpoints, ypoints, max);
        }
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved (e);
    }

    Point lastLoc = null;
    public void mouseMoved(MouseEvent e) {
        lastLoc = e.getPoint();
        if (!points.isEmpty()) {
            int modifiers = e.getModifiersEx();
            if (modifiers == KeyEvent.CTRL_DOWN_MASK) {
                Point p = lastPoint();
                lastLoc.x = p.x;
            } else if (modifiers == KeyEvent.SHIFT_DOWN_MASK) {
                Point p = lastPoint();
                lastLoc.y = p.y;
            }
        }
        change();
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            commit();
        }
    }

    private List points = new ArrayList();
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        addPoint (e.getPoint(), e.getModifiersEx());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private Point lastPoint() {
        return points.isEmpty() ? null : (Point) points.get(points.size() - 1);
    }

    private void clear() {
        points.clear();
        change();
    }

    private void addPoint(Point point, int modifiers) {
        if (!points.isEmpty()) {
            if (modifiers == KeyEvent.CTRL_DOWN_MASK) {
                Point p = lastPoint();
                point.x = p.x;
            } else if (modifiers == KeyEvent.SHIFT_DOWN_MASK) {
                Point p = lastPoint();
                point.y = p.y;
            }
        }
        points.add (point);
        change();
    }

    private void change() {
        if (repainter != null) {
            repainter.requestRepaint(null);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    private void commit() {
        if (!points.isEmpty()) {
            repainter.requestCommit();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && lastLoc != null) {
            addPoint (lastLoc, e.getModifiersEx());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            clear();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !points.isEmpty()) {
            points.remove(points.size()-1);
            change();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            commit();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    @Override
    public String toString() {
        return NbBundle.getMessage (LineTool.class, "Line"); //NOI18N
    }

    public String getInstructions() {
        return NbBundle.getMessage (LineTool.class, 
                "Click_to_create_anchor_points,_Enter_or_dbl-click_to_finish"); //NOI18N
    }

    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(DrawTool.class, "line.png"));
    }

    public String getName() {
        return toString();
    }

    private Layer layer;
    public void activate(Layer layer) {
        this.layer = layer;
    }

    public void deactivate() {
        points.clear();
        lastLoc = null;
        layer = null;
        repainter = null;
    }

    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public void attachRepainter(PaintParticipant.Repainter repainter) {
        this.repainter = repainter;
    }

    private final FillCustomizer fc = FillCustomizer.getDefault();
    private final Customizer<Boolean> fillC = Customizers.getCustomizer(Boolean.class, Constants.FILL);
    private final Customizer<Float> strokeC = Customizers.getCustomizer(Float.class, Constants.STROKE);
    public Customizer getCustomizer() {
        return new AggregateCustomizer("stuff", fillC, strokeC, fc); //NOI18N
    }
}
