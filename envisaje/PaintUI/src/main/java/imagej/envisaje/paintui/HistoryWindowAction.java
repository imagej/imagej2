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

package imagej.envisaje.paintui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.windows.TopComponent;

/**
 *
 * @author Timothy Boudreau
 */
public class HistoryWindowAction extends AbstractAction {
    
    /** Creates a new instance of UndoWindowAction */
    public HistoryWindowAction() {
	putValue (Action.NAME, "Undo History");
    }
    
    public void actionPerformed (ActionEvent ae) {
	TopComponent tc = new HistoryTopComponent();
	tc.open();
	tc.requestActive();
    }
    
}
