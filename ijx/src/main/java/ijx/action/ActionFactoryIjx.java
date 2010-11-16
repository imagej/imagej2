/*
 * 
 * $Id$
 * 
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010, Expression company is undefined on line 9, column 62 in Templates/Licenses/license-bsd.txt.
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 * 
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 *   Neither the name of Expression company is undefined on line 24, column 41 in Templates/Licenses/license-bsd.txt. nor the names of its
 *   contributors may be used to endorse or promote products
 *   derived from this software without specific prior
 *   written permission of Expression company is undefined on line 27, column 43 in Templates/Licenses/license-bsd.txt.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ijx.action;

import ijx.sezpoz.ActionIjx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ActionFactoryIjx {

    public List actions = new ArrayList(); // Actions to be added to menus
    public List actionsToolbar = new ArrayList(); // Actions to be added to the ToolBar
    //
    static ActionManager manager = ActionManager.getInstance();
    static ActionContainerFactory factory = new ActionContainerFactory(manager);
    static TargetManager targetManager = TargetManager.getInstance();

    public static AbstractActionExt createActionExt(String commandKey, final IndexItem<ActionIjx, ActionListener> item) {
        AbstractActionExt action = new AbstractActionExt() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // class is instantiated when this is invoked
                    item.instance().actionPerformed(e);
                } catch (InstantiationException x) {
                    x.printStackTrace();
                }
            }
        };
        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
        decorateAction(action, item);
        if (item.annotation().toggle()) {
            action.setStateAction();
        }
        if (!item.annotation().group().isEmpty()) {
            action.setGroup(item.annotation().group());
        }
        manager.addAction(action);
        return action;
    }
    //

    public static TargetableAction createTargetableAction(String id, String name,
            String mnemonic, boolean toggle,
            String group) {
        return (TargetableAction) configureAction(new TargetableAction(name, id),
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

    public static void decorateAction(Action action, final IndexItem<ActionIjx, ?> item) {
        if (!item.annotation().icon().isEmpty()) {
            try {
                ImageIcon img = new ImageIcon(ClassLoader.getSystemResource(item.annotation().icon()));
                //java.net.URL imgURL = item.getClass().getResource(item.annotation().icon());
                if (img == null) {
                    System.err.println("Couldn't find icon: " + item.annotation().icon());
                }
                if (img != null) {
                    action.putValue(Action.SMALL_ICON, img);
                }
            } catch (Exception e) {
            }
        }
        if (!item.annotation().label().isEmpty()) {
            action.putValue(Action.NAME, item.annotation().label());
        }
        if (!item.annotation().tip().isEmpty()) {
            action.putValue(Action.SHORT_DESCRIPTION, item.annotation().tip());
        }
        if (item.annotation().mnemonic() != 0) {
            action.putValue(Action.MNEMONIC_KEY, item.annotation().mnemonic());
        }
        if (!item.annotation().hotKey().isEmpty()) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(item.annotation().hotKey()));
        }
    }

    /* for reference:
    public interface Action extends ActionListener {
    public static final String DEFAULT = "Default";
    public static final String NAME = "Name";
    public static final String SHORT_DESCRIPTION = "ShortDescription";
    public static final String LONG_DESCRIPTION = "LongDescription";
    public static final String SMALL_ICON = "SmallIcon";
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public Object getValue(String key);
    public boolean isEnabled();
    public void putValue(String key, Object value);
    public void removePropertyChangeListener(PropertyChangeListener listener);
    public void setEnabled(boolean b);
    }

     */
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
            AbstractActionExt a = (AbstractActionExt) action;
            a.setShortDescription(shortDesc);
            a.setLongDescription(longDesc);
            a.setSmallIcon(smallIcon);
            a.setLargeIcon(largeIcon);
            a.setAccelerator(accel);
        } else {
            action.putValue(Action.SHORT_DESCRIPTION, shortDesc);
            action.putValue(Action.LONG_DESCRIPTION, longDesc);
            action.putValue(Action.SMALL_ICON, smallIcon);
            action.putValue(AbstractActionExt.LARGE_ICON, largeIcon);
            action.putValue(Action.ACCELERATOR_KEY, accel);
        }
    }
    // <editor-fold desc=">>> Actions <<<" defaultstate="collapsed">

    public void addTarget(Targetable target) {
        targetManager.addTarget(target);
    }

    public void addAction(AbstractAction action) { // adds to menus
        addAction(action, false);
    }

    public void addAction(AbstractAction action, boolean onToolbar) {
        manager.addAction(action);
        actions.add(action.getValue(Action.ACTION_COMMAND_KEY));
        if (onToolbar) {
            actionsToolbar.add(action.getValue(Action.ACTION_COMMAND_KEY));
        }
    }

    public static JMenuBar createMenuBar(List actions) {
        JMenuBar menuBar = factory.createMenuBar(actions);
        menuBar.setBorderPainted(false);
        return menuBar;
    }

    public JToolBar createToolBar(List actions) {
        JToolBar toolBar = factory.createToolBar(actions);
        //toolBar.setBorderPainted(false);
        //toolBar.setMargin(new Insets(0, 0, 0, 0));
        //toolBar.setBorder(null);
        return toolBar;
    }
    //
//    public AbstractActionExt getManagedAction(String actionName) {
//        return ((AbstractActionExt) ActionManager.getInstance().getAction(actionName));
//    }
//
//    public void addDependentAction(String name, String commandKey,
//                                   String iconFile, Object handler,
//                                   String method, int key,
//                                   ValueModel depend) {
//        ActionManager manager = ActionManager.getInstance();
//        DependentAction dAction = new DependentAction(name, commandKey,
//            new ImageIcon(getClass().getResource(iconFile)), handler, method);
//        dAction.setMnemonic(new Integer(key));
//        manager.addAction(dAction);
//        actions.add(dAction.getValue(Action.ACTION_COMMAND_KEY));
//        //e.g. manager.getBoundAction("acqImage").setEnabled(false);
//        if (depend != null) {
//            dAction.addDependencyAnd(depend);
//        }
//    // @todo  add setSmallIcon();
//    }
//
//    public void addDependentActionToggle(String name, String commandKey,
//                                         String iconFile,
//                                Object handler,
//                                String method,
//                                int key,
//                                         ValueModel depend) {
//        ActionManager manager = ActionManager.getInstance();
//        DependentAction dAction = new DependentActionToggle(name, commandKey,
//            new ImageIcon(getClass().getResource(iconFile)), handler, method);
//        dAction.setMnemonic(new Integer(key));
//        manager.addAction(dAction);
//        actions.add(dAction.getValue(Action.ACTION_COMMAND_KEY));
//        dAction.addDependencyAnd(depend);
//    // @todo  add setSmallIcon();
//    }
    // </editor-fold>
}
