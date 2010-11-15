/***********************************************************************************************************************
 *
 * OpenBlueSky - NetBeans Platform Enhancements
 * ============================================
 *
 * Copyright (C) 2007-2010 by Tidalwave s.a.s.
 * Project home page: http://openbluesky.kenai.com
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * $Id$
 *
 **********************************************************************************************************************/
package org.imagejdev.sandbox.lookup.capabilities.it.tidalwave.netbeans.capabilitiesprovider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
//import it.tidalwave.util.logging.Logger;

/***********************************************************************************************************************
 *
 * A utility class for creating a {@link Lookup} containing capabilities for am {@link Object}. A typical example of
 * use is:
 *
 * <pre>
 * public class MyStuff implements Lookup.Provider
 *   {
 *     @CheckForNull
 *     private Lookup lookup;
 *
 *     @Override @Nonnull
 *     public synchronized Lookup getLookup()
 *       {
 *         if (lookup == null)
 *           {
 *             lookup = LookupFactory.createLookup(this);
 *           }
 *
 *         return lookup;
 *       }
 *
 *     ...
 *   }
 * </pre>
 *
 * If you need to, you can customize your {@code Lookup}:
 *
 * <pre>
 * public class MyStuff implements Lookup.Provider
 *   {
 *     @CheckForNull
 *     private Lookup lookup;
 *
 *     @Override @Nonnull
 *     public synchronized Lookup getLookup()
 *       {
 *         if (lookup == null)
 *           {
 *             Lookup myOtherLookup = Lookups.fixed(...);
 *             Lookup yetAnotherLookup = ...;
 *             lookup = LookupFactory.createLookup(this, myOtherLookup, yetAnotherLookup);
 *           }
 *
 *         return lookup;
 *       }
 *
 *     ...
 *   }
 * </pre>
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class LookupFactory {

    private static final String CLASS = LookupFactory.class.getName();
    //   private static final Logger logger = Logger.getLogger(CLASS);

    /*******************************************************************************************************************
     *
     * Create a {@link Lookup} that contains the capabilities of the owner object, retrieved by looking up one or more
     * {@link CapabilitiesProvider}s. It is possible to pass an optional set of extra {@code Lookup} instances that will
     * be joined together.
     * 
     * @param  dataObject    the owner
     * @param  extraLookups  the extra <code>Lookup</code>s
     * @return               the <code>Lookup</code> for the owner
     * 
     ******************************************************************************************************************/
    @Nonnull
    public static Lookup createLookup(final @Nonnull Object owner,
            final @Nonnull Lookup... extraLookups) {
        System.out.format("createLookup(%s, ...)", owner);
        // logger.fine("createLookup(%s, ...)", owner);
        final List<Lookup> lookups = new ArrayList<Lookup>();

        for (final CapabilitiesProvider capabilitiesProvider : Lookup.getDefault().lookupAll(CapabilitiesProvider.class)) {
            if (capabilitiesProvider.getManagedClass().isAssignableFrom(owner.getClass())) {
                lookups.add(Lookups.fixed(capabilitiesProvider.createCapabilities(owner).toArray()));
                lookups.addAll(capabilitiesProvider.createLookups(owner));
            }
        }

        lookups.addAll(Arrays.asList(extraLookups));
        System.out.format(">>>> lookups for %s: %s", owner, Arrays.toString(extraLookups));

        //    logger.finer(">>>> lookups for %s: %s", owner, Arrays.toString(extraLookups));

        return new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
    }
}
