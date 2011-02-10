package org.imagejdev.sandbox.lookup;


import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/*
Dynamic Proxy Lookup

Netbeans platform has a special class that act as a proxy to lookups on the system, namely ProxyLookup.
Using this class, we can gather more than one lookups and handle them at once.
You can look the detailed information about ProxyLookup on Netbeans platform's javadoc,
particularly in Utilities module for platform v6.8 and prior, or in Lookup module for platform v6.9.
ProxyLookup doesn't have a dynamic behaviour.
Everytime we want to add a lookup, we have to use setLookup() method and define the entire lookup collection.
So what I'm gonna add is a slight dynamic behaviour, just a simple additions so I can add a single
lookup and let the proxy do the rest of the job like managing the entire lookup location when a
lookup addition or a lookup removal performed. What I'm gonna add is two simple method,
let say …  addLookup() and removeLookup(). I'm using getLookups() and setLookups(),
both of them are protected methods of ProxyLookup, so I need to create a subclass of ProxyLookup …
say DynamicProxyLookup. Just look at the source code below.
 *
 * ...
 *
So, its pretty much simple. I get all current lookups from getLookups(), create a new lookup collection,
add the new lookup, then register the new collection using setLookups(). When lookup addition performed,
I simply create a bigger collection, and move the old collection plus the new lookup to the new
collection. When lookup removal performed, I create a smaller collection and move all unmatched lookups
to the new collection. Easy and simple.
 */
public class DynamicProxyLookup extends ProxyLookup {

    public void addLookup(Lookup lookup) {
        Lookup[] newLookup = null;
        Lookup[] currentLookup = getLookups();
        if ((currentLookup != null) && (currentLookup.length > 0)) {
            newLookup = new Lookup[currentLookup.length + 1];
            for (int i = newLookup.length - 2; i >= 0; i--) {
                newLookup[i] = currentLookup[i];
            }
            newLookup[currentLookup.length] = lookup;
        } else {
            newLookup = new Lookup[]{lookup};
        }

        if (newLookup != null) {
            setLookups(newLookup);
        }
    }

    public void removeLookup(Lookup lookup) {
        Lookup[] currentLookup = getLookups();
        if ((currentLookup != null) && (currentLookup.length > 0)) {
            int removedIndex = -1;
            for (int i = currentLookup.length - 1; i >= 0; i--) {
                if (currentLookup[i].equals(lookup)) {
                    removedIndex = i;
                    break;
                }
            }

            if (removedIndex > 0) {
                Lookup[] newLookup = new Lookup[currentLookup.length - 1];
                int newIndex = 0;
                for (int i = currentLookup.length - 1; i >= 0; i--) {
                    if (i != removedIndex) {
                        newLookup[newIndex] = currentLookup[i];
                        newIndex++;
                    }
                }
            }
        }
    }
}
