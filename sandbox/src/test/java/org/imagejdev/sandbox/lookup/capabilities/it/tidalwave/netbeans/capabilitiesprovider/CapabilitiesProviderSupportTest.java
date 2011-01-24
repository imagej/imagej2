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

import java.util.Date;
import org.imagejdev.sandbox.lookup.capabilities.it.tidalwave.netbeans.capabilitiesprovider.CapabilitiesProviderSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
//import static org.hamcrest.CoreMatchers.*;

class CapabilitiesProvider1 extends CapabilitiesProviderSupport<String>
  {
  }

class CapabilitiesProvider2 extends CapabilitiesProviderSupport<Date>
  {
  }

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class CapabilitiesProviderSupportTest
  {
//    private CapabilitiesProvider1 cps1;
//    private CapabilitiesProvider2 cps2;
//
//    @Before
//    public void setUp()
//      {
//        cps1 = new CapabilitiesProvider1();
//        cps2 = new CapabilitiesProvider2();
//      }
//
//    @Test
//    public void mustReturnTheProperManagedClass()
//      {
//        assertThat(cps1.getManagedClass(), is(sameInstance(String.class)));
//        assertThat(cps2.getManagedClass(), is(sameInstance(Date.class)));
//      }
//
//    @Test
//    public void mustReturnNoCapabilities()
//      {
//        assertThat(cps1.createCapabilities("").size(), is(0));
//        assertThat(cps2.createCapabilities(new Date()).size(), is(0));
//      }
//
//    @Test
//    public void mustReturnNoLookups()
//      {
//        assertThat(cps1.createLookups("").size(), is(0));
//        assertThat(cps2.createLookups(new Date()).size(), is(0));
//      }
}