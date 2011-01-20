/*
 *
 * Sun Public License Notice
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

/**
 *
 * Undo action for standalone app.  Contains a few protected methods so that
 * RedoAction can subclass it.
 *
 * @author Timothy Boudreau
 */
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.*;
import javax.swing.undo.*;
import imagej.envisaje.paintui.PaintTopComponent;
import imagej.envisaje.paintui.UIContextLookupProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;


/** Undo an edit.
*
* @see UndoRedo
* @author   Ian Formanek, Jaroslav Tulach, Tim Boudreau
*/
public class UndoAction extends CallableSystemAction {
    /** initialized listener */
    private static Listener listener;

    /** last edit */
    protected static UndoRedo last = UndoRedo.NONE;
    protected static String SWING_DEFAULT_LABEL = UIManager.getString("AbstractUndoableEdit.undoText"); //NOI18N
    private static UndoAction undoAction = null;
    private static RedoAction redoAction = null;

    @Override
    public boolean isEnabled() {
        initializeUndoRedo();
        return super.isEnabled();
    }

    static Lookup.Result <PaintTopComponent.UndoMgr> res;
    /** Initializes the object.
    */
    static synchronized void initializeUndoRedo() {
        if (listener != null) {
            return;
        }
        listener = new Listener();
        res = UIContextLookupProvider.theLookup().lookupResult(PaintTopComponent.UndoMgr.class);
        res.allInstances();
        res.addLookupListener(listener);
        updateUndoRedo();
        last.addChangeListener(listener);
        updateStatus();
    }

    /** Update status of action.
    */
    static synchronized void updateStatus() {
        if (undoAction == null) {
            undoAction = (UndoAction) findObject(UndoAction.class, false);
        }

        if (redoAction == null) {
            redoAction = (RedoAction) findObject(RedoAction.class, false);
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    updateUndoRedo();

                    if (undoAction != null) {
                        undoAction.setEnabled(last.canUndo());
                    }

                    if (redoAction != null) {
                        redoAction.setEnabled(last.canRedo());
                    }
                }
            }
        );
    }

    /** Finds current undo/redo.
    */
    static void updateUndoRedo() {
        last = UIContextLookupProvider.theLookup().lookup (
                PaintTopComponent.UndoMgr.class);
        if (last == null) {
            last = UndoRedo.NONE;
        }
    }

    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        updateUndoRedo();
        String undo = last.getUndoPresentationName();
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "getUndoRedo().getUndoPresentationName() returns " + undo);
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "SWING_DEFAULT_LABEL is " + SWING_DEFAULT_LABEL);

        if ((undo != null) && (SWING_DEFAULT_LABEL != null) && undo.startsWith(SWING_DEFAULT_LABEL)) {
            undo = undo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Name adapted by SWING_DEFAULT_LABEL is " + undo);
        String presentationName = NbBundle.getMessage(UndoAction.class, "ACT_UNDO_WITHVALUE", undo);
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Result name is " + presentationName);

        return presentationName;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(UndoAction.class);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/undo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            updateUndoRedo();
            UndoRedo undoRedo = last;

            if (undoRedo.canUndo()) {
                undoRedo.undo();
            }
        } catch (CannotUndoException ex) {
            Exceptions.printStackTrace(ex);
        }

        updateStatus();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /** Listener on changes of selected workspace element and
    * its changes.
    */
    private static final class Listener implements ChangeListener, LookupListener {
        Listener() {
        }

        public void stateChanged(ChangeEvent ev) {
            updateStatus();
        }

        public void resultChanged(LookupEvent arg0) {
            last.removeChangeListener(this);
            updateUndoRedo();
            last.addChangeListener(this);
            updateStatus();
        }
    }
}
