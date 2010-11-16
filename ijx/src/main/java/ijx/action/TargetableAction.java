/*
 * $Id: TargetableAction.java 3475 2009-08-28 08:30:47Z kleopatra $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ijx.action;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.Icon;

/**
 * A class that represents a dynamically targetable action. The invocation of this
 * action will be dispatched to the <code>TargetManager</code> instance.
 * <p>
 * You would create instances of this class to let the TargetManager handle the
 * action invocations from the ui components constructed with this action.
 * The TargetManager could be configured depending on application state to
 * handle these actions.
 *
 * @see TargetManager
 * @author Mark Davidson
 */
public class TargetableAction extends AbstractActionExt {

    private TargetManager targetManager;

    public TargetableAction() {
        this("action");
    }

    public TargetableAction(String name) {
        super(name);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     */
    public TargetableAction(String name, String command) {
        super(name, command);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     * @param icon icon to display
     */
    public TargetableAction(String name, String command, Icon icon) {
        super(name, command, icon);
    }

    public TargetableAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * Set target manager which will handle this command. This action
     * may be reset to use the singleton instance if set to null.
     *
     * @param tm the target manager instance to dispatch the actions
     */
    public void setTargetManager(TargetManager tm) {
        this.targetManager = tm;
    }

    /**
     * Returns the target manager instance which will be used for action
     * dispatch. If the target manager has not previously been set then the
     * singleton instance will be returned.
     *
     * @return a non null target manager
     */
    public TargetManager getTargetManager() {
        if (targetManager == null) {
            targetManager = TargetManager.getInstance();
        }
        return targetManager;
    }

    // Callbacks...

    /**
     * Callback for command actions. This event will be redispatched to
     * the target manager along with the value of the Action.ACTION_COMMAND_KEY
     *
     * @param evt event which will be forwarded to the TargetManager
     * @see TargetManager
     */
    public void actionPerformed(ActionEvent evt) {
        if (!isStateAction()) {
            // Do not process this event if it's a toggle action.
            getTargetManager().doCommand(getActionCommand(), evt);
        }
    }

    /**
     * Callback for toggle actions. This event will be redispatched to
     * the target manager along with value of the the Action.ACTION_COMMAND_KEY
     *
     * @param evt event which will be forwarded to the TargetManager
     * @see TargetManager
     */
    @Override
    public void itemStateChanged(ItemEvent evt) {
        // Update all objects that share this item
        boolean newValue;
        boolean oldValue = isSelected();

        newValue = evt.getStateChange() == ItemEvent.SELECTED;

        if (oldValue != newValue) {
            setSelected(newValue);

            getTargetManager().doCommand(getActionCommand(), evt);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
