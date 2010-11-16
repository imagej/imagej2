/*
 * $Id: ActionManager.java 3197 2009-01-21 17:54:30Z kschaefe $
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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

/**
 * The ActionManager manages sets of <code>javax.swing.Action</code>s for an
 * application. There are convenience methods for getting and setting the state
 * of the action.
 * All of these elements have a unique id tag which is used by the ActionManager
 * to reference the action. This id maps to the <code>Action.ACTION_COMMAND_KEY</code>
 * on the Action.
 * <p>
 * The ActionManager may be used to conveniently register callback methods
 * on BoundActions.
 * <p>
 * A typical use case of the ActionManager is:
 * <p>
 *  <pre>
 *   ActionManager manager = ActionManager.getInstance();
 *
 *   // load Actions
 *   manager.addAction(action);
 *
 *   // Change the state of the action:
 *   manager.setEnabled("new-action", newState);
 * </pre>
 *
 * The ActionManager also supports Actions that can have a selected state
 * associated with them. These Actions are typically represented by a
 * JCheckBox or similar widget. For such actions the registered method is
 * invoked with an additional parameter indicating the selected state of
 * the widget. For example, for the callback handler:
 *<p>
 * <pre>
 *  public class Handler {
 *      public void stateChanged(boolean newState);
 *   }
 * </pre>
 * The registration method would look similar:
 * <pre>
 *  manager.registerCallback("select-action", new Handler(), "stateChanged");
 * </pre>
 *<p>
 * The stateChanged method would be invoked as the selected state of
 * the widget changed. Additionally if you need to change the selected
 * state of the Action use the ActionManager method <code>setSelected</code>.
 * <p>
 * The <code>ActionContainerFactory</code> uses the managed Actions in a
 * ActionManager to create user interface components. It uses the shared
 * instance of ActionManager by default. For example, to create a JMenu based on an
 * action-list id:
 * <pre>
 * ActionContainerFactory factory = new ActionContainerFactory();
 * JMenu file = factory.createMenu(list);
 * </pre>
 *
 * @see ActionContainerFactory
 * @see TargetableAction
 * @see BoundAction
 * @author Mark Davidson
 * @author Neil Weber
 */
public class ActionManager extends ActionMap {

    /**
     * Shared instance of the singleton ActionManager.
     */
    private static ActionManager INSTANCE;

    /**
     * Creates the action manager. Use this constuctor if the application should
     * support many ActionManagers. Otherwise, using the getInstance method will
     * return a singleton.
     */
    public ActionManager() {
    }

