
/**
 * This package contains... examples and experiments using Lookup API
 * @author GBH <imagejdev.org>
 */

package org.imagejdev.sandbox.lookup;


/* =========================================================================================
   This is a parking lot of code snippets:
 * =========================================================================================

 * Lookups
==============
Lookups.singleton( Object ) - one item Lookup
Lookups.fixed( Object... ) - unchanging Lookup
Lookups.exclude ( Lookup, Class... );
ProxyLookup ( Lookup... otherLookups ) - compose multiple lookups

// A Lookup that never changes – org.openide.util.lookup.Lookups
// -A utility class that provides some convenient Lookup implementations;
// you set the contents once and it stays this way forever

Lookup lkp = Lookups.fixed ( obj1, obj2, obj3 );
Lookup lkp = Lookups.singleton( onlyObject );
//-------------------------------------------------------
// AbstractLookup + InstanceContent

/* Lookup whose contents you can manage
AbstractLookup - Driven by an InstanceContent object; You can add/remove/set the contents on the fly;
Appropriate changes will be fired to listeners */

/*

InstanceContent content = new InstanceContent();
Lookup lkp = new AbstractLookup ( content );
content.add(objA);
content.set ( obj1, obj2, obj3 );
content.remove ( obj3 );

 InstanceContent ic = new InstanceContent();
 Lookup lookup = new AbstractLookup(ic);
 ic.add(new Object ());
 ic.add(new Dimension (...));
 Dimension theDim = lookup.lookup (Dimension.class);

//-----------------------------------------------------------
// ProxyLookup – Merge multiple lookups together
// - A lookup that proxies a bunch of other lookups
// – Can change which lookups are merged together on the fly, And appropriate events will be fired

Lookup lkp = new ProxyLookup ( otherLookup1, otherLookup2, otherLookup3  );

 // query a travel bag (a Lookup object) using a Lookup.Template object and
 // get the results of such a query using a Lookup.Result object.

    Lookup myBag = ...
    Lookup.Template query = new Lookup.Template( Pencil.class );
    Lookup.Result result = myBag.lookup( query );
//	retrieve the results of the lookup
    Collection myPencils = result.allInstances();
    //do something with x

// or

    for (X x : lkp.lookupAll(X.class)) {}

// or for deferred instantiation...
// invoke the "allItems()" method, that retrieves a collection of Lookup.Item, an object used to
// defer instantiation of objects until such an instantiation is required:

    Collection<? extends Lookup.Item> items = result.allItems();
    for( Lookup.Item item : items )
      System.out.println(item.getInstance());


// Adding a listener (a LookupListener) to a Lookup.Result object
	final Lookup.Result result = ...
    result.addLookupListener(
      new LookupListener() {
        public void resultChanged( LookupEvent anEvent ) {
          System.out.println("Mmm... you added or removed a pencil!");
        }
      }
	);

// See http://wiki.netbeans.org/DevFaqLookupGenerics

/*
lookup.lookupResult
public <T> Lookup.Result<T> lookupResult(Class<T> clazz)
    Equivalent to calling lookup(Lookup.Template) but slightly more convenient.

Lookup.lookupAll
public <T> Collection<? extends T> lookupAll(Class<T> clazz)
    Equivalent to calling lookupResult(java.lang.Class) and asking for Lookup.Result.allInstances()


ProxyLookup#setLookups(Executor notifyIn, Lookup... lookups)
*/

/*
Lookups moreElements = Lookups.fixed( "Hello", "World", new Integer(5) );

// using an InstanceContent Object to add and remove stuff:

InstanceContent content = new InstanceContent();
Lookup dynamicLookup = new AbstractLookup(content);
content.add("Hello");
content.add(5);

Listeners registered for the matching class will be informed when something changes.
If you would like to query more than one Lookup at a time you can use a ProxyLookup.
This for example, combines two of the Lookups created above into one:

ProxyLookup proxy = new ProxyLookup(dynamicLookup, moreElements);


//=======================================================================
    private static final class MutableProxyLookup extends ProxyLookup {
        final void setOtherLookups(Lookup... lookups) {
            Lookup[] lkps = new Lookup [lookups.length + 1];
            System.arraycopy(lookups, 0, lkps, 1, lookups.length);
            lkps[0] = Lookup.getDefault();
            super.setLookups (lkps);
        }
    }


============================

Collection <ServiceInterface> services= Lookup.getDefault.lookupAll(ServiceInterface.class);

// forPath()
Lookup lkp = Lookups.forPath("ServiceProviders");
*/

/*
Lookup metaInfServices(ClassLoader classLoader) - Note: It is not dynamic - so if you need to change the classloader or JARs, wrap it in a ProxyLookup
    and change the delegate when necessary. Existing instances will be kept if the implementation
    classes are unchanged, so there is "stability" in doing this provided some parent loaders are
    the same as the previous ones.
*/

