/*
 * $Id: ToggleActionPropertyChangeListener.java 3100 2008-10-14 22:33:10Z rah003 $
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;

/**
 * Added to the Toggle type buttons and menu items so that various components
 * which have been created from a single StateChangeAction can be in synch. 
 * 
 * This listener is responsible for updating the selected property from the 
 * Action to the AbstractButton. <p>
 * 
 * It guarantees a maximum of 1 instance of 
 * ToggleActionPCL to be installed per button (PENDING JW: add test to verify). 
 * It removes all ToggleActionPCLs which are targeted to unreachable buttons
 * from the action's listener list.
 * 
 * 
 */
class ToggleActionPropertyChangeListener implements PropertyChangeListener {


    private WeakReference<AbstractButton> buttonRef;
    
    public ToggleActionPropertyChangeListener(Action action, AbstractButton button) {
        if (shouldAddListener(action, button)) {
            this.buttonRef = new WeakReference<AbstractButton>(button);
            action.addPropertyChangeListener(this);
        }
    }

    
    protected synchronized boolean shouldAddListener(Action action, AbstractButton button) {
        releasePCLs(action);
        // PENDING JW: revisit - we need a configurator to maintain at most a 1:1 from button to
        // action anyway: so a true in isToggling must not happen.
        // 
        return !isToggling(action, button);
//        return true;
    }


    protected boolean isToggling(Action action, AbstractButton button) {
        if (!(action instanceof AbstractAction)) return false;
        PropertyChangeListener[] listeners = ((AbstractAction)action).getPropertyChangeListeners();
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (listeners[i] instanceof ToggleActionPropertyChangeListener) {
                if (((ToggleActionPropertyChangeListener) listeners[i]).isToggling(button)) return true;
            }
        }
        return false;
    }

    /**
     * Removes all ToggleActionPCLs with unreachable target buttons from the 
     * Action's PCL-listeners.
     * 
     * @param action to cleanup.
     */
    protected void releasePCLs(Action action) {
        if (!(action instanceof AbstractAction)) return;
        PropertyChangeListener[] listeners = ((AbstractAction)action).getPropertyChangeListeners();
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (listeners[i] instanceof ToggleActionPropertyChangeListener) {
                ((ToggleActionPropertyChangeListener) listeners[i]).checkReferent(action);
            }
        }
    }

    
    public void propertyChange(PropertyChangeEvent evt) {
        AbstractButton button = checkReferent((Action) evt.getSource());
        if (button == null) return;
        String propertyName = evt.getPropertyName();

        if (propertyName.equals("selected")) {
            Boolean selected = (Boolean)evt.getNewValue();
            button.setSelected(selected.booleanValue());
        }
    }

    /**
     * Returns the target button to synchronize from the listener.
     * 
     * Side-effects if the target is no longer reachable:
     *  - the internal reference to target is nulled.
     *  - if the given action is != null, this listener removes 
     *       itself from the action's listener list.
     * 
     * @param action The action this is listening to.
     * @return the target button if it is strongly reachable or null 
     *    if it is no longer strongly reachable.
     */
    protected AbstractButton checkReferent(Action action) {
        AbstractButton button = null;
        if (buttonRef != null) {
            button = buttonRef.get();
        }
        if (button == null) {
            if (action != null) {
                action.removePropertyChangeListener(this);
            }
           buttonRef = null;
        }
        return button;
    }


    /**
     * Check if this is already synchronizing the given AbstractButton.
     * 
     * This may have the side-effect of releasing the weak reference to
     * the target button.
     * 
     * @param button must not be null
     * @return true if this target button and the given comp are equal 
     *         false otherwise. 
     * @throws NullPointerException if the button is null.
     */
    public boolean isToggling(AbstractButton button) {
        // JW: should check identity instead of equality?
        return button.equals(checkReferent(null));
    }

    /**
     * Checks if this is synchronizing to the same target button as the
     * given listener.
     * 
     * This may have the side-effect of releasing the weak reference to
     * the target button.
     * 
     * @param pcl The listener to test, must not be null.
     * @return true if the target buttons of the given is equal to this target
     *    button and the button is strongly reachable, false otherwise.
     */
//    public boolean isToggling(ToggleActionPropertyChangeListener pcl) {
//        AbstractButton other = pcl.checkReferent(null);
//        if (other != null) {
//            return isToggling(other);
//        }
//        return false;
//    }
    
    
}
