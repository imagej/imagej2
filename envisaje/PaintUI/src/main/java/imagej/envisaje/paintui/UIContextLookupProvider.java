/*
 * ToolSelectorImpl.java
 *
 * Created on October 15, 2005, 5:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package imagej.envisaje.paintui;

import java.util.Arrays;
import java.util.Collection;
import imagej.envisaje.spi.SelectionContextContributor;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service = SelectionContextContributor.class)

public class UIContextLookupProvider implements SelectionContextContributor {

    private InstanceContent content = new InstanceContent();
    private AbstractLookup custom = new AbstractLookup(content);
    private ProxyLookup lkp = new ProxyLookup(new Lookup[]{custom});
    private static UIContextLookupProvider INSTANCE = null;

    public UIContextLookupProvider() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Tried to create ToolSelectorImpl twice");
        }
        INSTANCE = this;
    }

    public Lookup getLookup() {
        return lkp;
    }

    public static Lookup theLookup() {
        ensureCreated();
        return INSTANCE.lkp;
    }

    static void set(Object[] o) {
        ensureCreated();
        INSTANCE.content.set(Arrays.asList(o), null);
    }

    static void set(Collection c) {
        ensureCreated();
        INSTANCE.content.set(c, null);
    }

    static Object lookup(Class clazz) {
        ensureCreated();
        return INSTANCE.lkp.lookup(clazz);
    }

    static Lookup.Result lookup(Lookup.Template tpl) {
        ensureCreated();
        return INSTANCE.lkp.lookup(tpl);
    }

    private static void ensureCreated() {
        if (INSTANCE == null) {
            Lookup.getDefault().lookup(UIContextLookupProvider.class);
            if (INSTANCE == null) {
                ErrorManager.getDefault().notify(new IllegalStateException(
                        "Implementation of "
                        + "UIContextLookupProvider not found in default lookup."
                        + " Check the META-INF/services directory of ToolsUI,"
                        + " and make sure it provides a Lookup.Provider.  Probably"
                        + " tool selection is broken for the application."));
                INSTANCE = new UIContextLookupProvider();
            }
        }
    }
}
