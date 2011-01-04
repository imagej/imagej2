package pipesentity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

/*
 * Used to represent the conf JSON entity
 */
public class Conf {
	
	private Name name;
	private Type type;
	private Value value;
	
	/**
	 * 
	 * @param name - Conf name
	 * @param type - conf type
	 * @param value - conf value
	 */
	public Conf( Name name, Type type, Value value )
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	/**
	 * 
	 * @param confJSON - JSON input string used to create new Conf type
	 */
	public Conf( String name, JSONObject confJSON ) 
	{
		this.name = new Name( name );
		this.type = new Type( confJSON.getString("type") );
		this.value = new Value( confJSON.getString("value") );
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
	
	

}
