/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.imagejdev.imagine;

import java.util.Collection;
import javax.imageio.ImageIO;
import org.imagejdev.imagine.spi.SelectionContextContributor;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class serves to give us a persistent selection context;  NetBeans'
 * default context sensitivity is very aggressive - selection depends 
 * completely on what component has focus.  
 * <p>
 * Our application is one where components <i>combine</i> context - the
 * selected tool along with the current image editor, etc.  So this class
 * gives us a lookup which merges the default, component-sensitive lookup
 * with other, more persistent lookups, to form a unified "selection context" 
 * for the application.
 * <p>
 * So what really will be in this lookup?  Well, it proxies 
 * Utilities.actionsGlobalContext, so anything a TopComponent (top level
 * component in a tab in the main window) will be there when that component
 * is focused.
 * <p>
 * In addition to this, several pieces of infrastructure will merge their
 * own Lookups into this one - for example, when the selected tool changes,
 * because the user selected a menu item or pressed a button provided by
 * the ToolsUI module, whatever Tool the user selected will appear, and the
 * previously selected Tool, if any, will disappear.  So any UI that is 
 * interested in what tool is active needs to simply listen for changes in
 * the presence or absence of Tool.class:
 * <pre>
 * Lookup.Result result = ApplicationContext.get (new Lookup.Template(Tool.class));
 * result.addLookupListener (...)
 * </pre>
 * and it will be notified whenever the active tool changes.
 * <p>
 * What generally can you expect to find here?
 * <ul>
 * <li>Picture.class - the layers object of the active editor, if any </li>
 * <li>Layer.class - the active layer of the active editor, if any </li>
 * <li>IO.class - the object for saving/reloading the image in the active editor</li>
 * <li>Zoom.class - the object for adjusting the zoom of the active editor</li>
 * <li>UndoRedo.Manager - a NetBeans subclass of UndoManager which supports
 *    listening for undoable edits being added/removed</li>
 * <li>PaintTopComponent.class - this class is not visible outside the 
 *    PaintUI module, but some actions it itself provides use it directly
 * </ul>
 * <p>
 * <h2>How to merger other Lookups into the Application Context</h2>
 * The ApplicationContext is composed of a set of lookups, including
 * Utilities.actionsGlobalContext().  To merge your own lookup into this,
 * implement Lookup.Provider and put an instance of your Lookup.Provider in
 * the default Lookup by adding a file to META-INF/services in your module
 * jar.
 *
 * @author Timothy Boudreau
 */

@ServiceProvider(service = ContextGlobalProvider.class,
    supersedes = "org.netbeans.modules.openide.windows.GlobalActionContextImpl")

public final class ApplicationContext implements ContextGlobalProvider {

    private final MutableProxyLookup proxy = new MutableProxyLookup();
    private static ApplicationContext INSTANCE;
    private final Lookup.Result lookupsLookup;

    /** Creates a new instance of ApplicationContext */
    public ApplicationContext() {
        lookupsLookup = Lookup.getDefault().lookup(new Lookup.Template(SelectionContextContributor.class));

        // or using newer approach
        //Collection<? extends SelectionContextContributor> s =
        //Lookup.getDefault().lookupAll(SelectionContextContributor.class);

        // Note the listener below will only really be called if
        // a module providing a lookup we are proxying is
        // installed/uninstalled
        LookupListener ll = new LookupListener() {

            public void resultChanged(LookupEvent evt) {
                updateLookups(
                        ((Lookup.Result) evt.getSource()).allInstances());
            }
        };

        lookupsLookup.addLookupListener(ll);
        lookupsLookup.allInstances();
        ll.resultChanged(new LookupEvent(lookupsLookup));
    }

    private void updateLookups(Collection allContributors) {
        SelectionContextContributor[] p =
                (SelectionContextContributor[]) allContributors.toArray(new SelectionContextContributor[allContributors.size()]);

        Lookup[] lkp = new Lookup[p.length];
        for (int i = 0; i < p.length; i++) {
            lkp[i] = p[i].getLookup();
        }
        proxy.changeLookups(lkp);
    }

    private static ApplicationContext getContext() {
        if (INSTANCE == null) {
            //XXX I think we can use a weak reference here...
            INSTANCE = new ApplicationContext();
        }
        return INSTANCE;
    }

    public Lookup createGlobalContext() {
        return proxy;
    }

    private static final class MutableProxyLookup extends ProxyLookup {

        void changeLookups(Lookup[] lkp) {
            super.setLookups(lkp);
        }
    }

    static {
        //ImageIO init - needs to be in a class loaded early
        ImageIO.setUseCache(false);
        ImageIO.scanForPlugins();
    }
}
