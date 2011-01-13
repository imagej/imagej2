/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.imagecachediagnostics;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows Diagnostics component.
 */
public class DiagnosticsAction extends AbstractAction {

    public DiagnosticsAction() {
        super(NbBundle.getMessage(DiagnosticsAction.class, "CTL_DiagnosticsAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(DiagnosticsTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = DiagnosticsTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
