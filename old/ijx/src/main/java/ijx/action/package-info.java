/*
 * $Id: package-info.java 3245 2009-02-02 20:11:45Z kschaefe $
 *
 * Copyright 2009 Sun Microsystems, Inc., 4150 Network Circle,
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
/**
 *  Contains classes related to the JDNC actions architecture. The Actions 
 * architecture maintains the set of user initiated commands (referred to as 
 * <i>user actions</i>) in an application. These commands are represented as an 
 * {@link javax.swing.Action} and have properties like name and icon. The 
 * user actions
 * are represented in the user interface by controls like menu items and 
 * toolbar buttons.
 * <p>
 * The other type of actions used by the architecture are the internal 
 * swing Actions (refered to as <i>behaviour actions</i>) that are embedded
 * within the {@link javax.swing.ActionMap} of a {@link javax.swing.JComponent}.
 * <p>
 * These two types of actions are distinct from each other: user actions
 * have a lot of properties but very little semantics by default 
 * (unless explicity bound). Behavior actions have no properties but have
 * semantics. These two types of actions are linked by the action id 
 * which is the value of the <code>Action.ACTION_COMMAND_KEY</code>
 * <p>
 * The {@link org.jdesktop.swingx.action.AbstractActionExt} class extends the Swing
 * concept of the Action by adding support for toggle or two state actions.
 * Toggle type actions may be grouped into a set of mutually exclusive actions.
 * This binary actions are represented in the user interface as JToggleButtons,
 * JCheckBoxMenuItems or JRadioButtonMenuItems. 
 * <p>
 * There are two types of user actions: A {@link org.jdesktop.swingx.action.BoundAction} 
 * is an action that will invoke a specific method. It may be bound to an explict
 * component, a callback method on an object instance or one or more listeners. 
 * A {@link org.jdesktop.swingx.action.TargetableAction} is an action that doesn't have an
 * explicit binding and the invocation will be sent to an arbitrator 
 * (the {@link org.jdesktop.swingx.action.TargetManager}) which dispatches the Action 
 * to the "current component" - represented by a Targetable instance. 
 * The current component may be explictly set by some programmatic 
 * policy (for example, changes in state).
 * <p>
 * By defalt, the current component will be driven by the focus policy as dictated 
 * by the current FocusManager. If the current component cannot handle the action 
 * then the action will be dispatched up the containment hierarchy until the action 
 * is consumed. If the action is not consumed then it will be dispatched to the 
 * Application instance which manages an application global set of actions.
 * <p>
 * These are the key classes or the actions architecture:
 * <p>
 * <dl>
 * <dt> {@link org.jdesktop.swingx.action.ActionManager}</dt>
 * <dd> A repository of all shared actions in the application.
 * There will be one instance per application which can be accessed
 * via the Application object (was ClientApp)
 * </dd>
 *
 * <dt>{@link org.jdesktop.swingx.action.ActionContainerFactory}</dt>
 * <dd>Constructs JMenuBars, JMenus, JPopupMenus and
 * JToolBars using lists of action ids. This functionality may
 * be migrated into ActionManager.
 * </dd>
 *
 * <dt>{@link org.jdesktop.swingx.action.TargetableAction}</dt>
 * <dd>Represents an unbound Action. The invocation of this action
 * will be dispatched to the TargetManager.</dd>
 *
 * <dt>{@link org.jdesktop.swingx.action.BoundAction}</dt>
 * <dd>Represents an action which has an exclicit binding.</dd>
 *
 * <dt>{@link org.jdesktop.swingx.action.TargetManager}</dt>
 * <dd>Manages the targetable policy for actions which have no
 * explicit binding. The policy can be set by changes in application
 * state, event based criteria or whatever. If the policy has not been
 * set then it will dispatch the action to the current focusable
 * component.
 * </dd>
 *
 * <dt>{@link org.jdesktop.swingx.action.Targetable}</dt>
 * <dd>An interface that contains a few methods which expose actions to
 * the TargetManager. Targetable objects don't have to be visual
 * components they only have to be able to handle action invocations.
 * </dd>
 * </dl>
 *
 * <hr>
 * <address><a href="mailto:richard.bair@sun.com">Richard Bair</a></address>
 */
package ijx.action;