    /**
     * Return the instance of the ActionManger. If this has not been explicity
     * set then it will be created.
     *
     * @return the ActionManager instance.
     * @see #setInstance
     */
    public static ActionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ActionManager();
        }
        return INSTANCE;
    }

    /**
     * Sets the ActionManager instance.
     */
    public static void setInstance(ActionManager manager) {
        INSTANCE = manager;
    }

    /**
     * Returns the ids for all the managed actions.
     * <p>
     * An action id is a unique idenitfier which can
     * be used to retrieve the corrspondng Action from the ActionManager.
     * This identifier can also
     * be used to set the properties of the action through the action
     * manager like setting the state of the enabled or selected flags.
     *
     * @return a set which represents all the action ids
     */
    public Set<Object> getActionIDs() {
        Object[] keys = keys();
        if (keys == null) {
            return null;
        }

        return new HashSet<Object>(Arrays.asList(keys));
    }

    public Action addAction(Action action) {
        return addAction(action.getValue(Action.ACTION_COMMAND_KEY), action);
    }

    /**
     * Adds an action to the ActionManager
     * @param id value of the action id - which is value of the ACTION_COMMAND_KEY
     * @param action Action to be managed
     * @return the action that was added
     */
    public Action addAction(Object id, Action action)  {
        put(id, action);
        return action;
    }

    /**
     * Retrieves the action corresponding to an action id.
     *
     * @param id value of the action id
     * @return an Action or null if id
     */
    public Action getAction(Object id)  {
        return get(id);
    }

    /**
     * Convenience method for returning the TargetableAction
     *
     * @param id value of the action id
     * @return the TargetableAction referenced by the named id or null
     */
    public TargetableAction getTargetableAction(Object id) {
        Action a = getAction(id);
        if (a instanceof TargetableAction) {
            return (TargetableAction)a;
        }
        return null;
    }

    /**
     * Convenience method for returning the BoundAction
     *
     * @param id value of the action id
     * @return the TargetableAction referenced by the named id or null
     */
    public BoundAction getBoundAction(Object id) {
        Action a = getAction(id);
        if (a instanceof BoundAction) {
            return (BoundAction)a;
        }
        return null;
    }

    /**
     * Convenience method for returning the ServerAction
     *
     * @param id value of the action id
     * @return the TargetableAction referenced by the named id or null
     */
    public ServerAction getServerAction(Object id) {
        Action a = getAction(id);
        if (a instanceof ServerAction) {
            return (ServerAction)a;
        }
        return null;
    }

    /**
     * Convenience method for returning the CompositeAction
     *
     * @param id value of the action id
     * @return the TargetableAction referenced by the named id or null
     */
    public CompositeAction getCompositeAction(Object id) {
        Action a = getAction(id);
        if (a instanceof CompositeAction) {
            return (CompositeAction)a;
        }
        return null;
    }

    /**
     * Convenience method for returning the StateChangeAction
     *
     * @param id value of the action id
     * @return the StateChangeAction referenced by the named id or null
     */
    private AbstractActionExt getStateChangeAction(Object id) {
        Action a = getAction(id);
        if (a != null && a instanceof AbstractActionExt) {
            AbstractActionExt aa = (AbstractActionExt)a;
            if (aa.isStateAction()) {
                return aa;
            }
        }
        return null;
    }

    /**
     * Enables or disables the state of the Action corresponding to the
     * action id. This method should be used
     * by application developers to ensure that all components created from an
     * action remain in synch with respect to their enabled state.
     *
     * @param id value of the action id
     * @param enabled true if the action is to be enabled; otherwise false
     */
    public void setEnabled(Object id, boolean enabled) {
        Action action = getAction(id);
        if (action != null) {
            action.setEnabled(enabled);
        }
    }


    /**
     * Returns the enabled state of the <code>Action</code>. When enabled,
     * any component associated with this object is active and
     * able to fire this object's <code>actionPerformed</code> method.
     *
     * @param id value of the action id
     * @return true if this <code>Action</code> is enabled; false if the
     *         action doesn't exist or disabled.
     */
    public boolean isEnabled(Object id) {
        Action action = getAction(id);
        if (action != null) {
            return action.isEnabled();
        }
        return false;
    }

    /**
     * Sets the selected state of a toggle action. If the id doesn't
     * correspond to a toggle action then it will fail silently.
     *
     * @param id the value of the action id
     * @param selected true if the action is to be selected; otherwise false.
     */
    public void setSelected(Object id, boolean selected) {
        AbstractActionExt action = getStateChangeAction(id);
        if (action != null) {
            action.setSelected(selected);
        }
    }

    /**
     * Gets the selected state of a toggle action. If the id doesn't
     * correspond to a toggle action then it will fail silently.
     *
     * @param id the value of the action id
     * @return true if the action is selected; false if the action
     *         doesn't exist or is disabled.
     */
    public boolean isSelected(Object id) {
        AbstractActionExt action = getStateChangeAction(id);
        if (action != null) {
            return action.isSelected();
        }
        return false;
    }

    /**
     * A diagnostic which prints the Attributes of an action
     * on the printStream
     */
    static void printAction(PrintStream stream, Action action) {
        stream.println("Attributes for " + action.getValue(Action.ACTION_COMMAND_KEY));

        if (action instanceof AbstractAction) {
            Object[] keys = ((AbstractAction)action).getKeys();

            for (int i = 0; i < keys.length; i++) {
                stream.println("\tkey: " + keys[i] + "\tvalue: " +
                               action.getValue((String)keys[i]));
            }
        }
    }

    /**
     * Convenience method to register a callback method on a <code>BoundAction</code>
     *
     * @see BoundAction#registerCallback
     * @param id value of the action id - which is the value of the ACTION_COMMAND_KEY
     * @param handler the object which will be perform the action
     * @param method the name of the method on the handler which will be called.
     */
    public void registerCallback(Object id, Object handler, String method) {
        BoundAction action = getBoundAction(id);
        if (action != null) {
            action.registerCallback(handler, method);
        }
    }

    //
    // Convenience methods for determining the type of action.
    //

    /**
     * Determines if the Action corresponding to the action id is a state changed
     * action (toggle, group type action).
     *
     * @param id value of the action id
     * @return true if the action id represents a multi state action; false otherwise
     */
    public boolean isStateAction(Object id) {
        Action action = getAction(id);
        if (action != null && action instanceof AbstractActionExt) {
            return ((AbstractActionExt)action).isStateAction();
        }
        return false;
    }

    /**
     * Test to determine if the action is a <code>TargetableAction</code>
     */
    public boolean isTargetableAction(Object id) {
        return (getTargetableAction(id) != null);
    }

    /**
     * Test to determine if the action is a <code>BoundAction</code>
     */
    public boolean isBoundAction(Object id) {
        return (getBoundAction(id) != null);
    }

    /**
     * Test to determine if the action is a <code>BoundAction</code>
     */
    public boolean isCompositeAction(Object id) {
        return (getCompositeAction(id) != null);
    }

    /**
     * Test to determine if the action is a <code>ServerAction</code>
     */
    public boolean isServerAction(Object id) {
        return (getServerAction(id) != null);
    }
}
