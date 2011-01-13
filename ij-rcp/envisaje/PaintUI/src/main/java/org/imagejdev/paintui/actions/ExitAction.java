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
package org.imagejdev.paintui.actions;

import org.openide.LifecycleManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public final class ExitAction extends CallableSystemAction {

    public void performAction() {
        LifecycleManager.getDefault().exit();
    }

    public String getName() {
        return "Exit";
    }

    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

}
