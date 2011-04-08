/*
 * DrawTool.java
 *
 * Created on September 27, 2006, 8:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.spi.tools.CustomizerProvider;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.api.toolcustomizers.Constants;
import imagej.envisaje.api.toolcustomizers.Customizers;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)

public class DrawTool implements Tool, MouseListener, MouseMotionListener, CustomizerProvider {
    private Layer layer;

    public DrawTool() {
    }

    public boolean canAttach (Layer layer) {
        return layer.getLookup().lookup (Surface.class) != null;
    }

    Graphics2D g = null;
    public void mouseClicked(MouseEvent e) {
    }

    private Point lastPoint = null;
    public void mousePressed(MouseEvent e) {
        g = layer.getSurface().getGraphics();
        layer.getSurface().beginUndoableOperation(toString());
        lastPoint = e.getPoint();
        assert g != null;
    }

    public void mouseReleased(MouseEvent e) {
        layer.getSurface().endUndoableOperation();
        lastPoint = null;
        g = null;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    private BasicStroke LINE_STROKE = new BasicStroke (8);

    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        g.setColor (cust.get());
//        if (lastPoint != null && (Math.abs(lastPoint.x - p.x) > size * 2 ||  Math.abs (lastPoint.y - p.y) > size * 2)) {
        if (lastPoint != null) {
            g.setStroke (LINE_STROKE);
            g.drawLine (lastPoint.x, lastPoint.y, p.x, p.y);
        }
        lastPoint = p;
        int half = Math.max (1, (int) (getSize() / 2));
        int sz = (int) size;
        g.fillOval(p.x - half, p.y - half, sz, sz);
//        pc.repaint (p.x - half, p.y - half, sz, sz);
    }

    private float size = 5;
    public void setSize (float i) {
        this.size = i / 2;
        LINE_STROKE = new BasicStroke (i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public float getSize() {
        return size;
    }

    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public String toString() {
        return NbBundle.getMessage (getClass(), "Draw");
    }

    public String getInstructions() {
        return NbBundle.getMessage (getClass(), "Click_and_drag_to_draw");
    }

    public Icon getIcon() {
        return new ImageIcon (DrawTool.load(DrawTool.class, "draw.png"));
    }

    static BufferedImage load (Class c, String s) {
        return (BufferedImage)
                Utilities.loadImage ("imagej/envisaje/tools/resources/" + s);
    }

    public String getName() {
        return toString();
    }

    public void activate(Layer layer) {
        this.layer = layer;
    }

    public void deactivate() {
        if (this.layer != null && lastPoint != null) {
            layer.getSurface().cancelUndoableOperation();
        }
        this.layer = null;
    }

    public Lookup getLookup() {
        return Lookups.fixed (this);
    }

    private final Customizer<Color> cust = Customizers.getCustomizer(Color.class, Constants.FOREGROUND);
    public Customizer getCustomizer() {
        return cust;
    }
}
