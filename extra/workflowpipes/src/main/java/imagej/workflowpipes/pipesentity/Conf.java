//
// Conf.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

/*
 * Used to represent the conf JSON entity
 */
public class Conf implements Serializable {
	
	private Name name;
	private Type type;
	private Value value;
	
	/**
	 * 
	 * @param name - Conf name
	 * @param value - Conf value
	 * @param type - Conf type
	 */
	public Conf( Name name, Value value, Type type )
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	public Type getType() {
		return type;
	}

	public Value getValue() {
		return value;
	}

	/**
	 * returns true if conf's name matches inputName
	 * @param name
	 * @return
	 */
	public boolean isName( String inputName )
	{
		if( inputName.equals( this.name.getValue() ) )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param confJSON - JSON input string used to create new Conf type
	 */
	public Conf( String name, JSONObject confJSON ) 
	{
		this.name = new Name( name );
		this.type = new Type( confJSON.getString("type") );
                System.out.println("confJSON is " + confJSON);
                this.value = new Value("");
                try {
		    this.value = new Value( confJSON.getString("value") );
                }
                catch (java.util.NoSuchElementException e) {
                    System.out.println("NoSuchElementException " + e.getMessage());
                }
	}

	/**
	 * @returns the JSON Object representing this object
	 */
	public JSONObject getJSONObject()
	{
		//create a new object
		JSONObject json = new JSONObject();
		
		//populate the value
		json.put("value", value.getValue() );
		
		//poplutate the type
		json.put("type", type.getValue() );
		
		JSONObject jsonOutput = new JSONObject();
		
		jsonOutput.put( name.getValue(), json );
		
		//return the object
		return jsonOutput;
	}

	public static ArrayList<Conf> getConfs( String string ) 
	{
		ArrayList<Conf> confs = new ArrayList<Conf>();
		
		//let class parse the input string
		JSONObject json = null;
		try {
			json = new JSONObject( string );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//use an iterator to find all the confs in the JSONObject
		Iterator i = json.keys();
		while( i.hasNext() )	
		{
			JSONObject jsonConf;
			
			// get the name (key) value
			String name = (String) i.next();
			
			// get the next conf as JSONObject
			jsonConf = json.getJSONObject( name );
			
			//Create a conf object and add to the array
			confs.add( new Conf( name, jsonConf ) );
		}
		
		return confs;
	}

	/**
	 * returns the first conf found matching the string name
	 * @param confName
	 * @param confs
	 * @return null if no match
	 */
	public static Conf getConf( String confName, ArrayList<Conf> confs ) 
	{
		//for each conf in the array
		for(Conf conf:confs)
		{
			System.out.println("Conf :: getConf :: conf is " + conf.getJSONObject() );
			
			//check to see if the name matches
			if( conf.isName(confName))
			{
				//it matches so return this conf
				return conf;
			}
		}
		
		// no match - return null
		return null;
		
	}

	public static JSONObject getJSON(ArrayList<Conf> confs) {
		JSONObject json = new JSONObject(); 
		
		// iterate over the confs
		for ( Conf conf : confs )
		{
			JSONObject internalJSON = new JSONObject(); 
			internalJSON.put( "value", conf.value.getValue()  );
			internalJSON.put( "type", conf.type.getValue()  );
			
			json.put( conf.name.getValue(), internalJSON );
		}
		return json;
	}
	
	

}
