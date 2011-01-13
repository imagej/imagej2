package org.imagejdev.paint.api.splines;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

abstract class LocationEntry extends Point2D.Double implements Entry {
    final Rectangle test = new Rectangle();
    public LocationEntry(double x, double y) {
        super (x, y);
    }
    public Point2D getLocation() {
        return this;
    }

    @Override
    public String toString() {
        return '[' + getClass().getName() + getX() + ',' + getY() + ']';
    }
    
    public int hit (Point2D pt, int areaSize) {
        Point2D.Double[] p = getPoints();
        test.width = areaSize;
        test.height = areaSize;
        
        int sz = areaSize / 2;
        for (int i = 0; i < p.length; i++) {
            test.x = (int) (p[i].x - sz);
            test.y = (int) (p[i].y - sz);
            boolean match = test.contains(pt);
            if (match) {
                return i;
            }
        }
        return -1;
    }
    
    public final Rectangle getDrawBounds(Rectangle r, int areaSize) {
        r = cr(r);
        findDrawBounds (r, areaSize);
        return r;
    }
    
    protected void findDrawBounds (Rectangle r, int areaSize) {
        r.x = (int) getX()  - areaSize;
        r.y = (int) getY()  - areaSize;
        r.width = areaSize;
        r.height = areaSize;
    }
    
    static final Point p (Point2D.Double d) {
        return new Point ((int) d.x, (int) d.y);
    }
    
    static final Rectangle cr(Rectangle r) {
        return r == null ? new Rectangle() : r;
    }
}