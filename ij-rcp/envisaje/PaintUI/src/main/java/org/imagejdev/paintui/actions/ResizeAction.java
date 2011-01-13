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

import java.awt.Dimension;
import org.imagejdev.imagine.api.actions.GenericContextSensitiveAction;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Picture;
import org.imagejdev.paintui.PaintTopComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class ResizeAction extends GenericContextSensitiveAction <Picture> {

    public ResizeAction() {
	super(Utilities.actionsGlobalContext(), Picture.class);
        putValue (NAME, NbBundle.getMessage(ResizeAction.class, "ACT_Resize")); //NOI18N
    }

    public ResizeAction(Lookup lookup) {
        super (lookup);
        putValue (NAME, NbBundle.getMessage(ResizeAction.class, "ACT_Resize")); //NOI18N
    }

    public void performAction(Picture target) {
	ImageSizePanel pnl = new ImageSizePanel();
	DialogDescriptor dd = new DialogDescriptor (pnl,
		NbBundle.getMessage(ResizeAction.class,
		"TTL_Resize")); //NOI18N

	if (DialogDisplayer.getDefault().notify(dd) ==
		DialogDescriptor.OK_OPTION) {

	    Dimension d = pnl.getDimension();
            for (Layer l : target.getLayers()) {
                l.resize(d.width, d.height);
            }
            PaintTopComponent ptc = PaintTopComponent.tcFor(target);
            if (ptc != null) {
                ptc.pictureResized(d.width, d.height);
                ptc.repaint();
            }
	}
    }

}
