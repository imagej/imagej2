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



/**
 * A complex node is a node that is not primitive.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

import java.lang.reflect.*;
import java.util.*;
import javax.swing.tree.*;


abstract class ComplexNode implements InspectorNode
{

//------------------------------
  //instance variables
  private InspectorNode parent;
  private Class parentClass;
    //used to test our validity
    //we acccess values dynamically through our value
    //so as long as the parents class hasnt changed,
    //we can access it.

  private Vector children = new Vector(0);
  private String name;



//------------------------------
  //class methods

  /**
   * Create an inspector tree from the given object.
   * This should be the only method called to create a new InspectorNode
   * from outside the InspectorNode hierarchy.
   */
  public static final InspectorNode createInspectorTree(Object obj)
  {
    if(obj == null)
    {
      return new ObjectNode("<null>", null);
    }
    return createComplexNode(new SimpleValue(obj), null, obj.getClass().getName());
  }

  protected static final InspectorNode createNode(Field field, InspectorNode parent, Value value)
  {
    return createNode(field, parent, value, "");
  }

  /**
   * Create a node within the given context
   */
  protected static final InspectorNode createNode(Field field, InspectorNode parent, Value value, String prefix)
  {

    String name = prefix + field.getName();
    if(name.length() < 15)
    {
      name = name + "            ";
    }
    try
    {
      field.setAccessible(true);
    }
    catch(SecurityException e)
    {
      return new NotAccessibleNode(parent,name);
    }
    //this should never happen
    catch(IllegalArgumentException e)
    {
      //this should never happen
      throw new RuntimeException("Complex node could not create a node");
    }


    Class c = field.getType();
    //if a primitive type add a new primitive node
    if(c.isPrimitive())
    {
      return new PrimitiveNode(parent, new FieldValue(value,field), name);
    }
    else
    {
      return createComplexNode(new FieldValue(value,field),parent,name);
    }
  }

  /**
   * Create a non primitive node from this object, within the context.
   */
  protected static final InspectorNode createComplexNode(Value value, InspectorNode parent,  String name)

  {

    Object nextObject = value.getValue();

    //System.err.println("next node class" + ((nextObject == null) ? "null" : nextObject.getClass().toString() ) );
    //System.err.println("creating node " + nextObject);

    if(nextObject == null)
    {
      //System.out.println("null");
      return new ObjectNode(value, name, parent);
    }

    Class theClass = nextObject.getClass();


    if(theClass.isArray() )
    {
      //System.out.println("array");
      return new ArrayNode(parent, value,  name);
    }

    if(nextObject != null && nextObject instanceof Collection)
    {
      return new CollectionNode(value, name,parent);
    }


    if(nextObject != null && nextObject instanceof Map)
    {
      return new MapNode(value, name,parent);
    }



    //its not an array or primitive, so its just an object
    //nSystem.out.println("object node");
    return new ObjectNode(value, name, parent);
  }


//----------------------------------------
  //constructors
  /**
   * Parent can be null
   */
  ComplexNode(InspectorNode parent, String name)
  {
    this.parent = parent;

    if(parent != null)
    {
      Object parentValue = parent.getValue();
      if(parentValue != null)
      {
        parentClass = parentValue.getClass();
      }
    }
    this.name = name;
  }

//----------------------------------------
  //instance methods



//-----------------------------------
  //child creation methods

  /**
   * generate the indexth child
   */
  protected abstract InspectorNode generateChild(int index);

  protected void setNumberOfChildren(int numberOfChildren)
  {
    children.setSize(numberOfChildren);
  }

  protected int getNumberOfChildren()
  {
    return children.size();
  }


//-----------------------------------------
  //TreeNode methods
  public Enumeration children()
  {
    return children.elements();
  }

  public boolean getAllowsChildren()
  {
    return true;
  }

  public TreeNode getChildAt(int childIndex)
  {
    InspectorNode child = (InspectorNode) children.get(childIndex);

    if(child == null)
    {
      child = generateChild(childIndex);
      children.setElementAt(child, childIndex);

    }

    return child;
  }

  public int getChildCount()
  {
    return children.size();
  }

  public int getIndex(TreeNode node)
  {
    return children.indexOf(node);
  }

  public TreeNode getParent()
  {
    return parent;
  }

  public boolean isLeaf()
  {
    return children.size() == 0;
  }

//-----------------------------------------
  //TreeNode methods
  public boolean isValid()
  {
    //the root is always valid
    if(parent == null)
    {
      return true;
    }
    //we are valid if our parent is valid, and if our instance is the
    //same as our parent instance

    if(!parent.isValid() )
    {
      return false;
    }

    Object parentValue = parent.getValue();
    if(parentValue == null)
    {
      return parentClass == null;
    }
    else
    {
      return parentValue.getClass() == parentClass;
    }
  }




//----------------------------------
  //printing

  public String toString()
  {
    return name;
  }



  /**
   * Returns a simple top level debug string.
   */
  public String debugString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append(toString() );

    Enumeration e = children.elements();
    while(e.hasMoreElements() )
    {
      InspectorNode n = (InspectorNode) e.nextElement();
      buf.append("\n  ");
      buf.append(n.toString());
      buf.append(" ");
      buf.append(n.getValueString() );
    }
    return buf.toString();
  }

}

