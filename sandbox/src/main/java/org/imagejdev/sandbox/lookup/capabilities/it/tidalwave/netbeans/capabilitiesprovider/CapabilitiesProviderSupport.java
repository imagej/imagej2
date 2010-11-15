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
import java.util.Collection;
import java.util.Collections;
import org.openide.util.Lookup;

/***********************************************************************************************************************
 *
 * A convenience implementatior of {@link CapabilitiesProvider} that returns empty collections. Subclasses of this class
 * are not forced to implements all the methods, they just need to override the relevant ones.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class CapabilitiesProviderSupport<T> implements CapabilitiesProvider<T>
  {
    @Nonnull
    private final Class<T> managedClass;

    public CapabilitiesProviderSupport()
      {
        managedClass = (Class<T>)ReflectionUtils.getTypeArguments(CapabilitiesProviderSupport.class, getClass()).get(0);
      }

    /** @deprecated It's no more necessary to pass managedClass. Remove any call to the super constructor. */
    @Deprecated
    public CapabilitiesProviderSupport (final @Nonnull Class<T> managedClass)
      {
        this.managedClass = managedClass;
      }

    /** {@inheritDocs} */
    @Override @Nonnull
    public final Class<T> getManagedClass()
      {
        return managedClass;
      }
    
    /** {@inheritDocs} */
    @Override @Nonnull
    public Collection<? extends Object> createCapabilities (final @Nonnull T owner)
      {
        return Collections.emptyList();
      }

    /** {@inheritDocs} */
    @Override @Nonnull
    public Collection<? extends Lookup> createLookups (final @Nonnull T owner)
      {
        return Collections.emptyList();
      }
  }
