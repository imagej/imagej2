/*
 * Brush.java
 *
 * Created on October 15, 2005, 4:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.tools.spi;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Place .instance files for Brush implementations in the 
 * <code>brushes/</code> folder of the system filesystem and
 * they will be available when the PaintBrush tool is selected.
 * 
 * A brush is a tool which can draw on the screen using the mouse.
 * The standard brush can be found in the class StandardBrush;
 * it allows other modules to plug in different "tips" which
 * draw in different ways.
 *
 * @author Timothy Boudreau
 */
public interface Brush {
    public Component getCustomizer();
    public boolean isAntialiased();
    public Rectangle paint (Graphics2D g, Point loc);
}
