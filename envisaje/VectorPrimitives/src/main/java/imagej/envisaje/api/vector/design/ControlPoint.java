/*
 * ControlPoint.java
 *
 * Created on October 30, 2006, 10:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.design;

import imagej.envisaje.api.vector.Adjustable;
import imagej.envisaje.api.vector.Mutable;
import imagej.envisaje.api.vector.util.Pt;
import imagej.envisaje.api.vector.util.Size;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Tim Boudreau
 */
public final class ControlPoint <T extends Adjustable> {
    private final T primitive;
    private final ControlPoint.Controller controller;
    private int index;
    private boolean virtual;
    private final double[] vals;
    public ControlPoint(T primitive, Controller controller, int index, boolean virtual) {
        this.primitive = primitive;
        this.index = index;
        this.controller = controller;
        this.virtual = virtual;
        vals = new double[primitive.getControlPointCount() * 2];
    }

    public T getPrimitive() {
        return primitive;
    }
    
    private void upd() {
        primitive.getControlPoints(vals);
    }

    public double getX() {
        upd();
        int offset = index * 2;
        return vals [offset];
    }

    public double getY() {
        upd();
        int offset = (index * 2) + 1;
        return vals [offset];
    }

    public void move (double dx, double dy) {
        if (dx != 0 && dy != 0) {
            upd();
            double x = vals [index * 2];
            double y = vals [(index * 2) + 1];
            x += dx;
            y += dy;
            primitive.setControlPointLocation(index, new Pt (x, y));
            change();
        }
    }

    public void set (double dx, double dy) {
        primitive.setControlPointLocation(index, new Pt (dx, dy));
        change();
    }

    public void delete() {
        if (!isVirtual() && primitive instanceof Mutable) {
            if (!((Mutable) primitive).delete (index)) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    private void change() {
        controller.changed(this);
    }

    public boolean canDelete() {
        return !isVirtual() && primitive instanceof Mutable;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean hit (double hx, double hy) {
        Size s = controller.getControlPointSize();
        upd();
        double x = vals [index * 2];
        double y = vals [(index * 2) + 1];
        double halfx = s.w / 2;
        double halfy = s.h / 2;
        return new Rectangle2D.Double (x - halfx, y - halfy, s.w, s.h).contains (hx, hy);
    }

    public interface Controller {
        public void changed (ControlPoint pt);
        public Size getControlPointSize();
    }

    public static abstract class State {
        public abstract void restore(Adjustable target);
    }
}
