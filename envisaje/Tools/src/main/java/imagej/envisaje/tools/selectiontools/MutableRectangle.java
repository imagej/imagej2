package imagej.envisaje.tools.selectiontools;

import java.awt.Point;
import java.awt.Rectangle;


/**
 *
 * A rectangle which keeps an always-positive width and height, and which
 * can be adjusted by changing the location of any corner (and if the
 * transformation by adjusting a corner changes the logical corner being
 * dragged, it will say so).
 */
final class MutableRectangle extends Rectangle {
    static int findCorner(Point a, Point b) {
        return (b.x > a.x && b.y > a.y) ? SW
             : (b.x > a.x && b.y < a.y) ? NE
             : (b.x < a.x && b.y > a.y) ? SE
             : (b.x < a.x && b.y < a.y) ? NW
                                        : ANY;
    }
    static final int ANY = 0;
    static final int NE = 1;
    static final int SE = 2;
    static final int NW = 3;
    static final int SW = 4;

    /**
     * Given two points, generate a rectangle with positive width
     * and height
     */
    private static Rectangle genRectangle(Point nw, Point se) {
        Point a = new Point();
        Point b = new Point();

        if (nw.x == se.x || nw.y == se.y) {
            throw new IllegalArgumentException("" + nw + se);
        }
        if (nw.x < se.x) {
            a.x = nw.x;
            b.x = se.x;
        }
        else {
            a.x = se.x;
            b.x = nw.x;
        }
        if (nw.y < se.y) {
            a.y = nw.y;
            b.y = se.y;
        }
        else {
            a.y = se.y;
            b.y = nw.y;
        }
        int w = b.x - a.x;
        int h = b.y - a.y;

        return new Rectangle(a.x, a.y, w, h);
    }

    public MutableRectangle(Point nw, Point se) {
        super(genRectangle(nw, se));
    }

    public void makeSquare(int corner) {
        int n = Math.min(width, height);

        switch(corner) {
            case NW:

                x = (x + width) - n;

                y = (y + height) - n;

                width = n;

                height = n;

                break;
            case NE:

                width = n;

                y = (y + height) - n;

                height = n;

                break;
            case SW:

                width = n;

                height = n;

                break;
            case SE:

                x = (x + width) - n;

                width = n;

                height = n;

                break;
        }
    }

    /**
     * Get a point representing the requested corner
     */

    public Point getPoint(int which) {
        Point result = getLocation();

        switch(which) {
            case NW:

                break;
            case NE:

                result.x += width;

                break;
            case SE:

                result.y += height;

                break;
            case SW:

                result.x += width;

                result.y += height;

                break;
            default:

                throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Get the nearest corner to the passed point
     */

    public int nearestCorner(Point p) {
        int best = 0;
        double bestDistance = Integer.MAX_VALUE;

        for (int i = NE; i <= SW; i++) {
            Point check = getPoint(i);
            double dist = check.distance(p);

            if (dist < bestDistance) {
                bestDistance = dist;
                best = i;
            }
        }
        return best;
    }

    /**
     * Set one of the corners to a new location.
     * @param p The location
     * @param which ID of the corner to set
     * @return -1 if nothing has changed (the corner to set to the new point
     * already had those coordinates);  -2 if the corner was moved, but the
     * transform did not cause the corner to become logically a different
     * corner;  a corner ID, != which, that is now the corner being moved.
     * i.e. if you drag the southeast corner above the northeast corner,
     * you are now dragging the northeast corner.
     */

    public int setPoint(Point p, int which) {
        int newX;
        int newY;
        int newW;
        int newH;
        int result = -1;

        switch(which) {
            case NW:

                newW = width + (x - p.x);

                newH = height + (y - p.y);

                if (changeBounds(p.x, p.y, newW, newH)) {
                    if (newW <= 0 && newH > 0) {
                        result = NE;
                    }
                    else                        if (newW <= 0 && newH <= 0) {
                            result = SE;
                        }
                        else                            if (newW > 0 && newH <= 0) {
                                result = SE;
                            }
                            else {
                                result = -2;
                            }
                }

                break;
            case NE:

                newW = p.x - x;

                newY = p.y;

                newH = height + (y - p.y);

                if (changeBounds(x, newY, newW, newH)) {
                    if (newW <= 0 && newH > 0) {
                        result = NW;
                    }
                    else                        if (newW <= 0 && newH <= 0) {
                            result = SE;
                        }
                        else                            if (newW > 0 && newH <= 0) {
                                result = SW;
                            }
                            else {
                                result = -2;
                            }
                }

                break;
            case SW:

                newW = p.x - x;

                newH = p.y - y;

                if (changeBounds(x, y, newW, newH)) {
                    if (newW <= 0 && newH > 0) {
                        result = SE;
                    }
                    else                        if (newW <= 0 && newH <= 0) {
                            result = NW;
                        }
                        else                            if (newW > 0 && newH <= 0) {
                                result = NE;
                            }
                            else {
                                result = -2;
                            }
                }

                break;
            case SE:

                newX = p.x;

                newW = width + (x - p.x);

                newH = p.y - y;

                if (changeBounds(newX, y, newW, newH)) {
                    if (newW <= 0 && newH > 0) {
                        result = SW;
                    }
                    else                        if (newW <= 0 && newH <= 0) {
                            result = NE;
                        }
                        else                            if (newW > 0 && newH <= 0) {
                                result = NW;
                            }
                            else {
                                result = -2;
                            }
                }

                break;
            default:

                assert false : "Bad corner: " + which;
        }
        return result;
    }

    /**
     * Set the bounds, returning true if an actual change occurred
     */

    private boolean changeBounds(int x, int y, int w, int h) {
        boolean change = x != this.x || y != this.y || w != this.width ||
                             h != this.height;

        if (change) {
            setBounds(x, y, w, h);
        }
        return change;
    }

    /**
     * Overridden to convert negative width/height into relocation with
     * positive width/height
     */

    public void setBounds(int x, int y, int w, int h) {
        if (w < 0) {
            int newW = -w;

            x += w;
            w = newW;
        }
        if (h < 0) {
            int newH = -h;

            y += h;
            h = newH;
        }
        super.setBounds(x, y, w, h);
    }
}