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
 * Creates a value from an array and an index in that array.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */

import java.lang.reflect.Array;

class ArrayValue implements Value
{

//----------------------------
	//instance variables
	private Value parentValue; //getValue() must be an array
	private int index; //the index of the array we represent


//----------------------------
	//constructors

	/** 
	 * instance must be an array.
	 * If instance is a primitive array, then the values returned will
	 * be cast to their respective wrappers.
	 */
	ArrayValue(Value parentValue, int index)
	{
		this.parentValue = parentValue;
		this.index = index;
	}


//----------------------------
	//instance methods
	public Object getValue()
	{
		return Array.get(parentValue.getValue(), index);
	}
	



}
