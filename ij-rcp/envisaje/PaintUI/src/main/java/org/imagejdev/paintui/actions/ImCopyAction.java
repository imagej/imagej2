/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.paintui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import javax.swing.Action;
import org.imagejdev.imagine.api.actions.GenericContextSensitiveAction;
import org.imagejdev.imagine.api.image.Picture;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
public class ImCopyAction extends GenericContextSensitiveAction<Picture> {
    private final boolean allLayers;
    private final boolean isCut;
    public ImCopyAction() {
        this (Utilities.actionsGlobalContext(), false, false);
    }
    
    public ImCopyAction (Lookup lkp, boolean allLayers, boolean isCut) {
        super (lkp, Picture.class);
        this.allLayers = allLayers;
        this.isCut = isCut;
        String key;
        if (isCut) {
            if (allLayers) {
                key = "CUT_ALL_LAYERS";
            } else {
                key = "CUT";
            }
        } else {
            if (allLayers) {
                key = "COPY_ALL_LAYERS";
            } else {
                key = "COPY";
            }
        }
        putValue (NAME, NbBundle.getMessage(ImCopyAction.class, key));
    }
    
    public ImCopyAction (Lookup lkp) {
        this (lkp, false, false);
    }
    
    public static Action createCutAction() {
        return new ImCopyAction (Utilities.actionsGlobalContext(), false, true);
    }
    
    public static Action createCutAllAction() {
        return new ImCopyAction (Utilities.actionsGlobalContext(), true, true);
    }
    
    public static Action createCopyAllAction() {
        return new ImCopyAction (Utilities.actionsGlobalContext(), true, false);
    }

    public static Action createCopyAction() {
        return new ImCopyAction (Utilities.actionsGlobalContext(), false, false);
    }
    
    @Override
    protected void performAction(Picture p) {
        Transferable x;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (isCut) {
            x = p.copy(clipboard, allLayers);
        } else {
            x = p.cut(clipboard, allLayers);
        }
        if (x != null) {
            clipboard.setContents(x, (ClipboardOwner) x);
        }
    }

    @Override
    protected boolean checkEnabled(Collection<? extends Picture> coll, Class clazz) {
        boolean result = super.checkEnabled(coll, clazz);
        if (result) {
            Picture p = coll.iterator().next();
            result = !p.getLayers().isEmpty() && p.getActiveLayer() != null;
        }
        return result;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ImCopyAction (lkp, allLayers, isCut);
    }
}
