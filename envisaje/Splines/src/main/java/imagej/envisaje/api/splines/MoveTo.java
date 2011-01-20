package imagej.envisaje.api.splines;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class MoveTo extends LocationEntry {
    public MoveTo(double x, double y) {
        super (x, y);
    }
    
    public MoveTo(Point2D p) {
        this (p.getX(), p.getY());
    }
    
    public MoveTo(Point p) {
        this (p.x, p.y);
    }

    public void perform(GeneralPath path) {
        path.moveTo (getX(), getY());
    }

    public void draw(Graphics2D g) {
        Rectangle r = getDrawBounds(null, 4);
        g.fillRect (r.x, r.y, r.width, r.height);
    }
    
    public Node[] getPoints() {
        return new Node[]{ new Node (this, 0, this) };
    }
    
    @Override
    public String toString() {
        return "gp.moveTo (" + getX() + "D, " + getY() +"D);\n";
    }

    public boolean setPoint(int index, Point2D loc) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(index + "");
        }
        boolean result = loc.getX() == x && loc.getY() == y;
        this.setLocation (loc);
        return result;
    }
    
    public int size() {
        return 3;
    }
}