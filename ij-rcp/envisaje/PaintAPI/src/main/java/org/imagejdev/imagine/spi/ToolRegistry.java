/*
 * ToolRegistry.java
 *
 * Created on October 24, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi;

import java.util.Collection;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.spi.tools.Tool;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;

/**
 * A registry for all tools available in the system.  The set of tools is dynamic
 * and my change based on context.  The default implementation looks in two
 * places:  Tools registered in the default lookup are always available;
 * additionally the default implementation tracks the presence of instances of
 * Layer in the global selection lookup (Utilies.actionsGlobalContext()).  If
 * the active Layer has any Tool instances in <i>its</i> Lookup, those will be
 * available as well;  tools available via a Picture instance may change
 * on the fly.
 * <p>
 * If an application wants to supply its own tools, or a subset of the available
 * tools, it may simply implement ToolRegistry and put that implementation in
 * the default Lookup (Lookup.getDefault()) by registering it in META-INF/services
 * within the JAR.
 *
 * @author Tim Boudreau
 */
public abstract class ToolRegistry {
    protected ToolRegistry() {}
    protected abstract Lookup.Result <Tool> getToolsResult();
    protected static ToolRegistry getDefault() {
        ToolRegistry r = Lookup.getDefault().lookup (ToolRegistry.class);
        if (r == null) {
            if (TRIVIAL == null) {
                TRIVIAL = new Trivial();
            }
            r = TRIVIAL;
        }
        return r;
    }

    public static Lookup.Result <Tool> getTools() {
        return getDefault().getToolsResult();
    }

    public static Lookup getLookup() {
        return getDefault().getLkp();
    }

    protected abstract Lookup getLkp() ;

    private static Trivial TRIVIAL = null;
    private static final class Trivial extends ToolRegistry implements LookupListener {
        private final Lookup.Result <Layer> result;
        private final MutableProxyLookup lkp = new MutableProxyLookup();

        Trivial () {
            result = Utilities.actionsGlobalContext().lookupResult (Layer.class);
            result.addLookupListener (this);
            resultChanged (null);
        }

        protected Lookup.Result <Tool> getToolsResult() {
            return lkp.lookupResult(Tool.class);
        }

        public void resultChanged(LookupEvent lookupEvent) {
            Collection l = result.allInstances();
            Layer[] layers = (Layer[]) l.toArray(new Layer[l.size()]);

            Lookup[] lkps = new Lookup [ layers.length ];
            for (int i = 0; i < layers.length; i++) {
                lkps[i] = layers[i].getLookup();
            }
            lkp.setOtherLookups (lkps);
        }

        public Lookup getLkp() {
            return lkp;
        }
    }

    private static final class MutableProxyLookup extends ProxyLookup {
        final void setOtherLookups(Lookup... lookups) {
            Lookup[] lkps = new Lookup [lookups.length + 1];
            System.arraycopy(lookups, 0, lkps, 1, lookups.length);
            lkps[0] = Lookup.getDefault();
            super.setLookups (lkps);
        }
    }
}
