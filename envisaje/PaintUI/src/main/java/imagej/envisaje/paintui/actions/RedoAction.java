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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Timothy Boudreau
 */
public class RedoAction extends UndoAction {

    /** Creates a new instance of UndoAction */
    public RedoAction() {
    }

    @Override
    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        updateUndoRedo();
        String undo = last.getRedoPresentationName();
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "getUndoRedo().getUndoPresentationName() returns " + undo);
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "SWING_DEFAULT_LABEL is " + SWING_DEFAULT_LABEL);

        if ((undo != null) && (SWING_DEFAULT_LABEL != null) && undo.startsWith(SWING_DEFAULT_LABEL)) {
            undo = undo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Name adapted by SWING_DEFAULT_LABEL is " + undo);
        String presentationName = NbBundle.getMessage(UndoAction.class, "ACT_REDO_WITHVALUE", undo);
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Result name is " + presentationName);

        return presentationName;
    }

    @Override
    public void performAction() {
        try {
            updateUndoRedo();
            UndoRedo undoRedo = last;

            if (undoRedo.canRedo()) {
                undoRedo.redo();
            }
        } catch (CannotUndoException ex) {
            Exceptions.printStackTrace(ex);
        }
        updateStatus();

    }
}
