package org.imagejdev.paint.api.splines;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class Close implements Entry {

    public void perform(GeneralPath path) {
        path.closePath();
    }

    public void draw(Graphics2D g) {
        //do nothing
    }

    public Rectangle getDrawBounds(Rectangle r, int areaSize) {
        return new Rectangle (0,0);
    }

    public Node[] getPoints() {
        return new Node[0];
    }

    public boolean setPoint(int index, Point2D loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int size() {
        return 1;
    }

    public int hit(Point2D pt, int areaSize) {
        return 0;
    }
}