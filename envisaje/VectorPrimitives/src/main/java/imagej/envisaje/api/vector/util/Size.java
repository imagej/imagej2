/*
 * Size.java
 *
 * Created on October 31, 2006, 1:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.util;

import java.io.Serializable;

/**
 * Immutable equivalent of a Dimension
 *
 * @author Tim Boudreau
 */
public final class Size implements Serializable {
    public final double w;
    public final double h;
    public Size(final double w, final double h) {
        this.w = w;
        this.h = h;
    }

    public boolean equals (Object o) {
        boolean result = o instanceof Size;
        if (result) {
            Size s = (Size) o;
            result = s.w == w && s.h == h;
        }
        return result;
    }

    public int hashCode() {
        return (int) (w * h * 1000);
    }
}
