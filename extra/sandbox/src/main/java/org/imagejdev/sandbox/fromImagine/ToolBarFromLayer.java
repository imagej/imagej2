package org.imagejdev.sandbox.fromImagine;

/***********************************************************************************************************************
 *
 * OpenBlueSky - NetBeans Platform Enhancements
 * ============================================
 *
 * Copyright (C) 2007-2010 by Tidalwave s.a.s.
 * Project home page: http://openbluesky.kenai.com
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * $Id$
 *
 **********************************************************************************************************************/


import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * Utility for connecting layer entries to a toolbar.
 * Similar to {@link Utilities#actionsForPath} and {@link Utilities#actionsToPopup(Action[], Lookup)}.
 *
 * From https://hg.kenai.com/hg/sqe~trunk/raw-file/tip/core.ui.components/src/org/nbheaven/sqe/core/ui/components/toolbar/ToolBarFromLayer.java
 */
public class ToolBarFromLayer {

    private ToolBarFromLayer() {}

    /**
     * Makes a toolbar display actions or other components registered in a layer path.
     * Dynamic changes to the layer should result in changes to the toolbar.
     * Actions may wish to set the property {@code hideActionText} to true.
     * @param toolBar a toolbar which will be fully populated
     * @param path a layer path to load actions from
     * @param context context to pass to any context-sensitive actions, or null
     * @param largeIcons true to try to use 24x24 icons for actions defining {@code iconBase},
     *                   false to use the default (usually 16x16) icons
     */
    public static void connect(final JToolBar toolBar, String path, final Lookup context, final boolean largeIcons) {
        final Lookup.Result<Object> actions = Lookups.forPath(path).lookupResult(Object.class);
        LookupListener listener = new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        toolBar.removeAll();
                        for (Object item : actions.allInstances()) {
                            if (context != null && item instanceof ContextAwareAction) {
                                item = ((ContextAwareAction) item).createContextAwareInstance(context);
                            }
                            Component c;
                            if (item instanceof Presenter.Toolbar) {
                                c = ((Presenter.Toolbar) item).getToolbarPresenter();
                            } else if (item instanceof Action) {
                                JButton button = new JButton();
                                Actions.connect(button, (Action) item);
                                if (largeIcons) {
                                    button.putClientProperty("PreferredIconSize", 24); // NOI18N
                                }
                                c = button;
                            } else if (item instanceof Component) {
                                c = (Component) item;
                            } else {
                                if (item != null) {
                                    Logger.getLogger(ToolBarFromLayer.class.getName()).warning("Unknown object: " + item);
                                }
                                continue;
                            }
                            toolBar.add(c);
                        }
                    }
                });
            }
        };
        actions.addLookupListener(listener);
        toolBar.putClientProperty("actionsLookupResult", actions); // prevent GC
        listener.resultChanged(null);
    }

}
