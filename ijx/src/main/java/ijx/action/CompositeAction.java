/*
 * $Id: CompositeAction.java 3475 2009-08-28 08:30:47Z kleopatra $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * A class that represents an action which will fire a sequence of actions.
 * The action ids are added to the internal list. When this action is invoked,
 * the event will be dispatched to the actions in the internal list.
 * <p>
 * The action ids are represented by the value of the <code>Action.ACTION_COMMAND_KEY</code>
 * and must be managed by the <code>ActionManager</code>. When this action is 
 * invoked, then the actions are retrieved from the ActionManager in list order
 * and invoked.
 * 
 * @see ActionManager
 * @author Mark Davidson
 */
public class CompositeAction extends AbstractActionExt {

     /**
     * Keys for storing extended action attributes. May make public.
     */
    private static final String LIST_IDS = "action-list-ids";

    public CompositeAction() {
        this("CompositeAction");
    }

    public CompositeAction(String name) {
        super(name);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     */
    public CompositeAction(String name, String command) {
        super(name, command);
    }

    public CompositeAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     * @param icon icon to display
     */
    public CompositeAction(String name, String command, Icon icon) {
        super(name, command, icon);
    }

    /**
     * Add an action id to the action list. This action will be invoked 
     * when this composite action is invoked.
     */
    @SuppressWarnings("unchecked")
    public void addAction(String id) {
        List<String> list = (List<String>) getValue(LIST_IDS);
        if (list == null) {
            list = new ArrayList<String>();
            putValue(LIST_IDS, list);
        }
        list.add(id);
    }

    /**
     * Returns a list of action ids which indicates that this is a composite
     * action. 
     * @return a valid list of action ids or null
     */
    @SuppressWarnings("unchecked")
    public List<String> getActionIDs() {
        return (List<String>) getValue(LIST_IDS);
    }    

    /**
     * Callback for composite actions. This method will redispatch the 
     * ActionEvent to all the actions held in the list.
     */
    public void actionPerformed(ActionEvent evt) {
        ActionManager manager = ActionManager.getInstance();
            
        Iterator<String> iter = getActionIDs().iterator();
        while (iter.hasNext()) {
            String id = iter.next();
            Action action = manager.getAction(id);
            if (action != null) {
            action.actionPerformed(evt);
            }
        }
    }

    /**
     * Callback for toggle actions.
     */
    @Override
    public void itemStateChanged(ItemEvent evt) {
        ActionManager manager = ActionManager.getInstance();
            
        Iterator<String> iter = getActionIDs().iterator();
        while (iter.hasNext()) {
            String id = iter.next();
            Action action = manager.getAction(id);
            if (action != null && action instanceof AbstractActionExt) {
            ((AbstractActionExt)action).itemStateChanged(evt);
            }
        }
    }
}
