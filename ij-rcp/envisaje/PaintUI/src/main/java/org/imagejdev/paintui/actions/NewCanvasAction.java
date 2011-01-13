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
package org.imagejdev.paintui.actions;

import java.awt.Frame;
import javax.swing.JOptionPane;
import org.imagejdev.paintui.PaintTopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

public final class NewCanvasAction extends org.openide.util.actions.CallableSystemAction {

    private static final String ICON_BASE = "org/imagejdev/paintui/resources/newFile.png"; //NOI18N

//    public NewCanvasAction() {
//        setIcon( new ImageIcon(Utilities.loadImage("org/imagedev/paintui/resources/newFile.png")));
//    }
    public void performAction() {
        final ImageSizePanel pnl = new ImageSizePanel();
        String ttl = NbBundle.getMessage(NewCanvasAction.class, "TTL_NewImage");
        //This code really should use DialogDisplayer, but is not due
        //to a bug in the window system
        int result = JOptionPane.showOptionDialog(Frame.getFrames()[0], pnl,
                ttl, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        if (result == JOptionPane.OK_OPTION) {
            PaintTopComponent tc = new PaintTopComponent(pnl.getDimension(), pnl.isTransparent());
            Mode m = WindowManager.getDefault().findMode("editor");
            if (m != null) {
                m.dockInto(tc);
            }
            tc.open();
            tc.requestActive();
        }
    }

    public String getName() {
        return NbBundle.getMessage(NewCanvasAction.class, "ACT_NewImage");
    }

    public String iconResource() {
        return ICON_BASE;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
}
