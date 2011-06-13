package imagej.util.diag.inspect;

/*
 * Copyright (C) 2000 Sean Bridges
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


import java.util.*;

/**
 * A CollectionNode is an ObjectNode that displays its values a little more intelligently
 */

public class CollectionNode extends ObjectNode {

  public CollectionNode(Value value, String name, InspectorNode parent)
  {
    super(value, name, parent);
  }

  public String getValueString()
  {
    Object instance = getValueReference().getValue();
    return instance.getClass().getName() + "\n" + asString(instance);
  }


  private String asString(Object value)
  {
    if(value == null)
      return "<null>";
    Collection collection = (Collection) value;
    Iterator iter = collection.iterator();

    StringBuffer buf = new StringBuffer();
    buf.append("[");
    while(iter.hasNext())
    {
      buf.append(iter.next());
      if(iter.hasNext())
        buf.append(",");
    }
    buf.append("]");

    return buf.toString();

  }
}
