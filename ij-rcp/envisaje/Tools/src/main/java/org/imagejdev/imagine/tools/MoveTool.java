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

package org.imagejdev.imagine.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.spi.tools.NonPaintingTool;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.tools.spi.MouseDrivenTool;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service=org.imagejdev.imagine.spi.tools.Tool.class)

public class MoveTool extends MouseDrivenTool implements KeyListener, NonPaintingTool {

    public MoveTool() {
        super( "org/imagejdev/imagine/tools/resources/movetool.png", //NOI18N
                NbBundle.getMessage(BrushTool.class, "NAME_MoveTool")); //NOI18N
    }

    private Point startPoint = null;
    protected void beginOperation(Point p, int modifiers) {
        startPoint = p;
    }

    protected void dragged(Point p, int modifiers) {
        int xdiff = startPoint.x - p.x;
        int ydiff = startPoint.y - p.y;
        if (xdiff != 0 || ydiff != 0) {
            Surface surface = getLayer().getSurface();
            Point loc = surface.getLocation();
            loc.x -= xdiff;
            loc.y -= ydiff;
            surface.setLocation(loc);
            startPoint = p;
        }
    }

    public boolean canAttach(Layer layer) {
        return layer.getLookup().lookup(Surface.class) != null;
    }

    protected void endOperation(Point p, int modifiers) {

    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (startPoint == null) {
            startPoint = new Point(0,0);
        }
        int amount = 1;
        if (e.isShiftDown()) {
            amount *= 2;
        }
        if (e.isControlDown()) {
            amount *= 4;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT :
                dragged(new Point(startPoint.x + amount, startPoint.y), 0);
                break;
            case KeyEvent.VK_LEFT :
                dragged(new Point(startPoint.x - amount, startPoint.y), 0);
                break;
            case KeyEvent.VK_UP :
                dragged(new Point(startPoint.x, startPoint.y - amount), 0);
                break;
            case KeyEvent.VK_DOWN :
                dragged(new Point(startPoint.x, startPoint.y + amount), 0);
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    protected void activated(Layer layer) {
        layer.getSurface().setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

}
