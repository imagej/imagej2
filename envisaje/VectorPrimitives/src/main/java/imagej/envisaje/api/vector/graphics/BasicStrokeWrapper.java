/*
 * BasicStrokeWrapper.java
 *
 * Created on September 27, 2006, 9:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.graphics;

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Primitive;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Arrays;


/**
 * Sets the Stroke of a Graphics2D.
 *
 * @author Tim Boudreau
 */
public final class BasicStrokeWrapper implements Primitive, Attribute <BasicStroke> {
    public float miterLimit;
    public float[] dashArray;
    public float dashPhase;
    public float lineWidth;
    public byte endCap;
    public byte lineJoin;

    public BasicStrokeWrapper(BasicStroke stroke) {
        this.dashArray = stroke.getDashArray();
        this.dashPhase = stroke.getDashPhase();
        this.endCap = (byte) stroke.getEndCap();
        this.lineJoin = (byte) stroke.getLineJoin();
        this.lineWidth = stroke.getLineWidth();
        this.miterLimit = stroke.getMiterLimit();
    }
    
    private BasicStrokeWrapper (BasicStrokeWrapper w) {
        this.dashArray = new float[w.dashArray.length];
        System.arraycopy (w.dashArray, 0, dashArray, 0, dashArray.length);
        this.dashPhase = w.dashPhase;
        this.endCap = w.endCap;
        this.lineJoin = w.lineJoin;
        this.lineWidth = w.lineWidth;
        this.miterLimit = w.miterLimit;
    }
    
    public BasicStrokeWrapper copy (float newLineWidth) {
        BasicStrokeWrapper result = new BasicStrokeWrapper (this);
        result.lineWidth = newLineWidth;
        return result;
    }

    public BasicStroke toStroke() {
        return new BasicStroke (lineWidth, endCap, lineJoin, miterLimit,
                dashArray, dashPhase);
    }

    public String toString() {
        return "BasicStroke: lineWidth " +
                lineWidth + " lineJoin  " + lineJoin +
                " endCap " + endCap + " dashPhase "
                + dashPhase + " miterLimit " +
                miterLimit + " dashArray " +
                toString (dashArray);
    }

    private static String toString (float[] f) {
        if (f == null) return "null";
        StringBuilder b = new StringBuilder (30);
        b.append ('[');
        for (int i = 0; i < f.length; i++) {
            b.append (f[i]);
            if (i != f.length - 1) {
                b.append (", ");
            }
        }
        b.append (']');
        return b.toString();
    }

    public boolean equals (Object o) {
        boolean result = o instanceof BasicStrokeWrapper;
        if (result) {
            BasicStrokeWrapper b = (BasicStrokeWrapper) o;
            result &= endCap == b.endCap && lineJoin == b.lineJoin
                && lineWidth == b.lineWidth && miterLimit ==
                b.miterLimit && dashPhase == b.dashPhase &&
                Arrays.equals (dashArray,
                b.dashArray);
        }
        return result;
    }

    public int hashCode() {
        int result = Arrays.hashCode(dashArray) +
                (int) (dashPhase * 100) + endCap +
                lineJoin + ((int) lineJoin * 100) +
                ((int) miterLimit * 100);
        return result;
    }

    public void paint(Graphics2D g) {
        g.setStroke(toStroke());
    }

    public Primitive copy() {
        return new BasicStrokeWrapper (this);
    }

    public BasicStroke get() {
        return toStroke();
    }
    
    public double getLineWidth() {
        return lineWidth;
    }
}
