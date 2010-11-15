package org.imagejdev.sandbox.lookup;


import org.openide.util.Lookup;
//import org.openide.util.MetaInfServicesLookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.AbstractLookup.Pair;
import org.openide.util.lookup.Lookups;

/** A simple lookup which allows adding and removing items.
 * This is just a simple front-end to the netbeans lookup api.
 * It provides the ability to lookup classes registered via the 
 * Sun service provider mechanism or registered directly with this class.
 *
 * @see <a href="http://openide.netbeans.org/lookup/">The Netbeans Lookup API</a>
 * @see <a href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">The service provider mechanism</a>
 * @author tonyj
 * @version $Id: FreeHEPLookup.java,v 1.3 2008-05-04 12:22:26 murkle Exp $
 */
public class FreeHEPLookup extends Lookup {

    private static InstanceContent ic = new InstanceContent();
    private static AbstractLookup contentLookup = new AbstractLookup(ic);
    private static FreeHEPLookup theLookup = new FreeHEPLookup();
    private MyProxyLookup proxy;

    private FreeHEPLookup() {
        proxy = new MyProxyLookup(contentLookup, Lookup.getDefault());
    }

    /** Access a singleton instance of FreeHEPLookup.ProxyLookup
     * @return The singleton instance of FreeHEPLookup
     */
    public static FreeHEPLookup instance() {
        return theLookup;
    }

    /** Add an instance of an Object to the lookup.
     * @param instance The object to add.
     */
    public void add(Object instance) {
        ic.add(instance);
    }

    /** Add an instance with an ID.
     * @param instance The instance to add.
     * @param id The ID associated with the instance
     */
    public void add(Object instance, String id) {
        ic.addPair(new DontAsk(instance, id));
    }

    /** Add an instance with multiple IDs.
     * @param instance The instance to add.
     * @param ids The IDs associated with the instance.
     */
    public void add(Object instance, String[] ids) {
        for (int i = 0; i < ids.length; i++) {
            ic.addPair(new DontAsk(instance, ids[i]));
        }
    }

    /** Remove an instance from the lookup
     * @param instance The instance to remove
     */
    public void remove(Object instance) {
        ic.remove(instance);
    }

    /** Remove an instance with a specific ID.
     * @param instance The instance to remove
     * @param id The ID of the instance
     */
    public void remove(Object instance, String id) {
        ic.removePair(new DontAsk(instance, id));
    }

    /** Remove an instance with multiple IDs
     * @param instance The instance to remove
     * @param ids The IDs of the instance
     */
    public void remove(Object instance, String[] ids) {
        for (int i = 0; i < ids.length; i++) {
            ic.removePair(new DontAsk(instance, ids[i]));
        }
    }

    /** Sets the ClassLoader used for subsequent service API lookups
     * @param loader The ClassLoader to use.
     */
    public void setClassLoader(ClassLoader loader) {
        // Lookup serviceLookup = new MetaInfServicesLookup(loader);
        // proxy.setLookups(contentLookup, serviceLookup);
        proxy.setLookups(contentLookup, Lookups.metaInfServices(loader));
    }

    public Lookup.Result lookup(Lookup.Template template) {
        return proxy.lookup(template);
    }

    public Object lookup(Class clazz) {
        return proxy.lookup(clazz);
    }

    private class DontAsk extends Pair {

        private static final long serialVersionUID = -6621505967094205187L;
        private Object instance;
        private String id;

        DontAsk(Object instance, String id) {
            this.id = id;
            this.instance = instance;
        }

        protected boolean creatorOf(Object obj) {
            return obj == instance;
        }

        public String getDisplayName() {
            return instance.toString();
        }

        public String getId() {
            return id;
        }

        public Object getInstance() {
            return instance;
        }

        public Class getType() {
            return instance.getClass();
        }

        protected boolean instanceOf(Class clazz) {
            return clazz.isInstance(instance);
        }

        public boolean equals(Object xx) {
            if (xx instanceof DontAsk) {
                DontAsk other = (DontAsk) xx;
                return other.instance == this.instance && other.id.equals(this.id);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return instance.hashCode() + id.hashCode();
        }
    }

    private class MyProxyLookup extends ProxyLookup {

        MyProxyLookup(Lookup a, Lookup b) {
            super(new Lookup[]{a, b});
        }

        private void setLookups(Lookup a, Lookup b) {
            super.setLookups(new Lookup[]{a, b});
        }
    }
}
