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
 * Creates a value from a value and a field.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

import java.lang.reflect.Field;

class FieldValue implements Value
{

//----------------------------
	//instance variables
	private Value parentValue; //the instance we belong to
	private Field field; //the field of the object


//----------------------------
	//constructors

	/**
	 * If field is a primitive , then the values returned will
	 * be cast to their respective wrappers.
	 */
	FieldValue(Value parentValue, Field field)
	{
		this.parentValue = parentValue;
		this.field = field;
	}


//----------------------------
	//instance methods
	public Object getValue()
	{
		try
		{
			return field.get(parentValue.getValue());
		}	
		catch(IllegalArgumentException e)
		{	
			//this should never happen
			System.err.println("Error in PrimitiveNode.getValueString()");
			throw new RuntimeException(e.toString());
		}
		catch(IllegalAccessException e)
		{
			//this could happen if we dont have privleges
			//it should have been caught earlier, and the node should
			//be a not accessible node
			return NotAccessibleNode.ACCESS_DENIED;
		}



	}
	

}

