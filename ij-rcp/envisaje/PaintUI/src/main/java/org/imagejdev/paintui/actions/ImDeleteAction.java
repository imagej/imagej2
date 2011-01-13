/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.paintui.actions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.api.actions.GenericContextSensitiveAction;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Picture;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.api.selection.Selection;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
public class ImDeleteAction extends GenericContextSensitiveAction<Picture> implements ChangeListener {
    public ImDeleteAction() {
        super (Utilities.actionsGlobalContext(), Picture.class);
        putValue (NAME, NbBundle.getMessage(ImDeleteAction.class, 
                "ACTION_DELETE")); //NOI18N
    }
    
    public ImDeleteAction (Lookup lkp) {
        super (lkp);
        putValue (NAME, NbBundle.getMessage(ImDeleteAction.class, 
                "ACTION_DELETE")); //NOI18N
    }
    
    @Override
    protected void performAction(Picture t) {
        Layer l = t.getActiveLayer();
        Surface surface = l.getSurface();
        Selection s = l.getLookup().lookup(Selection.class);
        Graphics2D gg = surface.getGraphics();
        surface.beginUndoableOperation((String) getValue(NAME));
        try {
            Shape clip = s.asShape();
            gg.setClip (clip);
            Rectangle bds = clip.getBounds();
            gg.setBackground(new Color(0,0,0,0));
            gg.clearRect(bds.x, bds.y, bds.width, bds.height);
        } finally {
            gg.dispose();
            surface.endUndoableOperation();
        }
    }
    
    private Selection lastSelection;

    @Override
    protected boolean checkEnabled(Collection<? extends Picture> coll, Class clazz) {
        boolean result = super.checkEnabled(coll, clazz);
        if (result) {
            Picture p = coll.iterator().next();
            Layer layer = p.getActiveLayer();
            result = layer != null;
            if (result) {
                Selection selection = layer.getLookup().lookup(Selection.class);
                if (selection != lastSelection) {
                    if (lastSelection != null) {
                        lastSelection.removeChangeListener (this);
                    }
                    lastSelection = selection;
                    if (lastSelection != null) {
                        lastSelection.addChangeListener(this);
                    }
                }
                result = selection != null;
                //XXX should there be an isEmpty() on selection?
                if (result) {
                    Shape shape = selection.asShape();
                    result = shape != null;
                    if (result) {
                        Rectangle r = shape.getBounds();
                        result = r.width > 0 && r.height > 0;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ImDeleteAction (lkp);
    }

    public void stateChanged(ChangeEvent e) {
        setEnabled (checkEnabled (this.lookup.lookupAll(Picture.class), Picture.class));
    }
}
