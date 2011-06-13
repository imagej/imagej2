package imagej.util.diag.inspect;

/*
 * Copyright (C) 2000 Sean Bridges

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


import java.lang.reflect.*;
import java.util.*;
import javax.swing.tree.*;

/**
 * A node created from an array.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

class ArrayNode extends ComplexNode
{

//------------------------------
  //instance variables
  Class startingComponentType;
  Value value;
  int startingSize; //used to track validity.
            //if our size changes we are no longer valid


//------------------------------
  //constructors

  ArrayNode(InspectorNode parent, Value value, String name)
  {
    super(parent,name);
    this.value = value;
    Object instance = value.getValue();

    Class c = instance.getClass();
    if(!c.isArray())
    {
      throw new RuntimeException("Not an array");
    }

    int length = Array.getLength(instance);
    startingSize = length;
    startingComponentType = instance.getClass().getComponentType();
    setNumberOfChildren(startingSize);


  }


  private String asString(Object array)
  {
    int length = Array.getLength(array);
    StringBuffer buf = new StringBuffer("[");
    for(int i = 0; i < length; i++)
    {
      buf.append(Array.get(array,i));
      if(i < length + 1)
        buf.append(",");
    }
    buf.append("]");
    return buf.toString();
  }


//-----------------------------
  //child creation

  protected InspectorNode generateChild(int index)
  {
    InspectorNode newChild;
    String childName = String.valueOf(index) + "           ";
    if(startingComponentType.isPrimitive())
    {
      return new PrimitiveNode(
            this,new ArrayValue(value,index), childName
            );
    }
    else
    {
      return ComplexNode.createComplexNode(
            new ArrayValue(value,index),
            (ComplexNode) getParent(),
            childName);
    }
  }

//-----------------------------
  //InspectorNode methods

  public String getValueString()
  {
    return startingComponentType.getName() + "[" + Array.getLength(value.getValue()) + "]" +
        "\n" + asString(value.getValue());
  }

  public Object getValue()
  {
    return value.getValue();
  }


  public boolean isValid()
  {
    if(super.isValid())
    {
      //if we are null, then we are not valid
      Object instance = value.getValue();
      if(instance == null)
      {
        return false;
      }
      //if our size has changed we are no longer valid
      //since we will have the wrong number of children
      else if(Array.getLength(instance) != startingSize)
      {
        return false;
      }
      //if our component type has changed, then we are no longer valid
      else if( instance.getClass().getComponentType() != startingComponentType)
      {
        return false;
      }
      else
      {
        return true;
      }

    }
    else
    {
      return false;
    }
  }


}
