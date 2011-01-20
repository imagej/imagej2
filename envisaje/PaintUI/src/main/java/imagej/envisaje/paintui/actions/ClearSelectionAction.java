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

import imagej.envisaje.api.actions.GenericContextSensitiveAction;
import imagej.envisaje.paintui.PaintTopComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class ClearSelectionAction extends GenericContextSensitiveAction <PaintTopComponent> {

    public ClearSelectionAction() {
	super (Utilities.actionsGlobalContext(), PaintTopComponent.class);
        putValue(NAME, NbBundle.getMessage (ClearSelectionAction.class,
                "ACT_ClearSelection")); //NOI18N
    }

    public ClearSelectionAction(Lookup lookup) {
        super (lookup, PaintTopComponent.class);
        putValue(NAME, NbBundle.getMessage (ClearSelectionAction.class,
                "ACT_ClearSelection")); //NOI18N
    }

    public void performAction(PaintTopComponent ptc) {
        ptc.clearSelection();
    }
}
