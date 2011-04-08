/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.paintui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.util.Collection;
import javax.swing.Action;
import imagej.envisaje.api.actions.GenericContextSensitiveAction;
import imagej.envisaje.api.image.Picture;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
public class ImPasteAction extends GenericContextSensitiveAction<Picture> implements FlavorListener {
    public ImPasteAction() {
        super (Utilities.actionsGlobalContext(), Picture.class);
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(this);
        putValue (NAME, NbBundle.getMessage(ImPasteAction.class, "ACTION_PASTE")); //NOI18N
    }
    
    public ImPasteAction (Lookup lkp) {
        super (lkp);
        putValue (NAME, NbBundle.getMessage(ImPasteAction.class, "ACTION_PASTE")); //NOI18N
    }
    
    @Override
    protected void performAction(Picture t) {
        t.paste(Toolkit.getDefaultToolkit().getSystemClipboard());
    }

    @Override
    protected boolean checkEnabled(Collection<? extends Picture> coll, Class clazz) {
        boolean result = super.checkEnabled(coll, clazz);
//        if (result) {
//            Picture p = coll.iterator().next();
//        }
        return result;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ImPasteAction (lkp);
    }

    public void flavorsChanged(FlavorEvent e) {
//        Clipboard c = (Clipboard) e.getSource();
//        c.getAvailableDataFlavors();
//        List <DataFlavor> l = Arrays.asList(c.getAvailableDataFlavors());
//        this.setEnabled(l.contains(DataFlavor.imageFlavor) || 
//                l.contains(PaintTopComponent.LAYER_DATA_FLAVOR));
    }
}
