package imagej.envisaje.api.splines;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Tim Boudreau
 */
public final class Node extends Point2D.Double {
    public final int index;
    public final Entry entry;
    public Node(Entry entry, int ix, double x, double y) {
        super (x, y);
        this.index = ix;
        this.entry = entry;
    }
    
    public Node(Entry entry, int ix, Point2D pt) {
        super (pt.getX(), pt.getY());
        this.index = ix;
        this.entry = entry;
    }
    
    @Override
    public void setLocation(Point2D p) {
	super.setLocation(p.getX(), p.getY());
        entry.setPoint(index, p);
    }
    
    @Override
    public boolean equals (Object o) {
        if (o instanceof Node) {
            Node other = (Node) o;
            return match (other) && other.entry == entry && other.index == 
                    index;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int xx = (int) getX() * 1000;
        int yy = (int) getY() * 1000;
        return (xx * yy * (index + 1)) ^ entry.hashCode();
    }
    
    public boolean match (Point2D pt) {
        return pt.getX() == getX() && pt.getY() == getY();
    }
    
    public int getIndex() {
        return index;
    }
    
    public int x() {
        return (int) x;
    }
    
    public int y() {
        return (int) y;
    }
    
    public Point point() {
        return new Point (x(), y());
    }
    
    public void paint (Graphics2D g2d, boolean selected) {
        //XXX handle zooming - points should not grow
        Rectangle2D.Double r = new Rectangle2D.Double (x - 3, y - 3, 6, 6);
        Color c = g2d.getColor();
        g2d.setColor(selected ? Color.ORANGE : Color.WHITE);
        g2d.fill(r);
        g2d.setColor(Color.BLACK);
        g2d.draw(r);
        g2d.setColor (c);
    }
}
