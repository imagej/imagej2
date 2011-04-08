package imagej.envisaje.api.splines;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;


public final class CurveTo extends LocationEntry {
    private final Point2D.Double a;
    private final Point2D.Double b;
    public CurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        super (x, y);
        a = new Double(x1, y1);
        b = new Double(x2, y2);
    }
    
    public int size() {
        return 3;
    }
    
    public CurveTo(Point2D one, Point2D two, Point2D dest) {
        this (one.getX(), one.getY(), two.getX(), two.getY(), dest.getX(), dest.getY());
    }
    
    public CurveTo(Point one, Point two, Point dest) {
        this (one.x, one.y, two.x, two.y, dest.x, dest.y);
    }
    
    public void perform(GeneralPath path) {
        path.curveTo(a.getX(), a.getY(), b.getX(), b.getY(), getX(), getY());
    }

    public void draw(Graphics2D g) {
        new MoveTo(a.getX(), a.getY()).draw (g);
        new MoveTo(b.getX(), b.getY()).draw (g);
        new MoveTo(getX(), getY()).draw (g);
    }

    protected void findDrawBounds(Rectangle r, int areaSize) {
        Rectangle x = new Rectangle();
        Rectangle y = new Rectangle();
        Rectangle z = new Rectangle();
        new MoveTo (a).findDrawBounds(x, areaSize);
        new MoveTo (this).findDrawBounds(y, areaSize);
        new MoveTo (b).findDrawBounds(z, areaSize);
        r.setBounds(x.union(y).union(z));
    }

    Node[] nodes;
    public Node[] getPoints() {
        if (nodes == null) {
            nodes = new Node[]{ new Node(this, 0, this), new Node(this, 1, a), 
                new Node (this, 2, b) };
        }
        return nodes;
    }
    
    public String toString() {
        return "gp.curveTo (" + a.getX() + "D, " + a.getY() + "D, " +
                b.getX() + "D, " + b.getY() + "D, " + getX() +"D, " +
                getY() +"D);\n";
    }

    public boolean setPoint(int index, Point2D loc) {
        Point2D.Double toSet;
        switch (index) {
            case 0:
                toSet = this;
                break;
            case 1:
                toSet = a;
                break;
            case 2 :
                toSet = b;
                break;
            default :
                throw new IndexOutOfBoundsException ("" + index);
        }
        
        boolean result = toSet.getX() == getX() && toSet.getY() ==
                getY();
        toSet.setLocation (loc);
        return result;
    }
}