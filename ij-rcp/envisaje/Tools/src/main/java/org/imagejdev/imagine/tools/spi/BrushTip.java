/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.imagejdev.imagine.tools.spi;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * An interface to enable plugging in different "tips" on the standard brush.
 *
 * @author Timothy Boudreau
 */
public interface BrushTip {

    /** Draw whatever pattern this brush draws.  
     * @param g Graphics object to draw into
     * @param p The cursor location in the Graphics' coordinate space
     * @param size The brush size set in the customizer;  interpret as desired.
     */
    public Rectangle draw(Graphics2D g, Point p, int size);
    
}
