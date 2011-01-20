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

package imagej.envisaje.paintui.actions;

import javax.swing.ImageIcon;
import imagej.envisaje.api.actions.GenericContextSensitiveAction;
import imagej.envisaje.api.editor.Zoom;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class ZoomInAction extends GenericContextSensitiveAction <Zoom> {

    public ZoomInAction() {
        super(Utilities.actionsGlobalContext(), Zoom.class); //NOI18N
        setIcon(new ImageIcon(
                Utilities.loadImage("imagej/envisaje/paintui/resources/zoomIn.png")));
        putValue (NAME, NbBundle.getMessage(ZoomInAction.class, "ACT_ZoomIn"));
    }

    public ZoomInAction(Lookup lkp) {
        super(lkp);
        setIcon(new ImageIcon(
                Utilities.loadImage("imagej/envisaje/paintui/resources/zoomIn.png")));
        putValue (NAME, NbBundle.getMessage(ZoomInAction.class, "ACT_ZoomIn"));
    }

    public void performAction(Zoom zoom) {
        assert zoom != null;
        float f = zoom.getZoom();
        if (f < 1f) {
            f += 0.1f;
        } else {
            f += 1;
        }
        zoom.setZoom(f);
    }
}
