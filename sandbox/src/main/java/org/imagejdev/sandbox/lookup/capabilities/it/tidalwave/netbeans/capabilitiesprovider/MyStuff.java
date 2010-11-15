package org.imagejdev.sandbox.lookup.capabilities.it.tidalwave.netbeans.capabilitiesprovider;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.openide.util.Lookup;

public class MyStuff implements Lookup.Provider
  {
    @CheckForNull
    private Lookup lookup;

    @Override @Nonnull
    public synchronized Lookup getLookup()
      {
        if (lookup == null)
          {
            lookup = LookupFactory.createLookup(this);
          }

        return lookup;
      }

    //...
  }

//If you need to, you can customize your {@code Lookup}:
/*
  public class MyStuff implements Lookup.Provider
    {
      @CheckForNull
     private Lookup lookup;

      @Override @Nonnull
      public synchronized Lookup getLookup()
        {
          if (lookup == null)
            {
              Lookup myOtherLookup = Lookups.fixed(...);
              Lookup yetAnotherLookup = ...;
              lookup = LookupFactory.createLookup(this, myOtherLookup, yetAnotherLookup);
            }

          return lookup;
        }

   }
 */