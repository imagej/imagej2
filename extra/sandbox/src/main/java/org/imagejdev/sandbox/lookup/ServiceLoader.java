package org.imagejdev.sandbox.lookup;


import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

public class ServiceLoader {

    // Use: ServiceLoader.loadServices(ServiceProvider.class);


    public static <T> Collection<? extends T> loadServices(Class<T> clazz) {
        return Lookup.getDefault().lookupAll(clazz);
    }

    // Use: ServiceLoader.addServiceListener(ServiceProvider.class, listener);
    public static void addServiceListener(Class clazz, LookupListener listener) {
        Lookup.Result result = Lookup.getDefault().lookupResult(clazz);
        result.addLookupListener(listener);
        result.allInstances();
        // ...
    }

    public static void removeServiceListener(Class clazz, LookupListener listener) {
        // ...
    }
    /* http://eppleton.sharedhost.de/blog/?p=940
     * ServiceLoader
     * 
    So the first thing that could be done to simplify the API would be to create a ServiceLoader
    class with a user friendly name as a wrapper for Lookup.getDefault():

    public class ServiceLoader{
    public static <T> Collection<? extends T> loadServices(Class<T> clazz) { return Lookup.getDefault().lookupAll(clazz);}
    public static void addServiceListener(Class clazz, LookupListener listener) {Lookup.Result result = Lookup.getDefault().lookupResult(clazz); ...}
    public static void removeServiceListener(Class clazz, LookupListener listener){...}}
    This would serve several purposes.
    First it would use a name that reflects what the default Lookup is actually doing: loading services.
    Second it would remove the need to initialize results correctly, one of the most common errors of beginners.
    Third it would lead to clearer separation of concerns between specific service loader functionality and generic Lookup functionality.
    So instead of this:
    Lookup.getDefault().lookupAll(ServiceProvider.class);
    We would write:
    ServiceLoader.loadServices(ServiceProvider.class);
    and instead of:
    Lookup lookup = Lookup.getDefault();
    Lookup.Result result = lookup.lookupResult(ServiceProvider.class);
    result.addLookupListener(listener);
    result.allInstances();
    It would be:
    ServiceLoader.addServiceListener(ServiceProvider.class, listener);

    It would be even nicer if the LookupListener could be wrapped in a ServiceListener
    with convenience methods that totally hide the Lookup.Result. Another extension might
    be to have a method getRegisteredServices() that returns the fully qualified name of
    all class objects that are registered as keys.
     */
}
