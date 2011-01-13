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

package org.imagejdev.paintui.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import org.imagejdev.imagine.api.actions.GenericContextSensitiveAction;
import org.imagejdev.imagine.api.editor.Zoom;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class ZoomOutAction extends GenericContextSensitiveAction <Zoom> {
    private static final String ICON_BASE=
	    "org/imagejdev/paintui/resources/zoomOut.png";
	    
    public ZoomOutAction() {
	super (Utilities.actionsGlobalContext(), Zoom.class); //NOI18N
	setIcon(new ImageIcon (
            Utilities.loadImage (ICON_BASE)));
        putValue(Action.NAME, NbBundle.getMessage(ZoomOutAction.class, 
                "ACT_ZoomOut"));
    }
    
    public ZoomOutAction(Lookup lookup) {
        super (lookup);
    }

    public void performAction(Zoom zoom) {
	float f = zoom.getZoom();
	if (f <= 1f) {
	    f -= 0.1f;
	} else {
	    f -= 1;
	}
	f = Math.max (0, f);
	zoom.setZoom(f);
    }
    
    protected boolean shouldEnable (Object target) {
	if (target != null) {
	    Zoom zoom = (Zoom) target;
	    return zoom.getZoom() > 0.1f;
	}
         return false;
    }
}
