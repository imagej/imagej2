/*
 * RepaintHandle.java
 *
 * Created on October 15, 2005, 8:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.spi.image;

import java.awt.Cursor;

/**
 * Object passed to the PictureImplementation constructor.  Allows the Picture
 * implementation (and indirectly its individual Layer and Surface objects) to
 * notify when some part of the image has been modified, and the UI displaying
 * it should repaint.  Also enables tools to set the cursor.
 *
 * @author Timothy Boudreau
 */
public interface RepaintHandle {
    public void repaintArea (int x, int y, int w, int h);
    public void setCursor (Cursor cursor);
}
