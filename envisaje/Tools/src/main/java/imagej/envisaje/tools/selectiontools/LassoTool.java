/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.tools.selectiontools;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.api.selection.Selection.Op;
import imagej.envisaje.spi.tools.PaintParticipant;
import imagej.envisaje.spi.tools.Tool;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.splines.Close;
import imagej.envisaje.api.splines.CurveTo;
import imagej.envisaje.api.splines.DefaultPathModel;
import imagej.envisaje.api.splines.Entry;
import imagej.envisaje.api.splines.Hit;
import imagej.envisaje.api.splines.LineTo;
import imagej.envisaje.api.splines.MoveTo;
import imagej.envisaje.api.splines.Node;
import imagej.envisaje.api.splines.PathModel;
import imagej.envisaje.api.splines.QuadTo;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * A tool for adjusting control points on a selection
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=imagej.envisaje.spi.tools.Tool.class)

public class LassoTool extends MouseAdapter implements Tool, PaintParticipant, KeyListener, MouseMotionListener {
    private Repainter repainter;
    private Layer layer;
    private PathModel<Entry> mdl;
    private static final int hitZone = 10;

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mdl != null) {
            if (mdl.hit(e.getPoint(), hitZone) != null) {
                repainter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                repainter.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (hit != null) {
//            hit.getNode().setLocation(e.getPoint());
            mdl.setPoint(hit.getNode(), e.getPoint());
            repainter.requestRepaint();
        } else {
            mdl.add(new LineTo(e.getPoint()));
        }
    }

    private Hit hit;
    @Override
    public void mousePressed(MouseEvent e) {
        if (mdl == null) {
            mdl = DefaultPathModel.newInstance();
        } else {
            Hit theHit = mdl.hit(e.getPoint(), hitZone);
            if (theHit != null) {
                hit = theHit;
                //Test for AIOOBE
                hit.getNode();
                repainter.requestRepaint(mdl.getBounds());
            } else {
                if (hit != null) {
                    repainter.requestRepaint(mdl.getBounds());
                }
                hit = null;
            }
        }
        repainter.requestRepaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (hit == null) {
            if (e.getClickCount() >= 2) {
                mdl.add(new Close());
                commit (e.getModifiersEx());
            } else {
                boolean shiftDown = (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0;
                boolean altDown = (e.getModifiers() & KeyEvent.ALT_MASK) != 0;
                boolean ctrlDown = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
                if (mdl.isEmpty()) {
                    mdl.add (new MoveTo(e.getPoint()));
                } else {
                    if (ctrlDown) {
                        mdl.add (new MoveTo(e.getPoint()));
                    } else if (altDown) {
                        Entry entry = mdl.get(mdl.size() - 1);
                        Point intermed = new Point(e.getPoint());
                        intermed.x -= 20;
                        intermed.y -= 20; //XXX
                        mdl.add (new QuadTo (intermed.x, intermed.y, e.getPoint().x, e.getPoint().y));
                    } else if (shiftDown) {
                        Point ctrl1 = new Point(e.getPoint());
                        Point ctrl2 = new Point(e.getPoint());
                        ctrl1.x -= 15;
                        ctrl2.y -= 15; //XXX check bounds
                        mdl.add (new CurveTo(ctrl1, ctrl2, e.getPoint()));
                    } else {
                        mdl.add(new LineTo(e.getPoint()));
                    }
                }
                repainter.requestRepaint(mdl.getBounds());
            }
        } else if (e.getClickCount() >= 2) {
            mdl.add (new Close());
            commit (e.getModifiersEx());
        }
        repainter.requestRepaint();
    }

    public Icon getIcon() {
        return new ImageIcon (Utilities.loadImage(
                "imagej/envisaje/tools/resources/lasso.png"));
    }

    public String getName() {
        return NbBundle.getMessage (LassoTool.class, "Lasso");
    }

    public void activate(Layer layer) {
        this.layer = layer;
        Selection s = layer.getLookup().lookup(Selection.class);
        if (s != null) {
            Shape shape = s.asShape();
            if (shape != null) {
                mdl = DefaultPathModel.create(shape);
            }
        }
    }

    public void deactivate() {
       repainter.setCursor(Cursor.getDefaultCursor());
             this.layer = null;
        repainter = null;
        commit(0);
        mdl = null;
    }

    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    public boolean canAttach(Layer layer) {
        return layer.getLookup().lookup(Selection.class) != null &&
                layer.getLookup().lookup(Selection.class).type() == Shape.class;
    }

    public void attachRepainter(Repainter repainter) {
        this.repainter = repainter;
        repainter.requestRepaint();
    }

    public void paint(Graphics2D g2d, Rectangle layerBounds, boolean commit) {
        if (mdl != null && !commit) {
            Selection.paintSelectionAsShape(g2d, mdl);
            Node hitNode = hit == null ? null : hit.getNode();
            if (hitNode != null) {
                hitNode.paint(g2d, true);
            }
            for (Entry entry : mdl) {
                Node[] nodes = entry.getPoints();
                for (Node n : nodes) {
                    if (n != hitNode && n != null) {
                        n.paint(g2d, false);
                    }
                }
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        
    }

    public void keyPressed(KeyEvent e) {
        if (hit != null) {
            int m = e.getModifiersEx();
            boolean shiftDown = (m & KeyEvent.SHIFT_DOWN_MASK) != 0;
            boolean altDown = (m & KeyEvent.ALT_DOWN_MASK) != 0;
            int amt = shiftDown ? altDown ? 10 : 5 : 1;
            Node n = hit.getNode();
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP :
                    n.setLocation(new Point2D.Double(n.getX(), n.y() - amt));
                    break;
                case KeyEvent.VK_DOWN :
                    n.setLocation(new Point2D.Double(n.getX(), n.y() + amt));
                    break;
                case KeyEvent.VK_RIGHT :
                    n.setLocation(new Point2D.Double(n.getX() + amt, n.y()));
                    break;
                case KeyEvent.VK_LEFT :
                    n.setLocation(new Point2D.Double(n.getX() - amt, n.y()));
                    break;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!mdl.isEmpty()) {
                if (!(mdl.get(mdl.size() - 1) instanceof Close)) {
                    mdl.add(new Close());
                }
                commit(e.getModifiersEx());
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        
    }
    
    private void commit(int mods) {
        if (layer != null) {
            Selection<Shape> s = layer.getLookup().lookup(Selection.class);
            if (s == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                boolean shiftDown = (mods & KeyEvent.SHIFT_DOWN_MASK) != 0;
                Op op = shiftDown ? Op.ADD : Op.REPLACE;
                //XXX add ways to merge, etc
                s.add(mdl, op);
            }
        }
    }
}
