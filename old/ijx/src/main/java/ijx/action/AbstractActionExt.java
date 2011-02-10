/*
 * $Id: AbstractActionExt.java 3197 2009-01-21 17:54:30Z kschaefe $
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Extends the concept of the Action to include toggle or group states.
 *
 */
public abstract class AbstractActionExt extends AbstractAction
    implements ItemListener {

    /**
     * The key for the large icon
     */
    public static final String LARGE_ICON = "__LargeIcon__";

    /**
     * The key for the button group
     */
    public static final String GROUP = "__Group__";

    /**
     * The key for the flag which indicates that this is a state type.
     */
    public static final String IS_STATE = "__State__";

    /**
     * Specified whether the action is selected; the default is false
     */
    private boolean selected = false;

    /**
     * Default constructor, does nothing.
     *
     */
    public AbstractActionExt() {
        // default constructor
    }
    /**
     * Copy constructor copies the state.
     */
    public AbstractActionExt(AbstractActionExt action) {
        Object[] keys = action.getKeys();
        for (int i = 0; i < keys.length; i++) {
            putValue((String)keys[i], action.getValue((String)keys[i]));
        }
        this.selected = action.selected;
        this.enabled = action.enabled;

        // Copy change listeners.
        PropertyChangeListener[] listeners = action.getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            addPropertyChangeListener(listeners[i]);
        }
    }

    public AbstractActionExt(String name) {
        super(name);
    }

    public AbstractActionExt(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * Constructs an Action with the label and command
     *
     * @param name name of the action usually used as a label
     * @param command command key of the action
     */
    public AbstractActionExt(String name, String command) {
        this(name);
        setActionCommand(command);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     * @param icon icon to display
     */
    public AbstractActionExt(String name, String command, Icon icon) {
        super(name, icon);
        setActionCommand(command);
    }
    /**
     * Returns a short description of the action.
     *
     * @return the short description or null
     */
    public String getShortDescription()  {
        return (String)getValue(Action.SHORT_DESCRIPTION);
    }

    /**
     * Sets the short description of the action. This will also
     * set the long description value is it is null.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.SHORT_DESCRIPTION</code> key.
     *
     * @param desc the short description; can be <code>null</code>w
     * @see Action#SHORT_DESCRIPTION
     * @see Action#putValue
     */
    public void setShortDescription(String desc) {
        putValue(Action.SHORT_DESCRIPTION, desc);
        if (desc != null && getLongDescription() == null) {
            setLongDescription(desc);
        }
    }

    /**
     * Returns a long description of the action.
     *
     * @return the long description or null
     */
    public String getLongDescription()  {
        return (String)getValue(Action.LONG_DESCRIPTION);
    }

    /**
     * Sets the long description of the action. This will also set the
     * value of the short description if that value is null.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.LONG_DESCRIPTION</code> key.
     *
     * @param desc the long description; can be <code>null</code>
     * @see Action#LONG_DESCRIPTION
     * @see Action#putValue
     */
    public void setLongDescription(String desc) {
        putValue(Action.LONG_DESCRIPTION, desc);
        if (desc != null && getShortDescription() == null) {
            setShortDescription(desc);
        }
    }

    /**
     * Returns a small icon which represents the action.
     *
     * @return the small icon or null
     */
    public Icon getSmallIcon() {
        return (Icon)getValue(SMALL_ICON);
    }

    /**
     * Sets the small icon which represents the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.SMALL_ICON</code> key.
     *
     * @param icon the small icon; can be <code>null</code>
     * @see Action#SMALL_ICON
     * @see Action#putValue
     */
    public void setSmallIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
    }

    /**
     * Returns a large icon which represents the action.
     *
     * @return the large icon or null
     */
    public Icon getLargeIcon() {
        return (Icon)getValue(LARGE_ICON);
    }

    /**
     * Sets the large icon which represents the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>LARGE_ICON</code> key.
     *
     * @param icon the large icon; can be <code>null</code>
     * @see #LARGE_ICON
     * @see Action#putValue
     */
    public void setLargeIcon(Icon icon) {
        putValue(LARGE_ICON, icon);
    }

    /**
     * Sets the name of the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.NAME</code> key.
     *
     * @param name the name of the action; can be <code>null</code>
     * @see Action#NAME
     * @see Action#putValue
     */
    public void setName(String name) {
        putValue(Action.NAME, name);
    }

    /**
     * Returns the name of the action.
     *
     * @return the name of the action or null
     */
    public String getName() {
        return (String)getValue(Action.NAME);
    }

    public void setMnemonic(String mnemonic) {
        if (mnemonic != null && mnemonic.length() > 0) {
            putValue(Action.MNEMONIC_KEY, new Integer(mnemonic.charAt(0)));
        }
    }

    /**
     * Sets the mnemonic key code for the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.MNEMONIC_KEY</code> key.
     * <p>
     * This method does not validate the value. Please see
     * {@link javax.swing.AbstractButton#setMnemonic(int)} for details
     * concerning the value of the mnemonic.
     *
     * @param mnemonic an int key code mnemonic or 0
     * @see javax.swing.AbstractButton#setMnemonic(int)
     * @see Action#MNEMONIC_KEY
     * @see Action#putValue
     */
    public void setMnemonic(int mnemonic) {
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
    }

    /**
     * Return the mnemonic key code for the action.
     *
     * @return the mnemonic or 0
     */
    public int getMnemonic() {
        Integer value = (Integer)getValue(Action.MNEMONIC_KEY);
        if (value != null) {
            return value.intValue();
        }
        return '\0';
    }

    /**
     * Sets the action command key. The action command key
     * is used to identify the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.ACTION_COMMAND_KEY</code> key.
     *
     * @param key the action command
     * @see Action#ACTION_COMMAND_KEY
     * @see Action#putValue
     */
    public void setActionCommand(Object key) {
        putValue(Action.ACTION_COMMAND_KEY, key);
    }

    /**
     * Returns the action command.
     *
     * @return the action command or null
     */
    public Object getActionCommand() {
        return getValue(Action.ACTION_COMMAND_KEY);
    }

    /**
     * Returns the key stroke which represents an accelerator
     * for the action.
     *
     * @return the key stroke or null
     */
    public KeyStroke getAccelerator() {
        return (KeyStroke)getValue(Action.ACCELERATOR_KEY);
    }

    /**
     * Sets the key stroke which represents an accelerator
     * for the action.
     * <p>
     * This is a convenience method for <code>putValue</code> with the
     * <code>Action.ACCELERATOR_KEY</code> key.
     *
     * @param key the key stroke; can be <code>null</code>
     * @see Action#ACCELERATOR_KEY
     * @see Action#putValue
     */
    public void setAccelerator(KeyStroke key) {
        putValue(Action.ACCELERATOR_KEY, key);
    }

    /**
     * Sets the group identity of the state action. This is used to
     * identify the action as part of a button group.
     */
    public void setGroup(Object group) {
        putValue(GROUP, group);
    }

    public Object getGroup() {
        return getValue(GROUP);
    }

    /**
     * Will perform cleanup on the object.
     * Should be called when finished with the Action. This should be used if
     * a new action is constructed from the properties of an old action.
     * The old action properties should be disposed.
     */
    public void dispose() {
        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            removePropertyChangeListener(listeners[i]);
        }
    }

    // Properties etc....

    /**
     * Indicates if this action has states. If this method returns
     * true then the this will send ItemEvents to ItemListeners
     * when the control constructed with this action in invoked.
     *
     * @return true if this can handle states
     */
    public boolean isStateAction() {
        Boolean state = (Boolean)getValue(IS_STATE);
        if (state != null) {
            return state.booleanValue();
        }
        return false;
    }

    /**
     * Set the state property to true.
     */
    public void setStateAction() {
        setStateAction(true);
    }

    /**
     * Set the state property.
     *
     * @param state if true then this action will fire ItemEvents
     */
    public void setStateAction(boolean state) {
        putValue(IS_STATE, Boolean.valueOf(state));
    }

    /**
     * @return true if the action is in the selected state
     */
    public boolean isSelected()  {
        return selected;
    }

    /**
     * Changes the state of the action
     * @param newValue true to set the action as selected of the action.
     */
    public synchronized void setSelected(boolean newValue) {
        boolean oldValue = this.selected;
        if (oldValue != newValue) {
            this.selected = newValue;
            firePropertyChange("selected", Boolean.valueOf(oldValue),
                               Boolean.valueOf(newValue));
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("[");
        // RG: Fix for J2SE 5.0; Can't cascade append() calls because
        // return type in StringBuffer and AbstractStringBuilder are different
        buffer.append(this.getClass().toString());
        buffer.append(":");
        try {
            Object[] keys = getKeys();
            for (int i = 0; i < keys.length; i++) {
                buffer.append(keys[i]);
                buffer.append('=');
                buffer.append(getValue( (String) keys[i]).toString());
                if (i < keys.length - 1) {
                    buffer.append(',');
                }
            }
            buffer.append(']');
        }
        catch (Exception ex) {  // RG: append(char) throws IOException in J2SE 5.0
            /** @todo Log it */
        }
        return buffer.toString();
    }

    // /**
    // * @inheritDoc
    // * Default to no-op
    // */
    // public void itemStateChanged(ItemEvent e) {
    // }
    
    /**
     * Callback method as <code>ItemListener</code>. Updates internal state based
     * on the given ItemEvent. <p>
     * 
     * Here: synchs selected property if isStateAction(), does nothing otherwise.
     * 
     * @param e the ItemEvent fired by a ItemSelectable on changing the selected 
     *    state.
     */
    public void itemStateChanged(ItemEvent e) {
        if (isStateAction()) {
            setSelected(ItemEvent.SELECTED == e.getStateChange());
        }
    }


}
