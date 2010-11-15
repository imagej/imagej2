/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.sandbox.actioncontext;

// See: http://wiki.netbeans.org/DevFaqActionContextSensitive

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public class FooAction extends AbstractAction implements LookupListener, ContextAwareAction {
    private Lookup context;
    Lookup.Result<Whatever> lkpInfo;

    public FooAction() {
        this(Utilities.actionsGlobalContext());
    }

    private FooAction(Lookup context) {
        putValue(Action.NAME, NbBundle.getMessage(FooAction.class, "LBL_Action"));
        this.context = context;
    }

    void init() {
        assert SwingUtilities.isEventDispatchThread()
               : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }

        //The thing we want to listen for the presence or absence of on the global selection
        lkpInfo = context.lookupResult(Whatever.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        init();
        for (Whatever instance : lkpInfo.allInstances()) {
            // use it somehow...
        }
    }

    public void resultChanged(LookupEvent ev) {
        setEnabled(!lkpInfo.allInstances().isEmpty());
    }

    public Action createContextAwareInstance(Lookup context) {
        return new FooAction(context);
    }

    class Whatever {

    }
}
