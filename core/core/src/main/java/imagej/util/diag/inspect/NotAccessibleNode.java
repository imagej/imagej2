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



import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;


/**
 * A node whose values cannot be accessed.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

class NotAccessibleNode implements InspectorNode
{

//----------------------------
	//class variables
	public static final String ACCESS_DENIED = "<access denied>";

//----------------------------
	//instance variables
	InspectorNode parent;
	String name;

//-------------------------------
	//constructors

	/**
	 * Create a primitive node based on the instance and 
	 * field.
	 * The field should be accessible.
	 */
	NotAccessibleNode(InspectorNode parent, String name)
	{
		this.parent = parent;	
		this.name = name;
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
		return  ACCESS_DENIED;
	}

	public Object getValue()
	{
		return null;
	}

	public String toString()
	{
		return name; 
	}

	public boolean isValid()
	{
		return parent.isValid();
	}

}


