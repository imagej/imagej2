package imagej.envisaje.api.splines;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;


public final class QuadTo extends LocationEntry{
    private final Point2D.Double p;

    public QuadTo(double x1, double y1, double x2, double y2) {
        super (x1, y1);
        this.p = new Double(x2, y2);
    }

    public QuadTo(Point a, Point b) {
        this (a.x, a.y, b.x, b.y);
    }
    
    public QuadTo(Point2D a, Point2D b) {
        this (a.getX(), a.getY(), b.getX(), b.getY());
    }

    public void perform(GeneralPath path) {
        path.quadTo(getX(), getY(), p.getX(), p.getY());
    }
    
    public Rectangle getBounds(Rectangle r) {
        if (true) throw new UnsupportedOperationException ("Not Implemented");
        return cr(r);
    }

    public void draw(Graphics2D g) {
        new MoveTo(p.getX(), p.getY()).draw (g);
        new MoveTo(getX(), getY()).draw (g);
    }

    protected void findDrawBounds(Rectangle r, int areaSize) {
        Rectangle a = new Rectangle();
        new MoveTo (p).findDrawBounds(a, areaSize);
        Rectangle b = new Rectangle();
        new MoveTo (this).findDrawBounds(b, areaSize);
        r.setBounds(a.union(b));
    }

    public Node[] getPoints() {
        return new Node [] { 
            new Node(this, 0, this), 
            new Node(this, 1, p)
        };
    }
    
    public String toString() {
        return "gp.quadTo (" + getX() + "D, " + getY() +"D, " +
                p.getX() + "D, " + p.getY() + ");\n";
    }
    
    public boolean setPoint(int index, Point2D loc) {
        Point2D toSet;
        switch (index) {
            case 0:
                toSet = this;
                break;
            case 1:
                toSet = p;
                break;
            default :
                throw new IndexOutOfBoundsException ("" + index);
        }
        
        boolean result = toSet.getX() == getX() && toSet.getY() ==
                getY();
        toSet.setLocation (loc);
        return result;
    }
    
    public int size() {
        return 2;
    }
}