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

package imagej.envisaje.spi.tools;

/**
 * Here the plumbing sticks up through the floor a bit.  This is a marker
 * interface which indicates a tool will not actually try to modify the 
 * graphics context of the image - in other words, the tool is something like
 * a move tool for dragging the layer around, etc.  
 * <p>
 * The reason for its existence is basically to notify the object being
 * modified how it should back itself up for undo purposes;  the alternative
 * is poor performance.
 *
 * @author Timothy Boudreau
 */
public interface NonPaintingTool {
    //XXX make this an annotation
}
