/*
 * $Id: BoundAction.java 3471 2009-08-27 13:10:39Z kleopatra $
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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.EventHandler;
import java.beans.Statement;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.event.EventListenerList;

/**
 * A class that represents the many type of actions that this framework supports.
 * <p>
 * The command invocation of this action may be delegated to another action or item state
 * listener. If there isn't an explicit binding then the command is forwarded to 
 * the TargetManager.
 *
 * @author Mark Davidson
 */
public class BoundAction extends AbstractActionExt {
    private static final Logger LOG = Logger.getLogger(BoundAction.class.getName());
    // Holds the listeners
    private EventListenerList listeners;

    public BoundAction() {
        this("BoundAction");
    }

    public BoundAction(String name) {
        super(name);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     */
    public BoundAction(String name, String command) {
        super(name, command);
    }

    public BoundAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     * @param icon icon to display
     */
    public BoundAction(String name, String command, Icon icon) {
        super(name, command, icon);
    }

    /**
     * The callback string will be called to register the action callback.
     * Note the toggle property must be set if this is a state action before
     * this method is called.
     * For example, 
     * <pre>
     *     &lt;exec&gt;com.sun.foo.FubarHandler#handleBar&lt;/exec&gt;
     * </pre>
     * will register
     * <pre>
     *     registerCallback(com.sun.foo.FubarHandler(), "handleBar");
     * </pre>
     */
    public void setCallback(String callback) {
        String[] elems = callback.split("#", 2);
        if (elems.length == 2) {
            try {
                Class<?> clz = Class.forName(elems[0]);
                // May throw a security exception in an Applet context.
                Object obj = clz.newInstance();
                registerCallback(obj, elems[1]);
            } catch (Exception ex) {
                LOG.fine("ERROR: setCallback(" + callback
                        + ") - " + ex.getMessage());
            }
        }
    }

    /**
     * Registers a callback method when the Action corresponding to
     * the action id is invoked. When a Component that was constructed from the
     * Action identified by the action id invokes actionPerformed then the method
     * named will be invoked on the handler Object.
     * <p>
     * If the Action represented by the action id is a StateChangeAction, then
     * the method passed should take an int as an argument. The value of
     * getStateChange() on the ItemEvent object will be passed as the parameter.
     *
     * @param handler the object which will be perform the action
     * @param method the name of the method on the handler which will be called.
     */
    public void registerCallback(Object handler, String method) {
        if (isStateAction()) {
            // Create a handler for toogle type actions.
            addItemListener(new BooleanInvocationHandler(handler, method));
        } else {
            // Create a new ActionListener using the dynamic proxy api.
            addActionListener((ActionListener) EventHandler.create(ActionListener.class,
                    handler, method));
        }
    }

    /**
     * The callback for the toggle/state changed action that invokes a method 
     * with a boolean argument on a target.
     *
     * TODO: should reimplement this class as something that can be persistable.
     */
    private class BooleanInvocationHandler implements ItemListener {
        private Statement falseStatement;
        private Statement trueStatement;

        public BooleanInvocationHandler(Object target, String methodName) {
            // Create the true and false statements.
            falseStatement = new Statement(target, methodName, new Object[]{Boolean.FALSE});
            trueStatement = new Statement(target, methodName, new Object[]{Boolean.TRUE});
        }

        public void itemStateChanged(ItemEvent evt) {
            Statement statement = (evt.getStateChange() == ItemEvent.DESELECTED) ? falseStatement
                    : trueStatement;

            try {
                statement.execute();
            } catch (Exception ex) {
                LOG.log(Level.FINE,
                        "Couldn't execute boolean method via Statement "
                        + statement, ex);
            }
        }
    }

    // Listener registration...
    private <T extends EventListener> void addListener(Class<T> clz, T listener) {
        if (listeners == null) {
            listeners = new EventListenerList();
        }
        listeners.add(clz, listener);
    }

    private <T extends EventListener> void removeListener(Class<T> clz, T listener) {
        if (listeners != null) {
            listeners.remove(clz, listener);
        }
    }

    private EventListener[] getListeners(Class<? extends EventListener> clz) {
        if (listeners == null) {
            return null;
        }
        return listeners.getListeners(clz);
    }

    /**
     * Add an action listener which will be invoked when this action is invoked.
     */
    public void addActionListener(ActionListener listener) {
        addListener(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        removeListener(ActionListener.class, listener);
    }

    public ActionListener[] getActionListeners() {
        return (ActionListener[]) getListeners(ActionListener.class);
    }

    /**
     * Add an item listener which will be invoked for toggle actions.
     */
    public void addItemListener(ItemListener listener) {
        addListener(ItemListener.class, listener);
    }

    public void removeItemListener(ItemListener listener) {
        removeListener(ItemListener.class, listener);
    }

    public ItemListener[] getItemListeners() {
        return (ItemListener[]) getListeners(ItemListener.class);
    }

    // Callbacks...
    /**
     * Callback for command actions.
     */
    public void actionPerformed(ActionEvent evt) {
        ActionListener[] alist = getActionListeners();
        if (alist != null) {
            for (int i = 0; i < alist.length; i++) {
                alist[i].actionPerformed(evt);
            }
        }
    }

    /**
     * Callback for toggle actions.
     */
    @Override
    public void itemStateChanged(ItemEvent evt) {
        // Update all objects that share this item
        boolean newValue;
        boolean oldValue = isSelected();

        newValue = evt.getStateChange() == ItemEvent.SELECTED;

        if (oldValue != newValue) {
            setSelected(newValue);

            // Forward the event to the delgate for handling.
            ItemListener[] ilist = getItemListeners();
            if (ilist != null) {
                for (int i = 0; i < ilist.length; i++) {
                    ilist[i].itemStateChanged(evt);
                }
            }
        }
    }
}
