/*
 * $Id: ActionFactory.java 585 2005-10-26 14:31:21Z kleopatra $
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * A collection of static methods to make it easier to construct
 * Actions. Not sure how usefull they are in reality but it saves a
 * lot of typing.
 *
 * @author Mark Davidson
 */
public class ActionFactory {

    /**
     * Factory Methods for creating BoundActions
     */
    public static BoundAction createBoundAction(String id, String name,
                                                String mnemonic) {
        return createBoundAction(id, name, mnemonic, false);
    }

    public static BoundAction createBoundAction(String id, String name,
                                                String mnemonic, boolean toggle) {
        return createBoundAction(id, name, mnemonic, toggle, null);
    }


    public static BoundAction createBoundAction(String id, String name,
                                                String mnemonic, boolean toggle,
                                                String group) {
        return (BoundAction)configureAction(new BoundAction(name, id),
                                            mnemonic, toggle, group);
    }

    /**
     * Factory Methods for creating <code>CompositeAction</code>
     * @see CompositeAction
     */
    public static CompositeAction createCompositeAction(String id, String name,
                                                        String mnemonic) {
        return createCompositeAction(id, name, mnemonic, false);
    }

    public static CompositeAction createCompositeAction(String id, String name,
                                                        String mnemonic, boolean toggle) {
        return createCompositeAction(id, name, mnemonic, toggle, null);
    }

    public static CompositeAction createCompositeAction(String id, String name,
                                                        String mnemonic, boolean toggle,
                                                        String group) {
        return (CompositeAction)configureAction(new CompositeAction(name, id),
                                                mnemonic, toggle, group);
    }


    public static ServerAction createServerAction(String id, String name,
                                                  String mnemonic) {
        ServerAction action = new ServerAction(name, id);
        if (mnemonic != null && !mnemonic.equals("")) {
            action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic.charAt(0)));
        }
        return action;
    }


    /**
     * These methods are usefull for creating targetable actions
     */
    public static TargetableAction createTargetableAction(String id, String name) {
        return createTargetableAction(id, name, null);
    }

    public static TargetableAction createTargetableAction(String id, String name,
                                                          String mnemonic) {
        return createTargetableAction(id, name, mnemonic, false);
    }

    public static TargetableAction createTargetableAction(String id, String name,
                                                          String mnemonic, boolean toggle) {
        return createTargetableAction(id, name, mnemonic, toggle, null);
    }

    public static TargetableAction createTargetableAction(String id, String name,
                                                          String mnemonic, boolean toggle,
                                                          String group) {
        return (TargetableAction)configureAction(new TargetableAction(name, id),
                                                 mnemonic, toggle, group);
    }

    private static Action configureAction(AbstractActionExt action,
                                          String mnemonic, boolean toggle,
                                          String group) {
        action.setMnemonic(mnemonic);
        String description = action.getName() + " action with comand " + action.getActionCommand();
        action.setShortDescription(description);
        action.setLongDescription(description);

        if (toggle) {
            action.setStateAction();
        }
        if (group != null) {
            action.setGroup(group);
        }
        return action;
    }

    /**
     * Add additional attributes to the action. If any of these attributes
     * are null then they will still be set on the action. Many of these
     * attributes map to the set methods on <code>AbstractActionExt</code>
     *
     * @see AbstractActionExt
     * @param action the action which will all the attributes will be applied
     */
    public static void decorateAction(AbstractAction action,
                                      String shortDesc, String longDesc,
                                      Icon smallIcon, Icon largeIcon,
                                      KeyStroke accel) {
        if (action instanceof AbstractActionExt) {
            AbstractActionExt a = (AbstractActionExt)action;
            a.setShortDescription(shortDesc);
            a.setLongDescription(longDesc);
            a.setSmallIcon(smallIcon);
            a.setLargeIcon(largeIcon);
            a.setAccelerator(accel);
        }
        else {
            action.putValue(Action.SHORT_DESCRIPTION, shortDesc);
            action.putValue(Action.LONG_DESCRIPTION, longDesc);
            action.putValue(Action.SMALL_ICON, smallIcon);
            action.putValue(AbstractActionExt.LARGE_ICON, largeIcon);
            action.putValue(Action.ACCELERATOR_KEY, accel);
        }
    }
}
