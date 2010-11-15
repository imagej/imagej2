package org.imagejdev.sandbox.lookup;


import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

public class SelectionManager {

    // Use: SelectionManager.addSelectionListener(MyInteresting.class, listener);

    public static void addSelectionListener(Class clazz, LookupListener listener) {
        //...
        Lookup lookup = Utilities.actionsGlobalContext();
        Lookup.Result result = lookup.lookupResult(clazz);
        result.addLookupListener(listener);
        result.allInstances();
    }

    public static void removeSelectionListener(Class clazz, LookupListener listener) {
        //...
    }


    /*  http://eppleton.sharedhost.de/blog/?p=940
     * Selection Management
The same could be done  for Selection Management with a SelectionManager that 
     gives a nicer interface to Utilities.actionsGlobalContext().
     *
public class SelectionManager{

public static void addSelectionListener(Class clazz, LookupListener listener) {...}
public static void removeSelectionListener(Class clazz, LookupListener listener){...}

}
Again a user friendly name and hiding the initialization of the Lookup.Result 
     would simplify the usage a lot. So when you want to listen for selection instead of:
     *
Lookup lookup = Utilities.actionsGlobalContext();
Lookup.Result result = lookup.lookupResult(MyInteresting.class);
result.addLookupListener(listener);
result.allInstances();

     It would be:

SelectionManager.addSelectionListener(MyInteresting.class, listener);
     * 
This way the naming reflects what the user is using it for, so it’s much easier to remember.
     The main benefit when explaining Lookup is the separation of concerns,
     and since all the changes could be done by simple wrappers it would
     be fairly easy to implement it in a fully backward compatible way.

     */
}
