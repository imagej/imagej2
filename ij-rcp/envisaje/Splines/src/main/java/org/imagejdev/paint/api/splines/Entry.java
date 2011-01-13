package org.imagejdev.paint.api.splines;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;


public interface Entry {
    public void perform(GeneralPath path);
    public void draw(Graphics2D g);
    public Rectangle getDrawBounds(Rectangle r, int areaSize);
    public Node[] getPoints();
    public boolean setPoint (int index, Point2D loc);
    public int size();
    public int hit (Point2D pt, int areaSize);
}