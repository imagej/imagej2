/*
 * Mutable.java
 *
 * Created on November 1, 2006, 4:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector;

/**
 * Represents a Shape consisting of a set of points which may
 * be added to or deleted from.
 *
 * @author Tim Boudreau
 */
public interface Mutable extends Adjustable {
    /**
     * Delete the point at the specified index.
     * @param pointIndex An index from 0 to the point count of the shape
     * @throws IndexOutOfBoundsException if the point is not in range
     * @return true if the point could be deleted, false if it could not
     *  (it will be false for 2 dimensional objects which only have 3 remaining
     *   sides, 1 dimensional objects that have only one remaining line, and so
     *   forth)
     */
    public boolean delete (int pointIndex);

    /**
     * Insert a new point in the shape.  Note that calling this method may
     * mean more than one point is inserted - in the case of bezier curves in a
     * PathIteratorWrapper, two additional control points will be generated
     * @param x the x coordinate of the new point
     * @param y the y coordinate of the new point
     * @param index the index in the set of points which the
     *  new point should have
     * @param kind the <i>kind</i> of the point.  Ignored except in
     *  the case of PathIteratorWrapper and GlyphVectorWrapper.  Acceptable
     *  values are the constants on PathIterator.
     * @return true if the point could be inserted (it may not be possible if
     *  a point with the exact coordinates of the passed arguments already exists)
     */
    public boolean insert (double x, double y, int index, int kind);

    /**
     * Get the index of the point in the shape nearest the passed x/y coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return an index into the total point count indicating the nearest existing
     *  point making up the shape to the point that was passed
     */
    public int getPointIndexNearest (double x, double y);
}
