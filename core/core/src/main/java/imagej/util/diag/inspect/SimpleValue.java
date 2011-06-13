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
 * Creates a value from an object.
 * Simply returns the object in getValue().
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */


class SimpleValue implements Value
{

//----------------------------
	//instance variables
	private Object instance; 

//----------------------------
	//constructors

	/** 
	 * Creates a value around an object.
	 * getValue simply returns the object.
	 */
	SimpleValue(Object instance)
	{
		this.instance = instance;
	}


//----------------------------
	//instance methods
	public Object getValue()
	{
		return instance;
	}
	
}

