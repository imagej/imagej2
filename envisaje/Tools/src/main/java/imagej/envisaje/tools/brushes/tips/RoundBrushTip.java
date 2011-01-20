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

package imagej.envisaje.tools.brushes.tips;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import imagej.envisaje.tools.spi.BrushTip;
import imagej.envisaje.misccomponents.explorer.Customizable;

/**
 *
 * @author Timothy Boudreau
 */
public class RoundBrushTip implements BrushTip, Customizable {
    
    /** Creates a new instance of RoundBrushTip */
    public RoundBrushTip() {
    }

    public Rectangle draw(Graphics2D g, Point p, int size) {
	int half = size / 2;
	Rectangle result = new Rectangle (p.x - half, p.y - half, size, size);
	g.fillOval (result.x, result.y, result.width, result.height);
	return result;
    }

    public JComponent getCustomizer() {
	return new JLabel ();
    }
}
