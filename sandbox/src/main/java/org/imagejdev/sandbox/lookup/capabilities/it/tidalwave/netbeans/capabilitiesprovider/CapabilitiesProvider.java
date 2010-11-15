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
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import org.openide.util.Lookup;

/***********************************************************************************************************************
 *
 * This class is a factory that provides capabilities that should be put into a object {@link Lookup}.
 * Contents can be expressed both as simple objects (that will be wrapped into a fixed {@code Lookup}) and as pre-filled
 * (or custom-written) {@code Lookup}s in case some specific behaviour is needed.
 *
 * It's preferrable for you not to directly implement this interface, rather subclass
 * {@link CapabilitiesProviderSupport}. See javadoc of the latter class for an example. In order for the managed object
 * to retrieve its {@code Lookup}, you should use {@link LookupFactory}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ThreadSafe
public interface CapabilitiesProvider<T> 
  {
    /*******************************************************************************************************************
     *
     * Return the managed object class.
     * 
     * @return              the managed class
     *
     ******************************************************************************************************************/
    @Nonnull
    public Class<T> getManagedClass();
    
    /*******************************************************************************************************************
     *
     * Provides capabilities for the managed object as a collection of plain objects.
     * 
     * @param  owner        the <code>owner</code>
     * @return              the capabilities
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<? extends Object> createCapabilities (@Nonnull T owner);
    
    /*******************************************************************************************************************
     *
     * Provides capabilities for the managed object as {@link Lookup} instances.
     * 
     * @param  owner        the <code>owner</code>
     * @return              the <code>Lookup</code>s
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<? extends Lookup> createLookups (@Nonnull T owner);
  }
