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

import java.util.Collection;
import imagej.envisaje.api.actions.GenericContextSensitiveAction;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.paintui.PaintTopComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class InvertSelectionAction extends GenericContextSensitiveAction <PaintTopComponent> {

    public InvertSelectionAction() {
	super (Utilities.actionsGlobalContext(), PaintTopComponent.class);
        putValue (NAME, NbBundle.getMessage(ResizeAction.class, "ACT_InvertSelection")); //NOI18N
    }
    
    public InvertSelectionAction(Lookup lkp) {
        super (lkp, PaintTopComponent.class);
        putValue (NAME, NbBundle.getMessage(ResizeAction.class, "ACT_InvertSelection")); //NOI18N
    }

    @Override
    protected boolean checkEnabled(Collection<? extends PaintTopComponent> coll, Class clazz) {
        boolean result = super.checkEnabled(coll, clazz);
        if (result) {
            Collection<? extends Selection> sel = Utilities.actionsGlobalContext().lookupAll(Selection.class);
            result &= !sel.isEmpty();
        }
        result &= Utilities.actionsGlobalContext().lookup(PaintTopComponent.class) != null;
        return result;
    }

    @Override
    protected void performAction(PaintTopComponent t) {
        t.invertSelection();
    }
}
