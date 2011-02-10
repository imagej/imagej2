package org.imagejdev.sandbox.fromImagine;

 /*******************************************************************************
 *
 * OpenBlueSky - Enhanced NetBeans RCP Components
 * =====================================================
 *
 * Copyright (C) 2003-2009 by Fabrizio Giudici
 * Project home page: http://openbluesky.dev.java.net
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *******************************************************************************
 *
 * $Id$
 *
 ******************************************************************************/


import javax.annotation.Nonnull;
import javax.swing.JToolBar;
import org.openide.util.Lookup;
//import it.tidalwave.netbeans.swing.impl.ToolBarFromLayer;

/*******************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 * TODO: see if we can replace it with ToolbarPool
 *
 ******************************************************************************/
public class ToolBarFactory
  {
    @Nonnull
    public static JToolBar createToolBar (final @Nonnull String name)
      {
        return createToolBar(name, Lookup.EMPTY);
      }

    @Nonnull
    public static JToolBar createToolBar (final @Nonnull String name, final @Nonnull Lookup lookup)
      {
        final JToolBar toolBar = new JToolBar(name);
        toolBar.setOpaque(false);
        toolBar.setBorderPainted(false);
        toolBar.setFloatable(false);
        ToolBarFromLayer.connect(toolBar, "LocalToolbar/" + name, lookup, false);
        return toolBar;
      }
  }
