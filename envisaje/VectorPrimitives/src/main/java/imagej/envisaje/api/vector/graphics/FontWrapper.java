/*
 * FontWrapper.java
 *
 * Created on September 29, 2006, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.vector.graphics;

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.Primitive;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Sets the Font of a Graphics2D
 *
 * @author Tim Boudreau
 */
public class FontWrapper implements Primitive, Attribute <Font> {
    public long serialVersionUID = 5620138L;
    public final String name;
    public final int size;
    public final int style;

    public FontWrapper(Font f) {
        name = f.getName();
        size = f.getSize();
        style = f.getStyle();
    }

    private FontWrapper (String name, int size, int style) {
        this.name = name;
        this.size = size;
        this.style = style;
    }

    public Font toFont() {
        return new Font (name, style, size);
    }

    public String toString() {
        return "Font: " + name  + ", " + size
                + ", " + styleToString (style);
    }

    private static String styleToString (int style) {
        StringBuilder b = new StringBuilder();
        if ((style & Font.BOLD) != 0) {
            b.append ("BOLD");
        }
        if ((style & Font.ITALIC) != 0) {
            if (b.length() != 0) {
                b.append (", ");
            }
            b.append ("ITALIC");
        }
        if (b.length() == 0) {
            b.append ("PLAIN");
        }
        return b.toString();
    }

    public void paint(Graphics2D g) {
        g.setFont (toFont());
    }

    public Primitive copy() {
        return new FontWrapper (name, size, style);
    }

    public Font get() {
        return toFont();
    }
}
