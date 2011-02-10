
package ijx.etc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * http://netbeans.dzone.com/articles/sticky-lookup
 * @author GBH <imagejdev.org>
 *
 * The general sketch of the idea is to wrap Utilities.actionsGlobalContext() in a StickyLookup --
 * a lookup which makes objects of a specified class "sticky". While the contents of a specific
 * Lookup may change over time as objects of certain types move in and out of it, the sticky
 * class (S) of a StickyLookup ensures that performing a .lookup(S.class) or .lookupAll(S.class)
 * will always result in the last non-empty set of returned instances during its lifetime.
 *
 * Use: Lookup lookup = new StickyLookup(Utilities.actionsGlobalContext(), BusAcct.class);
 */


public class StickyLookup extends ProxyLookup implements LookupListener {
    private final Lookup lookup;
    private final Class clazz;
    private final Lookup.Result result;
    private final InstanceContent ic;
    private final Set icContent = new HashSet();

    public StickyLookup(final Lookup lookup, final Class<?> clazz) {
        this(lookup, clazz, new InstanceContent());
    }

    private StickyLookup(final Lookup lookup, final Class<?> clazz, InstanceContent ic) {
        super(Lookups.exclude(lookup, clazz), new AbstractLookup(ic));
        this.lookup = lookup;
        this.clazz = clazz;
        this.ic = ic;

        // initialize (pull this from wrapped lookup)
        for (Object t : lookup.lookupAll(clazz)) {
            ic.add(t);
            icContent.add(t);
        }

        this.result = lookup.lookupResult(clazz);
        this.result.addLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        boolean empty = true;
        if (lookup.lookup(clazz) != null) {
            empty = false;
        }
        if (empty) {
            for (Object obj : icContent) {
                ic.add(obj); // add 'em!
            }
            return; // don't force refresh at all, as nothing of type clazz is selected and we should therefore preserve what we have
        } else {
            // not empty, reset contents
            Collection<?> lookupAll = lookup.lookupAll(clazz);
            List<Object> toRemove = new ArrayList<Object>();
            for (Object obj : icContent) {
                if (lookupAll.contains(obj)) {
                    continue;
                }
                ic.remove(obj);
                toRemove.add(obj);
            }
            for (Object obj : toRemove) {
                icContent.remove(obj);
            }
            for (Object obj : lookupAll) {
                if (!icContent.contains(obj)) {
                    ic.add(obj);
                    icContent.add(obj);
                }
            }
        }
    }
}

