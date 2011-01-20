package imagej.envisaje.api.splines;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 * @author Tim Boudreau
 */
public final class Hit {
    public final int pointIndex;
    public final int entryIndex;
    public final Entry entry;
    public final PathModel model;
    
    /** Creates a new instance of Hit */
    public Hit(PathModel model, Entry entry, int entryIndex, int pointIndex) {
        if (model == null || entry == null) {
            throw new NullPointerException();
        }
        this.entry = entry;
        this.entryIndex = entryIndex;
        this.pointIndex = pointIndex;
        this.model = model;
    }
    
    @Override
    public String toString() {
        return "Hit for" + entry + " entryIndex=" + entryIndex + " pointIndex=" +
                pointIndex;
    }
    
    public int hitPoint (Point2D p, int sz) {
        return entry.hit(p, sz);
    }
    
    public Point2D getPoint () {
        return entry.getPoints()[pointIndex];
    }
    
    public void setPoint (Point2D pt) {
        entry.setPoint(pointIndex, pt);
    }
    
    @Override
    public boolean equals (Object o) {
        if (o instanceof Hit) {
            Hit h = (Hit) o;
            return h.pointIndex == pointIndex && h.entry.equals(entry) &&
                    h.model.equals(model);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return entry.hashCode() * (pointIndex + 1) * entry.hashCode() * 
                model.hashCode();
    }
    
    public Node getNode () {
        Node[] nodes = entry.getPoints();
        if (!(entry instanceof Close) && pointIndex >= nodes.length) {
            throw new ArrayIndexOutOfBoundsException("Bad index for Hit: " + this);
        }
        return entry instanceof Close ? null : nodes[pointIndex];
    }
    
    public Entry getEntry() {
        return entry;
    }
}
