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



import java.lang.reflect.*;
import java.util.*;
import javax.swing.tree.*;


/**
 * A node created from a primitive such as an int or float.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

class PrimitiveNode implements InspectorNode
{

//----------------------------
	//instance variables
	private Value value;
	private InspectorNode parent; //our parent
	private String name;

//-------------------------------
	//constructors

	/**
	 * Create a primitive node based on the instance and 
	 * field.
	 * The field should be accessible.
	 */
	PrimitiveNode(InspectorNode parent, Value value, String name)
	{
		this.name = name;
		this.parent =  parent;
		this.value = value;
	}

//-----------------------------------
	//Tree Node methods

	public Enumeration children()
	{
		return new Vector().elements();
	}

	public TreeNode getChildAt(int childIndex)
	{
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount()
	{
		return 0;
	}

	public int getIndex(TreeNode node)
	{
		return -1;
	}

	public boolean getAllowsChildren()
	{
		return false;
	}

	public boolean isLeaf()
	{
		return true;
	}

	public TreeNode getParent()
	{
		return parent;
	}

//-----------------------------
	//InspectorNode
	public String getValueString()
	{	
		return value.getValue().toString();
	}

	public Object getValue()
	{
		return value.getValue();
	}

	public boolean isValid()
	{	
		/*
		 * We are valid as long as our parent is valid, 
		 * since we calculate our value by accessing our parents field
		 */
		return parent.isValid();
	}

	public String toString()
	{
		return name;
	}

}

